package com.hzk.arthas.term.handlers;

import io.termd.core.function.Consumer;

public class CloseHandlerWrapper implements Consumer<Void> {
    private final Handler<Void> handler;

    public CloseHandlerWrapper(Handler<Void> handler) {
        this.handler = handler;
    }

    @Override
    public void accept(Void v) {
        handler.handle(v);
    }
}
