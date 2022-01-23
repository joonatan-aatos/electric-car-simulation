package simulation;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
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

    private class CarModelStatistics {
        public ArrayList<Integer> timesCharged;
        public ArrayList<Double> routeLengths;
        public final long[] totalStateTimes;
        public long totalTime;
        public int amount;

        public CarModelStatistics() {
            timesCharged = new ArrayList<>();
            routeLengths = new ArrayList<>();
            amount = 0;
            totalTime = 0;
            totalStateTimes = new long[Car.State.values().length];
            Arrays.fill(totalStateTimes, 0);
        }
    }

    private interface CarModelCallback {
        String run(CarModelStatistics carModelStatistics);
    }
    private interface CarCallback {
        String run(Car car);
    }

    private final String[] stateNames = new String[]{"Valtatiellä", "Matkalla valtatielle", "Matkalla valtatieltä", "Matkalla laturille", "Matkalla laturilta", "Odottamassa", "Latautumassa", "Akku loppunut", "Perillä"};

    private final int totalCars;
    private final int standardDeviation;
    private final int[] trafficStatistics; // count
    private final long[][] stateStatistics; // seconds
    private final long totalTime; // seconds
    private ArrayList<CarStatistics> carStatistics;
    private ArrayList<int[][]> stateStatisticsOverTime;
    private ArrayList<int[]> globalStateStatisticsOverTime;
    private ArrayList<int[]> roadStatisticsOverTime;
    private ArrayList<int[]> waitingStatisticsOverTime;


    private final long timeStep;

    private final ArrayList<Car> cars;
    private final HashMap<CarType, CarModelStatistics> carModelStatistics;
    private final HashMap<String, CarModelCallback> carModelRunnableCallbacks;
    private final ArrayList<AbstractMap.SimpleEntry<String, CarCallback>> carRunnableCallbacks;

    public Statistics (Simulation simulation) {
        trafficStatistics = new int[6];
        stateStatistics = new long[9][2];
        carStatistics = new ArrayList<>();
        totalTime = simulation.getPassedSeconds();
        timeStep = simulation.getTimeStep();
        stateStatisticsOverTime = simulation.getStateStatisticsOverTime();
        globalStateStatisticsOverTime = simulation.getGlobalStateStatisticsOverTime();
        roadStatisticsOverTime = simulation.getRoadStatisticsOverTime();
        waitingStatisticsOverTime = simulation.getWaitingStatisticsOverTime();

        cars = simulation.getCars();
        Collections.sort(cars);
        totalCars = cars.size();
        standardDeviation = simulation.getStandardDeviation();

        carModelStatistics = new HashMap<>();
        for (CarType carType : CarType.values()) {
            carModelStatistics.put(carType, new CarModelStatistics());
        }

        int[][] stateTimesArray = new int[9][simulation.getTotalCars()];  // Array for median
        for (int index = 0; index < cars.size(); index++) {                       // Car Loop
            Car car = cars.get(index);

            carStatistics.add(new CarStatistics(car));

            // Traffic statistics
            Route route = car.getRoute();
            int startIndex = Math.min(route.getStartPoint().index, route.getEndPoint().index);
            int endIndex = Math.max(route.getStartPoint().index, route.getEndPoint().index);
            for (int i = startIndex; i < endIndex; i++) {
                trafficStatistics[i]++;
            }

            // State statistics (sum)
            for (int i = 0; i < car.getStateTime().length; i++) {
                stateStatistics[i][0] += car.getStateTime()[i];
            }

            // State statistics (median)
            for (int i = 0; i < car.getStateTime().length; i++) {
                 stateTimesArray[i][index] += car.getStateTime()[i];
            }

            //  Model statistics
            carModelStatistics.get(car.getCarType()).amount++;
            carModelStatistics.get(car.getCarType()).routeLengths.add(car.getRoute().getLength());
            carModelStatistics.get(car.getCarType()).timesCharged.add(car.getTimesCharged());
            for (int i = 0; i < car.getStateTime().length; i++) {
                if (i == Car.State.DestinationReached.index || i == Car.State.BatteryDepleted.index)
                    continue;
                carModelStatistics.get(car.getCarType()).totalTime += car.getStateTime()[i];
            }
            for (int i = 0; i < car.getStateTime().length; i++) {
                carModelStatistics.get(car.getCarType()).totalStateTimes[i] += car.getStateTime()[i];
            }
        }

        // Sorting for median
        for (int i = 0; i < 9; i++) {
            Arrays.sort(stateTimesArray[i]);
            stateStatistics[i][1] = (stateTimesArray[i].length%2==0)? stateTimesArray[i][cars.size()/2] + stateTimesArray[i][cars.size()/2 - 1] / 2 : stateTimesArray[i][(int) (cars.size()/2f)];
        }

        carModelRunnableCallbacks = new HashMap<>();
        carRunnableCallbacks = new ArrayList<>();
        //  Functions to be run for each car
        carModelRunnableCallbacks.put("Latauskertojen määrä (Latauksien määrä/ 100km)", (CarModelStatistics carModel) -> {
            int sum = 0;
            int i = 0;
            while (i < carModel.timesCharged.size()) {
                sum += carModel.timesCharged.get(i)/carModel.routeLengths.get(i)*100;
                i++;
            }
            return String.format("%.2f", sum/(double)i);
        });
        carModelRunnableCallbacks.put("Kokonaisaika (min/auto)", (CarModelStatistics carModel) -> String.format("%.2f",
                carModel.totalTime / (double) (carModel.amount*60)
        ));
        carModelRunnableCallbacks.put("Aika laturilla (min/auto)", (CarModelStatistics carModel) -> String.format("%.2f",
                carModel.totalStateTimes[Car.State.Charging.index] / (double) (carModel.amount*60)
        ));
        carModelRunnableCallbacks.put("Aika odottamassa (min/auto)", (CarModelStatistics carModel) -> String.format("%.2f",
                carModel.totalStateTimes[Car.State.Waiting.index] / (double) (carModel.amount*60)
        ));
        carModelRunnableCallbacks.put("Aika valtatiellä (min/auto)", (CarModelStatistics carModel) -> String.format("%.2f",
                carModel.totalStateTimes[Car.State.OnHighway.index] / (double) (carModel.amount*60)
        ));


        for (int i = 0; i < Car.State.values().length; i++) {
            int index = i;
            carRunnableCallbacks.add(new AbstractMap.SimpleEntry<>(stateNames[i] + " (min)", (Car car) -> String.format("%.2f",
                    car.getStateTime()[index]/60f
            )));
        }
        carRunnableCallbacks.add(new AbstractMap.SimpleEntry<>("Kokonaisaika (min)", (Car car) -> String.format("%.2f", LongStream.of(car.getStateTime()).sum()/60f)));
        carRunnableCallbacks.add(new AbstractMap.SimpleEntry<>("Reitin Pituus (km)", (Car car) -> String.format("%.2f", car.getRoute().getLength())));
        carRunnableCallbacks.add(new AbstractMap.SimpleEntry<>("Lähtöaika (min)", (Car car) -> String.format("%.2f", car.getCreationTime()/60f)));

    }

    public void export(String simulationName) {
        PrintWriter statisticsPrintWriter = null;
        PrintWriter carModelStatisticsPrintWriter = null;
        PrintWriter carStatisticsPrintWriter = null;
        try {
            statisticsPrintWriter = new PrintWriter(String.format("output/%s-statistics.csv", simulationName), StandardCharsets.UTF_8);
            carModelStatisticsPrintWriter = new PrintWriter(String.format("output/%s-car_model_statistics.csv", simulationName), StandardCharsets.UTF_8);
            carStatisticsPrintWriter = new PrintWriter(String.format("output/%s-car_statistics.csv", simulationName), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            logger.severe("Failed to export statistics");
            return;
        }

        statisticsPrintWriter.println(this.statisticsToCSV());
        carModelStatisticsPrintWriter.println(this.carModelStatisticsToCSV());
        carStatisticsPrintWriter.println(this.carStatisticsToCSV());

        statisticsPrintWriter.close();
        carModelStatisticsPrintWriter.close();
        carStatisticsPrintWriter.close();
    }

    public String carModelStatisticsToCSV() {
        StringBuilder s = new StringBuilder();
        s.append("Malli;Määrä;Akun koko (kWh);Max AC teho (kW);Max DC teho (kW);Ajotehokkuus (kWh/100km)");
        for (String key : carModelRunnableCallbacks.keySet()) {
            s.append(";").append(key);
        }
        s.append("\n");

        for (CarType carType : CarType.values()) {
            s.append(String.format("%s;%d;%.2f;%.2f;%.2f;%.2f", carType.name(), carModelStatistics.get(carType).amount, carType.getCapacity(), carType.getMaxChargingPowerAC(), carType.getMaxChargingPowerDC(), carType.getDrivingEfficiency()));
            for (String key : carModelRunnableCallbacks.keySet()) {
                s.append(";").append(carModelRunnableCallbacks.get(key).run(carModelStatistics.get(carType)));
            }
            s.append("\n");
        }

        return s.toString();
    }
    public String carStatisticsToCSV() {
        StringBuilder s = new StringBuilder();
        s.append("Indeksi;Malli");
        for (AbstractMap.SimpleEntry<String, CarCallback> pair : carRunnableCallbacks) {
            s.append(";").append(pair.getKey());
        }
        s.append(";Akun koko (kWh);Max AC teho (kW);Max DC teho (kW);Ajotehokkuus (kWh/100km)");
        s.append("\n");

        for (Car car : cars) {
            s.append(String.format("%d;%s", car.getIndex(), car.getCarType().name()));
            for (AbstractMap.SimpleEntry<String, CarCallback> pair : carRunnableCallbacks) {
                s.append(";").append(pair.getValue().run(car));
            }
            s.append(String.format(";%.2f;%.2f;%.2f;%.2f",
                    car.getCarType().getCapacity(), car.getCarType().getMaxChargingPowerAC(), car.getCarType().getMaxChargingPowerDC(), car.getCarType().getDrivingEfficiency()
            ));
            s.append("\n");
        }

        return s.toString();
    }

    public String statisticsToCSV() {
        StringBuilder s = new StringBuilder();
        s.append("Autojen lukumäärä: ;").append(totalCars).append("\n");
        s.append("Keskihajonta: ;").append(standardDeviation).append("\n");
        s.append("Kulunut aika (s): ;").append(totalTime).append("\n\n");

        // State statistics
        long stateStatisticsSum = 0;
        for (int i = 0; i < 9; i++) {
            if (i == Car.State.DestinationReached.index || i == Car.State.BatteryDepleted.index)
                continue;
            stateStatisticsSum += stateStatistics[i][0];
        }
        long totalStateTime = stateStatisticsSum;
        Car.State[] states = Car.State.values();
        s.append("Keskimääräinen auton tila:\n");

        s.append("Tila;Aika (min/auto);Prosenttiosuus;Mediaaniaika (min)\n");
        for (int i = 0; i < states.length; i++) {
            if (i == Car.State.DestinationReached.index || i == Car.State.BatteryDepleted.index)
                continue;
            s.append(states[i].toString()).append(";")
                    .append((double) stateStatistics[i][0] / (double) (totalCars * 60L)).append(";")
                    .append((double) stateStatistics[i][0] / (double) totalStateTime * 100d).append(";")
                    .append((double) stateStatistics[i][1]/60d).append("\n");
        }
        s.append("\n\n");
        s.append(String.format("Ajamiseen kulunut aika; %.0f; min/auto\n", (double) totalStateTime / (double) (totalCars*60L)));


        // Traffic statistics
        int totalTraffic = Arrays.stream(trafficStatistics).sum();
        s.append("\nLiikennetilastot;Autojen määrä;Autojen määrä (%);\n");
        s.append("HeLa;").append(trafficStatistics[0]).append(String.format(";%.1f", (double) trafficStatistics[0] / (double) totalTraffic * 100d)).append("\n");
        s.append("LaJy;").append(trafficStatistics[1]).append(String.format(";%.1f", (double) trafficStatistics[1] / (double) totalTraffic * 100d)).append("\n");
        s.append("JyOu;").append(trafficStatistics[2]).append(String.format(";%.1f", (double) trafficStatistics[2] / (double) totalTraffic * 100d)).append("\n");
        s.append("OuKe;").append(trafficStatistics[3]).append(String.format(";%.1f", (double) trafficStatistics[3] / (double) totalTraffic * 100d)).append("\n");
        s.append("KeRo;").append(trafficStatistics[4]).append(String.format(";%.1f", (double) trafficStatistics[4] / (double) totalTraffic * 100d)).append("\n");
        s.append("RoUt;").append(trafficStatistics[5]).append(String.format(";%.1f", (double) trafficStatistics[5] / (double) totalTraffic * 100d)).append("\n");
        s.append("\n\n");


        // State stats over time
        s.append("Autojen tila joka ajanhetkeltä:\n");
        s.append("Aika (min);Valtatiellä;Matkalla valtatielle;Matkalla valtatieltä;Matkalla laturille;Matkalla laturilta;Odottamassa;Latautumassa;Akku loppunut;Perillä;Yhteensä;;Tiellä (HeLa);Tiellä (LaJy);Tiellä (JyOu);Tiellä (OuKe);Tiellä (KeRo);Tiellä (RoUt);Odottamassa (HeLa);Odottamassa (LaJy);Odottamassa (JyOu);Odottamassa (OuKe);Odottamassa (KeRo);Odottamassa (RoUt);\n");
        for (int i = 0; i < globalStateStatisticsOverTime.size(); i++) {
            s.append((double) i/6d);
            int sum = 0;
            for (int carCount : globalStateStatisticsOverTime.get(i)) {
                s.append(";").append(carCount);
                sum += carCount;
            }
            s.append(";").append(sum).append(";");
            for (int carCount : roadStatisticsOverTime.get(i)) {
                s.append(";").append(carCount);
            }
            for (int carCount : waitingStatisticsOverTime.get(i)) {
                s.append(";").append(carCount);
            }
            s.append("\n");
        }

        return s.toString();
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Total cars: ").append(totalCars).append("\n");
        s.append("Total time: ").append(totalTime).append(" seconds\n\n");

        // State statistics
        long stateStatisticsSum = 0;
        for (int i = 0; i < 9; i++) {
            stateStatisticsSum += stateStatistics[i][0];
        }
        long totalStateTime = stateStatisticsSum - stateStatistics[4][0];
        Car.State[] states = Car.State.values();
        s.append("Average state statistics:\n");
        for (int i = 0; i < states.length; i++) {
            if (i == 4)
                continue;
            s.append(" - ").append(states[i].toString()).append(String.format(": %.0f min/car (%.1f %%)", (double) stateStatistics[i][0] / (double) (totalCars*60L), (double) stateStatistics[i][0] / (double) totalStateTime * 100d)).append("\n");
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
