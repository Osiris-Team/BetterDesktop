package com.author.core.data;

import com.osiris.jlib.json.JsonFile;

import java.io.File;
import java.util.HashSet;

public class Settings extends JsonFile {
    public HashSet<File> dirsToScan = new HashSet<>();

    public Settings() {
        super(new File(System.getProperty("user.dir") + "/settings.json"));
    }
}
