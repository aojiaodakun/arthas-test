package com.hzk.arthas.term.handlers;

import io.termd.core.function.Consumer;
import io.termd.core.readline.Completion;

import java.util.Collections;
import java.util.List;

public class CompletionHandler implements Consumer<Completion> {
    private final Handler<Completion> completionHandler;

    public CompletionHandler(Handler<Completion> completionHandler) {
        this.completionHandler = completionHandler;
    }

    @Override
    public void accept(final Completion completion) {
        try {
            final String line = io.termd.core.util.Helper.fromCodePoints(completion.line());
            completionHandler.handle(completion);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
