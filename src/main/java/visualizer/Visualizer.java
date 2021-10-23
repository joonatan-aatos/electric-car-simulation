package visualizer;

import simulation.Route;
import simulation.Routes;
import simulation.Simulation;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Visualizer {

    JFrame frame;
    Canvas canvas;

    private enum Palette {
        Primary(Color.decode("#001D3D")),
        PrimaryDark(Color.decode("#000814")),
        PrimaryLight(Color.decode("#003566")),
        PrimaryContrast(Color.decode("#EEEEEE")),
        Secondary(Color.decode("#FFC300")),
        SecondaryLight(Color.decode("#FFD60A")),
        Black(Color.decode("#000000"));

        private final Color color;
        private Palette(Color color_) {
            color = color_;
        }

        public Color getColor() {
            return color;
        }
    }

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
            return;
        }

        Image dbImage = canvas.createImage(1000, 1000); // Create a 1000 wide and 1000 tall image to draw on
        Graphics2D g = (Graphics2D) dbImage.getGraphics(); // The graphics for the dbImage
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        // Draw here
        g.setColor(Palette.Primary.getColor());
        g.fillRect(0, 0, 1000, 1000);

        drawRouteBar(g);

        ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.drawImage(dbImage, 0, 0, canvas.getSize().width, canvas.getSize().height, canvas);

    }

    public void drawRouteBar(Graphics2D g) {

        final HashMap<String, Route> routes = Routes.routes;
        final ArrayList<String> routeKeys = Routes.routeKeys;

        final int BAR_PADDING = 30;
        final int BAR_WIDTH = 8;
        final int BAR_HEIGHT = 1000-2*BAR_PADDING;

        final int END_POINT_DIAMETER = 20;
        final int END_POINT_NAME_PADDING = 42;
        final int nameXCoordinate = BAR_PADDING + BAR_WIDTH / 2 - END_POINT_DIAMETER / 2 + END_POINT_NAME_PADDING;

        g.setColor(Palette.PrimaryLight.getColor());
        g.fillRect(BAR_PADDING, BAR_PADDING, BAR_WIDTH, BAR_HEIGHT);

        double routesLength = routes.values().stream().mapToDouble(Route::getLength).sum();

        double subRouteLength = 0;
        g.setColor(Palette.Secondary.getColor());
        g.setFont(new Font("arial", Font.PLAIN, 30));

        g.fillOval(
                BAR_PADDING + BAR_WIDTH / 2 - END_POINT_DIAMETER / 2,
                1000 - BAR_PADDING - (int) (subRouteLength / routesLength * BAR_HEIGHT) - END_POINT_DIAMETER / 2,
                END_POINT_DIAMETER,
                END_POINT_DIAMETER
        );
        g.drawString(
                routes.get(routeKeys.get(0)).getStartPoint().toString(),
                nameXCoordinate,
                1000 - BAR_PADDING - (int) (subRouteLength / routesLength * BAR_HEIGHT) + END_POINT_DIAMETER / 2
        );
        for (String routeKey : routeKeys) {
            Route route = routes.get(routeKey);
            subRouteLength += route.getLength();
            g.fillOval(
                    BAR_PADDING+BAR_WIDTH / 2 - END_POINT_DIAMETER / 2,
                    1000 - BAR_PADDING - (int) (subRouteLength / routesLength * BAR_HEIGHT) - END_POINT_DIAMETER / 2,
                    END_POINT_DIAMETER,
                    END_POINT_DIAMETER
            );
            g.drawString(
                    route.getEndPoint().toString(),
                    nameXCoordinate,
                    1000 - BAR_PADDING - (int) (subRouteLength / routesLength * BAR_HEIGHT) + END_POINT_DIAMETER / 2
            );
        }
    }
}
