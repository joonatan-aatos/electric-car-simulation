package simulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Routes {
    public static HashMap<String, Route> routes = new HashMap<>();

    public static void generateRoutes() {
        System.out.println("Generating routes...");
        RoadData heLa = RoadData.readChargingStations("/He-La-latauspisteet.csv");
        routes.put(
                "HeLa",
                new Route(
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
                        roUt.getLength(),
                        roUt.getChargingStations(),
                        EndPoint.Rovaniemi,
                        EndPoint.Utsjoki
                )
        );

    }
}
