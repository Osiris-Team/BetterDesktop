package com.osiris.betterdesktop;

import com.osiris.betterdesktop.data.Data;
import com.osiris.betterdesktop.views.AllTab;
import com.osiris.betterdesktop.views.FavoritesTab;
import com.osiris.betterdesktop.views.RecentTab;
import com.osiris.betterlayout.BLayout;
import com.osiris.betterlayout.utils.UI;
import com.osiris.betterlayout.utils.UIDebugWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class Main {

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setType(JFrame.Type.UTILITY);
        frame.setUndecorated(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Rectangle screenSize = screenSize();
        frame.setSize(screenSize.width, screenSize.height);
        frame.setLocationRelativeTo(null); // center
        frame.setVisible(true);

        BLayout root = new BLayout();
        frame.setContentPane(root);

        FavoritesTab favoritesTab = new FavoritesTab(root);
        RecentTab recentTab = new RecentTab(root);
        AllTab allTab = new AllTab(root);

        root.addH(favoritesTab).width(33);
        root.addH(recentTab).width(33);
        root.addH(allTab).width(33);

        JButton btnLoadData = new JButton("Load data");
        JLabel txtProgramsFound = new JLabel("-");
        root.addV(btnLoadData);
        root.addH(txtProgramsFound);

        btnLoadData.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Data.all_refresh();
                new Thread(() -> {
                    try{
                        while(Data.isLoadingPrograms.get()){
                            Thread.sleep(1000);
                            txtProgramsFound.setText("Loading... Programs found: "+ Data.countFound.get());
                            UI.refresh(txtProgramsFound);
                            UI.revalidateAllUp(txtProgramsFound);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }).start();
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
        });

        new UIDebugWindow(frame.getContentPane());
    }

    public static Rectangle screenSize(){ // without task bar
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        Rectangle screenSize = gc.getBounds();
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);

        int width = screenSize.width - screenInsets.left - screenInsets.right;
        int height = screenSize.height - screenInsets.top - screenInsets.bottom;
        return new Rectangle(width, height);
    }
}
