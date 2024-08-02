package com.hzk.arthas.term;

import io.termd.core.readline.Keymap;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class Helper {

    public static Keymap loadKeymap() {
        return new Keymap(loadInputRcFile());
    }

    public static InputStream loadInputRcFile() {
        InputStream inputrc;
        // step 1: load custom keymap file
        try {
            String customInputrc = System.getProperty("user.home") + "/.arthas/conf/inputrc";
            inputrc = new FileInputStream(customInputrc);
            return inputrc;
        } catch (Throwable e) {
            // ignore
        }

        // step 2: load arthas default keymap file
        inputrc = Helper.class.getClassLoader().getResourceAsStream("com/taobao/arthas/core/shell/term/readline/inputrc");
        if (inputrc != null) {
            System.out.println("Loaded arthas keymap file from " + "com/taobao/arthas/core/shell/term/readline/inputrc");
            return inputrc;
        }

        // step 3: fall back to termd default keymap file
        inputrc = Keymap.class.getResourceAsStream("inputrc");
        if (inputrc != null) {
            return inputrc;
        }

        throw new IllegalStateException("Could not load inputrc file.");
    }


//    public static Buffer loadResource(FileSystem fs, String path) {
//        try {
//            return fs.readFileBlocking(path);
//        } catch (Exception e) {
//            return loadResource(path);
//        }
//    }

//    public static Buffer loadResource(String path) {
//        URL resource = HttpTermServer.class.getResource(path);
//        if (resource != null) {
//            try {
//                byte[] tmp = new byte[512];
//                InputStream in = resource.openStream();
//                Buffer buffer = Buffer.buffer();
//                while (true) {
//                    int l = in.read(tmp);
//                    if (l == -1) {
//                        break;
//                    }
//                    buffer.appendBytes(tmp, 0, l);
//                }
//                return buffer;
//            } catch (IOException ignore) {
//            }
//        }
//        return null;
//    }
}
