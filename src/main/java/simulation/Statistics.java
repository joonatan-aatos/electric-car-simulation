package simulation;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;
import java.util.stream.LongStream;

public class Statistics {

    private static final Logger logger = Logger.getGlobal();

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
        stateStatistics = new long[9];
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

    public void export(String name) {
        PrintWriter printWriter = null;
        try {
            printWriter = new PrintWriter("output/"+name, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            logger.severe("Failed to export statistics");
            return;
        }

        printWriter.println(this.toCSV());

        printWriter.close();
    }

    public String toCSV() {
        StringBuilder s = new StringBuilder();
        s.append("Total cars: ;").append(totalCars).append("\n");
        s.append("Total time (s): ;").append(totalTime).append("\n\n");

        // State statistics
        long totalStateTime = LongStream.of(stateStatistics).sum() - stateStatistics[4];
        Car.State[] states = Car.State.values();
        s.append("Average state statistics:\n");

        s.append("State;Time (min/car);Percentage\n");
        for (int i = 0; i < states.length; i++) {
            if (i == 4)
                continue;
            s.append(states[i].toString()).append(";").append((double) stateStatistics[i] / (double) (totalCars*60L) + ";").append((double) stateStatistics[i] / (double) totalStateTime * 100d + "").append("\n");
        }
        s.append("\n\n");
        s.append(String.format("Total time driving; %.0f; min/car\n", (double) totalStateTime / (double) (totalCars*60L)));


        // Traffic statistics
        int totalTraffic = Arrays.stream(trafficStatistics).sum();
        s.append("\nTraffic statics:\n");
        s.append("HeLa;").append(trafficStatistics[0]).append(String.format(";%.1f", (double) trafficStatistics[0] / (double) totalTraffic * 100d)).append("\n");
        s.append("LaJy;").append(trafficStatistics[1]).append(String.format(";%.1f", (double) trafficStatistics[1] / (double) totalTraffic * 100d)).append("\n");
        s.append("JyOu;").append(trafficStatistics[2]).append(String.format(";%.1f", (double) trafficStatistics[2] / (double) totalTraffic * 100d)).append("\n");
        s.append("OuKe;").append(trafficStatistics[3]).append(String.format(";%.1f", (double) trafficStatistics[3] / (double) totalTraffic * 100d)).append("\n");
        s.append("KeRo;").append(trafficStatistics[4]).append(String.format(";%.1f", (double) trafficStatistics[4] / (double) totalTraffic * 100d)).append("\n");
        s.append("RoUt;").append(trafficStatistics[5]).append(String.format(";%.1f", (double) trafficStatistics[5] / (double) totalTraffic * 100d)).append("\n");
        s.append("\n");
        return s.toString();
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
            s.append(" - ").append(states[i].toString()).append(String.format(": %.0f min/car (%.1f %%)", (double) stateStatistics[i] / (double) (totalCars*60L), (double) stateStatistics[i] / (double) totalStateTime * 100d)).append("\n");
        }
        s.append(String.format("Total time driving: %.0f min/car\n", (double) totalStateTime / (double) (totalCars*60L)));
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
