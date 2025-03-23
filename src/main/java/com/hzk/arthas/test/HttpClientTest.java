package com.hzk.arthas.test;

import com.hzk.arthas.util.HttpClientUtil;

public class HttpClientTest {

    public static void main(String[] args) throws Exception {
        while (true) {
            String response = HttpClientUtil.get("http://localhost:8091/test");
            System.out.println(response);
            Thread.currentThread().sleep(1000);
        }
    }

}
