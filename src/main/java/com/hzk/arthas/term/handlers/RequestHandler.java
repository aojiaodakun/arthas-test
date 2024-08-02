package com.hzk.arthas.term.handlers;

import com.hzk.arthas.term.impl.TermImpl;
import io.termd.core.function.Consumer;

public class RequestHandler implements Consumer<String> {
    private TermImpl term;
    private final Handler<String> lineHandler;

    public RequestHandler(TermImpl term, Handler<String> lineHandler) {
        this.term = term;
        this.lineHandler = lineHandler;
    }

    @Override
    public void accept(String line) {
        term.setInReadline(false);
        lineHandler.handle(line);
    }
}
