package com.author.shared.views;

import com.author.shared.data.Data;
import com.osiris.betterdesktop.MyFile;
import com.osiris.desku.Icon;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.layout.Vertical;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.osiris.desku.Statics.*;


public class AllTab extends Vertical {

    public boolean isOnScrollRegistered = false;

    public AllTab() {
        final Vertical items = vertical()
            .scrollable(true, "100%", "100%", "100%", "2vh");
        refresh(items);

        add(
            horizontalCL()
                .add(textfield("Search in all").onValueChange(ViewUtils.getSearchFunction(items)))
                .add(button("").add(Icon.solid_arrows_rotate()).onClick(e -> {
                    e.comp.enable(false);
                    Data.all_refresh_async();
                    refresh(items);
                    while(Data.isLoadingPrograms.get()) Thread.yield();
                    e.comp.enable(true);
                })),
            items
        );
    }

    private void refresh(Vertical list) {
        list.removeAll();

        Text txt = text("");
        list.add(txt);
        txt.visible(false);
        later(c -> {
            while (Data.isLoadingPrograms.get()){
                txt.setValue("Searching programs...\n" +
                    "This might take a bit, please do not abort.\n" +
                    "Found: " + Data.countFound.get());
                txt.visible(true);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            txt.visible(false);
        });

        later(c -> {
                List<MyFile> finalList = new ArrayList<>();
                for (File program : Data.all().programs) {
                    finalList.add(new MyFile(program));
                    //countIconsLoaded.incrementAndGet();
                }
                finalList.sort(Comparator.comparing(o -> o.name.getValue())); // Sort alphabetically by name
            list.removeAll();
                for (MyFile myFile : finalList) {
                    list.add(myFile);
                }
                System.out.println("Loaded 'all' " + list.children.size() + " files into "+ list.getClass()+".");
                if(!isOnScrollRegistered){
                    isOnScrollRegistered = true;
                    OnScroll.registerForMyFilesContainer(list, true);
                }
            });
    }
}

