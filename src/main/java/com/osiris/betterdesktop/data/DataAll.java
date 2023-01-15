package com.osiris.betterdesktop.data;

import com.osiris.jlib.json.JsonFile;

import java.io.File;
import java.util.HashSet;

public class DataAll extends JsonFile {
    public HashSet<File> list = new HashSet<>();
    public DataAll() {
        super(new File(System.getProperty("user.dir")+"/all.json"));
    }
}
