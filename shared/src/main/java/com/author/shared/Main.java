package com.author.shared;

import com.author.shared.views.home.Home;
import com.author.shared.utils.UtilsNative;
import com.osiris.desku.App;
import com.osiris.desku.ui.UI;
import com.osiris.jlib.Stream;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static final ExecutorService executor = Executors.newCachedThreadPool();
    public static Home home = new Home();

    public static void main(String[] args) {
        try {
            App.name = "Desktop";
            var logger = new App.LoggerParams();
            //logger.debug = true; App.isInDepthDebugging = true;
            App.init(null, logger);
            try {
                new UtilsNative().addToAutoStartFolder(new File(
                    System.getProperty("user.dir") + "/BetterDesktop.jar"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            UI win = App.uis.create(home, false, false, 100, 100);
            // TODO win.resize(false).collapse(false)

        } catch (Exception e) {
            e.printStackTrace();
            try {
                File f = new File(System.getProperty("user.dir") + "/" + e.getClass().toString() + ".txt");
                f.createNewFile();
                String s = new Date() + "\n" + e.getClass() + "\n" + e.getMessage();
                for (StackTraceElement el : e.getStackTrace()) {
                    s += el.toString()+"\n";
                }
                Throwable cause = e.getCause();
                while(cause != null){
                    for (StackTraceElement el : cause.getStackTrace()) {
                        s += el.toString()+"\n";
                    }
                    cause = cause.getCause();
                }
                Stream.write(s, new FileOutputStream(f));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
