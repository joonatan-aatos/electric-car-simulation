package simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Route {
    private final double length;
    private final ArrayList<ChargingStation> chargingStations;
    private final ArrayList<Double> chargingStationDistances;
    private final String name;

    private final EndPoint startPoint;
    private final EndPoint endPoint;

    private static final Random random = new Random();

    public Route(String name_, double length_, ArrayList<ChargingStation> chargingStations_, EndPoint startPoint_, EndPoint endPoint_) {
        name = name_;
        length = length_;
        startPoint = startPoint_;
        endPoint = endPoint_;
        chargingStations = chargingStations_;

        chargingStationDistances = new ArrayList<>();
        for (ChargingStation station : chargingStations) {
            chargingStationDistances.add(station.getDistance());
        }
    }

    /**
     * Merge routes together
     *
     * @param rootRoutes Routes that will be merged together
     * @param startPoint_ Starting point of the route
     */
    public Route(ArrayList<Route> rootRoutes, EndPoint startPoint_) {
        if (rootRoutes.size() < 1)
            throw new IllegalArgumentException("No root routes were provided");
        startPoint = startPoint_;
        chargingStations = new ArrayList<>();
        chargingStationDistances = new ArrayList<>();

        double sumOfLengths = 0;
        EndPoint previousEndPoint = startPoint;

        for (Route route : rootRoutes) {
            if (route.startPoint != previousEndPoint && route.endPoint != previousEndPoint)
                throw new IllegalArgumentException("Invalid route");

            boolean shouldBeFlipped = route.startPoint != previousEndPoint;

            for (int i = 0; i < route.chargingStations.size(); i++) {
                int index = shouldBeFlipped ? route.chargingStations.size() - 1 - i : i;
                chargingStations.add(route.chargingStations.get(index));
                chargingStationDistances.add(
                        shouldBeFlipped ?
                                route.length - route.chargingStationDistances.get(index) + sumOfLengths :
                                route.chargingStationDistances.get(index) + sumOfLengths
                );
            }

            sumOfLengths += route.length;
            previousEndPoint = shouldBeFlipped ? route.startPoint : route.endPoint;
        }

        length = sumOfLengths;
        endPoint = previousEndPoint;
        name = startPoint.toString() + " - " + endPoint.toString();
    }

    public static Route generateRandomRoute() {
        ArrayList<EndPoint> endPoints = new ArrayList<>(List.of(EndPoint.values()));
        EndPoint startPoint = endPoints.get(random.nextInt(endPoints.size()));
        endPoints.remove(startPoint);
        EndPoint endPoint = endPoints.get(random.nextInt(endPoints.size()));
        return generateShortestRoute(startPoint, endPoint);
    }

    /**
     *
     * @param startingPoint
     * @param endingPoint
     * @return Returns the shortest route between the given points
     * @author Joonatan Aatos Korpela
     */
    public static Route generateShortestRoute(EndPoint startingPoint, EndPoint endingPoint) {
        if (startingPoint == endingPoint)
            throw new IllegalArgumentException("Can't generate the shortest route between given points");

        ArrayList<ArrayList<EndPoint>> listOfPreviousPoints = new ArrayList<>();

        ArrayList<Route> connectedRoutesToStartPoint = startingPoint.getConnectedRoutes();
        for (Route route : connectedRoutesToStartPoint) {
            if (route.startPoint == endingPoint || route.endPoint == endingPoint) {
                // Route has been found
                // Find the corresponding route from Routes.routes
                for (Route rootRoute : Routes.routes.values()) {
                    if (rootRoute.startPoint == startingPoint || rootRoute.endPoint == startingPoint) {
                        if (rootRoute.getOppositeEndPoint(startingPoint) == endingPoint) {
                            return rootRoute;
                        }
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
                ArrayList<Route> connectedRoutes = previousEndPoint.getConnectedRoutes();
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
                for (Route route : Routes.routes.values()) {
                    if (route.startPoint == possibleRoute.get(i-1) || route.endPoint == possibleRoute.get(i-1)) {
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
                shortestRoute = new Route(rootRoutes, startingPoint);
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
    public static Route generateShortestRoute2(EndPoint startingPoint, EndPoint desiredEndPoint) {
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

            for (Route route : currentPoint.getConnectedRoutes()) {
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

            for (Route connectedRoute : currentPoint.getConnectedRoutes()) {
                if (connectedRoute.getOppositeEndPoint(currentPoint).equals(endPointsOnRoute.get(1))) {
                    rootRoutes.add(connectedRoute);
                    endPointsOnRoute.remove(0);
                    currentPoint = connectedRoute.getOppositeEndPoint(currentPoint);
                    break;
                }
            }
        }

        return new Route(rootRoutes, startingPoint);
    }

    public EndPoint getOppositeEndPoint(EndPoint oppositeEndPoint) {
        if (oppositeEndPoint != startPoint && oppositeEndPoint != endPoint)
            throw new IllegalArgumentException("The given endpoint is not an endpoint of this route");

        return oppositeEndPoint == startPoint ? endPoint : startPoint;
    }

    public double getLength() {
        return length;
    }

    public ArrayList<ChargingStation> getChargingStations() {
        return chargingStations;
    }

    public EndPoint getStartPoint() {
        return startPoint;
    }

    public EndPoint getEndPoint() {
        return endPoint;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(name + "\n");
        for (ChargingStation chargingStation : chargingStations) {
            s.append(chargingStation.toString());
            s.append("\n");
        }
        return s.toString();
    }
}
