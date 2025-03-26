package com.hzk.arthas.demo;

import com.hzk.arthas.util.HttpClientUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *【前置条件】
 * 1、logback.xml的root lever=ERROR
 * 2、trd.url=http://www.baidu9.com
 *【演示】
 * logger、watch、sysprop、ognl
 * trace、thread、options、stack
 */
public class ThreadPoolTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(ThreadPoolTest.class);

    private static String TRD_NAME;
    private static String TRD_URL;

    static {
        System.setProperty("trd.url", "http://www.baidu9.com");
        System.setProperty("trd.name", "百度");

        TRD_URL = System.getProperty("trd.url");
        TRD_NAME = System.getProperty("trd.name");
    }

    private static ExecutorService threadPool = new ThreadPoolExecutor(
            500, 500,
            60L, TimeUnit.SECONDS, new SynchronousQueue<>()
            , new ThreadFactory() {
                AtomicInteger integer = new AtomicInteger(0);
                @Override
                public Thread newThread(Runnable runnable) {
                    return new Thread(runnable,  "fi_cal_thread_" + integer.getAndIncrement());
            }}, new ThreadPoolExecutor.AbortPolicy());

    /**
     * localhost:8091/test
     */
    public static void main(String[] args) throws Exception {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(8091), 0);
        httpServer.createContext("/test", new TestHandler());
        httpServer.start();
        LOGGER.info("start success");
    }

    public static void setTrdUrl(String trdUrl) {
        TRD_URL = trdUrl;
    }

    static class TestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "fail";
            String trdResponse = invokeTrdService();
            if (StringUtils.isNotEmpty(trdResponse)) {
                response = "success";
            }
            exchange.sendResponseHeaders(200, 0);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private String invokeTrdService() {
            String httpResponse = "";
            try {
                httpResponse = invokeWithThreadPool(new TrdServiceVO(TRD_URL, TRD_NAME)).get();
            } catch (Exception e) {
                /**
                 * logger指令
                 * logger --name com.hzk.arthas.demo.ThreadPoolTest
                 * logger --name com.hzk.arthas.demo.ThreadPoolTest --level error
                 */
                LOGGER.info("error:" + e.getMessage());
            }
            return httpResponse;
        }

        private Future<String> invokeWithThreadPool(TrdServiceVO trdServiceVO) {
            sleep();
            Future<String> future = threadPool.submit(() -> {
                return HttpClientUtil.get(trdServiceVO.getTrdUrl(), 5000, 5000);
            });
            return future;
        }

        private void sleep() {
            try {
                Thread.currentThread().sleep(1000 * 3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    static class TrdServiceVO {
        private String trdUrl;
        private String name;

        public TrdServiceVO(String trdUrl, String name) {
            this.trdUrl = trdUrl;
            this.name = name;
        }

        public String getTrdUrl() {
            return trdUrl;
        }

        public String getName() {
            return name;
        }
    }

}
