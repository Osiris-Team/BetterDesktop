package com.author.core.views;


import com.author.core.data.Data;
import com.osiris.betterdesktop.MyFile;
import com.osiris.desku.ui.layout.Vertical;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.osiris.desku.Statics.textfield;
import static com.osiris.desku.Statics.vertical;


public class RecentTab extends Vertical {
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

    public static Lock lock = new ReentrantLock();
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

    public RecentTab() {
        final Vertical list = vertical()
            .scrollable(true, "100%", "100%", "100%", "5vh");
        later(c -> {
            try {
                while (true) {
                    list.removeAll();
                    ConcurrentLinkedQueue<String> files = Data.recent.recentFiles;
                    for (String path : files) {
                        list.add(get(path));
                    }
                    //System.out.println("Loaded 'recent' files.");
                    Thread.sleep(3000); // 3sec
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        add(
            textfield("Search in recent").onValueChange(ViewUtils.getSearchFunction(list)),
            list
        );
    }
}
