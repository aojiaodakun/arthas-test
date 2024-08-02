package com.hzk.arthas.mservice;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPObject;
import com.hzk.arthas.term.io.RemoteInputWrapper;
import com.hzk.arthas.util.MultiTermIOUtil;
import com.hzk.arthas.util.ProcessUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.tools.attach.VirtualMachine;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.telnet.TelnetClient;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 启动httpServer，代码转发到mservice
 * http://localhost:3600/proxy?command=start
 * http://localhost:3600/proxy?command=sysprop
 * http://localhost:3600/proxy?command=quit
 * http://localhost:3600/proxy?command=stop
 */
public class WebService3500 {

    static {
        System.setProperty("monitor.port", "3500");
    }

//    private static final String ATTACH_MAINCLASS_PREFIX = "com.hzk.arthas.mservice.MService";
    private static final String ATTACH_MAINCLASS_PREFIX = "demo.MathGame";


    public static void main(String[] args) throws Exception{
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(Integer.getInteger("monitor.port")), 0);
        httpServer.createContext("/monitor/arthas", new ProxyHandler());
        httpServer.start();
    }

    static class ProxyHandler implements HttpHandler {

        // Map<pid, TelnetInfo>
        private static Map<String, TelnetInfo> PID_CLIENT_MAP = new HashMap<>();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "success";
            String queryString = exchange.getRequestURI().getQuery();
            Map<String,String> queryStringInfoMap = formData2Dic(queryString);
            String type = queryStringInfoMap.get("type");
            switch (type) {
                // http://localhost:3500/monitor/arthas?type=start
                case "start":
                    // 找到MService进程，attach后并启动TermServer
                    List<PidInfo> mServicePidList = findMServicePid();
                    for(PidInfo tempPidInfo : mServicePidList) {
                        try {
                            String jar = "arthas-test-1.0-SNAPSHOT.jar";
                            VirtualMachine virtualMachine = VirtualMachine.attach(tempPidInfo.getPid());
                            String targetClassPath = Thread.currentThread().getContextClassLoader().getResource("").toURI().getPath();
                            targetClassPath = targetClassPath.replace("classes/", "") + jar;
                            targetClassPath = targetClassPath.substring(1);
                            virtualMachine.loadAgent(targetClassPath);
                            System.err.println("attach pid:" + tempPidInfo.getPid());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case "exec":
                    /**
                     * 代理通讯
                     * http://localhost:3500/monitor/arthas?type=exec&command=aaa
                     */
                    String command = queryStringInfoMap.get("command");
                    if (StringUtils.isEmpty(command)) {
                        response = "command is required";
                    }
                    if (PID_CLIENT_MAP.size() == 0) {
                        List<PidInfo> tempServicePidList = findMServicePid();
                        for(PidInfo tempPidInfo : tempServicePidList) {
                            TelnetInfo telnetInfo = new TelnetInfo(tempPidInfo);
                            PID_CLIENT_MAP.put(tempPidInfo.getPid(), telnetInfo);
                            TelnetClient telnetClient = telnetInfo.getTelnetClient();
                            // 注册telnetClient
                            MultiTermIOUtil.registTelnetClient(tempPidInfo.getHost(), tempPidInfo.getPort(), telnetClient);
                        }
                    }
                    if (PID_CLIENT_MAP.size() > 0) {
                        for(Map.Entry<String, TelnetInfo> tempEntry : PID_CLIENT_MAP.entrySet()) {
                            TelnetInfo telnetInfo = tempEntry.getValue();
                            PidInfo pidInfo = telnetInfo.getPidInfo();
                            String host = pidInfo.getHost();
                            int port = pidInfo.getPort();
                            MultiTermIOUtil.write(host, port, command);
                        }
                    }
                    break;
                case "result":
                    /**
                     * 获取服务端响应，前端定时500毫秒发起http请求
                     * http://localhost:3500/monitor/arthas?type=result
                     */
                    Map<String, List<RemoteInputWrapper>> host2InputMap = MultiTermIOUtil.read();
                    if (host2InputMap.size() > 0) {
                        JSONObject jsonObject = new JSONObject();
                        for(Map.Entry<String, List<RemoteInputWrapper>> tempEntry : host2InputMap.entrySet()) {
                            String hostPort = tempEntry.getKey();
                            int allReadSize=0;
                            List<RemoteInputWrapper> readInputList = tempEntry.getValue();
                            for(RemoteInputWrapper tempInput : readInputList) {
                                allReadSize = allReadSize + tempInput.getReadSize();
                            }
                            byte[] allBytes = new byte[allReadSize];
                            int index = 0;
                            for(RemoteInputWrapper tempInput : readInputList) {
                                int tempReadSize = tempInput.getReadSize();
                                byte[] tempBytes = tempInput.getBytes();
                                System.arraycopy(tempBytes, 0, allBytes, index, tempReadSize);
                                index = index + tempReadSize;
                            }
//                            ExecResultVO execResultVO = new ExecResultVO(hostPort, allBytes);
                            // new String，转码
                            jsonObject.put(hostPort, new String(allBytes));
                        }
                        response = JSON.toJSONString(jsonObject);
                    }
                    break;
                default:
                    break;
            }
            exchange.sendResponseHeaders(200, 0);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }


        static Map<String,String> formData2Dic(String formData) {
            Map<String,String> result = new HashMap<>();
            if(formData== null || formData.trim().length() == 0) {
                return result;
            }
            final String[] items = formData.split("&");
            Arrays.stream(items).forEach(item ->{
                final String[] keyAndVal = item.split("=");
                if( keyAndVal.length == 2) {
                    try{
                        final String key = URLDecoder.decode( keyAndVal[0],"utf8");
                        final String val = URLDecoder.decode( keyAndVal[1],"utf8");
                        result.put(key,val);
                    }catch (UnsupportedEncodingException e) {}
                }
            });
            return result;
        }

        private List<PidInfo> findMServicePid() {
            List<PidInfo> pidList = new ArrayList<>();
            Map<Long, String> pid2mainClassMap = ProcessUtils.listProcessByJps(false);
            for (Map.Entry<Long, String> entry : pid2mainClassMap.entrySet()) {
                String value = entry.getValue();
                String[] tempArray = value.split(" ");
                if (tempArray.length > 1) {
                    String mainClass = tempArray[1];
                    if (mainClass.startsWith(ATTACH_MAINCLASS_PREFIX)) {
                        String tempPid = String.valueOf(entry.getKey());
                        PidInfo pidInfo = new PidInfo(tempPid, mainClass);
                        pidList.add(pidInfo);
                    }
                }
            }
            return pidList;
        }


    }

    static class ExecResultVO {
        private String hostPort;
        private byte[] bytes;

        public ExecResultVO(String hostPort, byte[] bytes) {
            this.hostPort = hostPort;
            this.bytes = bytes;
        }

        public String getHostPort() {
            return hostPort;
        }

        public void setHostPort(String hostPort) {
            this.hostPort = hostPort;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }
    }

    static class PidInfo {
        private String pid;
        private String mainClass;
        private String host = "127.0.0.1";
        private int port = 3658;

        public PidInfo(String pid, String mainClass) {
            this.pid = pid;
            this.mainClass = mainClass;
            if (mainClass.startsWith("com.hzk.arthas.mservice.MService")) {
                port = Integer.parseInt(mainClass.split("_")[1]);
            }
        }

        public String getHost() {
            return host;
        }

        public String getPid() {
            return pid;
        }

        public String getMainClass() {
            return mainClass;
        }

        public int getPort() {
            return port;
        }
    }

    static class TelnetInfo {
        private PidInfo pidInfo;
        private TelnetClient telnetClient;

        public TelnetInfo(PidInfo pidInfo) throws IOException {
            this.pidInfo = pidInfo;
            telnetClient = new TelnetClient();
            telnetClient.setConnectTimeout(5000);
            telnetClient.connect(pidInfo.getHost(), pidInfo.getPort());
        }

        public PidInfo getPidInfo() {
            return pidInfo;
        }

        public TelnetClient getTelnetClient() {
            return telnetClient;
        }
    }

}
