package com.author.core.data;

import com.osiris.jlib.json.JsonFile;

import java.io.File;
import java.util.HashSet;

public class DataAll extends JsonFile {
    /**
     * Contains .lnk and .exe files.
     */
    public HashSet<File> programs = new HashSet<>();

    public DataAll() {
        super(new File(System.getProperty("user.dir") + "/all.json"));
    }
}
