package com.hzk.arthas.term.handlers;

import io.termd.core.function.Consumer;
import io.termd.core.util.Helper;

public class StdinHandlerWrapper implements Consumer<int[]> {
    private final Handler<String> handler;

    public StdinHandlerWrapper(Handler<String> handler) {
        this.handler = handler;
    }

    @Override
    public void accept(int[] codePoints) {
        handler.handle(Helper.fromCodePoints(codePoints));
    }
}
