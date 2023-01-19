package com.osiris.betterdesktop.utils.flex;

public class Component {
    public String name;
    public Component parent;
    public float x, y, width, height;
    public Runnable render;

    public Component(Component parent, Runnable render) {
        this(parent, 0, 0, 0, 0, render);
    }

    public Component(Component parent, float x, float y, float width, float height, Runnable render) {
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.render = render;
    }

    public String toPrintString() {
        return "Component: (" + name + ")" + this + " " + x + "x " + y + "y " + width + "w " + height + "h Parent: " + this.parent;
    }
}
