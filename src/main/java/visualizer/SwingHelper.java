package visualizer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class SwingHelper {
    public static ArrayList<Component> getOptionsPanel(ActionListener actionListener) {
        ArrayList<Component> components = new ArrayList<>();

        final int SPACING = 30;
        final int PADDING_TOP = 15;

        JCheckBox checkBox1 = new JCheckBox("Näytä latauspisteet", true);
        checkBox1.setBounds(600, PADDING_TOP, 180, 20);
        checkBox1.addActionListener(actionListener);
        components.add(checkBox1);

        JCheckBox checkBox2 = new JCheckBox("Näytä kaupungit", true);
        checkBox2.setBounds(600, PADDING_TOP + SPACING, 180, 20);
        checkBox2.addActionListener(actionListener);
        components.add(checkBox2);

        return components;
    }
}
