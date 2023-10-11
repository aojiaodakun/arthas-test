package com.hzk.arthas.agent;

/**
 * 参考：https://blog.csdn.net/m0_73311735/article/details/132426495
 */
public class TimeTest {


    public static void main(String[] args) {
        while (true) {
            sayHello();
        }
    }

    public static void sayHello() {
        try {
            Thread.sleep(2000);
            System.out.println("hello world!!");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
