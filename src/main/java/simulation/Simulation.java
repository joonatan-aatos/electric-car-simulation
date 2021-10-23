package simulation;

import java.util.ArrayList;

public class Simulation implements Runnable {

    ArrayList<Car> cars;
    int timeElapsed;

    public Simulation() {
        Routes.generateRoutes();
        createCars();
    }

    private void createCars() {
        cars = new ArrayList<>();
        cars.add(new Car(CarType.TESLAMOTORS_MODEL3));

        for (Car car : cars) {
            car.setRoute(Route.generateRandomRoute());
        }
    }

    public void start() {
        System.out.println("Starting...");

        timeElapsed = 0;

        while (!allCarsHaveReachedTheirDestination()) {
            for (Car car : cars) {
                car.tick();
            }
            timeElapsed++;
            if (timeElapsed == Integer.MAX_VALUE)
                throw new RuntimeException("Time exceeded maximum integer value");

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Done.");
    }

    private boolean allCarsHaveReachedTheirDestination() {
        for (Car car : cars) {
            if (!car.isHasReachedDestination())
                return false;
        }
        return true;
    }

    @Override
    public void run() {
        start();
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
}
