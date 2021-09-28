package simulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class Routes {
    public static ArrayList<Route> routes = new ArrayList<>();

    public static void generateRoutes() {
        ArrayList<ChargingStation> chargingStationsHeLa = new ArrayList<>();
        chargingStationsHeLa.add(new ChargingStation(4, new ArrayList<Integer>(Arrays.asList(20, 30, 40))));
        routes.add(new Route(
                10,
                new ArrayList<ChargingStation>(chargingStationsHeLa),
                EndPoint.Helsinki,
                EndPoint.Lahti
        ));
    }
}
