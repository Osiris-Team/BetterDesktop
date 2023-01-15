package com.osiris.betterdesktop.views;

import com.osiris.betterdesktop.data.Data;
import com.osiris.betterdesktop.utils.AsyncTerminal;
import com.osiris.betterlayout.BLayout;
import com.osiris.betterlayout.utils.UI;
import mslinks.ShellLink;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

public class AllTab extends BLayout {

    public AllTab() {
        init();
    }

    public AllTab(Container parent) {
        super(parent);
        init();
    }

    public AllTab(Container parent, boolean isCropToContent) {
        super(parent, isCropToContent);
        init();
    }

    public AllTab(Container parent, int widthPercent, int heightPercent) {
        super(parent, widthPercent, heightPercent);
        init();
    }

    public void init(){
        try{
            new Thread(() -> {
                HashSet<File> programs = Data.all().list;
                int numberOfRows = (int) Math.ceil(programs.size() / 4.0); // calculate number of rows required to display all the programs
                System.out.println(numberOfRows+"x"+4);
                int i = 1;
                for (File program : programs) {
                    ImageIcon icon = (ImageIcon) FileSystemView.getFileSystemView().getSystemIcon(program);
                    //Image image = icon.getImage(); // get the image from the icon
                    //Image newImage = image.getScaledInstance(64, 64, Image.SCALE_SMOOTH); // scale the image to 64x64 pixels
                    //icon = new ImageIcon(newImage); // create a new icon from the scaled image
                    JLabel jLabelIcon = new JLabel(icon);
                    MouseListener mouseAdapter = new MouseListener() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            try{
                                ShellLink link = new ShellLink(program);
                                String exe = link.resolveTarget();
                                if(Objects.equals(exe, "<unknown>")){
                                    System.out.println("start \"\" \""+program+"\"");
                                    new AsyncTerminal(
                                            new File(System.getProperty("user.dir")),
                                            newLine -> {},
                                            System.err::println,
                                            "start \""+program+"\"" // This command closes the parent terminal
                                    );
                                    //Runtime.getRuntime().exec();
                                }
                                else{
                                    System.out.println(program+" -> "+exe);
                                    new ProcessBuilder().command(exe).start();
                                }
                            } catch (Exception exception) {
                                exception.printStackTrace();
                            }
                        }

                        @Override
                        public void mousePressed(MouseEvent e) {

                        }

                        @Override
                        public void mouseReleased(MouseEvent e) {

                        }

                        @Override
                        public void mouseEntered(MouseEvent e) {

                        }

                        @Override
                        public void mouseExited(MouseEvent e) {

                        }
                    };
                    jLabelIcon.addMouseListener(mouseAdapter);
                    addV(jLabelIcon);
                    JLabel jLabelText = new JLabel(program.getName().replace(".lnk", ""));
                    jLabelText.addMouseListener(mouseAdapter);
                    addH(jLabelText);
                    i++;
                    //add(new JLabel(program.getName()));
                }
                makeScrollable();
                UI.refresh(this);
                UI.revalidateAllUp(this);
                System.out.println(programs.size());
            }).start();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public List<Icon> getIcons(List<File> programs){
        List<Icon> list = new ArrayList<>();
        for (File file : programs) {
            list.add(FileSystemView.getFileSystemView().getSystemIcon(file));
        }
        return list;
    }
}