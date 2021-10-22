package simulation;

import java.util.HashMap;

public class Routes {
    public static HashMap<String, Route> routes = new HashMap<>();

    public static void generateRoutes() {
        System.out.println("Generating routes...");
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
    }
}
