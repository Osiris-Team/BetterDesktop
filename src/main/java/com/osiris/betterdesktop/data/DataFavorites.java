package com.osiris.betterdesktop.data;

import com.osiris.jlib.json.JsonFile;

import java.io.File;

public class DataFavorites extends JsonFile {
    public DataFavorites() {
        super(new File(System.getProperty("user.dir")+"/favorites.json"));
    }
}
