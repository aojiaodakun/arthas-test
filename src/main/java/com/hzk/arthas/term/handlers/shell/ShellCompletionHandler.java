package com.hzk.arthas.term.handlers.shell;

import com.hzk.arthas.term.handlers.Handler;
import com.hzk.arthas.term.impl.TermImpl;
import io.termd.core.readline.Completion;

public class ShellCompletionHandler implements Handler<Completion> {

    private TermImpl term;

    public ShellCompletionHandler(TermImpl term){
        this.term = term;
    }

    @Override
    public void handle(Completion event) {

    }
}
