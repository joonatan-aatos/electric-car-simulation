package visualizer;

import simulation.Simulation;

import javax.swing.*;
import java.awt.*;

public class Visualizer {

    JFrame frame;
    Canvas canvas;

    public Visualizer() {
        frame = new JFrame("Simulaatio");
        frame.setSize(600, 600);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        canvas = new Canvas();
        frame.add(canvas);
    }

    public void draw(Simulation simulation) {
        Graphics graphics = canvas.getGraphics();
        if(graphics == null) {
            //System.out.println("Canvas.getGraphics() returned null!");
            return;
        }

        Image dbImage = canvas.createImage(1000, 1000); // Create a 1000 wide and 1000 tall image to draw on
        Graphics2D g = (Graphics2D) dbImage.getGraphics(); // The graphics for the dbImage
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        // Draw here
        g.setColor(Color.RED);
        g.fillRect(0, 0, 400, 400);

        ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.drawImage(dbImage, 0, 0, canvas.getSize().width, canvas.getSize().height, canvas);

    }
}
