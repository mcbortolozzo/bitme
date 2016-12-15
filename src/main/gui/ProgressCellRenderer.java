package main.gui;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aahdi
 * Thibault Tourailles
 */
public class ProgressCellRenderer extends JProgressBar implements TableCellRenderer {

    public ProgressCellRenderer() {
        super(0, 100);
        this.setValue(0);
        this.setString("0 %");
        this.setStringPainted(true);
        //this.setBackground(Color.RED);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        int length = value.toString().length() >= 4 ? 4 : value.toString().length();
        final String sValue = value.toString().substring(0,length) + "%";
        int index = sValue.indexOf('%');
        if (index != -1) {
            float p = 0;
            try {
                p = Float.parseFloat(sValue.substring(0, index));
            } catch(NumberFormatException e) {
                System.err.println("Impossible to parse");
            }
            this.setValue((int) p);
            this.setString(sValue);
        }
        return this;
    }

}