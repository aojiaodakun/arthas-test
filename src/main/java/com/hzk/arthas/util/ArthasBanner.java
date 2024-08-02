package com.hzk.arthas.util;

import com.taobao.text.ui.TableElement;
import com.taobao.text.util.RenderUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class ArthasBanner {

    private static final String LOGO_LOCATION = "/com/taobao/arthas/core/res/logo.txt";
    private static final String CREDIT_LOCATION = "/com/taobao/arthas/core/res/thanks.txt";
    private static final String VERSION_LOCATION = "/com/taobao/arthas/core/res/version";
    private static final String WIKI = "https://arthas.aliyun.com/doc";
    private static final String TUTORIALS = "https://arthas.aliyun.com/doc/arthas-tutorials.html";
    private static final String ARTHAS_LATEST_VERSIONS_URL = "https://arthas.aliyun.com/api/latest_version";
    private static String LOGO = "Welcome to Arthas-hzk-test";
    private static String VERSION = "unknown";

    public static String welcome(Map<String, String> infos) {
        TableElement table = new TableElement().rightCellPadding(1)
                .row("wiki", wiki())
                .row("tutorials", tutorials())
                .row("version", version())
                .row("main_class", PidUtils.mainClass())
                .row("pid", PidUtils.currentPid())
                .row("time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            table.row(entry.getKey(), entry.getValue());
        }
        return logo() + "\n" + RenderUtil.render(table);
    }

    public static String wiki() {
        return WIKI;
    }

    public static String tutorials() {
        return TUTORIALS;
    }

    public static String version() {
        return VERSION;
    }

    public static String logo() {
        return LOGO;
    }


}
