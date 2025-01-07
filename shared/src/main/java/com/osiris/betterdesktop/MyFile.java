package com.osiris.betterdesktop;

import com.author.shared.data.Data;
import com.author.shared.utils.AWT;
import com.author.shared.utils.AsyncTerminal;
import com.osiris.desku.Icon;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.UI;
import com.osiris.desku.ui.display.Image;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.event.JavaScriptEvent;
import com.osiris.desku.ui.layout.Vertical;
import com.osiris.events.Event;
import com.osiris.jlib.logger.AL;
import mslinks.ShellLink;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static com.osiris.desku.Statics.*;

public class MyFile extends Component<MyFile, File> {

    public Vertical lyIcon;
    public File file;
    public Text name;
    public Exception exception = null;
    /**
     * Only relevant when performing sort/comparison operations.
     */
    public float similariy = 0;

    public MyFile(File file) {
        super(file, File.class);
        this.lyIcon = verticalCL().height("100%");
        this.lyIcon.add(Icon.solid_circle_question().height("100%").width("unset"));
        this.file = file;
        this.name = text(file.getName().replace(".lnk", "")
                .replace(".exe", "")).grow(1).selfCenter2();
        childGap(true);
        padding(false);
        add(name);

        if(file.getName().endsWith(".lnk"))
            add(button("").add(Icon.solid_arrow_right()).secondary());
        else if(file.getName().endsWith(".exe"))
            add(button("").add(Icon.solid_arrow_right()).success());

        sty("cursor", "pointer");
        onClick(e -> {
            start();
        });
    }

    public void fetchIcon(){
        later(_this -> {
            try{
                RenderedImage icon = AWT.convertToRenderedImage(
                    Objects.requireNonNull((ImageIcon) FileSystemView.getFileSystemView().getSystemIcon(file), "Failed to fetch icon for file: "+ file));
                lyIcon.removeAll();
                lyIcon.add(image(icon, file.getParentFile().getName()+"."+file.getName()+".png").height("100%"));
            } catch (Exception e) {
                AL.warn(e);
            }
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
