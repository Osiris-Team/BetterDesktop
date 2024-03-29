package com.osiris.betterdesktop;

import com.osiris.betterdesktop.data.Data;
import com.osiris.betterdesktop.utils.NoExRunnable;
import com.osiris.betterdesktop.utils.UtilsNative;
import com.osiris.betterdesktop.views.AllTab;
import com.osiris.betterdesktop.views.FavoritesTab;
import com.osiris.betterdesktop.views.RecentTab;
import com.osiris.jlib.Stream;
import imgui.app.Color;
import imgui.flag.ImGuiWindowFlags;

import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static imgui.ImGui.*;

public class Main {

    public static void main(String[] args) {
        try {
            try {
                new UtilsNative().addToAutoStartFolder(new File(
                        System.getProperty("user.dir") + "/BetterDesktop.jar"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            NativeWindow2 win = new NativeWindow2("BetterDesktop", new NativeWindow2.Hints()
                    .decorate(false).focusOnShow(false));
            try{
                win.iconFromResources("icon.png");
            } catch (Exception e) {
                try{
                    win.iconFromFile(new File(System.getProperty("user.dir")+
                        "/src/main/resources/icon.png"));
                } catch (Exception ex) {
                    e.printStackTrace();
                    ex.printStackTrace();
                }
            }
            win.backgroundColor = new Color(0, 0, 0, 0.6f);
            win.onClose.add(() -> System.exit(0));
            win.onRender.add(() -> {
                // Main
                begin("Main Layout", ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize
                        | ImGuiWindowFlags.NoBackground | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoDecoration
                );
                setWindowPos(0, 0);
                setWindowSize(win.width, win.height);

                float panelWidth = win.width / 3;
                FavoritesTab.render(0, 0, panelWidth, win.height);
                RecentTab.render(panelWidth, 0, panelWidth, win.height);
                AllTab.render(panelWidth * 2, 0, panelWidth, win.height);

                end();
            });


            //
            // Set FPS limit to 1 once unfocused.
            //
            AtomicReference<NoExRunnable> oldSleepRunnable = new AtomicReference<>();
            oldSleepRunnable.set(win.sleepRunnable);
            win.onFocus.add((isFocus) -> {
                if(isFocus)
                    win.sleepRunnable = oldSleepRunnable.get();
                else{
                    oldSleepRunnable.set(win.sleepRunnable);
                    win.fpsLimit(1);
                }
            });

            try {
                // Because loading of icons needs to be done
                // on the render thread, we remove the fps limit
                // until there aren't any new icons being loaded, to be able
                // to load all those icons faster.
                win.fpsNoLimit();
                Thread.sleep(10000);
                while(Data.isLoadingPrograms.get()) Thread.sleep(1000);
                long oldCount = 0;
                long newCount = 1;
                while (oldCount != newCount) {
                    oldCount = AllTab.countIconsLoaded.get();
                    Thread.sleep(5000);
                    newCount = AllTab.countIconsLoaded.get();
                }
                win.fpsLimit(60);
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
