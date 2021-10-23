package simulation;

public class Car {
    private Route route;
    private final CarType carType;

    private int batteryLife;
    private double drivenDistance;
    private double drivingSpeed;
    private boolean hasReachedDestination;

    public Car(CarType carType_) {
        carType = carType_;

        batteryLife = carType.capacity;
        drivenDistance = 0;
        drivingSpeed = 120;
        hasReachedDestination = false;
    }

    public void tick() {
        double deltaDistance = drivingSpeed / 60d;
        drivenDistance += deltaDistance;
        batteryLife -= deltaDistance * carType.drivingEfficiency;

        if (drivenDistance >= route.getLength())
            hasReachedDestination = true;
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

    public boolean isHasReachedDestination() {
        return hasReachedDestination;
    }

    public int getBatteryLife() {
        return batteryLife;
    }
}
