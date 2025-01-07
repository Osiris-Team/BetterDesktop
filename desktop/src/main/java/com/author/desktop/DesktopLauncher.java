package com.author.desktop;

import com.osiris.desku.App;
import com.osiris.desku.ui.DesktopUIManager;

public class DesktopLauncher {

    public static void main(String[] args) {
        try {
            App.uis = new DesktopUIManager();
            com.author.shared.Main.main(args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
