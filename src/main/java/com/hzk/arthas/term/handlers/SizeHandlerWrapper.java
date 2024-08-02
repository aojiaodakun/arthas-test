package com.hzk.arthas.term.handlers;

import io.termd.core.function.Consumer;
import io.termd.core.util.Vector;

public class SizeHandlerWrapper implements Consumer<Vector> {
    private final Handler<Void> handler;

    public SizeHandlerWrapper(Handler<Void> handler) {
        this.handler = handler;
    }

    @Override
    public void accept(Vector resize) {
        handler.handle(null);
    }
}

