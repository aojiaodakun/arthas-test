package com.hzk.arthas.agent;


import com.hzk.arthas.attach.AttachMethodTimerTransformer;

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
        System.out.println("Agent premain called");
        instrumentation.addTransformer(new MethodTimerTransformer());
    }

    /**
     * 被动attach，即attach其他jvm进程
     * com.hzk.arthas.attach.AttacthMain
     *
     * @param agentArgs
     * @param instrumentation
     */
    public static void agentmain(String agentArgs, Instrumentation instrumentation) {
        System.out.println("Agent agentmain called");
        /**
         * canRetransform=true
         * 运行过程中只能对类的方法体进行修改，无法新增或者删除方法
         */
        instrumentation.addTransformer(new AttachMethodTimerTransformer(), true);

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

}