import simulation.Simulation;
import visualizer.Visualizer;

public class CarSimulation {
    public static void main(String[] args) throws InterruptedException {
        Simulation simulation = new Simulation();
        Visualizer visualizer = new Visualizer(simulation);
        Thread simulationThread = new Thread(simulation);
        simulationThread.start();
        while (simulationThread.isAlive()) {
            visualizer.draw();
            Thread.sleep(10);
        }
    }
}
