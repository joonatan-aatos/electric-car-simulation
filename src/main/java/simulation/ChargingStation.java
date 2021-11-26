package simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChargingStation {

    public enum ChargerType {
        Type2,
        CCS,
        CCS_HPC,
        Tesla,
        CHAdeMO,
        Tyomaapistoke
    }

    public class Charger implements Comparable {
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
    // Chargers are sorted by charging power
    private final ArrayList<Charger> chargers;
    private final boolean hasShop, hasFood, customerExclusive;
    private final String name;
    private final ArrayList<Integer> queue;

    public ChargingStation(String name_, double distance_, double distanceFromHighway_, boolean[] amenities) {
        name = name_;
        distance = distance_;
        distanceFromHighway = distanceFromHighway_;
        hasShop = amenities[0];
        hasFood = amenities[1];
        customerExclusive = amenities[2];
        chargers = new ArrayList<>();
        queue = new ArrayList<>();
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

    public int getQueueLength() {
        return queue.size();
    }

    public int getNextInQueue() {
        try {
            if (queue.size() > 0) {
                Integer i = queue.get(0);
                if (i == null) {
                    System.out.println("bruh");
                    int i2 = queue.get(0);
                    System.out.println(i2);
                }
                i.toString();
            }
            return queue.size() > 0 ? queue.get(0) : -1;
        } catch (NullPointerException e) {
            e.printStackTrace();
            System.exit(-1);
            return -1;
        }
    }

    public void addToQueue(int carIndex) {
        queue.add(carIndex);
    }

    public void removeFromQueue(int carIndex) {
        queue.remove(Integer.valueOf(carIndex));
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

    public boolean isHasShop() {
        return hasShop;
    }

    public boolean isHasFood() {
        return hasFood;
    }

    public boolean isCustomerExclusive() {
        return customerExclusive;
    }

    public void sortChargers() {
        Collections.sort(chargers);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(name).append(": ");
        for (Charger charger : chargers) {
            s.append(charger.toString());
        }
        return s.toString();
    }

    public ChargingStation createNew() {
        ChargingStation chargingStation = new ChargingStation(name, distance, distanceFromHighway, new boolean[] {hasShop, hasFood, customerExclusive});
        chargers.forEach(charger -> chargingStation.addCharger(charger.getPower(), charger.getType()));
        return chargingStation;
    }
}
