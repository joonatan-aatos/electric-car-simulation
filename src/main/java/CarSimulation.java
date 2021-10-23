import simulation.Simulation;
import visualizer.Visualizer;

public class CarSimulation {
    public static void main(String[] args) throws Exception {
        Simulation simulation = new Simulation();
        Visualizer visualizer = new Visualizer();
        simulation.start();
        visualizer.draw(simulation);
        System.out.println("Done.");
    }
}
