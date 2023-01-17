package com.osiris.betterdesktop;

import com.osiris.betterdesktop.data.Data;
import com.osiris.betterdesktop.utils.AsyncTerminal;
import mslinks.ShellLink;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import static imgui.ImGui.openPopup;

public class MyFile {
    public ImageIcon icon;
    public File file;
    public String name;
    public int iconTexture = -1;
    public Exception exception = null;
    public Runnable start = () -> {
        try {
            System.out.println(file);
            File finalFile = file;
            if (file.getName().endsWith(".lnk")) {
                ShellLink link = new ShellLink(file);
                String linkRoot = link.resolveTarget();
                if (Objects.equals(linkRoot, "<unknown>")) {
                    System.out.println("Unable to resolve link, thus: start \"\" \"" + file + "\"");
                    startAsync(file);
                } else {
                    finalFile = new File(linkRoot);
                    // TODO support executables that don't end with .exe
                    if (finalFile.isDirectory() || !finalFile.getName().endsWith(".exe"))
                        Desktop.getDesktop().open(finalFile); // Open folder in system file manager or file with default program
                    else
                        startAsync(finalFile);
                }
            } else {
                // TODO support executables that don't end with .exe
                if (finalFile.isDirectory() || !finalFile.getName().endsWith(".exe"))
                    Desktop.getDesktop().open(finalFile); // Open folder in system file manager or file with default program
                else
                    startAsync(finalFile);
            }

            // Add to recently used
            Data.recent.recentFiles.add(file.getAbsolutePath());
            Data.recent.save();

            // Increase execution count in favorites
            if (Data.favorites.programAndExecution.get(file) == null)
                Data.favorites.programAndExecution.put(file, new AtomicLong(0));
            Data.favorites.programAndExecution.get(file).incrementAndGet();
            Data.favorites.save();

        } catch (Exception exception) {
            this.exception = exception;
            exception.printStackTrace();
            openPopup("Error!");
        }
    };
    /**
     * Only relevant when performing sort/comparison operations.
     */
    public float similariy = 0;

    public MyFile(ImageIcon icon, File file) {
        this.icon = icon;
        this.file = file;
        this.name = file.getName().replace(".lnk", "")
                .replace(".exe", "");
    }

    private void startAsync(File file) throws IOException {
        new AsyncTerminal(
                new File(System.getProperty("user.dir")),
                newLine -> {
                },
                System.err::println,
                "start \"" + file + "\"" // This command closes the parent terminal
        );
    }
}
