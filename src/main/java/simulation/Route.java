package simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Route {
    private final double length;
    private final ArrayList<ChargingStation> chargingStations;
    private final ArrayList<Double> chargingStationDistances;

    private final EndPoint startPoint;
    private final EndPoint endPoint;

    private static final Random random = new Random();

    public Route(double length_, ArrayList<ChargingStation> chargingStations_, EndPoint startPoint_, EndPoint endPoint_) {
        length = length_;
        startPoint = startPoint_;
        endPoint = endPoint_;
        chargingStations = chargingStations_;

        chargingStationDistances = new ArrayList<>();
        for (ChargingStation station : chargingStations) {
            chargingStationDistances.add(station.getDistance());
        }
    }

    public Route(ArrayList<Route> rootRoutes, EndPoint startPoint_) {
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
            previousEndPoint = route.endPoint;
        }

        length = sumOfLengths;
        endPoint = previousEndPoint;
    }

    public static Route generateRandomRoute() {
        ArrayList<EndPoint> endPoints = new ArrayList<>(List.of(EndPoint.values()));
        EndPoint startPoint = endPoints.get(random.nextInt(endPoints.size()));
        endPoints.remove(startPoint);
        EndPoint endPoint = endPoints.get(random.nextInt(endPoints.size()));
        return generateShortestRoute(startPoint, endPoint);
    }

    public static Route generateShortestRoute(EndPoint startPoint, EndPoint endPoint) {
        // TODO: Implement this
        return null;
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

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (ChargingStation chargingStation : chargingStations) {
            s.append(chargingStation.toString());
            s.append("\n");
        }
        return s.toString();
    }
}
