package simulation;

import java.util.ArrayList;
import java.util.logging.Logger;

public class Simulation implements Runnable {

    private static final Logger logger = Logger.getGlobal();

    ArrayList<Car> cars;
    private long seconds;
    private final long TIME_STEP = 10; // seconds
    private int tps;
    private final int NORM_DIST_MEAN;
    private final int NORM_DIST_STANDARD_DEVIATION;
    private final int TOTAL_CARS;
    private final boolean shouldWait;
    private final Routes routes;
    private final String name;
    private final ArrayList<int[][]> stateStatisticsOverTime;
    private final ArrayList<int[]> globalStateStatisticsOverTime;

    private double cumulativeDistributionCounter = 0;

    public Simulation(String name_, Routes routes_, int carCount, int standardDeviation, int mean, boolean shouldWait_) {
        name = name_;
        routes = routes_ != null ? routes_ : new Routes();
        createCars();
        TOTAL_CARS = carCount;
        NORM_DIST_STANDARD_DEVIATION = standardDeviation;
        NORM_DIST_MEAN = mean;
        tps = 100;
        shouldWait = shouldWait_;
        stateStatisticsOverTime = new ArrayList<>();
        globalStateStatisticsOverTime = new ArrayList<>();
    }

    private void createCars() {
        cars = new ArrayList<>();
    }

    public void start() {
        logger.config(String.format("[%s]: Starting...", name));

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

            int[] globalStateStatistics = new int[Car.State.values().length];
            int[][] stateStatistics = new int[routes.routeKeys.size()][Car.State.values().length];
            for (int i = 0; i < stateStatistics.length; i++) {
                for (int j = 0; j < stateStatistics[0].length; j++) {
                    stateStatistics[i][j] = 0;
                }
            }
            for (Car car : cars) {
                car.tick(TIME_STEP);

                // Collecting stats
                globalStateStatistics[car.getState().index]++;
                if (car.getState() != Car.State.DestinationReached) {
                    ArrayList<Route> rootRoutes = car.getRoute().getRootRoutes();
                    double drivenDistance = car.getDrivenDistance();
                    for (Route rootRoute : rootRoutes) {
                        drivenDistance -= rootRoute.getLength();
                        if (drivenDistance < 0) {
                            int index = Math.min(rootRoute.getStartPoint().index, rootRoute.getEndPoint().index);
                            stateStatistics[index][car.getState().index]++;
                            break;
                        }
                    }
                }
            }
            stateStatisticsOverTime.add(stateStatistics);
            globalStateStatisticsOverTime.add(globalStateStatistics);

            cumulativeDistributionCounter += distributionProbability()*(double)TOTAL_CARS*(double)TIME_STEP;
            while (cumulativeDistributionCounter >= 1) {
                Car car = new Car(CarType.TESLA_MODEL_3, cars.size());
                car.setRoute(routes.generateRandomRoute());
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
            // All cars created
            if (!carsCreatedLogged && cars.size() == TOTAL_CARS-1) {
                logger.config(String.format("[%s]: All cars created", name));
                carsCreatedLogged = true;
            }

            seconds += TIME_STEP;

            if (seconds > 720000) {
                logger.severe(String.format("[%s]: Simulation stopped by force", name));
                break;
            }
        }

        logger.info(String.format("[%s]: Done.", name));
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

    public long getTimeStep() {
        return TIME_STEP;
    }

    public ArrayList<Car> getCars() {
        return cars;
    }

    public Routes getRoutes() {
        return routes;
    }

    public String getName() {
        return name;
    }

    public ArrayList<int[][]> getStateStatisticsOverTime() {
        return stateStatisticsOverTime;
    }

    public ArrayList<int[]> getGlobalStateStatisticsOverTime() {
        return globalStateStatisticsOverTime;
    }

    @Override
    public void run() {
        start();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\nALL ROUTES\n\n");
        ArrayList<Route> allRoutes = new ArrayList<>(routes.routes.values());
        for (Route route : allRoutes) {
            stringBuilder.append(route.toString()).append("\n");
        }
        return stringBuilder.toString();
    }
}
