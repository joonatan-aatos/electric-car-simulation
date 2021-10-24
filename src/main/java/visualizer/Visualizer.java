package visualizer;

import simulation.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Visualizer {

    JFrame frame;
    Canvas canvas;

    private final double LENGTH_OF_ROUTES = Routes.routes.values().stream().mapToDouble(Route::getLength).sum();
    private final int BAR_PADDING = 30;
    private final int BAR_WIDTH = 8;
    private final int BAR_HEIGHT = 1000-2*BAR_PADDING;

    private final int END_POINT_DIAMETER = 20;
    private final int CAR_DIAMETER = 10;
    private final int END_POINT_NAME_PADDING = 42;
    private final int NAME_X_COORDINATE = BAR_PADDING + BAR_WIDTH / 2 - END_POINT_DIAMETER / 2 + END_POINT_NAME_PADDING;

    private final int INFO_AREA_WIDTH = 700;
    private final int TEXT_PADDING = 32;

    private int CAR_OPACITY = 255;

    private final Route wholeRoute;

    private enum Palette {
        Primary(Color.decode("#001D3D")),
        PrimaryDark(Color.decode("#001834")),
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
        frame.setSize(800, 800);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        canvas = new Canvas();
        frame.add(canvas);

        wholeRoute = Route.generateShortestRoute(EndPoint.Helsinki, EndPoint.Utsjoki);
    }

    public void draw(Simulation simulation) {
        Graphics graphics = canvas.getGraphics();
        if(graphics == null) {
            System.out.println("Unable to obtain graphics instance");
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
        drawChargingStations(g);
        drawCities(g);
        drawCars(g, simulation.getCars());
        drawInfo(g, simulation);

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

        g.setColor(Palette.PrimaryLight.getColor());
        g.fillRect(BAR_PADDING, BAR_PADDING, BAR_WIDTH, BAR_HEIGHT);
    }

    public void drawCities(Graphics2D g) {

        final HashMap<String, Route> routes = Routes.routes;
        final ArrayList<String> routeKeys = Routes.routeKeys;

        double subRouteLength = 0;
        g.setColor(Palette.Secondary.getColor());
        g.setFont(new Font("arial", Font.PLAIN, 30));

        g.fillOval(
                BAR_PADDING + BAR_WIDTH / 2 - END_POINT_DIAMETER / 2,
                1000 - BAR_PADDING - (int) (subRouteLength / LENGTH_OF_ROUTES * BAR_HEIGHT) - END_POINT_DIAMETER / 2,
                END_POINT_DIAMETER,
                END_POINT_DIAMETER
        );
        g.drawString(
                routes.get(routeKeys.get(0)).getStartPoint().toString(),
                NAME_X_COORDINATE,
                1000 - BAR_PADDING - (int) (subRouteLength / LENGTH_OF_ROUTES * BAR_HEIGHT) + END_POINT_DIAMETER / 2
        );
        for (String routeKey : routeKeys) {
            Route route = routes.get(routeKey);
            subRouteLength += route.getLength();
            g.fillOval(
                    BAR_PADDING+BAR_WIDTH / 2 - END_POINT_DIAMETER / 2,
                    1000 - BAR_PADDING - (int) (subRouteLength / LENGTH_OF_ROUTES * BAR_HEIGHT) - END_POINT_DIAMETER / 2,
                    END_POINT_DIAMETER,
                    END_POINT_DIAMETER
            );
            g.drawString(
                    route.getEndPoint().toString(),
                    NAME_X_COORDINATE,
                    1000 - BAR_PADDING - (int) (subRouteLength / LENGTH_OF_ROUTES * BAR_HEIGHT) + END_POINT_DIAMETER / 2
            );
        }
    }

    public void drawCars(Graphics2D g, ArrayList<Car> carsList) {
        ArrayList<Car> cars = (ArrayList<Car>) carsList.clone();
        for (Car car : cars) {
            Route route = car.getRoute();
            boolean goingUp = false;
            if (route.getStartPoint().index == 0)
                goingUp = true;
            else {
                for (
                        int startPointIndex = route.getStartPoint().index + 1;
                        startPointIndex < EndPoint.values().length;
                        startPointIndex++
                ) {
                    if (EndPoint.values()[startPointIndex] == route.getEndPoint()) {
                        goingUp = true;
                        break;
                    }
                }
            }

            // calculate the y coordinate of the car
            int carYCoordinate = -1;
            if (goingUp) {
                double startDistance = 0;
                for (int i = 0; i < route.getStartPoint().index; i++) {
                    startDistance += Routes.routes.get(Routes.routeKeys.get(i)).getLength();
                }
                double distance = car.getDrivenDistance() + startDistance;
                carYCoordinate = 1000 - BAR_PADDING - (int) (distance / LENGTH_OF_ROUTES * BAR_HEIGHT) - CAR_DIAMETER / 2;
            }
            else {
                double startDistance = 0;
                for (int i = route.getStartPoint().index; i < Routes.routeKeys.size(); i++) {
                    startDistance += Routes.routes.get(Routes.routeKeys.get(i)).getLength();
                }
                double distance = car.getDrivenDistance() + startDistance;
                carYCoordinate = BAR_PADDING + (int) (distance / LENGTH_OF_ROUTES * BAR_HEIGHT) - CAR_DIAMETER / 2;
            }

            // Calculate the x coordinate of the car
            int carXCoordinate = BAR_PADDING + BAR_WIDTH / 2 - CAR_DIAMETER / 2;

            // Calculate the color of the car
            switch (car.getState()) {
                case BatteryDepleted:
                    g.setColor(new Color(0, 0, 0, CAR_OPACITY));
                    break;
                default:
                    double batteryPercentage = car.getBatteryLife()/car.getCarType().capacity;
                    int red = batteryPercentage > 0.5 ? 255 - (int)(car.getBatteryLife()/car.getCarType().capacity/2*255) : 255;
                    int green = batteryPercentage > 0.5 ? 255 : (int)(car.getBatteryLife()/car.getCarType().capacity*2*255);
                    g.setColor(new Color(red, green, 0, CAR_OPACITY));
                    break;
            }

            g.fillOval(
                    carXCoordinate,
                    carYCoordinate,
                    CAR_DIAMETER,
                    CAR_DIAMETER
            );
        }
    }

    public void drawChargingStations(Graphics2D g) {

        ArrayList<ChargingStation> chargingStations = wholeRoute.getChargingStations();

        for (int i = 0; i < chargingStations.size(); i++) {
            ChargingStation chargingStation = chargingStations.get(i);
            double distance = wholeRoute.getChargingStationDistances().get(i);

            switch (chargingStation.getChargers().get(0).getType()) {
                case Tesla:
                    g.setColor(new Color(255, 0, 0));
                    break;
                case CHAdeMO:
                case CCS:
                    g.setColor(new Color(0, 92, 231));
                    break;
                case Tyomaapistoke:
                    g.setColor(new Color(4, 199, 192));
                    break;
                case Type2:
                    g.setColor(new Color(73, 235, 52));
                    break;
            }

            g.fillRect(
                    BAR_PADDING,
                    BAR_PADDING + (int) ((double) BAR_HEIGHT * (1d - distance/wholeRoute.getLength())) - 1,
                    BAR_WIDTH,
                    2
            );
        }
    }

    public void drawInfo(Graphics2D g, Simulation simulation) {
        g.setColor(Palette.PrimaryDark.getColor());
        g.fillRect(1000 - INFO_AREA_WIDTH, 0, INFO_AREA_WIDTH, 1000);

        ArrayList<Car> cars = (ArrayList<Car>) simulation.getCars().clone();
        int onHighwayCount = 0, onWayToChargerCount = 0, onWayFromChargerCount = 0, chargingCount = 0, waitingCount = 0, batteryDepleatedCount = 0, destinationReachedCount = 0;
        for (Car car : cars) {
            switch (car.getState()) {
                case Charging:
                    chargingCount++;
                    break;
                case Waiting:
                    waitingCount++;
                    break;
                case OnHighway:
                    onHighwayCount++;
                    break;
                case OnWayToCharger:
                    onWayToChargerCount++;
                    break;
                case OnWayFromCharger:
                    onWayFromChargerCount++;
                    break;
                case DestinationReached:
                    destinationReachedCount++;
                    break;
                case BatteryDepleted:
                    batteryDepleatedCount++;
                    break;
            }
        }
        final int textSpacing = (int) (TEXT_PADDING * 1.5);

        g.setColor(Palette.Secondary.getColor());
        g.setFont(new Font("arial", Font.PLAIN, 30));
        g.drawString("Valtatiellä: "+onHighwayCount, 1000 - INFO_AREA_WIDTH + TEXT_PADDING, 60);
        g.drawString("Matkalla laturille: "+onWayToChargerCount, 1000 - INFO_AREA_WIDTH + TEXT_PADDING, 60 + textSpacing);
        g.drawString("Matkalla laturilta: "+onWayFromChargerCount, 1000 - INFO_AREA_WIDTH + TEXT_PADDING, 60 + 2 * textSpacing);
        g.drawString("Latautumassa: "+chargingCount, 1000 - INFO_AREA_WIDTH + TEXT_PADDING, 60 + 3 * textSpacing);
        g.drawString("Odottamassa: "+waitingCount, 1000 - INFO_AREA_WIDTH + TEXT_PADDING, 60 + 4 * textSpacing);
        g.drawString("Akku loppunut: "+batteryDepleatedCount, 1000 - INFO_AREA_WIDTH + TEXT_PADDING, 60 + 5 * textSpacing);
        g.drawString("Perillä: "+destinationReachedCount, 1000 - INFO_AREA_WIDTH + TEXT_PADDING, 60 + 6 * textSpacing);
        g.drawString("Yhteensä: "+cars.size(), 1000 - INFO_AREA_WIDTH + TEXT_PADDING, 60 + 8 * textSpacing);
    }
}
