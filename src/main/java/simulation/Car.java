package simulation;

import java.util.ArrayList;
import java.util.Collections;

public class Car {
    private boolean eCar;
    private Route route;
    private int batteryCapacity;
    private int batteryLife;

    public Car(boolean eCar_, EndPoint start, int batteryCapacity_) {
        eCar = eCar_;
        batteryCapacity = batteryCapacity_;
        batteryLife = batteryCapacity_;
        generateRandomRoute(start);
    }

    private void generateRandomRoute(EndPoint currentEndPoint) {
        double length = 0;
        ArrayList<ChargingStation> chargingStations = new ArrayList<>();
        EndPoint startPoint = currentEndPoint;
        ArrayList<Route> blacklist = new ArrayList<>();
        while (true) {
            ArrayList<Route> endPointRoutes = new ArrayList<>(currentEndPoint.getConnectedRoutes());
            Collections.shuffle(endPointRoutes);
            Route r = null;
            for (Route route : endPointRoutes) {
                if (!blacklist.contains(route)) r = route;
                blacklist.add(route);
            }
            if (r == null) break;

            length += r.getLength();

            // TODO: Give route its charging stations

            if (Math.random()-1/4f < 0) {
                break;
            }
        }
        route = new Route(length, chargingStations, startPoint, currentEndPoint);
    }

}
