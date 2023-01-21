package com.osiris.betterdesktop.utils.flex;

import imgui.ImGui;

public class Text extends Component {
    public Text(Component parent, String text) {
        super(parent, null);
        render = () -> {
            ImGui.text(text);
            // TODO how to set width, height
        };
    }
}
