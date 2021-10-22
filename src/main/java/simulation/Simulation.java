package simulation;

import java.util.ArrayList;

public class Simulation {

    private Simulation() {
        Routes.generateRoutes();
    }

    public void start() {
        System.out.println("Starting...");
        System.out.println(this.toString());
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\nALL ROUTES\n\n");
        ArrayList<Route> allRoutes = new ArrayList<>(Routes.routes.values());
        for (Route route : allRoutes) {
            stringBuilder.append(route.toString()).append("\n");
        }
        return stringBuilder.toString();
    }

    public static void main(String[] args) {
        Simulation simulation = new Simulation();
        simulation.start();
    }
}
