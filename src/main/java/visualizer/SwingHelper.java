package visualizer;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class SwingHelper {
    public static ArrayList<Component> getOptionsPanel(ActionListener actionListener, ChangeListener changeListener) {
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

        JLabel label1 = new JLabel("TPS");
        label1.setBounds(677, PADDING_TOP + 2 * SPACING, 26, 20);
        label1.setOpaque(true);
        components.add(label1);

        JSlider slider1 = new JSlider(JSlider.HORIZONTAL, 1, 999, 100);
        slider1.setBounds(590, PADDING_TOP + 3 * SPACING, 200, 40);
        slider1.setMajorTickSpacing(499);
        slider1.setMinorTickSpacing(100);
        slider1.setPaintTicks(true);
        slider1.setPaintLabels(true);
        slider1.addChangeListener(changeListener);
        components.add(slider1);

        return components;
    }
}
