package com.osiris.betterdesktop.utils.flex;

public class FlexExample {
    public static void render(){
        FlexLayout ly = new FlexLayout(null); // parent == null thus sets the window as its parent
        // Default alignment of FlexLayout is vertical
        ly.horizontal() // Creates a new child FlexLayout with horizontal alignment
                .text("Some ").text("text!");
        ly.vertical() // Creates a new child FlexLayout with vertical alignment
                .text("More ").text("text!");
    }
}
