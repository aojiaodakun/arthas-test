package com.hzk.arthas.assist;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;

import java.lang.reflect.Method;

public class JavaassistTest {


    public static void main(String[] args) throws Exception{
//        Class.forName("com.hzk.arthas.vo.StudentVO");


//        ClassPool pool = ClassPool.getDefault();
        ClassPool pool = new ClassPool(true);
        CtClass cc = pool.get("com.hzk.arthas.assist.StudentVO");
        //第一种
        //CtMethod cm = CtMethod.make("public String getName(){return name;}", cc);
        //第二种
        //参数：返回值类型，方法名，参数，对象
        CtMethod cm = new CtMethod(CtClass.intType,"add", new CtClass[]{CtClass.intType, CtClass.intType}, cc);
        cm.setModifiers(Modifier.PUBLIC);//访问范围
        cm.setBody("{return $1+$2;}");
        //cc.removeMethod(m) 删除一个方法
        cc.addMethod(cm);
        //通过反射调用方法
        Class clazz = cc.toClass();
        Object obj = clazz.newInstance();//通过调用无参构造器，生成新的对象
        Method m = clazz.getDeclaredMethod("add", int.class, int.class);
        Object result = m.invoke(obj, 2, 3);
        System.out.println(result);


    }

}
