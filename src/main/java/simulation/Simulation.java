package simulation;

public class Simulation {

    private Simulation() {
        Routes.generateRoutes();
    }

    public void start() {
        System.out.println("Starting...");
        System.out.println(Routes.routes.get(0).toString());
    }

    public static void main(String[] args) {
        Simulation simulation = new Simulation();
        simulation.start();
    }
}
