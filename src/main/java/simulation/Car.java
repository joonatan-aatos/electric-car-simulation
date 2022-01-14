package simulation;

import java.util.logging.Logger;

public class Car implements Comparable<Car> {

    private static final Logger logger = Logger.getGlobal();

    private Route route;
    private final CarType carType;
    private final int index;
    private final double DESTINATION_BATTERY_THRESHOLD = 0.1;
    private final double BATTERY_CHARGING_THRESHOLD = 0.3;
    private final double SPEED_ON_HIGHWAY = 120;
    private final double SPEED_OUTSIDE_HIGHWAY = 30;
    private final double EATING_DURATION = 45 * 60; // In seconds

    private long timeStep;

    private double hunger; // In seconds since last eaten
    private double timeSinceLastShopped;
    private double timeSpentCharging;
    private double battery;
    private double drivenDistance;
    private double distanceFromHighway;
    private double drivingSpeed;
    private int currentChargingStationIndex;
    private int nextChargingStationIndex;
    private boolean continueDriving;
    private ChargingStation.Charger currentCharger;
    private State state;
    private long[] stateTime;   //  In seconds
    private double destinationDistanceFromEndPoint; // Is never changed, assigned once in setRoute

    private int timesCharged;

    @Override
    public int compareTo(Car car) {
        return this.index - car.index;
    }

    public enum State {
        OnHighway(0),
        OnWayToHighway(1),
        OnWayFromHighway(2),
        OnWayToCharger(3),
        OnWayFromCharger(4),
        Waiting(5),
        Charging(6),
        BatteryDepleted(7),
        DestinationReached(8);

        public final int index;

        private State(int index_) {
            index = index_;
        }
    };

    public Car(CarType carType_, int index_) {
        carType = carType_;
        index = index_;

        battery = carType.getCapacity();

        drivenDistance = 0;
        drivingSpeed = SPEED_ON_HIGHWAY;
        currentChargingStationIndex = -1;
        nextChargingStationIndex = -1;
        state = State.OnWayToHighway;
        distanceFromHighway = 0;
        continueDriving = false;
        currentCharger = null;
        hunger = 0;
        timeSinceLastShopped = 0;
        stateTime = new long[9];
        logger.info(String.format("Created car: %s", this.toString()));

        timesCharged = 0;
    }

    public double drive() {
        double deltaDistance = drivingSpeed * (timeStep / 3600d);

        if (battery - batteryUsedForDistance(deltaDistance) < 0)
            battery = 0;
        else
            battery -= batteryUsedForDistance(deltaDistance);

        return deltaDistance;
    }

    public void tick(long TIME_STEP) {
        timeStep = TIME_STEP;
        hunger += timeStep;
        timeSinceLastShopped += timeStep;
        switch (state) {
            case OnWayToHighway:
                driveToHighway();
                break;
            case OnHighway:
                driveOnHighway();
                break;
            case OnWayToCharger:
                driveToStation();
                break;
            case OnWayFromCharger:
                driveFromStation();
                break;
            case Waiting:
                waitOnStation();
                break;
            case Charging:
                charge();
                break;
            case OnWayFromHighway:
                driveFromHighway();
                break;
        }
        if (battery <= 0 && state != State.BatteryDepleted) {
            logger.fine(String.format("%s: %s", this.toString(), "Battery depleted"));
            state = State.BatteryDepleted;
        }
        stateTime[state.index] += timeStep;
    }

    public void driveToHighway() {
        distanceFromHighway -= drive();

        if (distanceFromHighway <= 0) {
            distanceFromHighway = 0;
            state = State.OnHighway;
            currentChargingStationIndex = calculateNextChargingStationIndex();
            if (currentChargingStationIndex == -1){
                if (!continueDriving)
                    continueDriving = true;
            }
        }
    }

    public void driveFromHighway() {
        distanceFromHighway += drive();

        if (distanceFromHighway >= destinationDistanceFromEndPoint) {
            state = State.DestinationReached;
            logger.info(String.format("%s: %s", this.toString(), "Destination reached"));
        }
    }

