package simulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.LongStream;

public class Statistics {

    private class CarStatistics {

        public final long[] stateStatistics; // %
        public final long totalTime; // seconds
        public final double averageSpeed; // m/s

        CarStatistics(Car car) {
            totalTime = LongStream.of(car.getStateTime()).sum();
            stateStatistics = car.getStateTime();
            averageSpeed = car.getRoute().getLength() / (double) totalTime;
        }
    }

    private final int totalCars;
    private final int[] trafficStatistics; // count
    private final long[] stateStatistics; // seconds
    private final long totalTime; // seconds
    private ArrayList<CarStatistics> carStatistics;

    public Statistics (Simulation simulation) {
        trafficStatistics = new int[6];
        stateStatistics = new long[7];
        carStatistics = new ArrayList<>();
        totalTime = simulation.getPassedSeconds();

        ArrayList<Car> cars = simulation.getCars();
        totalCars = cars.size();
        for (Car car : cars) {
            carStatistics.add(new CarStatistics(car));

            // Traffic statistics
            Route route = car.getRoute();
            int startIndex = route.getStartPoint().index;
            int endIndex = route.getEndPoint().index;
            for (int i = startIndex; i < endIndex; i++) {
                trafficStatistics[i]++;
            }

            // State statistics
            for (int i = 0; i < car.getStateTime().length; i++) {
                stateStatistics[i] += car.getStateTime()[i];
            }
        }
    }

    public static void exportStatistics(String path) {
        // TODO: write this.toString() to a file at the given path
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Total cars: ").append(totalCars).append("\n");
        s.append("Total time: ").append(totalTime).append(" seconds\n\n");

        // State statistics
        long totalStateTime = LongStream.of(stateStatistics).sum() - stateStatistics[4];
        Car.State[] states = Car.State.values();
        s.append("Average state statistics:\n");
        for (int i = 0; i < states.length; i++) {
            if (i == 4)
                continue;
            s.append(" - ").append(states[i].toString()).append(String.format(": %.1f h/car (%.1f %%)", (double) stateStatistics[i] / (double) (totalCars*3600L), (double) stateStatistics[i] / (double) totalStateTime * 100d)).append("\n");
        }
        s.append(String.format("Total time driving: %.1f h/car\n", (double) totalStateTime / (double) (totalCars*3600L)));
        s.append("\n");

        // Traffic statistics
        int totalTraffic = Arrays.stream(trafficStatistics).sum();
        s.append("Traffic statistics:\n");
        s.append(" - HeLa: ").append(trafficStatistics[0]).append(String.format(" (%.1f %%)", (double) trafficStatistics[0] / (double) totalTraffic * 100d)).append("\n");
        s.append(" - LaJy: ").append(trafficStatistics[1]).append(String.format(" (%.1f %%)", (double) trafficStatistics[1] / (double) totalTraffic * 100d)).append("\n");
        s.append(" - JyOu: ").append(trafficStatistics[2]).append(String.format(" (%.1f %%)", (double) trafficStatistics[2] / (double) totalTraffic * 100d)).append("\n");
        s.append(" - OuKe: ").append(trafficStatistics[3]).append(String.format(" (%.1f %%)", (double) trafficStatistics[3] / (double) totalTraffic * 100d)).append("\n");
        s.append(" - KeRo: ").append(trafficStatistics[4]).append(String.format(" (%.1f %%)", (double) trafficStatistics[4] / (double) totalTraffic * 100d)).append("\n");
        s.append(" - RoUt: ").append(trafficStatistics[5]).append(String.format(" (%.1f %%)", (double) trafficStatistics[5] / (double) totalTraffic * 100d)).append("\n");

        return s.toString();
    }
}
