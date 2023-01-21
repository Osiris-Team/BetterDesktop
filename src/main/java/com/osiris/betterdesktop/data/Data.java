package com.osiris.betterdesktop.data;

import com.osiris.jlib.UtilsFiles;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class Data {
    public static DataFavorites favorites;
    public static DataRecent recent;
    public static Settings settings;
    public static AtomicBoolean isMovingDesktopFiles = new AtomicBoolean(false);
    public static AtomicLong countMovedFiles = new AtomicLong();
    public static AtomicBoolean isLoadingPrograms = new AtomicBoolean(false);
    public static AtomicLong countFound = new AtomicLong();

    static {
        try {
            favorites = new DataFavorites();
            recent = new DataRecent();
            settings = new Settings();

            favorites.load();
            recent.load();
            settings.load();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // Move desktop files to other folder
        new Thread(() -> {
            File newDesktopDir = new File(System.getProperty("user.dir") + "/Desktop");
            newDesktopDir.mkdirs();
            for (File f : newDesktopDir.listFiles()) {
                if (favorites.programAndExecution.get(f) == null)
                    favorites.programAndExecution.put(f, new AtomicLong(0));
            }
            List<File> desktopFiles = getDesktopFiles();
            isMovingDesktopFiles.set(true);
            for (File f : desktopFiles) {
                if (f.getName().equals("desktop.ini")) continue;
                try {
                    File newFile = new File(newDesktopDir + "/" + f.getName());
                    if (f.isDirectory()) FileUtils.copyDirectory(f, newFile);
                    else Files.copy(f.toPath(), newFile.toPath());
                    new UtilsFiles().forceDeleteDirectory(f);
                    favorites.programAndExecution.put(newFile, new AtomicLong(0));
                    countMovedFiles.incrementAndGet();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            favorites.save();
            isMovingDesktopFiles.set(false);
        }).start();
    }

    public static DataAll all() {
        try {
            DataAll all = new DataAll();
            all.load();
            if (all.programs.isEmpty())
                all_refresh();
            all = new DataAll();
            all.load();
            if (countFound.get() == 0)
                countFound.set(all.programs.size());
            return all;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void all_refresh() {
        all_refresh_async();
        try{
            while (isLoadingPrograms.get())
                Thread.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void all_refresh_async() {
        isLoadingPrograms.set(true);
        countFound.set(0);
        new Thread(() -> {
            HashSet<File> programs = new HashSet<>();
            programs.addAll(getStartMenuPrograms());
            programs.addAll(getInstalledPrograms());
            DataAll dataAll = new DataAll();
            dataAll.programs = programs;
            try {
                dataAll.saveNow();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            isLoadingPrograms.set(false);
        }).start();
    }

    public static List<File> getStartMenuPrograms() {
        // TODO check if other platforms have something similar to this?
        List<File> list = new ArrayList<>();
        try {
            for (File root : File.listRoots()) {
                File target = new File(root + "\\ProgramData\\Microsoft\\Windows\\Start Menu\\Programs");
                if (target.exists())
                    walkFileTree(Files.newDirectoryStream(target.toPath()), file -> {
                        list.add(file);
                        countFound.incrementAndGet();
                    });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * @return list of files from all user desktops, where write permission exists.
     */
    public static List<File> getDesktopFiles() {
        // TODO support linux
        List<File> list = new ArrayList<>();
        try {
            // USER DESKTOP
            File desktop = new File(System.getProperty("user.home") + "/Desktop");
            // Check for write permission
            String name = desktop + "/better_desktop_test_file_" + System.currentTimeMillis() + ".txt";
            if (!new File(name).createNewFile())
                throw new Exception("Failed to create a file in desktop (no write permission): " + desktop);
            new File(name).delete();
            for (File f : desktop.listFiles()) {
                list.add(f);
            }

            // Do the same for PUBLIC DESKTOP
            desktop = new File(desktop.getParentFile().getParentFile() + "/Public/Desktop");
            // Write permission is not given, but we can delete and copy stuff here, so skip the check.
            for (File f : desktop.listFiles()) {
                list.add(f);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<File> getInstalledPrograms() {
        // TODO support linux
        List<File> list = new ArrayList<>();
        try {
            for (File root : File.listRoots()) {
                for (File dir : root.listFiles()) {
                    if (dir.getName().equals("Program Files (x86)")
                            || dir.getName().equals("Program Files")) {
                        walkFileTree(Files.newDirectoryStream(dir.toPath()), file -> {
                            if (file.getName().endsWith(".exe")) {
                                list.add(file);
                                countFound.incrementAndGet();
                            }
                        });
                    }
                }
            }
            // Scan user provided directories
            for (File dir : settings.dirsToScan) {
                walkFileTree(Files.newDirectoryStream(dir.toPath()), file -> {
                    if (file.getName().endsWith(".exe")) {
                        list.add(file);
                        countFound.incrementAndGet();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }


    private static void walkFileTree(DirectoryStream<Path> dir, Consumer<File> onFileFound) throws IOException {
        try {
            for (Path path : dir) {
                File file = path.toFile();
                //System.out.println(file);
                if (file.isDirectory())
                    walkFileTree(Files.newDirectoryStream(path), onFileFound);
                else
                    onFileFound.accept(file);
            }
            dir.close();
        } catch (AccessDeniedException ignored) {
        }
    }

}