    public void waitOnStation() {
        ChargingStation.Charger availableCharger = route.getChargingStations().get(currentChargingStationIndex).getAvailableCharger(carType.getSupportedChargers());
        int nextInQueue = route.getChargingStations().get(currentChargingStationIndex).getNextInQueue();
        if (availableCharger != null && (nextInQueue == index || nextInQueue == -1)) {
            assert !availableCharger.isInUse();
            availableCharger.setInUse(true);
            route.getChargingStations().get(currentChargingStationIndex).removeFromQueue(index);
            currentCharger = availableCharger;
            state = State.Charging;
            timesCharged++;
        }
        else {
            // Calculate whether it is best to go to the next charging station
            int bestChargingStationIndex = currentChargingStationIndex;
            double bestChargingStationPreferencePoints = calculatePreferencePointsOnCharger(currentChargingStationIndex);
            for (int i = 1; i <= route.getChargingStations().size(); i++) {
                if (
                    currentChargingStationIndex + i <= route.getChargingStations().size() - 1 &&
                    route.getChargingStations().get(currentChargingStationIndex + i).hasChargerType(carType.getSupportedChargers()) &&
                    canReachChargingStation(currentChargingStationIndex + i))
                {
                    double preferencePoints = calculatePreferencePointsOnCharger(currentChargingStationIndex + i);
                    if (preferencePoints > bestChargingStationPreferencePoints) {
                        bestChargingStationIndex = currentChargingStationIndex + i;
                        bestChargingStationPreferencePoints = preferencePoints;
                    }
                } else if (currentChargingStationIndex + i > route.getChargingStations().size() - 1 || !canReachChargingStation(currentChargingStationIndex + 1)) {
                    break;
                }
            }
            if (bestChargingStationIndex != currentChargingStationIndex) {
                route.getChargingStations().get(currentChargingStationIndex).removeFromQueue(index);
                nextChargingStationIndex = bestChargingStationIndex;
                logger.finer(String.format(
                        "%s: %s", this.toString(), "Continue to next charger: " +
                                route.getChargingStations().get(nextChargingStationIndex).toString()
                ));
                state = State.OnWayFromCharger;
            }
        }
    }

    public boolean canReachNextPlace(boolean isLastCharger) {
        if (!isLastCharger) {
            for (int i = currentChargingStationIndex + 1; i < route.getChargingStations().size(); i++) {
                if (route.getChargingStations().get(i).hasChargerType(carType.getSupportedChargers()))
                    return canReachChargingStation(i);
            }
        }
        return canReachDestination();
    }

    public void charge() {
        timeSpentCharging += timeStep;
        boolean isLastCharger = currentChargingStationIndex >= route.getChargingStations().size() - 1;

        if ((battery >= carType.getCapacity() * 0.8 && canReachNextPlace(isLastCharger)) || battery >= carType.getCapacity()) {
            if (route.getChargingStations().get(currentChargingStationIndex).isHasFood() && timeSpentCharging < EATING_DURATION ) {
                return;
            }
            if (route.getChargingStations().get(currentChargingStationIndex).isHasFood()) {
                hunger = 0;
            }
            if (route.getChargingStations().get(currentChargingStationIndex).isHasShop()) {
                timeSinceLastShopped = 0;
            }
            currentCharger.setInUse(false);
            currentCharger = null;
            state = State.OnWayFromCharger;
            logger.fine(String.format(
                    "%s: %s", this.toString(),
                    "Leaving charging station: " +
                            route.getChargingStations().get(currentChargingStationIndex).toString())
            );
        } else {
            double batteryBefore = battery;
            double maxChargingPower;
            if (currentCharger.getType() == ChargingStation.ChargerType.Type2 || currentCharger.getType() == ChargingStation.ChargerType.Tyomaapistoke) {
                maxChargingPower = Math.min(carType.getMaxChargingPowerAC(), currentCharger.getPower());
            }
            else {
                maxChargingPower = Math.min(carType.getMaxChargingPowerDC(), currentCharger.getPower());
            }
            // 0% - 5%
            if (battery / carType.getCapacity() < 0.05) {
                // y = k (x - x0) + y0
                double power = (0.6 * maxChargingPower / (0.05 * carType.getCapacity()) * battery + 0.4 * maxChargingPower);
                battery += power * (timeStep / 3600d);
            }
            // 5% - 25%
            else if (battery / carType.getCapacity() < 0.25) {
                battery += Math.min(maxChargingPower, currentCharger.getPower()) * (timeStep / 3600d);
            }
            // 25% - 100%
            else {
                // y = k (x - x0) + y0
                double power = (-0.9 * maxChargingPower / (0.75 * carType.getCapacity()) * (battery - 0.25 * carType.getCapacity()) + maxChargingPower);
                battery += power * (timeStep / 3600d);
            }
            if (battery - batteryBefore <= 0) {
                logger.severe(String.format(
                        "%s: %s [%s]", this.toString(), "Car is not charging!", route.getChargingStations().get(currentChargingStationIndex).toString()
                ));
            }
        }
        if (battery >= carType.getCapacity())
            battery = carType.getCapacity();
    }

