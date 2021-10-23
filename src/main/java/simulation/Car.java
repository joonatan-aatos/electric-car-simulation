package simulation;

public class Car {
    private Route route;
    private final CarType carType;

    private double batteryLife;
    private double drivenDistance;
    private double distanceOffHighway;
    private double drivingSpeed;
    private int nextChargingStationIndex;
    private final double THRESHOLD_PERCENTAGE = 0.3;
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
        drivingSpeed = 120;
        nextChargingStationIndex = -1;
        state = State.OnHighway;
        distanceOffHighway = 0;
        canReachDestination = false;
        chargerOn = null;
    }

    public void tick() {
        System.out.println(state + ", " + batteryLife);
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

    public void waitOnStation() {
        ChargingStation.Charger availableCharger = route.getChargingStations().get(nextChargingStationIndex).getAvailableCharger();
        if (availableCharger != null) {
            availableCharger.setInUse(true);
            state = State.Charging;
        }
    }

    public void charge() {
        batteryLife += Math.min(carType.chargingEfficiency, chargerOn.getPower()) / 60d;
        if (batteryLife >= carType.capacity) {
            batteryLife = carType.capacity;
            chargerOn.setInUse(false);
            chargerOn = null;
            state = State.OnWayFromCharger;
        }
    }

    public void driveToStation() {
        double deltaDistance = drivingSpeed / 60d;
        distanceOffHighway += deltaDistance;
        batteryLife -= deltaDistance * carType.drivingEfficiency / 100;

        ChargingStation station = route.getChargingStations().get(nextChargingStationIndex);
        if (distanceOffHighway >= station.getDistanceFromHighway()) {
            ChargingStation.Charger availableCharger = station.getAvailableCharger();
            if (availableCharger == null) {
                state = State.Waiting;
            } else {
                availableCharger.setInUse(true);
                chargerOn = availableCharger;
                state = State.Charging;
            }
        }
    }

    public void driveToHighway() {
        double deltaDistance = drivingSpeed / 60d;
        distanceOffHighway -= deltaDistance;
        batteryLife -= deltaDistance * carType.drivingEfficiency / 100;

        if (distanceOffHighway <= 0) {
            calculateNextChargingStationIndex();
            drivingSpeed = 120;
            distanceOffHighway = 0;
            state = State.OnHighway;
        }
    }

    public void driveOnHighway() {
        double deltaDistance = drivingSpeed / 60d;
        drivenDistance += deltaDistance;
        batteryLife -= deltaDistance * carType.drivingEfficiency / 100;


        if (nextChargingStationIndex == -1)
            calculateNextChargingStationIndex();

        if (route.getChargingStationDistances().get(nextChargingStationIndex) - drivenDistance < 0 && !canReachDestination) {
            state = State.OnWayToCharger;
            drivingSpeed = 40;
            distanceOffHighway = 0;
        }

        if (drivenDistance >= route.getLength()) {
            state = State.DestinationReached;
            drivenDistance = route.getLength();
        }
    }

    public void calculateNextChargingStationIndex() {
        double threshHoldDistance = batteryLife / carType.drivingEfficiency * 100 * (1 - THRESHOLD_PERCENTAGE) + drivenDistance;
        double maximumDistance = carType.capacity / carType.drivingEfficiency * 100 + drivenDistance;

        canReachDestination = maximumDistance > route.getLength();
        if (canReachDestination) {
            nextChargingStationIndex = 0;
            return;
        }

        int lastChargingStationIndex = Math.max(nextChargingStationIndex, 0);

        for (int i = lastChargingStationIndex; i < route.getChargingStations().size(); i++) {
            if (route.getChargingStationDistances().get(i) > threshHoldDistance) {
                nextChargingStationIndex =
                        (route.getChargingStationDistances().get(i) + route.getChargingStations().get(i).getDistanceFromHighway()
                                <= maximumDistance)?
                        i : i - 1;
                break;
            }
        }

        if (lastChargingStationIndex >= nextChargingStationIndex ) {
            nextChargingStationIndex = 0;
            canReachDestination = true; // Can't reach destination, but will desparately try anyway
        }

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
