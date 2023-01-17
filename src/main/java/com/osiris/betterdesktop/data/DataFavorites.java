package com.osiris.betterdesktop.data;

import com.osiris.jlib.json.JsonFile;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class DataFavorites extends JsonFile {
    public ConcurrentHashMap<File, AtomicLong> programAndExecution = new ConcurrentHashMap<>();

    public DataFavorites() {
        super(new File(System.getProperty("user.dir") + "/favorites.json"));
    }
}
