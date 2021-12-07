import simulation.Routes;
import simulation.Simulation;
import simulation.Statistics;
import util.CustomFormatter;
import visualizer.Visualizer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Objects;
import java.util.logging.*;

public class CarSimulation {

    private static final Logger logger = Logger.getGlobal();

    private static final int REPEAT_COUNT = 10;
    private static final int MAX_CAR_COUNT = 2000;
    private static final int MIN_CAR_COUNT = 100;
    private static final int CAR_COUNT_CHANGE = 1;
    private static boolean showUI = false;
    private static boolean EXPORT_FILES = false;

    private static final int THREAD_COUNT = 8;
    private static volatile int simulationsRan = 0;
    private static final ArrayList<Thread> threads = new ArrayList<>();
    private static final ArrayList<Simulation> simulations = new ArrayList<>();
    private static long simulationStartTime;
    private static long simulationEndTime;
    private static int simulationCount;

    public static void main(String[] args) throws InterruptedException {
        boolean showUI = false;
        File dir = new File("./output");
        if (!dir.mkdirs()) {
            purgeDirectory(dir);
        }
        configureLogger();

        if (showUI) {
            Routes routes = new Routes();
            routes.generateRoutes();

            Simulation simulation = new Simulation("visualized", routes, 1000, 3600, 14400, true);
            Visualizer visualizer = showUI ? new Visualizer(simulation, routes) : null;
            Thread simulationThread = new Thread(simulation);
            simulationThread.start();

            while (simulationThread.isAlive()) {
                visualizer.draw();
                Thread.sleep(10);
            }
            visualizer.draw();
        }
        else {
            for (int carCount = MIN_CAR_COUNT; carCount <= MAX_CAR_COUNT; carCount += CAR_COUNT_CHANGE) {
                for (int i = 0; i < REPEAT_COUNT; i++) {
                    Routes routes = new Routes();
                    routes.generateRoutes();
                    simulations.add(new Simulation(String.format("r%d-c%d", i+1, carCount), routes, carCount, 3600, 14400, false));
                }
            }
            simulationCount = simulations.size();
            System.out.println("Total simulation count: " + simulationCount);
            printState();
            simulationStartTime = System.currentTimeMillis();

            for (int i = 0; i < THREAD_COUNT; i++) {
                Thread thread = new Thread(new SimulationThread(simulations.subList(i*simulationCount/THREAD_COUNT, (i+1)*simulationCount/THREAD_COUNT)));
                threads.add(thread);
                thread.start();
            }
            Thread.sleep(100);
            simulations.clear();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        simulationEndTime = System.currentTimeMillis();
        long totalTime = simulationEndTime - simulationStartTime;

        System.out.printf("\nProcess finished in %d minutes and %d seconds.\n", (int) ((totalTime / (1000*60)) % 60), (int) (totalTime / 1000) % 60);
        logger.info(String.format("Ran %d simulations in %d minutes and %d seconds.", simulationCount, (int) ((totalTime / (1000*60)) % 60), (int) (totalTime / 1000) % 60));
    }

    private static class SimulationThread implements Runnable {

        private final ThreadLocal<List<Simulation>> localSimulations;
        public SimulationThread (List<Simulation> simulations_) {
            localSimulations = ThreadLocal.withInitial(() -> new ArrayList<>(simulations_));
        }

        @Override
        public void run() {
            while (localSimulations.get().size() > 0) {
                try {
                    Simulation simulation = localSimulations.get().remove(0);
                    simulation.start();
                    simulationsRan++;
                    // Export simulation statistics
                    Statistics statistics = new Statistics(simulation);
                    statistics.export(String.format("%s-statistics.csv", simulation.getName()));
                    printState();
                } catch (ConcurrentModificationException | NullPointerException | ArrayIndexOutOfBoundsException e) {
                    logger.warning("Exception caught: "+e.getLocalizedMessage());
                }
            }
        }
    }

    private static void purgeDirectory(File dir) {
        for (File file: Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory())
                purgeDirectory(file);
            file.delete();
        }
    }

    private static void printState() {
        double progress = (double) simulationsRan/simulationCount;
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
            double simulatingRate = (double) simulationsRan / (System.currentTimeMillis() - simulationStartTime);
            double timeLeft = (simulationCount - simulationsRan) / simulatingRate;
            if (timeLeft > 60000)
                s.append(String.format("(%d minutes and %d seconds left)", (int) ((timeLeft / (1000*60)) % 60), (int) (timeLeft / 1000) % 60));
            else
                s.append(String.format("(%d seconds left)", (int) (timeLeft / 1000) % 60));
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
        fileHandler.setLevel(Level.INFO);

        logger.addHandler(consoleHandler);
        logger.addHandler(fileHandler);
    }
}
