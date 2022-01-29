import simulation.CarType;
import simulation.Routes;
import simulation.Simulation;
import simulation.Statistics;
import util.CustomFormatter;
import visualizer.Visualizer;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.*;

public class CarSimulation {

    private static final Logger logger = Logger.getGlobal();

    private static final int REPEAT_COUNT = 30;
    private static final int MAX_CHARGING_POWER_COEFFICIENT = 200;
    private static final int MIN_CHARGING_POWER_COEFFICIENT = 100;
    private static final int CHARGING_POWER_COEFFICIENT_CHANGE = 10;
    private static final int MAX_CHARGER_AMOUNT_COEFFICIENT = 200;
    private static final int MIN_CHARGER_AMOUNT_COEFFICIENT = 100;
    private static final int CHARGER_AMOUNT_COEFFICIENT_CHANGE = 10;
    private static final int MAX_DRIVING_EFFICIENCY_COEFFICIENT = 200;
    private static final int MIN_DRIVING_EFFICIENCY_COEFFICIENT = 100;
    private static final int DRIVING_EFFICIENCY_COEFFICIENT_CHANGE = 10;
    private static final int MAX_CAR_COUNT = 1000;
    private static final int MIN_CAR_COUNT = 1000;
    private static final int CAR_COUNT_CHANGE = 1;
    private static final int MIN_STANDARD_DEVIATION = 21600;
    private static final int MAX_STANDARD_DEVIATION = 21600;
    private static final int STANDARD_DEVIATION_CHANGE = 1;
    private static final boolean isWinter = false;
    private static boolean showUI = false;

    private static int simulationsRan = 0;
    private static long simulationStartTime;
    private static long simulationEndTime;
    private static int simulationCount;

    private static final Random random = new Random();