    public void driveToStation() {
        distanceFromHighway += drive();

        ChargingStation station = route.getChargingStations().get(currentChargingStationIndex);
        if (distanceFromHighway >= station.getDistanceFromHighway()) {
            ChargingStation.Charger availableCharger = station.getAvailableCharger(carType.getSupportedChargers());
            if (availableCharger == null) {
                logger.finer(String.format(
                        "%s: %s", this.toString(), "Going to wait at charger: " +
                                route.getChargingStations().get(currentChargingStationIndex).toString()
                ));
                state = State.Waiting;
                route.getChargingStations().get(currentChargingStationIndex).addToQueue(index);
            } else {
                availableCharger.setInUse(true);
                currentCharger = availableCharger;
                logger.fine(String.format(
                        "%s: %s", this.toString(),
                        "Entering charging station: " +
                                route.getChargingStations().get(currentChargingStationIndex).toString())
                );
                state = State.Charging;
                timesCharged++;
            }
        }
    }

    public void driveFromStation() {
        distanceFromHighway -= drive();

        if (distanceFromHighway <= 0) {
            // Check if car has left the previous charging station without getting to charge
            if (nextChargingStationIndex != -1) {
                currentChargingStationIndex = nextChargingStationIndex;
                nextChargingStationIndex = -1;
            }
            else {
                currentChargingStationIndex = calculateNextChargingStationIndex();
                if (currentChargingStationIndex == -1){
                    if (!continueDriving)
                        continueDriving = true;
                }
            }

            drivingSpeed = SPEED_ON_HIGHWAY;
            distanceFromHighway = 0;
            state = State.OnHighway;
        }
    }

    public void driveOnHighway() {
        drivenDistance += drive();
                
        if (drivenDistance >= route.getLength()) {
            state = State.OnWayFromHighway;
            return;
        }

        if (continueDriving)
            return;

        if (route.getChargingStationDistances().get(currentChargingStationIndex) - drivenDistance < 0) {
            state = State.OnWayToCharger;
            drivingSpeed = SPEED_OUTSIDE_HIGHWAY;
            distanceFromHighway = 0;
        }
    }

    private double distanceFromStartToChargingStation(int i) {
        return route.getChargingStationDistances().get(i) + route.getChargingStations().get(i).getDistanceFromHighway();
    }

    /**
     * @return Index of the next charging station the car should stop by
     */
    private int calculateNextChargingStationIndex() {

        double optimalDistance = battery / carType.getDrivingEfficiency() * 100 * (1 - BATTERY_CHARGING_THRESHOLD) + drivenDistance;
        double maximumDistance = battery / carType.getDrivingEfficiency() * 100 * (1 - DESTINATION_BATTERY_THRESHOLD) + drivenDistance;

        if (maximumDistance > route.getLength() + destinationDistanceFromEndPoint) {
            return -1;
        }

        int previousChargingStationIndex = currentChargingStationIndex;
        int preferredStationIndex = -1;
        double mostPreferencePoints = 0;

        for (int i = previousChargingStationIndex + 1; i < route.getChargingStations().size(); i++) {
            if (distanceFromStartToChargingStation(i) > maximumDistance) {
                break;
            }
            if (!route.getChargingStations().get(i).hasChargerType(carType.getSupportedChargers()))
                continue;
            double preferencePoints = calculatePreferencePointsOnHighway(i, optimalDistance);
            if (preferencePoints > mostPreferencePoints) {
                mostPreferencePoints = preferencePoints;
                preferredStationIndex = i;
            }
        }

        return preferredStationIndex;
    }

