package com.osiris.betterdesktop.utils.flex;

import imgui.ImVec2;
import imgui.flag.ImGuiTableFlags;

import java.io.PrintStream;
import java.util.concurrent.CopyOnWriteArrayList;

import static imgui.ImGui.*;

public class FlexLayout extends Component {
    public boolean isHorizontal = false;
    public CopyOnWriteArrayList<Component> components = new CopyOnWriteArrayList<>();

    public FlexLayout(Component parent) {
        super(parent, null);
        this.width = -1; // Wrap to content
        this.height = -1; // Wrap to content
        if (parent == null) {
            // Assume that window is the parent.
            // Update window sizes // TODO only when resize event happens
            ImVec2 windowSize = getWindowSize();
            parent = new Component(null, getWindowPosX(), getWindowPosY(), windowSize.x, windowSize.y, null);
            this.parent = parent;
            // Same size  and pos as window as default.
            /*
            this.x = parent.x;
            this.y = parent.y;
            this.width = parent.width;
            this.height = parent.height;

             */
        }

        render = () -> {
            // Update child component sizes
            for (Component comp : components) {
                //TODO
            }

            // Actually render stuff:
            if (isHorizontal) {
                //TODO set width/height of table/layout somehow
                if (beginTable(this.toString(), components.size(), ImGuiTableFlags.SizingFixedFit)) {
                    tableNextRow(); // 1 row since this is horizontal
                    for (int column = 0; column < components.size(); column++) {
                        tableSetColumnIndex(column);
                        Component comp = components.get(column);
                        //TODO set width/height of comp somehow
                        comp.render.run();
                    }
                    endTable();
                }
            } else {
                if (beginTable(this.toString(), 1)) // 1 column since this is vertical
                {
                    for (int row = 0; row < components.size(); row++) {
                        tableNextRow();
                        tableSetColumnIndex(0);
                        Component comp = components.get(row);
                        //TODO set width/height of comp somehow
                        comp.render.run();
                    }
                    endTable();
                }
            }
            // endChild() cannot be called here, but must be called in the highest parent.
            // thus, we use tables instead.
        };
    }

    public FlexLayout add(Component comp) {
        components.add(comp);
        return this;
    }

    public FlexLayout nextLine() {
        add(new Component(this, this::nextLine));
        return this;
    }

    /**
     * Vertical Layout. <br>
     * Creates a new child {@link FlexLayout} with vertical component
     * alignment and returns it. <br>
     */
    public FlexLayout vertical() {
        FlexLayout vl = new FlexLayout(this);
        vl.isHorizontal = false;
        add(vl);
        return vl;
    }

    /**
     * Horizontal Layout. <br>
     * Creates a new child {@link FlexLayout} with horizontal component
     * alignment and returns it. <br>
     */
    public FlexLayout horizontal() {
        FlexLayout hl = new FlexLayout(this);
        hl.isHorizontal = true;
        add(hl);
        return hl;
    }

    public FlexLayout image(int textureId, float width, float height) {
        add(new Image(this, textureId, width, height));
        return this;
    }

    public FlexLayout imageButton(int textureId, float width, float height) {
        add(new ImageButton(this, textureId, width, height));
        return this;
    }

    public FlexLayout text(String s) {
        add(new Text(this, s));
        return this;
    }

    public FlexLayout setName(String s) {
        this.name = s;
        return this;
    }

    public FlexLayout printTree() {
        return printTree(System.out, 0);
    }

    public FlexLayout printTree(PrintStream out) {
        return printTree(out, 0);
    }

    public FlexLayout printTree(PrintStream out, int spaces) {
        String _spaces = "";
        for (int i = 0; i < spaces; i++) {
            _spaces += " ";
        }
        out.println(_spaces + this.toPrintString());
        for (Component comp : components) {
            if (comp instanceof FlexLayout)
                ((FlexLayout) comp).printTree(out, spaces + 2);
            else {
                out.println(_spaces + "  " + comp.toPrintString());
            }
        }
        return this;
    }

    /**
     * Sets the width and height to the same as its parent.
     */
    public FlexLayout fullSize() {
        this.width = parent.width;
        this.height = parent.height;
        return this;
    }

    /**
     * Sets the width to the same as its parent.
     */
    public FlexLayout fullWidth() {
        this.width = parent.width;
        return this;
    }

    /**
     * Sets the height to the same as its parent.
     */
    public FlexLayout fullHeight() {
        this.height = parent.height;
        return this;
    }

    public FlexLayout pos(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public FlexLayout posX(float x) {
        this.x = x;
        return this;
    }

    public FlexLayout posY(float y) {
        this.y = y;
        return this;
    }

}
