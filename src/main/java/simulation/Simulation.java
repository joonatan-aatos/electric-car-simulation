package simulation;


import java.util.*;
import java.util.logging.Logger;

public class Simulation implements Runnable {

    private static final Logger logger = Logger.getGlobal();

    ArrayList<Car> cars;
    ArrayList<Car> carsToBeAdded;
    private long seconds;
    private final long TIME_STEP = 10; // seconds
    private int tps;
    private final int NORM_DIST_MEAN;
    private final int NORM_DIST_STANDARD_DEVIATION;
    private final int TOTAL_CARS;
    private final double batteryCapacityCoefficient;
    private final double chargingPowerCoefficient;
    private final boolean shouldWait;
    private final boolean isWinter;
    private final Routes routes;
    private final String name;
    private final ArrayList<int[][]> stateStatisticsOverTime;
    private final ArrayList<int[]> globalStateStatisticsOverTime;
    private final ArrayList<int[]> roadStatisticsOverTime;
    private final ArrayList<int[]> waitingStatisticsOverTime;


    private double cumulativeDistributionCounter = 0.5;

    public Simulation(String name_, Routes routes_, int carCount, int standardDeviation, int mean, boolean shouldWait_, boolean isWinter_, double batteryCapacityCoefficient_, double chargingPowerCoefficient_) {
        name = name_;
        routes = routes_ != null ? routes_ : new Routes(0,1);
        TOTAL_CARS = carCount;
        NORM_DIST_STANDARD_DEVIATION = standardDeviation;
        NORM_DIST_MEAN = mean;
        isWinter = isWinter_;
        tps = 100;
        shouldWait = shouldWait_;
        stateStatisticsOverTime = new ArrayList<>();
        globalStateStatisticsOverTime = new ArrayList<>();
        roadStatisticsOverTime = new ArrayList<>();
        waitingStatisticsOverTime = new ArrayList<>();
        batteryCapacityCoefficient = batteryCapacityCoefficient_;
        chargingPowerCoefficient = chargingPowerCoefficient_;
        CarType.setBatteryCapacityCoefficient(batteryCapacityCoefficient);
        CarType.setChargingPowerCoefficient(chargingPowerCoefficient);

        createCars();
    }

    private void createCars() {
        logger.config(String.format("[%s]: Creating cars...", name));
        cars = new ArrayList<>();
        carsToBeAdded = new ArrayList<>();
        int carSum = Arrays.stream(CarType.values()).mapToInt(CarType::getAmount).sum();
        double carCounter = 0;
        List<CarType> carTypes = Arrays.asList(CarType.values());
        Collections.shuffle(carTypes, routes.random);
        for (CarType carType : carTypes) {
            carCounter += (double) carType.getAmount() / carSum * TOTAL_CARS;
            while (carCounter >= 1) {
                Car car = new Car(carType, carsToBeAdded.size(), batteryCapacityCoefficient);
                car.setRoute(routes.generateRandomRoute());
                carsToBeAdded.add(car);
                carCounter--;
            }
        }
        if (carsToBeAdded.size() == TOTAL_CARS - 1) {
            Car car = new Car(carTypes.get(carTypes.size() - 1), carsToBeAdded.size(), batteryCapacityCoefficient);
            car.setRoute(routes.generateRandomRoute());
            carsToBeAdded.add(car);
        }
        Collections.shuffle(carsToBeAdded, routes.random);
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

        while (!allCarsHaveReachedTheirDestination() || seconds < NORM_DIST_MEAN + 1) {

            int[] globalStateStatistics = new int[Car.State.values().length];
            int[][] stateStatistics = new int[routes.routeKeys.size()][Car.State.values().length];
            for (int i = 0; i < stateStatistics.length; i++) {
                for (int j = 0; j < stateStatistics[0].length; j++) {
                    stateStatistics[i][j] = 0;
                }
            }

            int[] carsOnRoad = {0, 0, 0, 0, 0, 0};
            int[] carsWaiting = {0, 0, 0, 0, 0, 0};

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
                // Cars waiting
                if (car.getState() == Car.State.Waiting) {
                    ArrayList<Route> rootRoutes = car.getRoute().getRootRoutes();
                    double drivenDistance = car.getDrivenDistance();
                    for (Route rootRoute : rootRoutes) {
                        drivenDistance -= rootRoute.getLength();
                        if (drivenDistance < 0) {
                            int index = Math.min(rootRoute.getStartPoint().index, rootRoute.getEndPoint().index);
                            carsWaiting[index]++;
                            break;
                        }
                    }
                }
                // Roads
                if (car.getState() != Car.State.DestinationReached) {
                    ArrayList<Route> rootRoutes = car.getRoute().getRootRoutes();
                    double drivenDistance = car.getDrivenDistance();
                    for (Route rootRoute : rootRoutes) {
                        drivenDistance -= rootRoute.getLength();
                        if (drivenDistance < 0) {
                            int index = Math.min(rootRoute.getStartPoint().index, rootRoute.getEndPoint().index);
                            carsOnRoad[index]++;
                            break;
                        }
                    }
                }
            }
            stateStatisticsOverTime.add(stateStatistics);
            globalStateStatisticsOverTime.add(globalStateStatistics);
            roadStatisticsOverTime.add(carsOnRoad);
            waitingStatisticsOverTime.add(carsWaiting);

            cumulativeDistributionCounter += distributionProbability()*(double)TOTAL_CARS*(double)TIME_STEP;
            while (cumulativeDistributionCounter >= 1) {
                carsToBeAdded.get(0).setCreationTime((int) seconds);
                cars.add(carsToBeAdded.remove(0));

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

            if (seconds > 36000000) {
                logger.severe(String.format("[%s]: Simulation stopped by force", name));
                System.exit(1);
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

    public ArrayList<int[]> getRoadStatisticsOverTime() {
        return roadStatisticsOverTime;
    }

    public ArrayList<int[]> getWaitingStatisticsOverTime() {
        return waitingStatisticsOverTime;
    }

    public int getTotalCars() {
        return TOTAL_CARS;
    }

    public int getStandardDeviation() {
        return NORM_DIST_STANDARD_DEVIATION;
    }

    public double getBatteryCapacityCoefficient() {
        return batteryCapacityCoefficient;
    }

    public double getChargingPowerCoefficient() {
        return chargingPowerCoefficient;
    }

    public boolean isWinter() {
        return isWinter;
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
