package com.hzk.arthas.util;

import com.hzk.arthas.mservice.WebService3500;
import com.hzk.arthas.term.io.RemoteInputWrapper;
import org.apache.commons.net.telnet.TelnetClient;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/***
 * This is a utility class providing a reader/writer capability required by the
 * weatherTelnet, rexec, rshell, and rlogin example programs. The only point of
 * the class is to hold the static method readWrite which spawns a reader thread
 * and a writer thread. The reader thread reads from a local input source
 * (presumably stdin) and writes the data to a remote output destination. The
 * writer thread reads from a remote input source and writes to a local output
 * destination. The threads terminate when the remote input source closes.
 ***/

/**
 * 多终端IO服务
 */
public final class MultiTermIOUtil {

    /**
     * 客户端
     */
    private static Map<String, TelnetClient> HOST_CLIENT_MAP = new HashMap<>();
    /**
     * 写相关
     */
    private static WriteThread writeThread;
    private static BlockingQueue<LocalInputWrapper> LOCALINPUT_QUEUE = new ArrayBlockingQueue<>(10000);
    /**
     * 读相关
     */
    private static Map<String, ReadThread> HOST_READTHREAD_MAP = new HashMap<>();
    private static Map<String, BlockingQueue<RemoteInputWrapper>> HOST_READQUEUE_MAP = new HashMap<>();

    public static void registTelnetClient(String host, int port, TelnetClient telnetClient) {
        HOST_CLIENT_MAP.putIfAbsent(getMapKey(host, port), telnetClient);
    }

    public static void write(String host, int port, String command){
        LocalInputWrapper localInputWrapper = new LocalInputWrapper(host, port, command);
        try {
            LOCALINPUT_QUEUE.put(localInputWrapper);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 写线程
        if (writeThread == null) {
            synchronized (MultiTermIOUtil.class) {
                writeThread = new WriteThread("WriteThread");
                writeThread.start();
            }
        }
        // 读功能
        String mapKey = getMapKey(host, port);
        if (HOST_READQUEUE_MAP.get(mapKey) == null) {
            synchronized (MultiTermIOUtil.class) {
                // 读队列
                BlockingQueue<RemoteInputWrapper> queue = new ArrayBlockingQueue<>(10000);
                HOST_READQUEUE_MAP.putIfAbsent(mapKey, queue);

                // 读线程
                ReadThread readThread = new ReadThread("ReadThread" + "_" + mapKey, host, port, HOST_CLIENT_MAP.get(mapKey), queue);
                HOST_READTHREAD_MAP.put(mapKey, readThread);
                readThread.start();
            }
        }

    }

    public static Map<String, List<RemoteInputWrapper>> read(){
        Map<String, List<RemoteInputWrapper>> resultMap = new HashMap<>();
        for(Map.Entry<String, BlockingQueue<RemoteInputWrapper>> tempEntry : HOST_READQUEUE_MAP.entrySet()) {
            String mapKey = tempEntry.getKey();
            List<RemoteInputWrapper> tempList = new ArrayList<>();
            BlockingQueue<RemoteInputWrapper> queue = tempEntry.getValue();
            int max = 3;
            int emptySize = 0;
            while (emptySize < max) {
                RemoteInputWrapper remoteInputWrapper = null;
                try {
                    remoteInputWrapper = queue.poll(10, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (remoteInputWrapper == null) {
                    emptySize++;
                } else {
                    tempList.add(remoteInputWrapper);
                    emptySize = 0;
                }
            }
            resultMap.put(mapKey, tempList);
        }
        return resultMap;
    }

    private static String getMapKey(String host, int port){
        return host + "_" + port;
    }

    static class WriteThread extends Thread {

        public WriteThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    LocalInputWrapper localInputWrapper = LOCALINPUT_QUEUE.take();
                    String host = localInputWrapper.getHost();
                    int port = localInputWrapper.getPort();
                    String command = localInputWrapper.getCommand();
                    String key = host + "_" + port;
                    TelnetClient telnetClient = HOST_CLIENT_MAP.get(key);
                    OutputStream outputStream = telnetClient.getOutputStream();
                    outputStream.write(command.getBytes());
                    // 回车键
                    outputStream.write(10);
                    outputStream.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    static class ReadThread extends Thread {

        private String host;
        private int port;
        private TelnetClient telnetClient;
        private BlockingQueue<RemoteInputWrapper> queue;

        public ReadThread(String name, String host, int port, TelnetClient telnetClient, BlockingQueue<RemoteInputWrapper> queue) {
            super(name);
            this.host = host;
            this.port = port;
            this.telnetClient = telnetClient;
            this.queue = queue;
        }
        @Override
        public void run() {
            while (true) {
                try {
                    DataInputStream dis = new DataInputStream(telnetClient.getInputStream());
                    while (true) {
                        // 10Kb
                        byte[] bytes = new byte[1024 * 10];
                        int readSize = dis.read(bytes);
                        RemoteInputWrapper remoteInputWrapper = new RemoteInputWrapper();
                        remoteInputWrapper.setHost(host);
                        remoteInputWrapper.setPort(port);
                        remoteInputWrapper.setReadSize(readSize);
                        if (readSize != 1024 * 10) {
                            byte[] minBytes = new byte[1024 * 10];
                            System.arraycopy(bytes, 0, minBytes, 0, readSize);
                            remoteInputWrapper.setBytes(minBytes);
                            System.out.println(new String(minBytes));
                        } else {
                            remoteInputWrapper.setBytes(bytes);
                            System.out.println(new String(bytes));
                        }
                        queue.put(remoteInputWrapper);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }



    public static final void readWrite(final InputStream remoteInput, final OutputStream remoteOutput,
                                       final InputStream localInput, final Writer localOutput) {
        Thread reader, writer;

        reader = new Thread() {
            @Override
            public void run() {
                int ch;

                try {
                    while (!interrupted() && (ch = localInput.read()) != -1) {
                        remoteOutput.write(ch);
                        remoteOutput.flush();
                    }
                } catch (IOException e) {
                    // e.printStackTrace();
                }
            }
        };
        reader.setName("reader");

        writer = new Thread() {
            @Override
            public void run() {
                try {
                    InputStreamReader reader = new InputStreamReader(remoteInput);
                    while (true) {
                        int singleChar = reader.read();
                        if (singleChar == -1) {
                            break;
                        }
                        localOutput.write(singleChar);
                        localOutput.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        writer.setName("writer");
        writer.setPriority(Thread.currentThread().getPriority() + 1);

        writer.start();
        reader.setDaemon(true);
        reader.start();

        try {
            writer.join();
            reader.interrupt();
        } catch (InterruptedException e) {
            // Ignored
        }
    }


    static class LocalInputWrapper {
        private String host;
        private int port;
        private String command;

        public LocalInputWrapper(String host, int port, String command) {
            this.host = host;
            this.port = port;
            this.command = command;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public String getCommand() {
            return command;
        }
    }
}