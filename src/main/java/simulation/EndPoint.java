package simulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum EndPoint {
    Helsinki    (new ArrayList<Integer>(List.of(0))),
    Lahti       (new ArrayList<Integer>(Arrays.asList(0,1))),
    Jyvaskyla   (new ArrayList<Integer>(Arrays.asList(1,2))),
    Oulu        (new ArrayList<Integer>(Arrays.asList(2,3))),
    Rovaniemi   (new ArrayList<Integer>(List.of(3)));


    private ArrayList<Route> connectedRoutes = new ArrayList<>();
    private ArrayList<Integer> connectedRouteIndexes = new ArrayList<>();

    public ArrayList<Route> getConnectedRoutes() {
        if (connectedRoutes.size() == 0) {
            for (int i : connectedRouteIndexes) {
                connectedRoutes.add(Routes.routes.get(i));
            }
        }
        return connectedRoutes;
    }
    EndPoint(ArrayList<Integer> routeIndexes){
        connectedRouteIndexes = routeIndexes;
    }
}
