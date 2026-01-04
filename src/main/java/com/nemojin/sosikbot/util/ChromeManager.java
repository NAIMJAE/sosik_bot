package com.nemojin.sosikbot.util;

import java.io.IOException;

public class ChromeManager {

    /// Kill Chrome Process And Driver for Memory Management
    public static void KillChromeProcess() {
        try {
            Runtime.getRuntime().exec("pkill -f chrome"); // Kill chrome main process
            Runtime.getRuntime().exec("pkill -f chromedriver"); // Kill chrome driver
        } catch (IOException e) {
            System.err.println(":: KILL PROCESS ERROR :: " + e.getMessage());
        }
    }
}
