package com.author.core.views;


import com.author.core.data.Data;
import com.author.core.utils.Arr;
import com.osiris.betterdesktop.MyFile;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.layout.Vertical;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.osiris.desku.Statics.*;

public class FavoritesTab extends Vertical {

    private static final HashMap<String, MyFile> cache = new HashMap<>();
    public static MyFile get(String path) {
        synchronized (cache){
            MyFile f = cache.get(path);
            if(f == null){
                File file = new File(path);
                f = new MyFile(file);
                cache.put(path, f);
            }
            return f;
        }
    }

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

    public FavoritesTab() {
        final Vertical list = vertical()
            .scrollable(true, "100%", "100%", "100%", "5vh");
        later(c -> {
                try {
                    while (true) {
                        ConcurrentHashMap<File, AtomicLong> programs = Data.favorites.programAndExecution;
                        List<FavoritesTab.Temp> finalList = new ArrayList<>();
                        programs.forEach((file, countOpened) -> {
                            finalList.add(new FavoritesTab.Temp(get(file.getAbsolutePath()), countOpened));
                        });
                        finalList.sort(Comparator.comparing(o -> o.countOpened.get())); // Sort ascending by count opened
                        Arr.flip(finalList);
                        lock.lock();
                        list.removeAll();
                        for (FavoritesTab.Temp temp : finalList) {
                            list.add(temp.myFile);
                        }
                        lock.unlock();
                        //System.out.println("Loaded 'favorites' files.");
                        Thread.sleep(3000); // 3sec
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

        add(
            textfield("Search in favorites").onValueChange(ViewUtils.getSearchFunction(list)),
            list
        );

        Text infoMovingDesktop = text("");
        infoMovingDesktop.visible(false);
        later(c -> {
           while (Data.isMovingDesktopFiles.get()){
               infoMovingDesktop.set("Moving desktop files to favorites...\n" +
                   "This might take a bit, please do not abort.\n" +
                   "Moved files: " + Data.countMovedFiles.get());
               infoMovingDesktop.visible(true);
               try {
                   Thread.sleep(1000);
               } catch (InterruptedException e) {
                   throw new RuntimeException(e);
               }
           }
           infoMovingDesktop.visible(false);
        });
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
