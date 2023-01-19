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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static imgui.ImGui.*;

public class FavoritesTab {
    public static CopyOnWriteArrayList<MyFile> list = new CopyOnWriteArrayList<>();
    public static CopyOnWriteArrayList<MyFile> listToDisplay = list;
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
    static Lock lock = new ReentrantLock();

    static {
        new Thread(() -> {
            try {
                while (true) {
                    ConcurrentHashMap<File, AtomicLong> programs = Data.favorites.programAndExecution;
                    List<Temp> finalList = new ArrayList<>();
                    programs.forEach((file, countOpened) -> {
                        ImageIcon icon = (ImageIcon) FileSystemView.getFileSystemView().getSystemIcon(file);
                        finalList.add(new Temp(new MyFile(icon, file), countOpened));
                    });
                    finalList.sort(Comparator.comparing(o -> o.countOpened.get())); // Sort ascending by count opened
                    Arr.flip(finalList);
                    lock.lock();
                    list.clear();
                    for (Temp temp : finalList) {
                        list.add(temp.myFile);
                    }
                    lock.unlock();
                    //System.out.println("Loaded 'favorites' files.");
                    Thread.sleep(3000); // 3sec
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }).start();
    }

    public FavoritesTab(float x, float y, float width, float height) {
        begin("favorites", ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize
                | ImGuiWindowFlags.NoBackground | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoDecoration);
        setWindowPos(x, y);
        setWindowSize(width, height);

        inputText("Search in favorites", inputValue);
        beginChild("favorites-list");
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
        if (Data.isMovingDesktopFiles.get()) {
            text("Moving desktop files to favorites...");
            text("This might take a bit, please do not abort.");
            text("Moved files: " + Data.countMovedFiles.get());
        }
        lock.lock();
        for (MyFile f : listToDisplay) {
            if (f.iconTexture == -1) {
                f.iconTexture = UI.toTexture(f.icon);
            }
            sameLine();
            if (imageButton(f.iconTexture, 16, 16))
                f.start.run();
            sameLine(0, 10); // move the next item 10 pixels to the right
            if (selectable(f.name))
                f.start.run();
            newLine();

            if (beginPopup("Error!")) {
                text(f.exception.toString());
                StackTraceElement[] stack = f.exception.getStackTrace();
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

    static class Temp {
        public MyFile myFile;
        public AtomicLong countOpened;

        public Temp(MyFile myFile, AtomicLong countOpened) {
            this.myFile = myFile;
            this.countOpened = countOpened;
        }
    }
}
