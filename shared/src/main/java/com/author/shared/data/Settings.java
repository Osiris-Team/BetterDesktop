package com.author.shared.data;


import java.io.File;
import java.util.HashSet;

public class Settings extends MyJsonFile {
    public HashSet<File> dirsToScan = new HashSet<>();

    public Settings() {
        super(new File(System.getProperty("user.dir") + "/settings.json"));
    }
}
