package simulation;

import java.util.ArrayList;
import java.util.Arrays;

public enum EndPoint {
    Helsinki(new ArrayList<String>(Arrays.asList("HeLa")), 0, 30),
    Lahti(new ArrayList<String>(Arrays.asList("HeLa", "LaJy")), 1, 30),
    Jyvaskyla(new ArrayList<String>(Arrays.asList("LaJy", "JyOu")), 2, 30),
    Oulu(new ArrayList<String>(Arrays.asList("JyOu", "OuKe")), 3, 30),
    Kemi(new ArrayList<String>(Arrays.asList("OuKe","KeRo")), 4, 30),
    Rovaniemi(new ArrayList<String>(Arrays.asList("KeRo", "RoUt")), 5, 30),
    Utsjoki(new ArrayList<String>(Arrays.asList("RoUt")), 6, 30);

    private final ArrayList<Route> connectedRoutes;
    private final ArrayList<String> connectedRouteIDs;
    public final int index;
    public final double maxDistanceFromStartPoint;

    public ArrayList<Route> getConnectedRoutes(Routes routes) {
        if (connectedRoutes.size() == 0) {
            for (String id : connectedRouteIDs) {
                connectedRoutes.add(routes.routes.get(id));
            }
        }
        return connectedRoutes;
    }

    EndPoint(ArrayList<String> routeIndexes, int index_, double mDFSP) {
        maxDistanceFromStartPoint = mDFSP;
        connectedRouteIDs = routeIndexes;
        index = index_;
        connectedRoutes = new ArrayList<>();
    }
}
