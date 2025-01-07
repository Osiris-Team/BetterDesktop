package com.author.shared.data;


import java.io.File;
import java.util.HashSet;

public class DataAll extends MyJsonFile {
    /**
     * Contains .lnk and .exe files.
     */
    public HashSet<File> programs = new HashSet<>();

    public DataAll() {
        super(new File(System.getProperty("user.dir") + "/all.json"));
    }
}
