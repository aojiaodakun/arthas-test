package com.hzk.arthas.term.client;


import com.taobao.arthas.client.IOUtil;
import org.apache.commons.net.telnet.TelnetClient;

import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class TelnetClientTest {

    public static void main(String[] args) throws Exception {
        final TelnetClient telnet = new TelnetClient();
        telnet.setConnectTimeout(5000);
        telnet.connect("localhost", 3658);
        // TODO hzk,增加认证
        OutputStream outputStream = telnet.getOutputStream();
        outputStream.write("auth -n arthas1 arthas1".getBytes());
        // 回车键
        outputStream.write(10);
        outputStream.flush();
        IOUtil.readWrite(telnet.getInputStream(), outputStream, System.in, new OutputStreamWriter(System.out));
    }

}
