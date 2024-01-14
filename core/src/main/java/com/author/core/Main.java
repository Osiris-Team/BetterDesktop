package com.author.core;

import com.author.core.home.Home;
import com.author.core.data.Data;
import com.author.core.utils.UtilsNative;
import com.author.core.views.AllTab;
import com.osiris.desku.App;
import com.osiris.desku.ui.UI;
import com.osiris.jlib.Stream;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

public class Main {
    public static final ExecutorService executor = Executors.newCachedThreadPool();
    public static Home home = new Home();

    public static void main(String[] args) {
        try {
            App.name = "Desktop";
            try {
                new UtilsNative().addToAutoStartFolder(new File(
                    System.getProperty("user.dir") + "/BetterDesktop.jar"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            UI win = App.uis.create(home, false, true, 100, 100);
            // TODO win.resize(false).collapse(false)
            try {
                Thread.sleep(10000);
                while(Data.isLoadingPrograms.get()) Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
