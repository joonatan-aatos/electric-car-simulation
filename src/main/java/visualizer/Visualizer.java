package visualizer;

import simulation.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class Visualizer {

    JFrame frame;
    Canvas canvas;
    Simulation simulation;

    private boolean showChargers = true;
    private boolean showCities = true;
    private int carColorCodeIndex = 0;

    private final Routes routes;
    private final double LENGTH_OF_ROUTES;
    private final int BAR_PADDING = 30;
    private final int BAR_WIDTH = 8;
    private final int BAR_HEIGHT = 1000-2*BAR_PADDING;

    private final int END_POINT_DIAMETER = 16;
    private final int CAR_DIAMETER = 10;
    private final int END_POINT_NAME_PADDING = 42;
    private final int NAME_X_COORDINATE = BAR_PADDING + BAR_WIDTH / 2 - END_POINT_DIAMETER / 2 + END_POINT_NAME_PADDING;

    private final int INFO_AREA_WIDTH = 700;
    private final int TEXT_PADDING = 32;
    private final int TEXT_PADDING_TOP = 50;

    private int CAR_OPACITY = 50;

    private final Route wholeRoute;
    private final ArrayList<Component> optionsComponents;

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

    public Visualizer(Simulation simulation_, Routes routes_) {
        routes = routes_;
        simulation = simulation_;

        LENGTH_OF_ROUTES = routes.routes.values().stream().mapToDouble(Route::getLength).sum();

        frame = new JFrame("Simulaatio");
        frame.setSize(800, 800);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        canvas = new Canvas();
        wholeRoute = routes.generateShortestRoute2(EndPoint.Helsinki, EndPoint.Utsjoki);

        optionsComponents = SwingHelper.getOptionsPanel(
                actionEvent -> {
                    if (actionEvent.getSource() instanceof JComboBox)
                        carColorCodeIndex = ((JComboBox<String>) actionEvent.getSource()).getSelectedIndex();
                    else {
                        switch (actionEvent.getActionCommand()) {
                            case "Näytä latauspisteet":
                                showChargers = ((JCheckBox) actionEvent.getSource()).isSelected();
                                break;
                            case "Näytä kaupungit":
                                showCities = ((JCheckBox) actionEvent.getSource()).isSelected();
                                break;
                        }
                    }
                }, changeEvent -> {
                    simulation.setTps(((JSlider) changeEvent.getSource()).getValue());
                }
        );
        for (Component c : optionsComponents) {
            c.setVisible(true);
            c.setBackground(Palette.PrimaryDark.getColor());
            c.setForeground(Palette.Secondary.getColor());
            frame.add(c);
        }

        frame.add(canvas);

        frame.setVisible(true);
    }

    public void draw() {
        Graphics graphics = canvas.getGraphics();
        if(graphics == null) {
            System.out.println("Unable to obtain graphics instance");
            return;
        }

        Image dbImage = canvas.createImage(1000, 1000); // Create a 1000 wide and 1000 tall image to draw on
        Graphics2D g = (Graphics2D) dbImage.getGraphics(); // The graphics for the dbImage
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setStroke(new BasicStroke(3));

        // Draw here
        g.setColor(Palette.Primary.getColor());
        g.fillRect(0, 0, 1000, 1000);

        drawRouteBar(g);
        if (showChargers) drawChargingStations(g);
        if (showCities) drawCities(g);
        drawCars(g, simulation.getCars());
        drawInfo(g, simulation);
        drawColorCodingInfo(g);

        ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.drawImage(dbImage, 0, 0, canvas.getSize().width, canvas.getSize().height, canvas);

    }

    public void drawRouteBar(Graphics2D g) {

        g.setColor(Palette.PrimaryLight.getColor());
        g.fillRect(BAR_PADDING, BAR_PADDING, BAR_WIDTH, BAR_HEIGHT);
    }

    public void drawCities(Graphics2D g) {

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
                routes.routes.get(routes.routeKeys.get(0)).getStartPoint().toString(),
                NAME_X_COORDINATE,
                1000 - BAR_PADDING - (int) (subRouteLength / LENGTH_OF_ROUTES * BAR_HEIGHT) + END_POINT_DIAMETER / 2 + 3
        );
        for (String routeKey : routes.routeKeys) {
            Route route = routes.routes.get(routeKey);
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
                    1000 - BAR_PADDING - (int) (subRouteLength / LENGTH_OF_ROUTES * BAR_HEIGHT) + END_POINT_DIAMETER / 2 + 3
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
                    startDistance += routes.routes.get(routes.routeKeys.get(i)).getLength();
                }
                double distance = car.getDrivenDistance() + startDistance;
                carYCoordinate = 1000 - BAR_PADDING - (int) (distance / LENGTH_OF_ROUTES * BAR_HEIGHT) - CAR_DIAMETER / 2;
            }
            else {
                double startDistance = 0;
                for (int i = route.getStartPoint().index; i < routes.routeKeys.size(); i++) {
                    startDistance += routes.routes.get(routes.routeKeys.get(i)).getLength();
                }
                double distance = car.getDrivenDistance() + startDistance;
                carYCoordinate = BAR_PADDING + (int) (distance / LENGTH_OF_ROUTES * BAR_HEIGHT) - CAR_DIAMETER / 2;
            }

            // Calculate the x coordinate of the car
            int carXCoordinate = BAR_PADDING + BAR_WIDTH / 2 - CAR_DIAMETER / 2;

            // Calculate the color of the car
            switch (carColorCodeIndex) {
                case 0:
                    // By battery
                    if (car.getState() == Car.State.BatteryDepleted) {
                        g.setColor(new Color(0, 0, 0, CAR_OPACITY));
                    } else if (car.getState() == Car.State.DestinationReached) {
                        g.setColor(new Color(255, 255, 255, CAR_OPACITY));
                    } else {
                        double batteryPercentage = car.getBattery() / car.getCarType().getCapacity();
                        int red = batteryPercentage > 0.5 ? 255 - (int) (car.getBattery() / car.getCarType().getCapacity() / 2 * 255) : 255;
                        int green = batteryPercentage > 0.5 ? 255 : (int) (car.getBattery() / car.getCarType().getCapacity() * 2 * 255);
                        g.setColor(new Color(red, green, 0, CAR_OPACITY));
                    }
                    break;
                case 1:
                    // By state
                    switch (car.getState()) {
                        case BatteryDepleted:
                            g.setColor(new Color(0, 0, 0, CAR_OPACITY));
                            break;
                        case DestinationReached:
                            g.setColor(new Color(255, 255, 255, CAR_OPACITY));
                            break;
                        case OnHighway:
                            g.setColor(new Color(0, 255, 0, CAR_OPACITY));
                            break;
                        case Waiting:
                            g.setColor(new Color(255, 0, 0, CAR_OPACITY));
                            break;
                        case Charging:
                            g.setColor(new Color(255, 255, 0, CAR_OPACITY));
                            break;
                        default:
                            g.setColor(new Color(0, 0, 255, CAR_OPACITY));
                            break;
                    }
                    break;
                default:
                    g.setColor(Color.BLACK);
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

        // CARS
        ArrayList<Car> cars = (ArrayList<Car>) simulation.getCars().clone();
        int[] stateCount = {0, 0, 0, 0, 0, 0, 0, 0, 0};
        for (Car car : cars) {
            stateCount[car.getState().index]++;
        }

        final int textSpacing = (int) (TEXT_PADDING * 1.1);

        g.setColor(Palette.Secondary.getColor());
        g.setFont(new Font("arial", Font.PLAIN, 24));

        long time = simulation.getPassedSeconds();
        int hours = (int) (time/3600);
        time -= hours*3600L;
        int minutes = (int) (time/60);
        time -= minutes*60L;
        int seconds = (int) time;

        g.drawString("Valtatiellä: "+stateCount[Car.State.OnHighway.index], 1000 - INFO_AREA_WIDTH + TEXT_PADDING, TEXT_PADDING_TOP + 3 * textSpacing);
        g.drawString("Matkalla valtatielle: "+stateCount[Car.State.OnWayToHighway.index], 1000 - INFO_AREA_WIDTH + TEXT_PADDING, TEXT_PADDING_TOP + 4 * textSpacing);
        g.drawString("Matkalla valtatieltä: "+stateCount[Car.State.OnWayFromHighway.index], 1000 - INFO_AREA_WIDTH + TEXT_PADDING, TEXT_PADDING_TOP + 5 * textSpacing);
        g.drawString("Matkalla laturille: "+stateCount[Car.State.OnWayToCharger.index], 1000 - INFO_AREA_WIDTH + TEXT_PADDING, TEXT_PADDING_TOP + 6 * textSpacing);
        g.drawString("Matkalla laturilta: "+stateCount[Car.State.OnWayFromCharger.index], 1000 - INFO_AREA_WIDTH + TEXT_PADDING, TEXT_PADDING_TOP + 7 * textSpacing);
        g.drawString("Latautumassa: "+stateCount[Car.State.Charging.index], 1000 - INFO_AREA_WIDTH + TEXT_PADDING, TEXT_PADDING_TOP + 8 * textSpacing);
        g.drawString("Odottamassa: "+stateCount[Car.State.Waiting.index], 1000 - INFO_AREA_WIDTH + TEXT_PADDING, TEXT_PADDING_TOP + 9 * textSpacing);
        g.drawString("Akku loppunut: "+stateCount[Car.State.BatteryDepleted.index], 1000 - INFO_AREA_WIDTH + TEXT_PADDING, TEXT_PADDING_TOP + 10 * textSpacing);
        g.drawString("Perillä: "+stateCount[Car.State.DestinationReached.index], 1000 - INFO_AREA_WIDTH + TEXT_PADDING, TEXT_PADDING_TOP + 11 * textSpacing);
        g.drawString("Yhteensä: "+cars.size(), 1000 - INFO_AREA_WIDTH + TEXT_PADDING, TEXT_PADDING_TOP + 12 * textSpacing);


        // ROADS
        int[] chargersInUse = {0, 0, 0, 0, 0, 0};
        for (int i = 0; i < routes.routeKeys.size(); i++) {
            String key = routes.routeKeys.get(i);
            for (ChargingStation station : routes.routes.get(key).getChargingStations()) {
                for (ChargingStation.Charger charger : station.getChargers()) {
                    if (charger.isInUse()) {
                        chargersInUse[i]++;
                    }
                }
            }
        }
        int[] carsOnRoad = {0, 0, 0, 0, 0, 0};
        for (Car car : cars) {
            if (car.getState() == Car.State.DestinationReached)
                continue;
            ArrayList<Route> rootRoutes = car.getRoute().getRootRoutes();
            double drivenDistance = car.getDrivenDistance();
            for (Route rootRoute : rootRoutes) {
                drivenDistance -= rootRoute.getLength();
                if (drivenDistance < 0) {
                    int index = Math.min(rootRoute.getStartPoint().index, rootRoute.getEndPoint().index);
                    carsOnRoad[index]++;
                    break;
                }
            }
        }
        for (int i = 0; i < routes.routeKeys.size(); i++) {
            String key = routes.routeKeys.get(i);
            Route route = routes.routes.get(key);
            String routeName = route.getStartPoint().toString() + " - " + route.getEndPoint().toString();
            int xCoordinate = 1000 - INFO_AREA_WIDTH + TEXT_PADDING + i / 3 * 350;
            g.setFont(new Font("arial", Font.BOLD, 24));
            g.drawString(routeName, xCoordinate, 690 + (3 * (i % 3)) * textSpacing);
            g.setFont(new Font("arial", Font.PLAIN, 24));
            g.drawString(" - Autoa: "+carsOnRoad[i], xCoordinate, 690 + (1 + 3 * (i % 3)) * textSpacing);
            g.drawString(" - Laturia käytössä: "+chargersInUse[i], xCoordinate, 690 + (2 + 3 * (i % 3)) * textSpacing);
        }

        g.setFont(new Font("arial", Font.BOLD, 24));

        g.drawString(String.format("Aika: %d h, %d min, %d sec", hours, minutes, seconds), 1000 - INFO_AREA_WIDTH + TEXT_PADDING, TEXT_PADDING_TOP);

        g.drawString("AUTOT:", 1000 - INFO_AREA_WIDTH + TEXT_PADDING, TEXT_PADDING_TOP + 2 * textSpacing);
    }

    public void drawColorCodingInfo(Graphics2D g) {

        final int COLOR_TOP = 390;
        final int COLOR_LEFT = 750;
        final int COLOR_SPACING = 75;
        final int COLOR_SIZE = 30;

        switch (carColorCodeIndex) {
            case 0:
                g.setColor(new Color(255, 0, 0));
                g.fillRect(COLOR_LEFT, COLOR_TOP, COLOR_SPACING, COLOR_SIZE);
                g.setColor(new Color(255, 255, 0));
                g.fillRect(COLOR_LEFT + COLOR_SPACING, COLOR_TOP, COLOR_SPACING, COLOR_SIZE);
                g.setColor(new Color(0, 255, 0));
                g.fillRect(COLOR_LEFT + 2 * COLOR_SPACING, COLOR_TOP, COLOR_SPACING, COLOR_SIZE);
                g.setColor(Color.BLACK);
                g.drawRect(COLOR_LEFT, COLOR_TOP, COLOR_SPACING*3, COLOR_SIZE);

                g.setColor(new Color(255, 255, 255));
                g.fillRect(COLOR_LEFT, COLOR_TOP + 2 * COLOR_SPACING / 2 + COLOR_SIZE /  2, COLOR_SIZE, COLOR_SIZE);
                g.setColor(new Color(0, 0, 0));
                g.fillRect(COLOR_LEFT, COLOR_TOP + 3 * COLOR_SPACING / 2 + COLOR_SIZE /  2, COLOR_SIZE, COLOR_SIZE);

                g.drawRect(COLOR_LEFT, COLOR_TOP + 2 * COLOR_SPACING / 2 + COLOR_SIZE /  2, COLOR_SIZE, COLOR_SIZE);
                g.drawRect(COLOR_LEFT, COLOR_TOP + 3 * COLOR_SPACING / 2 + COLOR_SIZE /  2, COLOR_SIZE, COLOR_SIZE);

                g.setColor(Palette.Secondary.getColor());
                g.setFont(new Font("arial", Font.PLAIN, 20));

                g.drawString("0%--------50%-----100%", COLOR_LEFT, COLOR_TOP + COLOR_SIZE * 2);
                g.drawString("Perillä", COLOR_LEFT + 3 * COLOR_SIZE / 2, COLOR_TOP + 24 + 2 * COLOR_SPACING / 2 + COLOR_SIZE /  2);
                g.drawString("Akku loppunut", COLOR_LEFT + 3 * COLOR_SIZE / 2, COLOR_TOP + 24 + 3 * COLOR_SPACING / 2 + COLOR_SIZE /  2);
                break;
            case 1:
                g.setColor(new Color(0, 255, 0));
                g.fillRect(COLOR_LEFT, COLOR_TOP, COLOR_SIZE, COLOR_SIZE);
                g.setColor(new Color(255, 255, 0));
                g.fillRect(COLOR_LEFT, COLOR_TOP + COLOR_SPACING / 2, COLOR_SIZE, COLOR_SIZE);
                g.setColor(new Color(0, 0, 255));
                g.fillRect(COLOR_LEFT, COLOR_TOP + 2 * COLOR_SPACING / 2, COLOR_SIZE, COLOR_SIZE);
                g.setColor(new Color(255, 0, 0));
                g.fillRect(COLOR_LEFT, COLOR_TOP + 3 * COLOR_SPACING / 2, COLOR_SIZE, COLOR_SIZE);
                g.setColor(new Color(255, 255, 255));
                g.fillRect(COLOR_LEFT, COLOR_TOP + 4 * COLOR_SPACING / 2, COLOR_SIZE, COLOR_SIZE);
                g.setColor(new Color(0, 0, 0));
                g.fillRect(COLOR_LEFT, COLOR_TOP + 5 * COLOR_SPACING / 2, COLOR_SIZE, COLOR_SIZE);
                for (int i = 0; i < 6; i++) {
                    g.setColor(Color.BLACK);
                    g.drawRect(COLOR_LEFT, COLOR_TOP + i * COLOR_SPACING / 2, COLOR_SIZE, COLOR_SIZE);
                }

                g.setColor(Palette.Secondary.getColor());
                g.setFont(new Font("arial", Font.PLAIN, 20));

                g.drawString("Valtatiellä", COLOR_LEFT + 10 + COLOR_SIZE, COLOR_TOP + 24);
                g.drawString("Latautumassa", COLOR_LEFT + 10 + COLOR_SIZE, COLOR_TOP + 24 + COLOR_SPACING / 2);
                g.drawString("Matkalla laturille", COLOR_LEFT + 10 + COLOR_SIZE, COLOR_TOP + 24 + 2 * COLOR_SPACING / 2);
                g.drawString("Odottamassa", COLOR_LEFT + 10 + COLOR_SIZE, COLOR_TOP + 24 + 3 * COLOR_SPACING / 2);
                g.drawString("Perillä", COLOR_LEFT + 10 + COLOR_SIZE, COLOR_TOP + 24 + 4 * COLOR_SPACING / 2);
                g.drawString("Akku loppunut", COLOR_LEFT + 10 + COLOR_SIZE, COLOR_TOP + 24 + 5 * COLOR_SPACING / 2);

                break;
        }

    }
}
