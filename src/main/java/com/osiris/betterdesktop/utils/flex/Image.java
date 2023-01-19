package com.osiris.betterdesktop.utils.flex;

import imgui.ImGui;

public class Image extends Component {
    public Image(Component parent, int textureId, float width, float height) {
        super(parent, null);
        render = () -> {
            ImGui.image(textureId, width, height);
        };
    }
}