    public static void main(String[] args) throws InterruptedException {
        if (isWinter) {
            for (CarType carType : CarType.values()) {
                carType.itIsWinter();
            }
        }
        File dir = new File("./output");
        if (!dir.mkdirs()) {
            purgeDirectory(dir);
        }
        configureLogger();

        simulationCount =
                ((MAX_CAR_COUNT-MIN_CAR_COUNT)/CAR_COUNT_CHANGE+1)*
                ((MAX_STANDARD_DEVIATION-MIN_STANDARD_DEVIATION)/STANDARD_DEVIATION_CHANGE+1)*
                ((MAX_CHARGING_POWER_COEFFICIENT-MIN_CHARGING_POWER_COEFFICIENT)/CHARGING_POWER_COEFFICIENT_CHANGE+1)*
                ((MAX_CHARGER_AMOUNT_COEFFICIENT-MIN_CHARGER_AMOUNT_COEFFICIENT)/CHARGER_AMOUNT_COEFFICIENT_CHANGE+1)*
                ((MAX_DRIVING_EFFICIENCY_COEFFICIENT-MIN_DRIVING_EFFICIENCY_COEFFICIENT)/DRIVING_EFFICIENCY_COEFFICIENT_CHANGE+1)*
                REPEAT_COUNT;
        simulationStartTime = System.currentTimeMillis();

        if (showUI) {
            Routes routes = new Routes(random.nextLong(), 1);
            routes.generateRoutes();

            Simulation simulation = new Simulation("visualized", routes, MAX_CAR_COUNT, 3600, 14400, true, false, 1, 1);
            Visualizer visualizer = showUI ? new Visualizer(simulation, routes) : null;
            Thread simulationThread = new Thread(simulation);
            simulationThread.start();

            while (simulationThread.isAlive()) {
                visualizer.draw();
                Thread.sleep(10);
            }
            visualizer.draw();

            Statistics statistics = new Statistics(simulation);
            statistics.export(simulation.getName());
        }
        else {
            for (int carCount = MIN_CAR_COUNT; carCount <= MAX_CAR_COUNT; carCount += CAR_COUNT_CHANGE) {
                for (int standardDeviation = MIN_STANDARD_DEVIATION; standardDeviation <= MAX_STANDARD_DEVIATION; standardDeviation += STANDARD_DEVIATION_CHANGE) {
                    for (int chargingPowerCoefficient = MIN_CHARGING_POWER_COEFFICIENT; chargingPowerCoefficient <= MAX_CHARGING_POWER_COEFFICIENT; chargingPowerCoefficient += CHARGING_POWER_COEFFICIENT_CHANGE) {
                        for (int chargerAmountCoefficient = MIN_CHARGER_AMOUNT_COEFFICIENT; chargerAmountCoefficient <= MAX_CHARGER_AMOUNT_COEFFICIENT; chargerAmountCoefficient += CHARGER_AMOUNT_COEFFICIENT_CHANGE) {
                            for (int drivingEfficiencyCoefficient = MIN_DRIVING_EFFICIENCY_COEFFICIENT; drivingEfficiencyCoefficient <= MAX_DRIVING_EFFICIENCY_COEFFICIENT; drivingEfficiencyCoefficient += DRIVING_EFFICIENCY_COEFFICIENT_CHANGE) {
                                for (int i = 0; i < REPEAT_COUNT; i++) {
                                    Routes routes = new Routes(random.nextLong(), (double) chargerAmountCoefficient / 100d);
                                    routes.generateRoutes();
                                    Simulation simulation = new Simulation(String.format("r%d-c%d-s%d-p%d-e%d-a%d-%s", i + 1, carCount, standardDeviation, chargingPowerCoefficient, drivingEfficiencyCoefficient, chargerAmountCoefficient, isWinter ? "w" : "s"), routes, carCount, standardDeviation, 4 * standardDeviation, false, isWinter, (double) drivingEfficiencyCoefficient / 100d, (double) chargingPowerCoefficient / 100d);
                                    simulation.start();
                                    simulationsRan++;
                                    // Export simulation statistics
                                    Statistics statistics = new Statistics(simulation);
                                    statistics.export(simulation.getName());
                                    printState((double) simulationsRan / simulationCount);
                                }
                            }
                        }
                    }
                }
            }
        }

        simulationEndTime = System.currentTimeMillis();
        long totalTime = simulationEndTime - simulationStartTime;

        System.out.printf("\nRan %d simulations in %d minutes and %d seconds.\n", simulationCount, (int) ((totalTime / (1000*60)) % 60), (int) (totalTime / 1000) % 60);
        logger.info(String.format("Ran %d simulations in %d minutes and %d seconds.", simulationCount, (int) ((totalTime / (1000*60)) % 60), (int) (totalTime / 1000) % 60));
    }

    private static void purgeDirectory(File dir) {
        for (File file: Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory())
                purgeDirectory(file);
            file.delete();
        }
    }

    private static void printState(double progress) {
        int barLength = 50;
        StringBuilder s = new StringBuilder();
        s.append("\rProgress: [");
        for (int i = 0; i < barLength; i++) {
            if (i <= progress*barLength)
                s.append("|");
            else
                s.append(" ");
        }
        s.append(String.format("] %.1f%%   ", progress*100));

        if (simulationsRan != 0) {
            /*
            double simulatingRate = (double) simulationsRan / (System.currentTimeMillis() - simulationStartTime);
            double timeLeft = (simulationCount - simulationsRan) / simulatingRate;
            if (timeLeft > 60000)
                s.append(String.format("(%d minutes and %d seconds left)", (int) ((timeLeft / (1000*60)) % 60), (int) (timeLeft / 1000) % 60));
            else
                s.append(String.format("(%d seconds left)", (int) (timeLeft / 1000) % 60));
             */
            s.append(String.format("(%d simulations remaining)", simulationCount-simulationsRan));
        }

        System.out.print(s);
    }

    private static void configureLogger() {

        final Logger logger = Logger.getGlobal();
        logger.setLevel(Level.WARNING);
        logger.setUseParentHandlers(false);

        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.WARNING);
        consoleHandler.setFormatter(new CustomFormatter());

        Handler fileHandler = null;
        try {
            fileHandler = new FileHandler("output/logs.txt");
        } catch (IOException e) {
            System.out.println("Failed to create file handler for logger");
        }
        fileHandler.setLevel(Level.WARNING);

        logger.addHandler(consoleHandler);
        logger.addHandler(fileHandler);
    }
}
