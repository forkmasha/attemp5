public class GasStationSimulationApp {
    public static void main(String[] args) {
        int numServers = 5;
        int queueLength = 10;
        int numStates = numServers + queueLength + 1;
        int maxCars = 500;
        double meanArrivalInterval = 0.1;
        double meanServiceTime = 1.5;

        GasStationSimulation gasStationSimulation = new GasStationSimulation(numServers, queueLength, numStates, maxCars, meanArrivalInterval, meanServiceTime);
        gasStationSimulation.runSimulation();
    }
}