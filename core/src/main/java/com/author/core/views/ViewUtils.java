package com.author.core.views;

import com.author.core.utils.Arr;
import com.osiris.betterdesktop.MyFile;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.event.TextChangeEvent;
import com.osiris.desku.ui.input.TextField;
import com.osiris.desku.ui.layout.Vertical;
import org.jline.utils.Levenshtein;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class ViewUtils {
    /**
     * Map that caches the previous input and its result list.
     */
    private static Map<String, CopyOnWriteArrayList<MyFile>> inputAndResult = new HashMap<>() {
        @Override
        public CopyOnWriteArrayList<MyFile> put(String key, CopyOnWriteArrayList<MyFile> value) {
            if (keySet().size() > 10) // Map has now size limit of 10
                for (Entry<String, CopyOnWriteArrayList<MyFile>> entry : this.entrySet()) {
                    remove(entry.getKey()); // Remove first entry we get
                    break;
                }
            return super.put(key, value);
        }
    };
    public static Consumer<TextChangeEvent<TextField>> getSearchFunction(Vertical list){
        return e -> {
            String input = e.value;
            if (!input.isEmpty()) {
                CopyOnWriteArrayList<MyFile> myFiles = inputAndResult.get(input);
                if (myFiles == null) {
                    myFiles = new CopyOnWriteArrayList<>();
                    for (Component<?> child : list.children) {
                        MyFile p = (MyFile) child;
                        String pName = p.name.get();
                        int levDistance = Levenshtein.distance(input, pName);
                        int maxLength = Math.max(input.length(), pName.length());
                        float similarity = (1 - (float) levDistance / maxLength) * 100;
                        if (pName.toLowerCase().startsWith(input.toLowerCase()))
                            similarity += 100; // Give a 100% boost if it starts the same
                        if (similarity > 30) {
                            p.similariy = similarity;
                            myFiles.add(p);
                        }
                    }
                    myFiles.sort(Comparator.comparing(o -> o.similariy));
                    Arr.flip(myFiles);
                    inputAndResult.put(input, myFiles);
                }
                // Update UI
                list.removeAll();
                for (MyFile myFile : myFiles) {
                    list.add(myFile);
                }
            }
        };
    }
}
