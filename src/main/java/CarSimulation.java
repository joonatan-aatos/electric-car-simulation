import simulation.Simulation;
import simulation.Statistics;
import util.CustomFormatter;
import visualizer.Visualizer;

import java.io.IOException;
import java.util.logging.*;

public class CarSimulation {
    public static void main(String[] args) throws InterruptedException {
        boolean showUI = false;
        configureLogger();
        Simulation simulation = new Simulation(showUI);
        Visualizer visualizer = showUI ? new Visualizer(simulation) : null;
        Thread simulationThread = new Thread(simulation);
        simulationThread.start();
        if (showUI) {
            while (simulationThread.isAlive()) {
                visualizer.draw();
                Thread.sleep(10);
            }
            visualizer.draw();
        }
        else
            simulationThread.join();

        // Export simulation statistics
        Statistics statistics = new Statistics(simulation);
        System.out.println(statistics);
    }

    private static void configureLogger() {

        final Logger logger = Logger.getGlobal();
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);

        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.CONFIG);
        consoleHandler.setFormatter(new CustomFormatter());

        Handler fileHandler = null;
        try {
            fileHandler = new FileHandler("logs.txt");
        } catch (IOException e) {
            System.out.println("Failed to create file handler for logger");
        }
        fileHandler.setLevel(Level.ALL);

        logger.addHandler(consoleHandler);
        logger.addHandler(fileHandler);
    }
}
