package com.hzk.arthas.term.server;

import com.hzk.arthas.term.Helper;
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

public class HttpTelnetTermServerNew {

    private NettyHttpTelnetTtyBootstrap bootstrap;
    private String hostIp;
    private int port;
    private long connectionTimeout;
    private EventExecutorGroup workerGroup;

    public HttpTelnetTermServerNew(String hostIp, int port, long connectionTimeout) {
        this.hostIp = hostIp;
        this.port = port;
        this.connectionTimeout = connectionTimeout;
        this.workerGroup = new NioEventLoopGroup(new DefaultThreadFactory("arthas-TermServer", true));
    }


    public HttpTelnetTermServerNew listen() {
        // TODO: charset and inputrc from options
        bootstrap = new NettyHttpTelnetTtyBootstrap(workerGroup).setHost(hostIp).setPort(port);
        try {
            bootstrap.start(new Consumer<TtyConnection>() {
                @Override
                public void accept(final TtyConnection conn) {
                    TermImpl termImpl = new TermImpl(Helper.loadKeymap(), conn);
                    // 建立连接后，输出welcome
                    String welcome = ArthasBanner.welcome(new HashMap<>());
                    termImpl.write(welcome);
                    // 设置读行
                    termImpl.readline( "[arthas-test]$", new ShellLineHandler(termImpl));
                }
            }).get(connectionTimeout, TimeUnit.MILLISECONDS);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return this;
    }

}
