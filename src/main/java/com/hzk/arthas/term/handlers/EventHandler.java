package com.hzk.arthas.term.handlers;

import com.hzk.arthas.term.impl.TermImpl;
import io.termd.core.function.BiConsumer;
import io.termd.core.readline.Function;
import io.termd.core.readline.Keymap;
import io.termd.core.readline.Readline;
import io.termd.core.tty.TtyConnection;
import io.termd.core.tty.TtyEvent;

public class EventHandler implements BiConsumer<TtyEvent, Integer> {

    private TermImpl term;

    public EventHandler(TermImpl term){
        this.term = term;
    }

    @Override
    public void accept(TtyEvent event, Integer key) {
        switch (event) {
            case INTR:
                term.handleIntr(key);
                break;
            case EOF:
                term.handleEof(key);
                break;
            case SUSP:
                term.handleSusp(key);
                break;
        }
    }
}
