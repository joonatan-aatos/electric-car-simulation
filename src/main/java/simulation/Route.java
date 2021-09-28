package simulation;

import java.util.ArrayList;

public class Route {
    private double length;
    private ArrayList<ChargingStation> chargingStations;
    private EndPoint startPoint;
    private EndPoint endPoint;

    public Route(double length_, ArrayList<ChargingStation> chargingStations_, EndPoint startPoint_, EndPoint endPoint_) {
        length = length_;
        chargingStations = chargingStations_;
        startPoint = startPoint_;
        endPoint = endPoint_;
    }

    public double getLength() {
        return length;
    }

    public ArrayList<ChargingStation> getChargingStations() {
        return chargingStations;
    }

    public EndPoint getStartPoint() {
        return startPoint;
    }

    public EndPoint getEndPoint() {
        return endPoint;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (ChargingStation chargingStation : chargingStations) {
            s.append(chargingStation.toString());
            s.append("\n");
        }
        return s.toString();
    }
}
