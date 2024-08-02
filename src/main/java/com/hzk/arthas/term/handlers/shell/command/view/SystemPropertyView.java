package com.hzk.arthas.term.handlers.shell.command.view;

import com.hzk.arthas.term.handlers.shell.command.model.SystemPropertyModel;
import com.taobao.text.Decoration;
import com.taobao.text.ui.TableElement;
import com.taobao.text.util.RenderUtil;

import java.util.Map;

import static com.taobao.text.ui.Element.label;

public class SystemPropertyView {

    public String draw(Map<String, String> map) {
        TableElement table = new TableElement(1, 4).leftCellPadding(1).rightCellPadding(1);
        table.row(true, label("KEY").style(Decoration.bold.bold()), label("VALUE").style(Decoration.bold.bold()));

        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getKey().equals("appName")) {
                table.row(false, label(entry.getKey()).style(Decoration.bold.bold()), label(entry.getValue()).style(Decoration.bold.bold()));
            } else {
                table.row(entry.getKey(), entry.getValue());
            }
        }
        return RenderUtil.render(table, 120);
    }

}
