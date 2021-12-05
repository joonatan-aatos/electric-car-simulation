package simulation;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Car {

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
    private double timesShopped;
    private double timeSpentCharging;
    private double batteryLife;
    private double drivenDistance;
    private double distanceFromHighway;
    private double drivingSpeed;
    private int currentChargingStationIndex;
    private int nextChargingStationIndex;
    private boolean canReachDestination;
    private ChargingStation.Charger currentCharger;
    private State state;
    private long[] stateTime;
    private double destinationDistanceFromEndPoint; // Is never changed, assigned once in setRoute

    public enum State {
        OnHighway(0),
        OnWayToHighway(1),
        OnWayFromHighway(2),
        OnWayToCharger(3),
        OnWayFromCharger(4),
        Charging(6),
        Waiting(5),
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

        batteryLife = carType.capacity;

        drivenDistance = 0;
        drivingSpeed = SPEED_ON_HIGHWAY;
        currentChargingStationIndex = -1;
        nextChargingStationIndex = -1;
        state = State.OnWayToHighway;
        distanceFromHighway = 0;
        canReachDestination = false;
        currentCharger = null;
        hunger = 0;
        timesShopped = 0;
        stateTime = new long[9];
        logger.finer("Created car: " + this.toString());
    }

    public void tick(long TIME_STEP) {
        timeStep = TIME_STEP;
        hunger += timeStep;
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
        if (batteryLife <= 0 && state != State.BatteryDepleted) {
            logger.severe(String.format("%s: %s", this.toString(), "Battery depleted"));
            state = State.BatteryDepleted;
        }
        stateTime[state.index] += timeStep;
    }

    public void driveToHighway() {
        double deltaDistance = drivingSpeed * (timeStep /3600d);
        distanceFromHighway -= deltaDistance;
        batteryLife -= deltaDistance * carType.drivingEfficiency / 100;

        if (distanceFromHighway <= 0) {
            distanceFromHighway = 0;
            state = State.OnHighway;
        }
    }

    public void driveFromHighway() {
        double deltaDistance = drivingSpeed * (timeStep /3600d);
        distanceFromHighway += deltaDistance;
        batteryLife -= deltaDistance * carType.drivingEfficiency / 100;

        if (distanceFromHighway >= destinationDistanceFromEndPoint) {
            state = State.DestinationReached;
        }
    }

    public void waitOnStation() {
        ChargingStation.Charger availableCharger = route.getChargingStations().get(currentChargingStationIndex).getAvailableCharger();
        int nextInQueue = route.getChargingStations().get(currentChargingStationIndex).getNextInQueue();
        if (availableCharger != null && (nextInQueue == index || nextInQueue == -1)) {
            assert !availableCharger.isInUse();
            availableCharger.setInUse(true);
            route.getChargingStations().get(currentChargingStationIndex).removeFromQueue(index);
            currentCharger = availableCharger;
            state = State.Charging;
        }
        else {
            int bestChargingStationIndex = currentChargingStationIndex;
            double bestChargingStationPreferencePoints = calculatePreferencePointsOnCharger(currentChargingStationIndex);
            for (int i = 1; i <= 3; i++) {
                if (
                    currentChargingStationIndex + i <= route.getChargingStations().size() - 1 &&
                    canReachChargingStation(currentChargingStationIndex + i))
                {
                    double preferencePoints = calculatePreferencePointsOnCharger(currentChargingStationIndex + i);
                    if (preferencePoints > bestChargingStationPreferencePoints) {
                        bestChargingStationIndex = currentChargingStationIndex + i;
                        bestChargingStationPreferencePoints = preferencePoints;
                    }
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

    public void charge() {
        timeSpentCharging += timeStep;
        if (batteryLife >= carType.capacity) {
            if (route.getChargingStations().get(currentChargingStationIndex).isHasFood() && timeSpentCharging < EATING_DURATION ) {
                return;
            }
            if (route.getChargingStations().get(currentChargingStationIndex).isHasFood()) {
                hunger = 0;
            }
            batteryLife = carType.capacity;
            currentCharger.setInUse(false);
            currentCharger = null;
            state = State.OnWayFromCharger;
        } else {
            batteryLife += Math.min(carType.chargingEfficiency, currentCharger.getPower()) * (timeStep / 3600d);
        }
    }

    public void driveToStation() {
        double deltaDistance = drivingSpeed * (timeStep /3600d);
        distanceFromHighway += deltaDistance;
        batteryLife -= deltaDistance * carType.drivingEfficiency / 100;

        ChargingStation station = route.getChargingStations().get(currentChargingStationIndex);
        if (distanceFromHighway >= station.getDistanceFromHighway()) {
            if (station.isHasShop()) {
                timesShopped++;
            }
            ChargingStation.Charger availableCharger = station.getAvailableCharger();
            if (availableCharger == null) {
                logger.finer(String.format(
                        "%s: %s", this.toString(), "Going to wait at charger: " +
                                route.getChargingStations().get(currentChargingStationIndex).toString()
                ));
                state = State.Waiting;
                route.getChargingStations().get(currentChargingStationIndex).addToQueue(index);
            } else {
                assert !availableCharger.isInUse();
                availableCharger.setInUse(true);
                currentCharger = availableCharger;
                logger.fine(String.format(
                        "%s: %s", this.toString(),
                        "Entering charging station: " +
                                route.getChargingStations().get(currentChargingStationIndex).toString())
                );
                state = State.Charging;
            }
        }
    }

    public void driveFromStation() {
        double deltaDistance = drivingSpeed * (timeStep /3600d);
        distanceFromHighway -= deltaDistance;
        batteryLife -= deltaDistance * carType.drivingEfficiency / 100;

        if (distanceFromHighway <= 0) {
            if (nextChargingStationIndex != -1) {
                currentChargingStationIndex = nextChargingStationIndex;
                nextChargingStationIndex = -1;
            }
            else {
                currentChargingStationIndex = calculateNextChargingStationIndex();
            }

            drivingSpeed = SPEED_ON_HIGHWAY;
            distanceFromHighway = 0;
            state = State.OnHighway;
        }
    }

    public void driveOnHighway() {
        double deltaDistance = drivingSpeed * (timeStep /3600d);
        drivenDistance += deltaDistance;
        batteryLife -= deltaDistance * carType.drivingEfficiency / 100;

        if (drivenDistance >= route.getLength()) {
            state = State.OnWayFromHighway;
        }

        if (currentChargingStationIndex == -1)
            currentChargingStationIndex = calculateNextChargingStationIndex();

        if (canReachDestination)
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

        double optimalDistance = batteryLife / carType.drivingEfficiency * 100 * (1 - BATTERY_CHARGING_THRESHOLD) + drivenDistance;
        double maximumDistance = batteryLife / carType.drivingEfficiency * 100 * (1 - DESTINATION_BATTERY_THRESHOLD) + drivenDistance;

        canReachDestination = maximumDistance > route.getLength() + destinationDistanceFromEndPoint;
        if (canReachDestination) {
            // The car can reach its destination
            return -1;
        }

        if (
            // If this is true, there are no more charging stations left, so the car will
            // drive until it has either reached its destination or drained its battery
            route.getChargingStationDistances().size() <= currentChargingStationIndex + 1
        ) {
            return -1;
        }

        int previousChargingStationIndex = currentChargingStationIndex;
        int preferredStationIndex = 0;
        double mostPreferencePoints = 0;

        for (int i = previousChargingStationIndex + 1; i < route.getChargingStations().size(); i++) {
            if (distanceFromStartToChargingStation(i) > maximumDistance) {
                break;
            }
            double preferencePoints = calculatePreferencePointsOnHighway(i, optimalDistance);
            if (preferencePoints > mostPreferencePoints){
                mostPreferencePoints = preferencePoints;
                preferredStationIndex = i;
            }
        }

        return preferredStationIndex;
    }

    private double calculatePreferencePointsOnHighway(int chargingStationIndex, double optimalDistance) {

        ChargingStation chargingStation = route.getChargingStations().get(chargingStationIndex);

        double distanceCoefficient = 1 +
                Math.abs(distanceFromStartToChargingStation(chargingStationIndex) - optimalDistance);
        distanceCoefficient = Math.pow(distanceCoefficient, 2);

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
                (chargingStation.isHasShop() ? 1 : 0) / Math.pow(2, timesShopped) + //  Shopping number gets multiplied by 1/2 for every time shopped
                (chargingStation.isCustomerExclusive() ? -1 : 0);

        return
                (chargerPowerCoefficient * chargerCountCoefficient * amenitiesCoefficient) /
                /*-----------------------------division line-------------------------------*/
                (lineLengthCoefficient);
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

        double maximumDistance = batteryLife / carType.drivingEfficiency * 100 * (1 - DESTINATION_BATTERY_THRESHOLD);

        return maximumDistance > distanceToNextChargingStation + 1;
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
    }

    public State getState() {
        return state;
    }

    public double getBatteryLife() {
        return batteryLife;
    }

    public long[] getStateTime() {
        return stateTime;
    }

    @Override
    public String toString() {
        return String.format("Car %d (%s %.1f%%)", index, state.toString(), batteryLife/carType.capacity*100);
    }
}
