package com.author.core.home;

import com.author.core.views.AllTab;
import com.author.core.views.FavoritesTab;
import com.author.core.views.RecentTab;
import com.osiris.desku.App;
import com.osiris.desku.Route;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.display.RTable;
import com.osiris.desku.ui.display.Table;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.layout.Horizontal;
import com.osiris.desku.ui.layout.PageLayout;
import com.osiris.desku.ui.layout.Vertical;
import com.osiris.jlib.logger.AL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
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
    public Component<?> loadContent() {
        Horizontal ly = horizontal().size("100vw", "100vh")
            .childGap(true).padding(true);

        String w = (100.0 / 3.0) + "%";
        ly.add(
            new FavoritesTab().width(w),
            new RecentTab().width(w),
            new AllTab().width(w)
        );

        return ly;
    }
}
