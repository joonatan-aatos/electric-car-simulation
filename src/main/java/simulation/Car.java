package simulation;

public class Car {
    private Route route;
    private final CarType carType;
    private final double DESTINATION_BATTERY_THRESHOLD = 0.1;
    private final double BATTERY_CHARGING_THRESHOLD = 0.3;
    private final double SPEED_ON_HIGHWAY = 120;
    private final double SPEED_OUTSIDE_HIGHWAY = 30;

    private final double DISTANCE_FROM_THRESHOLD_FACTOR = 5;
    private final double LINE_LENGHT_FACTOR = 0.5;


    private long timestep;

    private double batteryLife;
    private double drivenDistance;
    private double distanceFromHighway;
    private double drivingSpeed;
    private int nextChargingStationIndex;
    private boolean canReachDestination;
    private ChargingStation.Charger chargerOn;
    private State state;

    public enum State {
        OnHighway,
        OnWayToCharger,
        OnWayFromCharger,
        Waiting,
        DestinationReached,
        BatteryDepleted,
        Charging
    };

    public Car(CarType carType_) {
        carType = carType_;
        batteryLife = carType.capacity;
        drivenDistance = 0;
        drivingSpeed = SPEED_ON_HIGHWAY;
        nextChargingStationIndex = -1;
        state = State.OnHighway;
        distanceFromHighway = 0;
        canReachDestination = false;
        chargerOn = null;
    }

    public void tick(long TIME_STEP) {
        timestep = TIME_STEP;
        switch (state) {
            case OnHighway:
                driveOnHighway();
                break;
            case OnWayToCharger:
                driveToStation();
                break;
            case OnWayFromCharger:
                driveToHighway();
                break;
            case Waiting:
                waitOnStation();
                break;
            case Charging:
                charge();
                break;
        }
        if (batteryLife <= 0) state = State.BatteryDepleted;
    }

    public void waitOnStation() {       // TODO: Add some sort of system to prevent long-ass waiting due to being the last car to get ticked.
        ChargingStation.Charger availableCharger = route.getChargingStations().get(nextChargingStationIndex).getAvailableCharger();
        if (availableCharger != null) {
            availableCharger.setInUse(true);
            route.getChargingStations().get(nextChargingStationIndex).remove1FromLine();
            chargerOn = availableCharger;
            state = State.Charging;
        }
    }

    public void charge() {
        batteryLife += Math.min(carType.chargingEfficiency, chargerOn.getPower()) * (timestep/3600d);
        if (batteryLife >= carType.capacity) {
            batteryLife = carType.capacity;
            chargerOn.setInUse(false);
            chargerOn = null;
            state = State.OnWayFromCharger;
        }
    }

    public void driveToStation() {
        double deltaDistance = drivingSpeed * (timestep/3600d);
        distanceFromHighway += deltaDistance;
        batteryLife -= deltaDistance * carType.drivingEfficiency / 100;

        ChargingStation station = route.getChargingStations().get(nextChargingStationIndex);
        if (distanceFromHighway >= station.getDistanceFromHighway()) {
            ChargingStation.Charger availableCharger = station.getAvailableCharger();
            if (availableCharger == null) {
                state = State.Waiting;
                route.getChargingStations().get(nextChargingStationIndex).add1ToLine();
            } else {
                availableCharger.setInUse(true);
                chargerOn = availableCharger;
                state = State.Charging;
            }
        }
    }

    public void driveToHighway() {
        double deltaDistance = drivingSpeed * (timestep/3600d);
        distanceFromHighway -= deltaDistance;
        batteryLife -= deltaDistance * carType.drivingEfficiency / 100;

        if (distanceFromHighway <= 0) {
            calculateNextChargingStationIndex();
            drivingSpeed = SPEED_ON_HIGHWAY;
            distanceFromHighway = 0;
            state = State.OnHighway;
        }
    }

    public void driveOnHighway() {
        double deltaDistance = drivingSpeed * (timestep/3600d);
        drivenDistance += deltaDistance;
        batteryLife -= deltaDistance * carType.drivingEfficiency / 100;


        if (nextChargingStationIndex == -1)
            calculateNextChargingStationIndex();

        if (route.getChargingStationDistances().get(nextChargingStationIndex) - drivenDistance < 0 && !canReachDestination) {
            state = State.OnWayToCharger;
            drivingSpeed = SPEED_OUTSIDE_HIGHWAY;
            distanceFromHighway = 0;
        }

        if (drivenDistance >= route.getLength()) {
            state = State.DestinationReached;
            drivenDistance = route.getLength();
        }
    }

    public double fullDistanceTo(int i) {
        return route.getChargingStationDistances().get(i) + route.getChargingStations().get(i).getDistanceFromHighway();
    }

    public void calculateNextChargingStationIndex() {
        if (route.getChargingStationDistances().size() <= nextChargingStationIndex + 1) {
            nextChargingStationIndex = 0;
            canReachDestination = true;
            return;
        }
        if (route.getChargingStationDistances().size() <= nextChargingStationIndex + 2 && fullDistanceTo(nextChargingStationIndex + 1) >= route.getLength()) {
            nextChargingStationIndex = 0;
            canReachDestination = true;
            return;
        }

        double threshHoldDistance = batteryLife / carType.drivingEfficiency * 100 * (1 - BATTERY_CHARGING_THRESHOLD) + drivenDistance;
        double maximumDistance = batteryLife / carType.drivingEfficiency * 100 * (1 - DESTINATION_BATTERY_THRESHOLD) + drivenDistance;

        canReachDestination = maximumDistance > route.getLength();
        if (canReachDestination) {
            nextChargingStationIndex = 0;
            return;
        }

        int lastChargingStationIndex = Math.max(nextChargingStationIndex, 0);
        int preferredStationIndex = 0;
        double mostPreferencePoints = 0;

        for (int i = lastChargingStationIndex; i < route.getChargingStations().size(); i++) {
            if (fullDistanceTo(i) > maximumDistance) {
                break;
            }
            double pfp = preferencePoints(i, threshHoldDistance);
            if (pfp > mostPreferencePoints){
                mostPreferencePoints = pfp;
                preferredStationIndex = i;
            }
        }

        nextChargingStationIndex = preferredStationIndex;

        if (lastChargingStationIndex >= nextChargingStationIndex ) {
            if (route.getChargingStationDistances().size() > nextChargingStationIndex + 1) {
                nextChargingStationIndex++;
            } else {
                nextChargingStationIndex = 0;
            }
            canReachDestination = true; // Can't reach destination, but will desparately try anyway
        }

    }

    public double preferencePoints(int i, double thresholdDistance) {
        double points = 0;

        double distanceFromThreshold = Math.abs(route.getChargingStationDistances().get(i) - thresholdDistance);
        double normalizedDFT = distanceFromThreshold / (thresholdDistance - drivenDistance);
        points += Math.pow(1 - normalizedDFT, 2) * DISTANCE_FROM_THRESHOLD_FACTOR;

        points -= route.getChargingStations().get(i).getLineLength() * LINE_LENGHT_FACTOR;

        return points;
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
    }

    public State getState() {
        return state;
    }

    public double getBatteryLife() {
        return batteryLife;
    }
}
