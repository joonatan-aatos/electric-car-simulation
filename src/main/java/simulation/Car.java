package simulation;

import java.util.ArrayList;
import java.util.Collections;

public class Car {
    private boolean isElectric;
    private Route route;
    private int batteryCapacity;
    private int batteryLife;

    public Car(boolean isElectric_, EndPoint start, int batteryCapacity_) {
        isElectric = isElectric_;
        batteryCapacity = batteryCapacity_;
        batteryLife = batteryCapacity_;
        route = Route.generateRandomRoute();
    }
}
