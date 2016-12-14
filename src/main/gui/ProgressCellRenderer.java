package main.gui;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Created by aval0n on 28/11/2016.
 */
public class ProgressCellRenderer extends JProgressBar implements TableCellRenderer {

    public ProgressCellRenderer() {
        super(0, 100);
        this.setValue(0);
        this.setString("0%");
        this.setStringPainted(true);
        //this.setBackground(Color.RED);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        final String sValue = value.toString().substring(0,4) + "%";
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