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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import static imgui.ImGui.*;

public class AllTab {

    public static CopyOnWriteArrayList<MyFile> list = new CopyOnWriteArrayList<>();
    public static CopyOnWriteArrayList<MyFile> listToDisplay = list;
    public static AtomicLong countIconsLoaded = new AtomicLong();
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
            try{
                HashSet<File> programs = Data.all().programs;
                List<MyFile> finalList = new ArrayList<>();
                for (File program : programs) {
                    ImageIcon icon = (ImageIcon) FileSystemView.getFileSystemView().getSystemIcon(program);
                    finalList.add(new MyFile(icon, program));
                    countIconsLoaded.incrementAndGet();
                }
                finalList.sort(Comparator.comparing(o -> o.name)); // Sort alphabetically by name
                list.addAll(finalList);
                //System.out.println("Loaded 'all' " + list.size() + " files.");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public AllTab(float x, float y, float width, float height) {
        begin("all", ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize
                | ImGuiWindowFlags.NoBackground | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoDecoration);
        setWindowPos(x, y);
        setWindowSize(width, height);
        inputText("Search in all", inputValue);
        beginChild("all-list");
        //setWindowPos(x, y + 10);
        //setWindowSize(width, height - 10);
        if (list.isEmpty()) {
            if (Data.isLoadingPrograms.get()) {
                text("Loading data. This might take a while...");
                text("Programs found: " + Data.countFound.get());
            } else {
                text("Loading data. This might take a while...");
                text("Icons loaded: " + countIconsLoaded.get() + "/" + Data.countFound.get());
            }
        } else {
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
        }
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
        /*
        // Working grid view:
        // Problem is that icons are very low quality and
        // app/file names cannot be shown completely
        int countIconsInLine = 10;
        float iconWidth = width / countIconsInLine;
        beginTable(this.toString(), countIconsInLine);
        tableNextRow();
        int a = 0;
        for (MyFile f : listToDisplay) {
            if(a == countIconsInLine) {
                a = 0;
                tableNextRow();
            }
            tableNextColumn();
            if (f.iconTexture == -1) {
                f.iconTexture = UI.toTexture(f.icon);
            }
            if (imageButton(f.iconTexture, iconWidth - 10, iconWidth - 10))
                f.start.run();

            if (selectable(f.name))
                f.start.run();

            if (beginPopup("Error!")) {
                if (f.exception == null) {
                    text("Something went wrong!");
                    text("Check the logs for further information.");
                } else {
                    text("" + f.exception);
                    StackTraceElement[] stack = f.exception.getStackTrace();
                    for (StackTraceElement el : stack) {
                        text(el.toString());
                    }
                }
                endPopup();
            }

            a++;
        }
        endTable();
         */

        endChild();
        end();
    }

    public List<Icon> getIcons(List<File> programs) {
        List<Icon> list = new ArrayList<>();
        for (File file : programs) {
            list.add(FileSystemView.getFileSystemView().getSystemIcon(file));
        }
        return list;
    }
}

