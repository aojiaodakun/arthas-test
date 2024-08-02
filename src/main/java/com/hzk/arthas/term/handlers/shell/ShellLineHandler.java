package com.hzk.arthas.term.handlers.shell;

import com.hzk.arthas.term.handlers.Handler;
import com.hzk.arthas.term.handlers.shell.command.SystemPropertyCommand;
import com.hzk.arthas.term.impl.TermImpl;

public class ShellLineHandler implements Handler<String> {

    private TermImpl term;

    public ShellLineHandler(TermImpl term){
        this.term = term;
    }


    @Override
    public void handle(String line) {
        System.out.println(line);
        term.write("You enter:" + line + "\n");
        if (line.equals("sysprop")) {
            SystemPropertyCommand systemPropertyCommand = new SystemPropertyCommand();
            term.write(systemPropertyCommand.process());
        } else if(line.equals("stop")) {
            term.close();
        }
        // 设置读行
        term.readline( "[arthas-test]$", this);
    }


}
