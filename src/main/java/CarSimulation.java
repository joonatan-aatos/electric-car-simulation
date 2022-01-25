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

    private static final double CHARGING_POWER_COEFFICIENT = 1;
    private static final double DRIVING_EFFICIENCY_COEFFICIENT = 1;
    private static final int REPEAT_COUNT = 3;
    private static final int MAX_CAR_COUNT = 1000;
    private static final int MIN_CAR_COUNT = 100;
    private static final int CAR_COUNT_CHANGE = 100;
    private static final int MIN_STANDARD_DEVIATION = 3600;
    private static final int MAX_STANDARD_DEVIATION = 3600;
    private static final int STANDARD_DEVIATION_CHANGE = 900;
    private static boolean showUI = false;

    private static int simulationsRan = 0;
    private static long simulationStartTime;
    private static long simulationEndTime;
    private static int simulationCount;

    private static final Random random = new Random();

    public static void main(String[] args) throws InterruptedException {
        File dir = new File("./output");
        if (!dir.mkdirs()) {
            purgeDirectory(dir);
        }
        configureLogger();

        simulationCount = ((MAX_CAR_COUNT-MIN_CAR_COUNT)/CAR_COUNT_CHANGE+1)*REPEAT_COUNT;
        simulationStartTime = System.currentTimeMillis();

        if (showUI) {
            Routes routes = new Routes(random.nextLong(), CHARGING_POWER_COEFFICIENT);
            routes.generateRoutes();

            Simulation simulation = new Simulation("visualized", routes, MAX_CAR_COUNT, 3600, 14400, true, false, DRIVING_EFFICIENCY_COEFFICIENT);
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
                    for (int i = 0; i < REPEAT_COUNT; i++) {
                        Routes routes = new Routes(random.nextLong(), CHARGING_POWER_COEFFICIENT);
                        routes.generateRoutes();
                        Simulation simulation = new Simulation(String.format("r%d-c%d-s%d", i+1, carCount, standardDeviation), routes, carCount, standardDeviation, 4 * standardDeviation, false, false, DRIVING_EFFICIENCY_COEFFICIENT);
                        simulation.start();
                        simulationsRan++;
                        // Export simulation statistics
                        Statistics statistics = new Statistics(simulation);
                        statistics.export(simulation.getName());
                        printState((double) simulationsRan/simulationCount);
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
        logger.setLevel(Level.ALL);
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
        fileHandler.setLevel(Level.ALL);

        logger.addHandler(consoleHandler);
        logger.addHandler(fileHandler);
    }
}
