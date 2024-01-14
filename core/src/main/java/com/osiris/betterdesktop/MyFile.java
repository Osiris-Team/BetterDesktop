package com.osiris.betterdesktop;

import com.author.core.data.Data;
import com.author.core.utils.AWT;
import com.author.core.utils.AsyncTerminal;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.display.Text;
import mslinks.ShellLink;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import static com.osiris.desku.Statics.*;

public class MyFile extends Component<MyFile> {

    public File file;
    public Text name;
    public int iconTexture = -1;
    public Exception exception = null;
    /**
     * Only relevant when performing sort/comparison operations.
     */
    public float similariy = 0;

    public MyFile(File file) {
        this.file = file;
        this.name = text(file.getName().replace(".lnk", "")
                .replace(".exe", ""));
        childGap(true);
        add(name);
    }

    public void fetchIcon(){
        later(_this -> {
            RenderedImage icon = AWT.convertToRenderedImage(
                (ImageIcon) FileSystemView.getFileSystemView().getSystemIcon(file));
            addAt(0, image(icon, file.getParentFile().getName()+"."+file.getName()));
        });
    }

    public void start(){
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
        }
    };

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
