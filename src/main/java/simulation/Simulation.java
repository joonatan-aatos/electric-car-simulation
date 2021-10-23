package simulation;

import java.util.ArrayList;

public class Simulation {

    ArrayList<Car> cars;

    public Simulation() {
        Routes.generateRoutes();
        createCars();
    }

    private void createCars() {
        cars = new ArrayList<>();
        cars.add(new Car(CarType.TESLAMOTORS_MODEL3));
    }

    public void start() {
        System.out.println("Starting...");
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
        System.out.println("Done.");
    }
}
