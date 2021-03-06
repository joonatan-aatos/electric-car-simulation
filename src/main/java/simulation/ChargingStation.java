package simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChargingStation {

    public enum ChargerType {
        Type2,
        CCS,
        Tesla,
        CHAdeMO,
        Tyomaapistoke
    }

    public class Charger implements Comparable {
        private final double power;
        private boolean inUse;
        private ChargerType type;

        public Charger(double power_, ChargerType type_) {
            power = power_;
            inUse = false;
            type = type_;
        }

        public double getPower() {
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
            return (int) Math.round(comparableCharger.power-power);
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
    private final ThreadLocal<List<Integer>> queue;

    public ChargingStation(String name_, double distance_, double distanceFromHighway_, boolean[] amenities) {
        name = name_;
        distance = distance_;
        distanceFromHighway = distanceFromHighway_;
        hasShop = amenities[0];
        hasFood = amenities[1];
        customerExclusive = amenities[2];
        chargers = new ArrayList<>();
        queue = ThreadLocal.withInitial(ArrayList::new);
    }

    public void addCharger(double power, ChargerType type) {
        chargers.add(new Charger(power, type));
    }

    public Charger getAvailableCharger(ArrayList<ChargerType> chargerTypes) {
        for (Charger charger : chargers) {
            if (chargerTypes.contains(charger.getType()) && !charger.isInUse())
                return charger;
        }
        return null;
    }

    public int getQueueLength() {
        return queue.get().size();
    }

    public int getNextInQueue() {
        return queue.get().size() > 0 ? queue.get().get(0) : -1;
    }

    public void addToQueue(int carIndex) {
        queue.get().add(carIndex);
    }

    public void removeFromQueue(int carIndex) {
        queue.get().remove(Integer.valueOf(carIndex));
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

    public boolean hasChargerType(ArrayList<ChargerType> chargerTypes) {
        for (Charger charger : chargers) {
            if (chargerTypes.contains(charger.getType()))
                return true;
        }
        return false;
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
