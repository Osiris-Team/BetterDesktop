package com.author.shared.data;

import com.osiris.jlib.json.JsonFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class MyJsonFile extends JsonFile {
    public transient static CopyOnWriteArrayList<Consumer<MyJsonFile>> onLoaded = new CopyOnWriteArrayList<>();
    public transient static CopyOnWriteArrayList<Consumer<MyJsonFile>> onSaved = new CopyOnWriteArrayList<>();

    public MyJsonFile(File file) {
        super(file);
    }

    @Override
    public void load() throws FileNotFoundException, IllegalAccessException {
        super.load();
        for (var code : onLoaded) {
            code.accept(this);
        }
    }

    @Override
    public void saveNow() throws IOException {
        super.saveNow();
        for (Consumer<MyJsonFile> code : onSaved) {
            code.accept(this);
        }
    }
}
