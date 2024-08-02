package com.hzk.arthas.agent;


import com.hzk.arthas.attach.AttachMethodTimerTransformer;
import com.hzk.arthas.term.server.HttpTelnetTermServer;
import com.hzk.arthas.term.server.HttpTelnetTermServerNew;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

public class MethodTimerAgent {

    /**
     * 主动agent，即自己启动时添加jvmOpts
     * -javaagent:D:\project\arthas-test\target\arthas-test-1.0-SNAPSHOT.jar
     *
     * @param agentArgs
     * @param instrumentation
     */
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        System.err.println("Agent premain called");
        main(agentArgs, instrumentation);
    }

    /**
     * 被动attach，即attach其他jvm进程
     * com.hzk.arthas.attach.AttacthMain
     *
     * @param agentArgs
     * @param instrumentation
     */
    public static void agentmain(String agentArgs, Instrumentation instrumentation) {
        System.err.println("Agent agentmain called");
        main(agentArgs, instrumentation);

        String className = "com.hzk.arthas.agent.TimeTest";
        Class[] loadedClasses = instrumentation.getAllLoadedClasses();
        Class targetClass = null;
        for (Class tempClass : loadedClasses) {
            if (tempClass.getName().equals(className)) {
                targetClass = tempClass;
                break;
            }
        }
        if (targetClass != null) {
            try {
                instrumentation.retransformClasses(targetClass);
            } catch (UnmodifiableClassException e) {
                e.printStackTrace();
            }
        }
    }


    private static synchronized void main(String args, final Instrumentation inst) {
        /**
         * canRetransform=true
         * 运行过程中只能对类的方法体进行修改，无法新增或者删除方法
         */
        inst.addTransformer(new AttachMethodTimerTransformer(), true);
        // 启动NettyServer，监听传入端口号
        initTelnetTermServer();
    }

    private static void initTelnetTermServer(){
        int port = Integer.getInteger("telnet.port", 3659);
//        HttpTelnetTermServer telnetTermServer = new HttpTelnetTermServer("127.0.0.1", port, 5000L);
//        telnetTermServer.listen();

        HttpTelnetTermServerNew telnetTermServer = new HttpTelnetTermServerNew("127.0.0.1", port, 5000L);
        telnetTermServer.listen();
    }


}