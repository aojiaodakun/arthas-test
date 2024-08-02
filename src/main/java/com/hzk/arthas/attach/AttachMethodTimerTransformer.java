package com.hzk.arthas.attach;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttachMethodTimerTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(
            ClassLoader loader,
            String className,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain,
            byte[] classfileBuffer) throws IllegalClassFormatException {

        if (className.startsWith("com/hzk/")) {
            className = className.replace("/", ".");
            if (className.equals("com.hzk.arthas.agent.TimeTest")) {
                try {
                    String methodName = "sayHello";
                    CtClass ctclass = ClassPool.getDefault().get(className);// 使用全称,用于取得字节码类<使用javassist>
                    CtMethod ctmethod = ctclass.getDeclaredMethod(methodName);// 得到这方法实例
                    ctmethod.insertBefore("System.out.print(\" dakun\");System.out.println();");
                    return ctclass.toBytecode();
                } catch (Exception ex){
                    ex.printStackTrace();
                }
                return classfileBuffer;
            }
        }
        return classfileBuffer;
    }
}
