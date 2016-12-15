package main.gui;

import main.peer.Bitfield;
import main.torrent.TorrentFile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.BitSet;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aahdi
 * Thibault Tourailles
 */
public class PiecesPanel extends JPanel {

    private Bitfield bfield;
    private BitSet set;

    public PiecesPanel() {
        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PiecesPanel.this.repaint();
            }
        });
        timer.start();
    }

    public void setTorrentFile(TorrentFile t) {
        this.bfield = t.getBitfield();
        this.set = bfield.getBitfield();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (bfield == null)
            return;

        Dimension size = getSize();
        int x = 5, y= 5;

        for (int i = 0 ; i < bfield.getBitfieldLength() ; ++i) {
            g.setColor(set.get(i) ? Color.BLUE : Color.RED);
            if (x + 10 >= size.width) {
                x = 5; y += 15;
            }
            g.fillRect(x, y,10, 10);
            x += 15;
        }
    }
}
