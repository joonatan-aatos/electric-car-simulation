package simulation;

public class Car {
    private final Route route;
    private final int batteryCapacity;
    private final int batteryLife;
    private final CarType carType;

    public Car(CarType carType_, EndPoint start, int batteryCapacity_) {
        carType = carType_;
        batteryCapacity = batteryCapacity_;
        batteryLife = batteryCapacity_;
        route = Route.generateRandomRoute();
    }

    public CarType getCarType() {
        return carType;
    }

    public Route getRoute() {
        return route;
    }

    public int getBatteryCapacity() {
        return batteryCapacity;
    }

    public int getBatteryLife() {
        return batteryLife;
    }
}
