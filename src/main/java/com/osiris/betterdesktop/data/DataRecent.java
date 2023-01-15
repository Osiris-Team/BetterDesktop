package com.osiris.betterdesktop.data;

import com.osiris.jlib.json.JsonFile;

import java.io.File;

public class DataRecent extends JsonFile {
    public DataRecent() {
        super(new File(System.getProperty("user.dir")+"/recent.json"));
    }
}
