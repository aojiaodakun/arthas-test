package com.hzk.arthas.term.handlers;

import com.hzk.arthas.term.impl.TermImpl;
import io.termd.core.function.Consumer;

public class DefaultTermStdinHandler implements Consumer<int[]> {

    private TermImpl term;

    public DefaultTermStdinHandler(TermImpl term){
        this.term = term;
    }


    @Override
    public void accept(int[] codePoints) {
        term.echo(codePoints);
        term.getReadline().queueEvent(codePoints);
    }

}
