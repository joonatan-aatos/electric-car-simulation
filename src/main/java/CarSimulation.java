import simulation.Routes;
import simulation.Simulation;
import simulation.Statistics;
import util.CustomFormatter;
import visualizer.Visualizer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.logging.*;

public class CarSimulation {

    private static final Logger logger = Logger.getGlobal();

    private static final int REPEAT_COUNT = 5;
    private static final int MAX_CAR_COUNT = 2000;
    private static final int MIN_CAR_COUNT = 2000;
    private static final int CAR_COUNT_CHANGE = 1;

    private static final int THREAD_COUNT = 8;
    private static final ArrayList<Thread> threads = new ArrayList<>();
    private static final ArrayList<Simulation> simulations = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {
        boolean showUI = false;
        File dir = new File("./output");
        dir.mkdirs();
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

                    simulations.add(new Simulation(String.format("r%d-%d", i, carCount), routes, carCount, 3600, 14400, false));
                }
            }

            for (int i = 0; i < THREAD_COUNT; i++) {
                Thread thread = new Thread(new SimulationThread());
                thread.start();
                threads.add(thread);
            }
        }

        for (Thread thread : threads) {
            thread.join();
        }

        logger.info("Exiting Car Simulation...");
    }

    private static class SimulationThread implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    if (simulations.size() <= 0)
                        return;
                    Simulation simulation = simulations.remove(0);
                    simulation.start();
                    // Export simulation statistics
                    Statistics statistics = new Statistics(simulation);
                    //statistics.export("statistics.csv");
                } catch (ConcurrentModificationException | NullPointerException | ArrayIndexOutOfBoundsException e) {
                    logger.warning("Exception caught: "+e.getLocalizedMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private static void configureLogger() {

        final Logger logger = Logger.getGlobal();
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);

        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.INFO);
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
