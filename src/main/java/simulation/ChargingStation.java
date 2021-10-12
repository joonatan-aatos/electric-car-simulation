package simulation;

import java.util.ArrayList;
import java.util.Collections;

public class ChargingStation {

    private class Charger implements Comparable {
        private final int power;
        private boolean inUse;
        private ChargerType type;

        public Charger(int power_, ChargerType type_) {
            power = power_;
            inUse = false;
            type = type_;
        }

        public int getPower() {
            return power;
        }

        public boolean isInUse() {
            return inUse;
        }

        public void setInUse(boolean inUse_) {
            inUse = inUse_;
        }

        public ChargerType getType() { return type; }

        @Override
        public int compareTo(Object o) {
            Charger comparableCharger = null;
            try {
                comparableCharger = (Charger) o;
            } catch (ClassCastException e) {
                e.printStackTrace();
                System.out.println("Failed to compare Chargers");
                System.exit(1);
            }
            return comparableCharger.power-power;
        }

        @Override
        public String toString() {
            return "["+power+"W] ";
        }
    }

    private final double distance;
    private final double distanceFromHighway;
    private final ArrayList<Charger> chargers;
    private final boolean customerExclusive, hasShop, hasFood;

    public ChargingStation(double distance_, double distanceFromHighway_, ArrayList<Integer> powers, ArrayList<ChargerType> types, boolean[] info) {
        distance = distance_;
        distanceFromHighway = distanceFromHighway_;
        chargers = new ArrayList<>();
        customerExclusive = info[0];
        hasShop = info[1];
        hasFood = info[2];
        for (int i = 0; i < powers.size(); i++) {
            chargers.add(new Charger(powers.get(i), types.get(i)));
        }
        Collections.sort(chargers);
    }

    public void addCharger(int power, ChargerType type) {
        chargers.add(new Charger(power, type));
    }

    public Charger getAvailableCharger() {
        for (Charger charger : chargers) {
            if (!charger.isInUse())
                return charger;
        }
        return null;
    }

    public double getDistance() {
        return distance;
    }

    public double getDistanceFromHighway() {
        return distanceFromHighway;
    }

    public ArrayList<Charger> getChargers() {
        return chargers;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (Charger charger : chargers) {
            s.append(charger.toString());
        }
        return s.toString();
    }
}
