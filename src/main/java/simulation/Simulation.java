package simulation;

import java.util.ArrayList;
import java.util.logging.Logger;

public class Simulation implements Runnable {

    private static final Logger logger = Logger.getGlobal();

    ArrayList<Car> cars;
    private long seconds;
    private final long TIME_STEP = 10; // simulationSeconds / realSeconds
    private int tps;
    private final int NORM_DIST_MEAN = 7200;
    private final int NORM_DIST_STANDARD_DEVIATION = 1800;
    private final int TOTAL_CARS = 10000;
    private final boolean shouldWait;

    private double cumulativeDistributionCounter = 0;

    public Simulation(boolean shouldWait_) {
        Routes.generateRoutes();
        createCars();
        tps = 100;
        shouldWait = shouldWait_;
    }

    private void createCars() {
        cars = new ArrayList<>();
    }

    public void start() {
        logger.info("Starting...");

        seconds = 0;

        /*
        for (int i = 0; i < 10; i++) {
            Car car = new Car(CarType.TESLAMOTORS_MODEL3, i);
            car.setRoute(Route.generateShortestRoute(EndPoint.Helsinki, EndPoint.Utsjoki));
            cars.add(car);
        }
         */

        boolean carsCreatedLogged = false;

        while (!allCarsHaveReachedTheirDestination() || seconds < NORM_DIST_MEAN) {
            for (Car car : cars) {
                car.tick(TIME_STEP);
            }

            cumulativeDistributionCounter += distributionProbability()*(double)TOTAL_CARS*(double)TIME_STEP;
            while (cumulativeDistributionCounter >= 1) {
                Car car = new Car(CarType.TESLAMOTORS_MODEL3, cars.size());
                car.setRoute(Route.generateRandomRoute());
                cars.add(car);
                cumulativeDistributionCounter -= 1;
            }

            if (shouldWait) {
                try {
                    Thread.sleep(Math.round(1000d / tps));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Logging
            if (!carsCreatedLogged && cars.size() == TOTAL_CARS-1) {
                logger.config("All cars created");
                carsCreatedLogged = true;
            }

            seconds += TIME_STEP;
            if (seconds == Long.MAX_VALUE)
                throw new RuntimeException("Time exceeded maximum long value");
        }

        logger.info("Done.");
    }

    private double distributionProbability() {
        return (Math.pow(Math.E, -1/2d * Math.pow((seconds -  NORM_DIST_MEAN)/(double)NORM_DIST_STANDARD_DEVIATION,2)))
                / (NORM_DIST_STANDARD_DEVIATION*Math.sqrt(2*Math.PI));
    }

    private boolean allCarsHaveReachedTheirDestination() {
        for (Car car : cars) {
            if (car.getState() != Car.State.DestinationReached && car.getState() != Car.State.BatteryDepleted)
                return false;
        }
        return true;
    }

    public long getPassedSeconds() {
        return seconds;
    }

    public void setTps(int tps_) {
        tps = tps_;
    }

    public int getTps() {
        return tps;
    }

    public ArrayList<Car> getCars() {
        return cars;
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
