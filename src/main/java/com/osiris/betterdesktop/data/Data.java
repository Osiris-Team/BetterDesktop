package com.osiris.betterdesktop.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class Data {
    public static DataAll all(){
        try{
            DataAll all = new DataAll();
            all.load();
            return all;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static DataFavorites favorites;
    public static DataRecent recent;
    static{
        try{
            favorites = new DataFavorites();
            recent = new DataRecent();

            favorites.load();
            recent.load();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static AtomicBoolean isLoadingPrograms = new AtomicBoolean(false);
    public static AtomicLong countFound = new AtomicLong();
    public static void all_refresh(){
        isLoadingPrograms.set(true);
        countFound.set(0);
        new Thread(() -> {
            HashSet<File> programs = new HashSet<>();
            programs.addAll(getStartMenuPrograms());
            programs.addAll(getInstalledPrograms());
            DataAll dataAll = new DataAll();
            dataAll.list = programs;
            try {
                dataAll.saveNow();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            isLoadingPrograms.set(false);
        }).start();
    }

    public static List<File> getStartMenuPrograms(){
        List<File> list = new ArrayList<>();
        try{
            for (File root : File.listRoots()) {
                File target = new File(root+ "\\ProgramData\\Microsoft\\Windows\\Start Menu\\Programs");
                if(target.exists())
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

    public static List<File> getInstalledPrograms() {
        List<File> list = new ArrayList<>();
        try{
            for (File root : File.listRoots()) {
                for (File dir : root.listFiles()) {
                    if(dir.getName().equals("Program Files (x86)")
                            || dir.getName().equals("Program Files")){
                        walkFileTree(Files.newDirectoryStream(dir.toPath()), file -> {
                            if(file.getName().endsWith(".exe")){
                                list.add(file);
                                countFound.incrementAndGet();
                            }
                        });
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }


    private static void walkFileTree(DirectoryStream<Path> dir, Consumer<File> onFileFound) throws IOException {
        try{
            for (Path path : dir) {
                File file = path.toFile();
                //System.out.println(file);
                if(file.isDirectory())
                    walkFileTree(Files.newDirectoryStream(path), onFileFound);
                else
                    onFileFound.accept(file);
            }
            dir.close();
        } catch (AccessDeniedException ignored) {}
    }

}
