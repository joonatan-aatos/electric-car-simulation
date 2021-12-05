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
    private ArrayList<int[][]> stateStatisticsOverTime;
    private ArrayList<int[]> globalStateStatisticsOverTime;
    private final long timeStep;

    public Statistics (Simulation simulation) {
        trafficStatistics = new int[6];
        stateStatistics = new long[9];
        carStatistics = new ArrayList<>();
        totalTime = simulation.getPassedSeconds();
        timeStep = simulation.getTimeStep();
        stateStatisticsOverTime = simulation.getStateStatisticsOverTime();
        globalStateStatisticsOverTime = simulation.getGlobalStateStatisticsOverTime();

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
        s.append("Autojen lukumäärä: ;").append(totalCars).append("\n");
        s.append("Kulunut aika (s): ;").append(totalTime).append("\n\n");

        // State statistics
        long totalStateTime = LongStream.of(stateStatistics).sum() - stateStatistics[4];
        Car.State[] states = Car.State.values();
        s.append("Keskimääräinen auton tila:\n");

        s.append("Tila;Aika (min/auto);Prosenttiosuus\n");
        for (int i = 0; i < states.length; i++) {
            if (i == 4)
                continue;
            s.append(states[i].toString()).append(";").append((double) stateStatistics[i] / (double) (totalCars*60L) + ";").append((double) stateStatistics[i] / (double) totalStateTime * 100d + "").append("\n");
        }
        s.append("\n\n");
        s.append(String.format("Ajamiseen kulunut aika; %.0f; min/auto\n", (double) totalStateTime / (double) (totalCars*60L)));


        // Traffic statistics
        int totalTraffic = Arrays.stream(trafficStatistics).sum();
        s.append("\nLiikennetilastot:\n");
        s.append("HeLa;").append(trafficStatistics[0]).append(String.format(";%.1f", (double) trafficStatistics[0] / (double) totalTraffic * 100d)).append("\n");
        s.append("LaJy;").append(trafficStatistics[1]).append(String.format(";%.1f", (double) trafficStatistics[1] / (double) totalTraffic * 100d)).append("\n");
        s.append("JyOu;").append(trafficStatistics[2]).append(String.format(";%.1f", (double) trafficStatistics[2] / (double) totalTraffic * 100d)).append("\n");
        s.append("OuKe;").append(trafficStatistics[3]).append(String.format(";%.1f", (double) trafficStatistics[3] / (double) totalTraffic * 100d)).append("\n");
        s.append("KeRo;").append(trafficStatistics[4]).append(String.format(";%.1f", (double) trafficStatistics[4] / (double) totalTraffic * 100d)).append("\n");
        s.append("RoUt;").append(trafficStatistics[5]).append(String.format(";%.1f", (double) trafficStatistics[5] / (double) totalTraffic * 100d)).append("\n");
        s.append("\n\n");


        // State stats over time
        s.append("Autojen tila joka ajanhetkeltä:\n");
        s.append("Aika (min);Valtatiellä;Matkalla valtatielle;Matkalla valtatieltä;Matkalla laturille;Matkalla laturilta;Latautumassa;Odottamassa;Akku loppunut;Perillä;Yhteensä\n");
        for (int i = 0; i < globalStateStatisticsOverTime.size(); i+=30) {
            s.append(i/6);
            int sum = 0;
            for (int carCount : globalStateStatisticsOverTime.get(i)) {
                s.append(";").append(carCount);
                sum += carCount;
            }
            s.append(";").append(sum).append("\n");
        }

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
