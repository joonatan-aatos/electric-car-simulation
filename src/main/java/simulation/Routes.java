package simulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Routes {
    public static HashMap<String, Route> routes = new HashMap<>();

    public static void generateRoutes() {
        ArrayList<ChargingStation> chargingStationsHeLa = new ArrayList<>();
        chargingStationsHeLa.add(new ChargingStation(4, new ArrayList<Integer>(Arrays.asList(20, 30, 40))));
        routes.put(
                "HeLa",
                new Route(
                    10,
                    new ArrayList<ChargingStation>(chargingStationsHeLa),
                    EndPoint.Helsinki,
                    EndPoint.Lahti
                )
        );
        routes.put(
                "LaJy",
                null
        );
        routes.put(
                "JyOu",
                null
        );
        routes.put(
                "OuRo",
                null
        );
    }
}
