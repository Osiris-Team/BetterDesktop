package com.author.core.views;

import com.author.core.data.Data;
import com.osiris.betterdesktop.MyFile;
import com.osiris.desku.ui.layout.Vertical;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.osiris.desku.Statics.textfield;
import static com.osiris.desku.Statics.vertical;


public class AllTab extends Vertical {

    public AllTab() {
        final Vertical list = vertical()
            .scrollable(true, "100%", "100%", "100%", "5vh");
        later(c -> {
                List<MyFile> finalList = new ArrayList<>();
                for (File program : Data.all().programs) {
                    finalList.add(new MyFile(program));
                    //countIconsLoaded.incrementAndGet();
                }
                finalList.sort(Comparator.comparing(o -> o.name.get())); // Sort alphabetically by name
            list.removeAll();
            int i = 0;
                for (MyFile myFile : finalList) {
                    list.add(myFile);
                    if(i >= 10) break;
                    i++;
                }
                System.out.println("Loaded 'all' " + c.children.size() + " files into "+list.getClass()+".");
            });

        add(
            textfield("Search in all").onValueChange(ViewUtils.getSearchFunction(list)),
            list
        );
    }
}

