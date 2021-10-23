package simulation;

public class Car {
    private final Route route;
    private final CarType carType;

    private int batteryLife;
    private double drivenDistance;

    public Car(CarType carType_) {
        carType = carType_;
        route = Route.generateRandomRoute();

        batteryLife = carType.capacity;
        drivenDistance = 0;
    }

    public void tick() {

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

    public int getBatteryLife() {
        return batteryLife;
    }
}
