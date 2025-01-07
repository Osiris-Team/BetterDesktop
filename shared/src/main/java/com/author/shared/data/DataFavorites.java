package com.author.shared.data;


import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class DataFavorites extends MyJsonFile {
    public ConcurrentHashMap<File, AtomicLong> programAndExecution = new ConcurrentHashMap<>();

    public DataFavorites() {
        super(new File(System.getProperty("user.dir") + "/favorites.json"));
    }
}
