package simulation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Routes {
    private static final Logger logger = Logger.getGlobal();

    public static HashMap<String, Route> routes = new HashMap<>();
    public static HashMap<String, Double> trafficData = new HashMap<>();
    public static ArrayList<String> routeKeys = new ArrayList<>();
    public static ArrayList<Double> endPointWeights = new ArrayList<>();

    public static void generateRoutes() {
        logger.info("Generating routes...");
        RoadData heLa = RoadData.readChargingStations("/He-La-latauspisteet.csv");
        routes.put(
                "HeLa",
                new Route(
                        "HeLa",
                        heLa.getLength(),
                        heLa.getChargingStations(),
                        EndPoint.Helsinki,
                        EndPoint.Lahti
                )
        );
        RoadData laJy = RoadData.readChargingStations("/La-Jy-latauspisteet.csv");
        routes.put(
                "LaJy",
                new Route(
                        "LaJy",
                        laJy.getLength(),
                        laJy.getChargingStations(),
                        EndPoint.Lahti,
                        EndPoint.Jyvaskyla
                )
        );
        RoadData jyOu = RoadData.readChargingStations("/Jy-Ou-latauspisteet.csv");
        routes.put(
                "JyOu",
                new Route(
                        "JyOu",
                        jyOu.getLength(),
                        jyOu.getChargingStations(),
                        EndPoint.Jyvaskyla,
                        EndPoint.Oulu
                )
        );
        RoadData ouKe = RoadData.readChargingStations("/Ou-Ke-latauspisteet.csv");
        routes.put(
                "OuKe",
                new Route(
                        "OuKe",
                        ouKe.getLength(),
                        ouKe.getChargingStations(),
                        EndPoint.Oulu,
                        EndPoint.Kemi
                )
        );
        RoadData keRo = RoadData.readChargingStations("/Ke-Ro-latauspisteet.csv");
        routes.put(
                "KeRo",
                new Route(
                        "KeRo",
                        keRo.getLength(),
                        keRo.getChargingStations(),
                        EndPoint.Kemi,
                        EndPoint.Rovaniemi
                )
        );
        RoadData roUt = RoadData.readChargingStations("/Ro-Ut-latauspisteet.csv");
        routes.put(
                "RoUt",
                new Route(
                        "RoUt",
                        roUt.getLength(),
                        roUt.getChargingStations(),
                        EndPoint.Rovaniemi,
                        EndPoint.Utsjoki
                )
        );

        routeKeys.add("HeLa");
        routeKeys.add("LaJy");
        routeKeys.add("JyOu");
        routeKeys.add("OuKe");
        routeKeys.add("KeRo");
        routeKeys.add("RoUt");

        trafficData = readTrafficData("/Liikennemaarat.csv");
        endPointWeights = calculateEndpointWeights();
    }

    private static HashMap<String, Double> readTrafficData(String path) {

        HashMap<String, Double> trafficData = new HashMap<>();

        try {
            InputStream in = RoadData.class.getResourceAsStream(path);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            br.readLine();

            String[] data = null;
            String line;
            while ((line = br.readLine()) != null) {
                if (line.length() <= 0) break;
                data = line.split(";");
                String routeId = data[0];
                double routeTraffic = Double.parseDouble(data[2]);
                trafficData.put(routeId, routeTraffic);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException | NullPointerException | NumberFormatException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Charger file is in wrong format");
        }

        return trafficData;
    }

    private static ArrayList<Double> calculateEndpointWeights() {

        if (trafficData.size() <= 0)
            return new ArrayList<>();

        ArrayList<Double> weights = new ArrayList<>();
        double sum = 0;
        weights.add(trafficData.get(routeKeys.get(0)));
        sum += weights.get(0);

        for (int i = 1; i < trafficData.size(); i++) {
            weights.add(trafficData.get(routeKeys.get(i)) + trafficData.get(routeKeys.get(i-1)));
            sum += weights.get(i);
        }

        weights.add(trafficData.get(routeKeys.get(routeKeys.size()-1)));
        sum += weights.get(weights.size()-1);

        final double finalSum = sum;
        weights = weights.stream().map(weight -> weight / finalSum).collect(Collectors.toCollection(ArrayList::new));

        return weights;
    }
}
