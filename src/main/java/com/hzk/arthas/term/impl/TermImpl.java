package com.hzk.arthas.term.impl;

import com.hzk.arthas.term.handlers.CloseHandlerWrapper;
import com.hzk.arthas.term.handlers.CompletionHandler;
import com.hzk.arthas.term.handlers.DefaultTermStdinHandler;
import com.hzk.arthas.term.handlers.EventHandler;
import com.hzk.arthas.term.handlers.Handler;
import com.hzk.arthas.term.handlers.RequestHandler;
import com.hzk.arthas.term.handlers.SizeHandlerWrapper;
import com.hzk.arthas.term.handlers.StdinHandlerWrapper;
import io.termd.core.function.Consumer;
import io.termd.core.readline.Completion;
import io.termd.core.readline.Function;
import io.termd.core.readline.Keymap;
import io.termd.core.readline.Readline;
import io.termd.core.tty.TtyConnection;
import io.termd.core.util.Helper;

import java.util.ArrayList;
import java.util.List;

public class TermImpl {

    private static final List<Function> readlineFunctions = Helper.loadServices(Function.class.getClassLoader(), Function.class);

    private Readline readline;
    private Consumer<int[]> echoHandler;
    private TtyConnection conn;
    private volatile Handler<String> stdinHandler;
    private List<io.termd.core.function.Function<String, String>> stdoutHandlerChain;
    private boolean inReadline;

    public TermImpl(TtyConnection conn) {
        this(com.hzk.arthas.term.Helper.loadKeymap(), conn);
    }

    public TermImpl(Keymap keymap, TtyConnection conn) {
        this.conn = conn;
        readline = new Readline(keymap);
        for (Function function : readlineFunctions) {
            readline.addFunction(function);
        }
        echoHandler = new DefaultTermStdinHandler(this);
        conn.setStdinHandler(echoHandler);
        conn.setEventHandler(new EventHandler(this));
    }

    public void readline(String prompt, Handler<String> lineHandler) {
        if (conn.getStdinHandler() != echoHandler) {
            throw new IllegalStateException();
        }
        if (inReadline) {
            throw new IllegalStateException();
        }
        inReadline = true;
        readline.readline(conn, prompt, new RequestHandler(this, lineHandler));
    }

    public void readline(String prompt, Handler<String> lineHandler, Handler<Completion> completionHandler) {
        if (conn.getStdinHandler() != echoHandler) {
            throw new IllegalStateException();
        }
        if (inReadline) {
            throw new IllegalStateException();
        }
        inReadline = true;
        readline.readline(conn, prompt, new RequestHandler(this, lineHandler), new CompletionHandler(completionHandler));
    }

    public TermImpl closeHandler(final Handler<Void> handler) {
        if (handler != null) {
            conn.setCloseHandler(new CloseHandlerWrapper(handler));
        } else {
            conn.setCloseHandler(null);
        }
        return this;
    }

    public long lastAccessedTime() {
        return conn.lastAccessedTime();
    }

    public String type() {
        return conn.terminalType();
    }

    public int width() {
        return conn.size() != null ? conn.size().x() : -1;
    }

    public int height() {
        return conn.size() != null ? conn.size().y() : -1;
    }

    void checkPending() {
        if (stdinHandler != null && readline.hasEvent()) {
            stdinHandler.handle(Helper.fromCodePoints(readline.nextEvent().buffer().array()));
            checkPending();
        }
    }

    public TermImpl resizehandler(Handler<Void> handler) {
        if (inReadline) {
            throw new IllegalStateException();
        }
        if (handler != null) {
            conn.setSizeHandler(new SizeHandlerWrapper(handler));
        } else {
            conn.setSizeHandler(null);
        }
        return this;
    }

    public TermImpl stdinHandler(final Handler<String> handler) {
        if (inReadline) {
            throw new IllegalStateException();
        }
        stdinHandler = handler;
        if (handler != null) {
            conn.setStdinHandler(new StdinHandlerWrapper(handler));
            checkPending();
        } else {
            conn.setStdinHandler(echoHandler);
        }
        return this;
    }

    public TermImpl stdoutHandler(io.termd.core.function.Function<String, String>  handler) {
        if (stdoutHandlerChain == null) {
            stdoutHandlerChain = new ArrayList<io.termd.core.function.Function<String, String>>();
        }
        stdoutHandlerChain.add(handler);
        return this;
    }

    public TermImpl write(String data) {
        if (stdoutHandlerChain != null) {
            for (io.termd.core.function.Function<String, String> function : stdoutHandlerChain) {
                data = function.apply(data);
            }
        }
        conn.write(data);
        return this;
    }

//    public TermImpl interruptHandler(SignalHandler handler) {
//        interruptHandler = handler;
//        return this;
//    }
//
//    public TermImpl suspendHandler(SignalHandler handler) {
//        suspendHandler = handler;
//        return this;
//    }

    public void close() {
        conn.close();
//        FileUtils.saveCommandHistory(readline.getHistory(), new File(Constants.CMD_HISTORY_FILE));
    }

    public TermImpl echo(String text) {
        echo(Helper.toCodePoints(text));
        return this;
    }

    public void setInReadline(boolean inReadline) {
        this.inReadline = inReadline;
    }

    public Readline getReadline() {
        return readline;
    }

    public void handleIntr(Integer key) {
        System.out.println("handleIntr:" + key);
//        if (interruptHandler == null || !interruptHandler.deliver(key)) {
//            echo(key, '\n');
//        }
    }

    public void handleEof(Integer key) {
        // Pseudo signal
        if (stdinHandler != null) {
            stdinHandler.handle(Helper.fromCodePoints(new int[]{key}));
        } else {
            echo(key);
            readline.queueEvent(new int[]{key});
        }
    }

    public void handleSusp(Integer key) {
//        if (suspendHandler == null || !suspendHandler.deliver(key)) {
//            echo(key, 'Z' - 64);
//        }

        System.out.println("handleSusp:" + key);
        echo(key, 'Z' - 64);
    }

    public TtyConnection getConn() {
        return conn;
    }

    public void echo(int... codePoints) {
        Consumer<int[]> out = conn.stdoutHandler();
        for (int codePoint : codePoints) {
            if (codePoint < 32) {
                if (codePoint == '\t') {
                    out.accept(new int[]{'\t'});
                } else if (codePoint == '\b') {
                    out.accept(new int[]{'\b', ' ', '\b'});
                } else if (codePoint == '\r' || codePoint == '\n') {
                    out.accept(new int[]{'\n'});
                } else {
                    out.accept(new int[]{'^', codePoint + 64});
                }
            } else {
                if (codePoint == 127) {
                    out.accept(new int[]{'\b', ' ', '\b'});
                } else {
                    out.accept(new int[]{codePoint});
                }
            }
        }
    }

}
