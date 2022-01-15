package simulation;

import java.util.*;
import java.util.stream.Collectors;

public class Route {
    private final double length;
    private final ArrayList<ChargingStation> chargingStations;
    private final ArrayList<Double> chargingStationDistances;
    private final String name;

    private final EndPoint startPoint;
    private final EndPoint endPoint;
    private final Routes routes;

    /**
     * Is null if this is a root route.
     * Otherwise, contains all root routes this route consists of
     */
    private final ArrayList<Route> rootRoutes;

    public Route(Routes routes_, String name_, double length_, ArrayList<ChargingStation> chargingStations_, EndPoint startPoint_, EndPoint endPoint_) {
        routes = routes_;
        rootRoutes = null;
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

    public Route(Routes routes_, String name_, double length_, ArrayList<ChargingStation> chargingStations_, EndPoint startPoint_, EndPoint endPoint_, ArrayList<Route> rootRoutes_) {
        routes = routes_;
        rootRoutes = rootRoutes_;
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
     * @param rootRoutes_ routes that will be merged together
     * @param startPoint_ Starting point of the route
     */
    public Route(Routes routes_, ArrayList<Route> rootRoutes_, EndPoint startPoint_) {
        routes = routes_;
        if (rootRoutes_.size() < 1)
            throw new IllegalArgumentException("No root routes were provided");
        rootRoutes = rootRoutes_;
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

    public Route getFlippedRoute(Routes routes) {

        Route route = this;
        ArrayList<ChargingStation> newChargingStations = route.chargingStations.stream()
            .map(ChargingStation::createNew)
            .collect(Collectors.toCollection(ArrayList::new));

        ArrayList<Route> newRootRoutes = route.getRootRoutes();
        Collections.reverse(newChargingStations);
        if (newRootRoutes != null)
            Collections.reverse(newRootRoutes);
        Route newRoute = new Route(
                routes,
                route.getEndPoint().toString() + " - " + route.getStartPoint().toString(),
                route.length,
                newChargingStations,
                route.endPoint,
                route.startPoint,
                newRootRoutes
        );
        ArrayList<Double> newChargingStationDistances = new ArrayList<>();
        for (int i = 0; i < route.chargingStationDistances.size(); i++) {
            newChargingStationDistances.add(route.length - route.chargingStationDistances.get(i));
        }
        Collections.reverse(newChargingStationDistances);
        newRoute.chargingStationDistances.clear();
        newRoute.chargingStationDistances.addAll(newChargingStationDistances);
        return newRoute;
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

    public ArrayList<Double> getChargingStationDistances() {
        return chargingStationDistances;
    }

    public ArrayList<Route> getRootRoutes() {
        return rootRoutes;
    }

    public Routes getRoutes() {
        return routes;
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
