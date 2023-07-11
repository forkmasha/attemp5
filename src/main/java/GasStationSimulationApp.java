import java.util.ArrayList;
import java.util.List;

public class GasStationSimulationApp {
    public static void main(String[] args) {
        int numServers = 5;
        int queueLength = 10;
        int numStates = numServers + queueLength + 1;
        int maxCars = 500;
        double meanServiceTime = 0.5;

        List<Double> arrivalRates = new ArrayList<>();
        List<Double> meanSystemTimes = new ArrayList<>();

        for (double meanArrivalInterval = 2.001; meanArrivalInterval >= 0.001; meanArrivalInterval -= 0.2) {
            GasStationSimulation gasStationSimulation = new GasStationSimulation(numServers, queueLength, numStates, maxCars, meanArrivalInterval, meanServiceTime);
            GasStation gasStation = new GasStation(numServers, queueLength, numStates, maxCars, meanArrivalInterval, meanServiceTime);
            gasStation.simulate();
            arrivalRates.add(gasStation.getMeanArrivalInterval());
            meanSystemTimes.add(gasStation.getMeanSystemTime());
        }

        GasStation gasStation = new GasStation(numServers, queueLength, numStates, maxCars, 0.0, meanServiceTime);
       // gasStation.drawGraph(arrivalRates, meanSystemTimes);
    }

}