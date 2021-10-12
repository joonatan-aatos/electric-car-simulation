package simulation;

import java.util.ArrayList;
import java.util.Arrays;

public enum EndPoint {
    Helsinki(new ArrayList<String>(Arrays.asList("HeLa"))),
    Lahti(new ArrayList<String>(Arrays.asList("HeLa", "LaJy"))),
    Jyvaskyla(new ArrayList<String>(Arrays.asList("LaJy", "JyOu"))),
    Oulu(new ArrayList<String>(Arrays.asList("JyOu", "OuKe"))),
    Kemi(new ArrayList<String>(Arrays.asList("OuKe","KeRo"))),
    Rovaniemi(new ArrayList<String>(Arrays.asList("KeRo", "RoUt"))),
    Utsjoki(new ArrayList<String>(Arrays.asList("RoUt")));

    private final ArrayList<Route> connectedRoutes;
    private final ArrayList<String> connectedRouteIDs;

    public ArrayList<Route> getConnectedRoutes() {
        if (connectedRoutes.size() == 0) {
            for (String id : connectedRouteIDs) {
                connectedRoutes.add(Routes.routes.get(id));
            }
        }
        return connectedRoutes;
    }

    EndPoint(ArrayList<String> routeIndexes) {
        connectedRouteIDs = routeIndexes;
        connectedRoutes = new ArrayList<>();
    }
}
