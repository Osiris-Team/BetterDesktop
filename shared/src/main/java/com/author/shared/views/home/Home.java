package com.author.shared.views.home;

import com.author.shared.data.Data;
import com.author.shared.views.AllTab;
import com.author.shared.views.FavoritesTab;
import com.author.shared.views.RecentTab;
import com.osiris.desku.App;
import com.osiris.desku.Route;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.layout.Horizontal;

import java.io.IOException;

import static com.osiris.desku.Statics.*;

public class Home extends Route {
    static {
        try {
            App.appendToGlobalCSS(App.getCSS(Home.class));
            App.appendToGlobalJS(App.getJS(Home.class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Home() {
        super("/");
    }

    @Override
    public Component<?, ?> loadContent() {
        var ly = vertical().size("100vw", "100vh")
            .childGap(true).padding(true);

        String w = (100.0 / 3.0) + "%";
        ly.childHorizontal().grow(1).add(
            new FavoritesTab().width(w),
            new RecentTab().width(w),
            new AllTab().width(w)
        );

        if(Data.favorites.programAndExecution.isEmpty()){
            var lyMsg = horizontal();
            ly.add(lyMsg);
            var txt = text("Do you wish to move your original desktop into the favorites section?").grow(1);
            lyMsg.add(txt);
            lyMsg.add(button("Yes").onClick(e -> {
                Data.moveDesktop();
                txt.setValue("Moving files, this might take a bit...");
                lyMsg.later(c -> {
                   while(Data.isMovingDesktopFiles.get()){
                       Thread.yield();
                   }
                   lyMsg.removeSelf();
                });
            }));
            lyMsg.add(button("No").danger().onClick(e -> lyMsg.removeSelf()));
        }
        return ly;
    }
}
