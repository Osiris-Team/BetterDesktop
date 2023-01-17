package com.osiris.betterdesktop;

import com.osiris.betterdesktop.views.AllTab;
import com.osiris.betterdesktop.views.FavoritesTab;
import com.osiris.betterdesktop.views.RecentTab;
import imgui.flag.ImGuiWindowFlags;

import static imgui.ImGui.*;

public class Main {

    public static void main(String[] args) {
        NativeWindow2 win = new NativeWindow2("Hello mom!");
        win.decorate(false).showIcon(false);
        win.onRender.add(() -> {
            // Main
            begin("Main Layout", ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize
                    | ImGuiWindowFlags.NoBackground | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoDecoration
            );
            setWindowPos(0, 0);
            setWindowSize(win.width, win.height);

            float panelWidth = win.width / 3;
            new FavoritesTab(0, 0, panelWidth, win.height);
            new RecentTab(panelWidth, 0, panelWidth, win.height);
            new AllTab(panelWidth * 2, 0, panelWidth, win.height);

            end();
        });

    }

}
