package simulation;

import java.util.ArrayList;
import java.util.Collections;

public class ChargingStation {

    private class Charger implements Comparable {
        private final int power;
        private boolean inUse;

        public Charger(int power_) {
            power = power_;
            inUse = false;
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
    private final ArrayList<Charger> chargers;

    public ChargingStation(double distance_, ArrayList<Integer> powers) {
        distance = distance_;
        chargers = new ArrayList<>();
        for (int power : powers) {
            chargers.add(new Charger(power));
        }
        Collections.sort(chargers);
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
