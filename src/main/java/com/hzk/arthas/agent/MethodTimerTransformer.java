package com.hzk.arthas.agent;

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

public class MethodTimerTransformer implements ClassFileTransformer {
    private final static String prefix = "\nlong startTime = System.currentTimeMillis();\n";
    private final static String postfix = "\nlong endTime = System.currentTimeMillis();\n";

    // 被处理的方法列表
    final static Map<String, List<String>> methodMap = new HashMap<>();
    public MethodTimerTransformer() {
        add("com.hzk.arthas.agent.TimeTest.sayHello");
    }

    private void add(String methodString) {
        String className = methodString.substring(0, methodString.lastIndexOf("."));
        String methodName = methodString.substring(methodString.lastIndexOf(".") + 1);
        List<String> list = methodMap.get(className);
        if (list == null) {
            list = new ArrayList<>();
            methodMap.put(className, list);
        }
        list.add(methodName);
    }

    @Override
    public byte[] transform(
            ClassLoader loader,
            String className,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain,
            byte[] classfileBuffer) throws IllegalClassFormatException {

        if (className.startsWith("com/hzk/")) {
            className = className.replace("/", ".");
            System.out.println("Transforming class: " + className);

            // 在方法前后添加计时逻辑
            CtClass ctclass = null;

            try {
                ctclass = ClassPool.getDefault().get(className);// 使用全称,用于取得字节码类<使用javassist>
                System.out.println(ctclass.getName());
                if (methodMap.containsKey(className)) {
                    for (String methodName : methodMap.get(className)) {
                        System.out.println(methodName);

                        CtMethod ctmethod = ctclass.getDeclaredMethod(methodName);// 得到这方法实例
                        String newMethodName = methodName + "$old";// 新定义一个方法叫做比如sayHello$old
                        ctmethod.setName(newMethodName);// 将原来的方法名字修改

                        // 创建新的方法，复制原来的方法，名字为原来的名字
                        CtMethod newMethod = CtNewMethod.copy(ctmethod, methodName, ctclass, null);

                        // 构建新的方法体
                        StringBuilder bodyStr = new StringBuilder();
                        bodyStr.append("{");
                        bodyStr.append(prefix);
                        bodyStr.append(newMethodName + "($$);\n");// 调用原有代码，类似于method();($$)表示所有的参数
                        bodyStr.append(postfix);
                        String outputStr = "\nSystem.out.println(\"this method " + methodName
                                + " cost:\" +(endTime - startTime) +\"ms.\");";
                        bodyStr.append(outputStr);
                        bodyStr.append("}");

                        newMethod.setBody(bodyStr.toString());// 替换新方法
                        ctclass.addMethod(newMethod);// 增加新方法
                    }

//                    // test,当前工程的target/classes目录
//                    final String targetClassPath = Thread.currentThread().getContextClassLoader().getResource("").toURI().getPath();
//                    //test,输出生成.class文件，仅用于查看
//                    ctclass.writeFile(targetClassPath);

                    return ctclass.toBytecode();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return classfileBuffer;
    }
}
