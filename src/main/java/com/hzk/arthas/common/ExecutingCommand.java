package com.hzk.arthas.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ExecutingCommand {

    /**
     * Executes a command on the native command line and returns the result line by
     * line.
     *
     * @param cmdToRunWithArgs
     *            Command to run and args, in an array
     * @return A list of Strings representing the result of the command, or empty
     *         string if the command failed
     */
    public static List<String> runNative(String[] cmdToRunWithArgs) {
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(cmdToRunWithArgs);
        } catch (SecurityException e) {
            e.printStackTrace();
            return new ArrayList<String>(0);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<String>(0);
        }

        ArrayList<String> sa = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                sa.add(line);
            }
            p.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<String>(0);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
            Thread.currentThread().interrupt();
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sa;
    }

}
