package com.hzk.arthas.term.server;

import com.hzk.arthas.term.Helper;
import com.hzk.arthas.term.handlers.DefaultTermStdinHandler;
import com.hzk.arthas.term.handlers.EventHandler;
import com.hzk.arthas.term.handlers.shell.ShellLineHandler;
import com.hzk.arthas.term.impl.TermImpl;
import com.hzk.arthas.util.ArthasBanner;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutorGroup;
import io.termd.core.function.Consumer;
import io.termd.core.telnet.netty.NettyTelnetTtyBootstrap;
import io.termd.core.tty.TtyConnection;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class HttpTelnetTermServer {

    private NettyTelnetTtyBootstrap bootstrap;
    private String hostIp;
    private int port;
    private long connectionTimeout;
    private EventExecutorGroup workerGroup;

    public HttpTelnetTermServer(String hostIp, int port, long connectionTimeout) {
        this.hostIp = hostIp;
        this.port = port;
        this.connectionTimeout = connectionTimeout;
        this.workerGroup = new NioEventLoopGroup(new DefaultThreadFactory("arthas-TermServer", true));
    }


    public HttpTelnetTermServer listen() {
        bootstrap = new NettyTelnetTtyBootstrap().setOutBinary(true).setHost(this.hostIp).setPort(this.port);
        try {
            bootstrap.start(new Consumer<TtyConnection>() {
                @Override
                public void accept(TtyConnection conn) {
                    TermImpl termImpl = new TermImpl(Helper.loadKeymap(), conn);
                    // 建立连接后，输出welcome
                    String welcome = ArthasBanner.welcome(new HashMap<>());
                    termImpl.write(welcome);
                    // 设置读行
                    termImpl.readline( "[arthas-test]$", new ShellLineHandler(termImpl));
                }
            }).get(10, TimeUnit.SECONDS);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        System.out.println("Telnet server started on localhost:" + port);
        return this;
    }

}
