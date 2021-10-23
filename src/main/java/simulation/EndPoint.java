package simulation;

import java.util.ArrayList;
import java.util.Arrays;

public enum EndPoint {
    Helsinki(new ArrayList<String>(Arrays.asList("HeLa")), 0),
    Lahti(new ArrayList<String>(Arrays.asList("HeLa", "LaJy")), 1),
    Jyvaskyla(new ArrayList<String>(Arrays.asList("LaJy", "JyOu")), 2),
    Oulu(new ArrayList<String>(Arrays.asList("JyOu", "OuKe")), 3),
    Kemi(new ArrayList<String>(Arrays.asList("OuKe","KeRo")), 4),
    Rovaniemi(new ArrayList<String>(Arrays.asList("KeRo", "RoUt")), 5),
    Utsjoki(new ArrayList<String>(Arrays.asList("RoUt")), 6);

    private final ArrayList<Route> connectedRoutes;
    private final ArrayList<String> connectedRouteIDs;
    public final int index;

    public ArrayList<Route> getConnectedRoutes() {
        if (connectedRoutes.size() == 0) {
            for (String id : connectedRouteIDs) {
                connectedRoutes.add(Routes.routes.get(id));
            }
        }
        return connectedRoutes;
    }

    EndPoint(ArrayList<String> routeIndexes, int index_) {
        connectedRouteIDs = routeIndexes;
        index = index_;
        connectedRoutes = new ArrayList<>();
    }
}
