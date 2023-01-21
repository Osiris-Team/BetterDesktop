package com.osiris.betterdesktop.views;


import com.osiris.betterdesktop.MyFile;
import com.osiris.betterdesktop.UI;
import com.osiris.betterdesktop.data.Data;
import com.osiris.betterdesktop.utils.Arr;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;
import org.jline.utils.Levenshtein;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static imgui.ImGui.*;

public class RecentTab {
    private static final HashMap<String, MyFile> cache = new HashMap<>();
    public static MyFile get(String path) {
        synchronized (cache){
            MyFile f = cache.get(path);
            if(f == null){
                File file = new File(path);
                f = new MyFile((ImageIcon) FileSystemView.getFileSystemView().getSystemIcon(file),
                        file);
                cache.put(path, f);
            }
            return f;
        }
    }

    public static CopyOnWriteArrayList<MyFile> list = new CopyOnWriteArrayList<>();
    public static CopyOnWriteArrayList<MyFile> listToDisplay = list;
    public static Lock lock = new ReentrantLock();
    static ImString inputValue = new ImString("", 100);
    static Map<String, CopyOnWriteArrayList<MyFile>> inputAndResult = new HashMap<>() {
        @Override
        public CopyOnWriteArrayList<MyFile> put(String key, CopyOnWriteArrayList<MyFile> value) {
            if (keySet().size() > 10) // Map has now size limit of 10
                for (Entry<String, CopyOnWriteArrayList<MyFile>> entry : this.entrySet()) {
                    remove(entry.getKey()); // Remove first entry we get
                    break;
                }
            return super.put(key, value);
        }
    };

    static {
        new Thread(() -> {
            try {
                while (true) {
                    ConcurrentLinkedQueue<String> files = Data.recent.recentFiles;
                    java.util.List<MyFile> list = new ArrayList<>();
                    for (String path : files) {
                        list.add(get(path));
                    }
                    lock.lock();
                    RecentTab.list.clear();
                    RecentTab.list.addAll(list);
                    lock.unlock();
                    //System.out.println("Loaded 'recent' files.");
                    Thread.sleep(3000); // 3sec
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        //});
        }).start();
    }

    public static void render(float x, float y, float width, float height) {
        begin("recent", ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize
                | ImGuiWindowFlags.NoBackground | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoDecoration);
        setWindowPos(x, y);
        setWindowSize(width, height);
        inputText("Search in recent", inputValue);
        beginChild("recent-list");
        //setWindowPos(x, y + 10);
        //setWindowSize(width, height - 10);
        String input = inputValue.get();
        if (!input.isEmpty()) {
            CopyOnWriteArrayList<MyFile> myFiles = inputAndResult.get(input);
            if (myFiles == null) {
                myFiles = new CopyOnWriteArrayList<>();
                for (MyFile p : list) {
                    int levDistance = Levenshtein.distance(input, p.name);
                    int maxLength = Math.max(input.length(), p.name.length());
                    float similarity = (1 - (float) levDistance / maxLength) * 100;
                    if (p.name.toLowerCase().startsWith(input.toLowerCase()))
                        similarity += 100; // Give a 100% boost if it starts the same
                    if (similarity > 30) {
                        p.similariy = similarity;
                        myFiles.add(p);
                    }
                }
                myFiles.sort(Comparator.comparing(o -> o.similariy));
                Arr.flip(myFiles);
                inputAndResult.put(input, myFiles);
            }
            listToDisplay = myFiles;
        } else
            listToDisplay = list;
        lock.lock();
        for (int i = listToDisplay.size() - 1; i >= 0; i--) {
            MyFile p = listToDisplay.get(i);
            if (p.iconTexture == -1) {
                p.iconTexture = UI.toTexture(p.icon);
            }
            sameLine();
            if (imageButton(p.iconTexture, 16, 16))
                p.start.run();
            sameLine(0, 10); // move the next item 10 pixels to the right
            if (selectable(p.name))
                p.start.run();
            newLine();

            if (beginPopup("Error!")) {
                text(p.exception.toString());
                StackTraceElement[] stack = p.exception.getStackTrace();
                for (StackTraceElement el : stack) {
                    text(el.toString());
                }
                endPopup();
            }

        }
        lock.unlock();
        endChild();
        end();
    }

    public java.util.List<Icon> getIcons(java.util.List<File> programs) {
        List<Icon> list = new ArrayList<>();
        for (File file : programs) {
            list.add(FileSystemView.getFileSystemView().getSystemIcon(file));
        }
        return list;
    }
}
