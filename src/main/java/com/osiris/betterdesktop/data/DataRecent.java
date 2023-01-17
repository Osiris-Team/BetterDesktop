package com.osiris.betterdesktop.data;

import com.osiris.jlib.json.JsonFile;

import java.io.File;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DataRecent extends JsonFile {
    /**
     * Contains the last used program/file is the last index.
     */
    public AsyncQueue<String> recentFiles = new AsyncQueue<String>();

    public DataRecent() {
        super(new File(System.getProperty("user.dir") + "/recent.json"));
    }

    public static class AsyncQueue<T> extends ConcurrentLinkedQueue<T> {
        @Override
        public boolean add(T o) {
            if (contains(o)) {
                // Means that it already exists, thus remove and re-add
                // to make sure it's at the lists end.
                remove(o);
            }
            return super.add(o);
        }
    }
}
