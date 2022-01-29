package simulation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Routes {
    private final Logger logger = Logger.getGlobal();

    public HashMap<String, Route> routes = new HashMap<>();
    public HashMap<String, Double> trafficData = new HashMap<>();
    public ArrayList<String> routeKeys = new ArrayList<>();
    public ArrayList<Double> endPointWeights = new ArrayList<>();
    public final Random random;
    public final long seed;
    public final double chargerAmountCoefficient;

    public Routes(long seed_, double chargerAmountCoefficient_) {
        seed = seed_;
        random = new Random(seed);
        chargerAmountCoefficient = chargerAmountCoefficient_;
    }

    private HashMap<String, Double> readTrafficData(String path) {

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

    public void generateRoutes() {
        logger.config("Generating routes...");
        RoadData heLa = RoadData.readChargingStations("/He-La-latauspisteet.csv", chargerAmountCoefficient);
        routes.put(
                "HeLa",
                new Route(
                        this,
                        "HeLa",
                        heLa.getLength(),
                        heLa.getChargingStations(),
                        EndPoint.Helsinki,
                        EndPoint.Lahti
                )
        );
        RoadData laJy = RoadData.readChargingStations("/La-Jy-latauspisteet.csv", chargerAmountCoefficient);
        routes.put(
                "LaJy",
                new Route(
                        this,
                        "LaJy",
                        laJy.getLength(),
                        laJy.getChargingStations(),
                        EndPoint.Lahti,
                        EndPoint.Jyvaskyla
                )
        );
        RoadData jyOu = RoadData.readChargingStations("/Jy-Ou-latauspisteet.csv", chargerAmountCoefficient);
        routes.put(
                "JyOu",
                new Route(
                        this,
                        "JyOu",
                        jyOu.getLength(),
                        jyOu.getChargingStations(),
                        EndPoint.Jyvaskyla,
                        EndPoint.Oulu
                )
        );
        RoadData ouKe = RoadData.readChargingStations("/Ou-Ke-latauspisteet.csv", chargerAmountCoefficient);
        routes.put(
                "OuKe",
                new Route(
                        this,
                        "OuKe",
                        ouKe.getLength(),
                        ouKe.getChargingStations(),
                        EndPoint.Oulu,
                        EndPoint.Kemi
                )
        );
        RoadData keRo = RoadData.readChargingStations("/Ke-Ro-latauspisteet.csv", chargerAmountCoefficient);
        routes.put(
                "KeRo",
                new Route(
                        this,
                        "KeRo",
                        keRo.getLength(),
                        keRo.getChargingStations(),
                        EndPoint.Kemi,
                        EndPoint.Rovaniemi
                )
        );
        RoadData roUt = RoadData.readChargingStations("/Ro-Ut-latauspisteet.csv", chargerAmountCoefficient);
        routes.put(
                "RoUt",
                new Route(
                        this,
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

    private ArrayList<Double> calculateEndpointWeights() {

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

    public Route generateRandomRoute() {
        ArrayList<EndPoint> endPoints = new ArrayList<>(List.of(EndPoint.values()));
        // Select random start point with weights
        int startPointIndex = 0;
        for (double r = random.nextDouble(); startPointIndex < endPointWeights.size() - 1; ++startPointIndex) {
            r -= endPointWeights.get(startPointIndex);
            if (r <= 0.0) break;
        }
        EndPoint startPoint = endPoints.get(startPointIndex);

        // Calculate weights for end points
        ArrayList<Double> weights = new ArrayList<>();
        double sumOfWeights = 0;
        boolean before = true;
        for (int i = 0; i < endPoints.size(); i++) {
            if (i == startPointIndex) {
                weights.add(0d);
                before = false;
            }
            else {
                double weight;
                if (before) {
                    weight = trafficData.get(routeKeys.get(i));
                    for (int j = i+1; j < startPointIndex; j++) {
                        weight *= trafficData.get(routeKeys.get(j));
                    }
                }
                else {
                    weight = trafficData.get(routeKeys.get(i-1));
                    for (int j = startPointIndex + 1; j < i; j++) {
                        weight *= trafficData.get(routeKeys.get(j-1));
                    }
                }
                weights.add(weight);
                sumOfWeights += weight;
            }
        }
        final double finalSumOfWeights = sumOfWeights;
        weights = weights.stream().map(weight -> weight / finalSumOfWeights).collect(Collectors.toCollection(ArrayList::new));

        // Select random end point with weights
        int endPointIndex = 0;
        for (double r = random.nextDouble(); endPointIndex < weights.size() - 1; ++endPointIndex) {
            r -= weights.get(endPointIndex);
            if (r <= 0.0) break;
        }
        EndPoint endPoint = endPoints.get(endPointIndex);

        // Randomly flip the route
        Route route = generateShortestRoute2(startPoint, endPoint);
        if (random.nextDouble() > 0.5) {
            route = route.getFlippedRoute(this);
        }
        return route;
    }

    /**
     *
     * @param startingPoint
     * @param endingPoint
     * @return Returns the shortest route between the given points
     * @author Joonatan Aatos Korpela
     */
    public Route generateShortestRoute(EndPoint startingPoint, EndPoint endingPoint) {
        if (startingPoint == endingPoint)
            throw new IllegalArgumentException("Can't generate the shortest route between given points");

        ArrayList<ArrayList<EndPoint>> listOfPreviousPoints = new ArrayList<>();

        ArrayList<Route> connectedRoutesToStartPoint = startingPoint.getConnectedRoutes(this);
        for (Route route : connectedRoutesToStartPoint) {
            if (route.getStartPoint() == endingPoint || route.getEndPoint() == endingPoint) {
                // Route has been found
                // Find the corresponding route from routes.routes
                for (Route rootRoute : routes.values()) {
                    if (rootRoute.getStartPoint() == startingPoint && rootRoute.getEndPoint() == endingPoint) {
                        return new Route(
                                this,
                                route.getStartPoint().toString() + " - " + route.getEndPoint().toString(),
                                route.getLength(),
                                route.getChargingStations(),
                                route.getStartPoint(),
                                route.getEndPoint()
                        );
                    }
                    else if (rootRoute.getStartPoint() == endingPoint && rootRoute.getEndPoint() == startingPoint) {
                        return route.getFlippedRoute(this);
                    }
                }
                throw new RuntimeException("Generate Shortest Route algorithm failed");
            }
            else {
                // Add new endPoints to list of previous end points
                ArrayList<EndPoint> previousPoints = new ArrayList<>();
                previousPoints.add(startingPoint);
                previousPoints.add(route.getOppositeEndPoint(startingPoint));
                listOfPreviousPoints.add(previousPoints);
            }
        }

        ArrayList<ArrayList<EndPoint>> possibleRoutes = new ArrayList<>();

        while (possibleRoutes.size() == 0) {
            ArrayList<ArrayList<EndPoint>> newListOfPreviousPoints = new ArrayList<>();
            for (ArrayList<EndPoint> previousPoints : listOfPreviousPoints) {
                EndPoint previousEndPoint = previousPoints.get(previousPoints.size() - 1);
                ArrayList<Route> connectedRoutes = previousEndPoint.getConnectedRoutes(this);
                for (Route route : connectedRoutes) {

                    // Add new endPoints to reachedEndPoints
                    ArrayList<EndPoint> newPreviousPoints = new ArrayList<>(previousPoints);
                    newPreviousPoints.add(route.getOppositeEndPoint(previousEndPoint));
                    newListOfPreviousPoints.add(newPreviousPoints);

                    if (newPreviousPoints.get(newPreviousPoints.size() - 1) == endingPoint) {
                        possibleRoutes.add(newPreviousPoints);
                    }
                }
            }
            listOfPreviousPoints = newListOfPreviousPoints;
        }

        // Find the shortest route
        Route shortestRoute = null;
        double shortestRouteLength = Double.MAX_VALUE;
        for (ArrayList<EndPoint> possibleRoute : possibleRoutes) {
            ArrayList<Route> rootRoutes = new ArrayList<>();
            double routeLength = 0;
            for (int i = 1; i < possibleRoute.size(); i++) {
                for (Route route : routes.values()) {
                    if (route.getStartPoint() == possibleRoute.get(i-1) || route.getEndPoint() == possibleRoute.get(i-1)) {
                        if (route.getOppositeEndPoint(possibleRoute.get(i - 1)) == possibleRoute.get(i)) {
                            rootRoutes.add(route);
                            routeLength += route.getLength();
                            break;
                        }
                    }
                }
            }
            if (routeLength < shortestRouteLength) {
                shortestRouteLength = routeLength;
                shortestRoute = new Route(this, rootRoutes, startingPoint);
            }
        }
        return shortestRoute;
    }

    /**
     *
     * @param startingPoint
     * @param desiredEndPoint
     * @return Returns the shortest route between the given points
     * @author Justus Zendium Christian Hansen
     */
    public Route generateShortestRoute2(EndPoint startingPoint, EndPoint desiredEndPoint) {
        if (startingPoint == desiredEndPoint)
            throw new IllegalArgumentException("Can't generate the shortest route between given points");

        ArrayList<EndPoint> toDoEndPoints = new ArrayList<>();
        ArrayList<ArrayList<EndPoint>> toDoPreviousEndPoints = new ArrayList<>();

        HashMap<EndPoint, Boolean> isEndPointDone = new HashMap<>();
        for (EndPoint e : EndPoint.values()) {
            isEndPointDone.put(e, false);
        }

        int amountDone = 0;
        EndPoint currentPoint = startingPoint;
        ArrayList<EndPoint> currentPreviousEndPoints = new ArrayList<>();
        currentPreviousEndPoints.add(currentPoint);

        whileLoop:
        while (amountDone < EndPoint.values().length) {

            for (Route route : currentPoint.getConnectedRoutes(this)) {
                EndPoint nextEndPoint = route.getOppositeEndPoint(currentPoint);
                if (isEndPointDone.get(nextEndPoint))
                    continue;

                if (nextEndPoint.equals(desiredEndPoint)) {
                    currentPreviousEndPoints.add(nextEndPoint);
                    break whileLoop;
                }

                ArrayList<EndPoint> nextPreviousEndPoints = new ArrayList<>(currentPreviousEndPoints);
                nextPreviousEndPoints.add(nextEndPoint);
                toDoPreviousEndPoints.add(nextPreviousEndPoints);
                toDoEndPoints.add(nextEndPoint);
            }

            isEndPointDone.put(currentPoint, true);
            amountDone++;
            if (toDoEndPoints.size() > 0) {
                currentPreviousEndPoints = toDoPreviousEndPoints.get(0);
                currentPoint = toDoEndPoints.get(0);
                toDoPreviousEndPoints.remove(0);
                toDoEndPoints.remove(0);
            } else {
                throw new RuntimeException("Generate Shortest Route algorithm failed");
            }
        }

        ArrayList<EndPoint> endPointsOnRoute = currentPreviousEndPoints;
        ArrayList<Route> rootRoutes = new ArrayList<>();
        currentPoint = startingPoint;

        while (endPointsOnRoute.size() > 1) {

            for (Route connectedRoute : currentPoint.getConnectedRoutes(this)) {
                if (connectedRoute.getOppositeEndPoint(currentPoint).equals(endPointsOnRoute.get(1))) {
                    rootRoutes.add(connectedRoute);
                    endPointsOnRoute.remove(0);
                    currentPoint = connectedRoute.getOppositeEndPoint(currentPoint);
                    break;
                }
            }
        }

        return new Route(this, rootRoutes, startingPoint);
    }

    public double getChargerAmountCoefficient() {
        return chargerAmountCoefficient;
    }
}