    private double calculatePreferencePointsOnHighway(int chargingStationIndex, double optimalDistance) {

        ChargingStation chargingStation = route.getChargingStations().get(chargingStationIndex);

        double distanceCoefficient = 1 +
                Math.pow(distanceFromStartToChargingStation(chargingStationIndex) - optimalDistance, 2);

        double commonPreferencePoints = calculateCommonPreferencePoints(chargingStation);

        return
                (commonPreferencePoints) /
                /*-----division line-----*/
                (distanceCoefficient);
    }

    private double calculatePreferencePointsOnCharger(int nextChargingStationIndex) {

        ChargingStation currentChargingStation = route.getChargingStations().get(currentChargingStationIndex);
        ChargingStation nextChargingStation = route.getChargingStations().get(nextChargingStationIndex);

        double chargerDistanceCoefficient = 1 +
                distanceFromStartToChargingStation(nextChargingStationIndex) -
                route.getChargingStationDistances().get(currentChargingStationIndex) +
                currentChargingStation.getDistanceFromHighway();

        double commonPreferencePoints = calculateCommonPreferencePoints(nextChargingStation);

        return
            (commonPreferencePoints) /
            /*-----division line-----*/
            (chargerDistanceCoefficient);
    }

    private double calculateCommonPreferencePoints(ChargingStation chargingStation) {

        double chargerPowerCoefficient = chargingStation.getChargers().get(0).getPower();
        double chargerCountCoefficient = Math.pow(chargingStation.getChargers().size(), 2);
        double lineLengthCoefficient = Math.pow(1 + chargingStation.getQueueLength(), 2);

        double amenitiesCoefficient = 2 +
                (chargingStation.isHasFood() ? 1 : 0) * hunger/3600 +       // Coefficient attains +1 per hour hungry
                (chargingStation.isHasShop() ? 1 : 0) * timeSinceLastShopped /3600 + //  Shopping number gets multiplied by 1/2 for every time shopped
                (chargingStation.isCustomerExclusive() ? -1 : 0);

        return
                (chargerPowerCoefficient * chargerCountCoefficient * amenitiesCoefficient) /
                /*-----------------------------division line-------------------------------*/
                (lineLengthCoefficient);
    }

    private boolean canReachDestination() {
        double maximumDistance = battery / carType.getDrivingEfficiency() * 100 * (1 - DESTINATION_BATTERY_THRESHOLD);
        double distanceToDestination = distanceFromHighway + (route.getLength() - drivenDistance) + destinationDistanceFromEndPoint;
        return maximumDistance > distanceToDestination;
    }

    private boolean canReachChargingStation(int chargingStationIndex) throws IndexOutOfBoundsException {

        ChargingStation currentChargingStation = route.getChargingStations().get(currentChargingStationIndex);
        double currentChargingStationDistance = route.getChargingStationDistances().get(currentChargingStationIndex);
        ChargingStation nextChargingStation = route.getChargingStations().get(chargingStationIndex);
        double nextChargingStationDistance = route.getChargingStationDistances().get(chargingStationIndex);

        double distanceToNextChargingStation =
                nextChargingStation.getDistanceFromHighway() +
                nextChargingStationDistance - currentChargingStationDistance +
                currentChargingStation.getDistanceFromHighway();

        double maximumDistance = battery / carType.getDrivingEfficiency() * 100 * (1 - DESTINATION_BATTERY_THRESHOLD);

        return maximumDistance > distanceToNextChargingStation + 1;
    }

    public double batteryUsedForDistance(double deltaDistance) {
        return deltaDistance * carType.getDrivingEfficiency() / 100;
    }

    public double getDistanceFromHighway() {
        return distanceFromHighway;
    }

    public double getDrivenDistance() {
        return drivenDistance;
    }

    public CarType getCarType() {
        return carType;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
        distanceFromHighway = Math.random() * route.getStartPoint().maxDistanceFromStartPoint;
        destinationDistanceFromEndPoint = Math.random() * route.getEndPoint().maxDistanceFromStartPoint;
        logger.info(String.format("%s: %s: %s", this.toString(), "Route set", route.getName()));
    }

    public State getState() {
        return state;
    }

    public double getBattery() {
        return battery;
    }

    public long[] getStateTime() {
        return stateTime;
    }

    public int getIndex() {
        return index;
    }

    public int getTimesCharged() {
        return timesCharged;
    }

    @Override
    public String toString() {
        return String.format("%s %d (%s %.1f%%)", carType.toString(), index, state.toString(), battery /carType.getCapacity()*100);
    }
}
