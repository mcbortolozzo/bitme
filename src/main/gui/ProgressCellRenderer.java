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
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        final String sValue = value.toString();
        int index = sValue.indexOf('%');
        if (index != -1) {
            int p = 0;
            try {
                p = Integer.parseInt(sValue.substring(0, index));
            } catch(NumberFormatException e) {
                System.err.println("Inpossible to parse");
            }
            this.setValue(p);
            this.setString(sValue);
        }
        return this;
    }

}