package com.hzk.arthas.term.handlers.shell.command;

import com.hzk.arthas.term.handlers.shell.command.model.SystemPropertyModel;
import com.hzk.arthas.term.handlers.shell.command.view.SystemPropertyView;

public class SystemPropertyCommand {


    public String process() {
        SystemPropertyModel systemPropertyModel = new SystemPropertyModel(System.getProperties());
        SystemPropertyView view = new SystemPropertyView();
        return view.draw(systemPropertyModel.getProps());
    }

}
