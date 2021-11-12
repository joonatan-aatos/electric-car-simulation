import simulation.Simulation;
import visualizer.Visualizer;

import java.io.IOException;
import java.util.logging.*;

public class CarSimulation {
    public static void main(String[] args) throws InterruptedException {
        configureLogger();
        Simulation simulation = new Simulation();
        Visualizer visualizer = new Visualizer(simulation);
        Thread simulationThread = new Thread(simulation);
        simulationThread.start();
        while (simulationThread.isAlive()) {
            visualizer.draw();
            Thread.sleep(10);
        }
    }

    private static void configureLogger() {

        final Logger logger = Logger.getGlobal();
        logger.setLevel(Level.ALL);

        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.CONFIG);

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
