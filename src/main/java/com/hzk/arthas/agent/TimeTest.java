package com.hzk.arthas.agent;

import java.nio.channels.Pipe;

/**
 * 参考：https://blog.csdn.net/m0_73311735/article/details/132426495
 */
public class TimeTest {

    static {
        System.setProperty("appName", "arthas-test");
    }


    public static void main(String[] args) {
        while (true) {
            sayHello();
        }
    }

    public static void sayHello() {
        try {
            Thread.sleep(2000);
            System.out.print("hello");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
