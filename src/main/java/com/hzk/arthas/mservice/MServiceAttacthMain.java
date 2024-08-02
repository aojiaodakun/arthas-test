package com.hzk.arthas.mservice;


import com.hzk.arthas.agent.TimeTest;
import com.hzk.arthas.util.ProcessUtils;
import com.sun.tools.attach.VirtualMachine;

import java.util.Map;

/**
 * 先启动com.hzk.arthas.agent.TimeTest
 */
public class MServiceAttacthMain {


    public static void main(String[] args) throws Exception{
        String pidArg = System.getProperty("pid");
        String pid = pidArg;
        if (pid == null) {
            String mainClassName = "com.hzk.arthas.mservice.MService";
            if (Boolean.parseBoolean(System.getProperty("rongqi.test", "false"))) {
                mainClassName = "kd.bos.service.bootstrap.Booter";
            }
            Map<Long, String> pid2mainClassMap = ProcessUtils.listProcessByJps(false);
            for (Map.Entry<Long, String> entry : pid2mainClassMap.entrySet()) {
                String value = entry.getValue();
                String[] tempArray = value.split(" ");
                if (tempArray.length > 1) {
                    if (tempArray[1].startsWith(mainClassName)) {
                        pid = String.valueOf(entry.getKey());
                        break;
                    }
                }
            }
        }
        System.err.println("attach pid:" + pid);
        String jar = System.getProperty("agentJar", "arthas-test-1.0-SNAPSHOT.jar");
        System.err.println("agentJar=" + jar);
        VirtualMachine virtualMachine = VirtualMachine.attach(pid);
        if (Boolean.parseBoolean(System.getProperty("rongqi.test", "false"))) {
            virtualMachine.loadAgent("/mservice/" + jar);
        } else {
            String targetClassPath = Thread.currentThread().getContextClassLoader().getResource("").toURI().getPath();
            targetClassPath = targetClassPath.replace("classes/", "") + jar;
            targetClassPath = targetClassPath.substring(1);
            System.out.println("targetClassPath=" + targetClassPath);
            virtualMachine.loadAgent(targetClassPath);
        }


//        System.in.read();
    }

}
