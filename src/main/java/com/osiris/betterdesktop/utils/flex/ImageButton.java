package com.osiris.betterdesktop.utils.flex;

import imgui.ImGui;

public class ImageButton extends Component {
    public ImageButton(Component parent, int textureId, float width, float height) {
        super(parent, null);
        render = () -> {
            ImGui.imageButton(textureId, width, height);
        };
    }
}
