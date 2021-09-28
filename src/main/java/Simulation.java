public class Simulation {

    private Simulation() {

    }

    public void start() {
        System.out.println("Starting...");
    }

    public static void main(String[] args) {
        Simulation simulation = new Simulation();
        simulation.start();
    }
}
