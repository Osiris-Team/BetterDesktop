package com.author.shared.views;


import com.author.shared.data.Data;
import com.author.shared.data.DataRecent;
import com.author.shared.data.MyJsonFile;
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
            .scrollable(true, "100%", "100%", "100%", "2vh");

        MyJsonFile.onSaved.add((json) -> {
            if(!this.isAttached()) return;
            if(!(json instanceof DataRecent)) return;

            later(c -> {
                refresh(list);
            });
        });

        later(c -> {
            refresh(list);
            OnScroll.registerForMyFilesContainer(list, true);
        });

        add(
            textfield("Search in recent").onValueChange(ViewUtils.getSearchFunction(list)),
            list
        );
    }

    private static void refresh(Vertical list) {
        list.removeAll();
        ConcurrentLinkedQueue<String> files = Data.recent.recentFiles;
        for (String path : files) {
            list.add(get(path));
        }
    }
}
