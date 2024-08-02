package com.hzk.arthas.attach;


import com.hzk.arthas.agent.TimeTest;
import com.hzk.arthas.util.ProcessUtils;
import com.sun.tools.attach.VirtualMachine;

import java.util.Map;

/**
 * 先启动com.hzk.arthas.agent.TimeTest
 */
public class AttacthMain {

    public static void main(String[] args) throws Exception{
        String pid = "";
        Map<Long, String> pid2mainClassMap = ProcessUtils.listProcessByJps(false);
        for (Map.Entry<Long, String> entry : pid2mainClassMap.entrySet()) {
            String value = entry.getValue();
            String[] tempArray = value.split(" ");
            if (tempArray.length > 1) {
                if (tempArray[1].startsWith("com.hzk.arthas.agent.TimeTest")) {
//                if (tempArray[1].startsWith("com.hzk.arthas.mservice.MService")) {
                    pid = String.valueOf(entry.getKey());
                    break;
                }
            }
        }
        String jar = "arthas-test-1.0-SNAPSHOT.jar";
        VirtualMachine virtualMachine = VirtualMachine.attach(pid);
        String targetClassPath = Thread.currentThread().getContextClassLoader().getResource("").toURI().getPath();
        targetClassPath = targetClassPath.replace("classes/", "") + jar;
        targetClassPath = targetClassPath.substring(1);
        virtualMachine.loadAgent(targetClassPath);

//        System.in.read();
    }

}
