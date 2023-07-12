import java.util.ArrayList;
import java.util.List;

public class GasStationSimulationApp {
    public static void main(String[] args) {
        int numServers = 5;
        int queueLength = 10;
        int numStates = numServers + queueLength + 1;
        int maxCars = 5000;
        double meanServiceTime = 0.5;

        List<Double> arrivalRates = new ArrayList<>();
        List<Double> meanSystemTimes = new ArrayList<>();
        List<Double> meanQueueTimes=new ArrayList<>();
        List<Double> meanServiceTimes=new ArrayList<>();

        List<double[]> systemTimesConfidences = new ArrayList<>();
        List<double[]> queueTimesConfidences = new ArrayList<>();
        List<double[]> serviceTimesConfidences = new ArrayList<>();

        for (double arrivalRate = 0.5; arrivalRate <= 25; arrivalRate += 0.5) {
            GasStationSimulation gasStationSimulation = new GasStationSimulation(numServers, queueLength, numStates, maxCars, 1.0/arrivalRate, meanServiceTime);
            GasStation gasStation = new GasStation(numServers, queueLength, numStates, maxCars, 1.0/arrivalRate, meanServiceTime);
            gasStation.simulate();
            arrivalRates.add(arrivalRate);

            meanSystemTimes.add(gasStation.getMeanSystemTime());
            systemTimesConfidences.add(gasStation.getSystemTimeConfidence());

            meanQueueTimes.add(gasStation.getMeanQueueTime());
            queueTimesConfidences.add(gasStation.getQueueTimeConfidence());

            meanServiceTimes.add(gasStation.getMeanServiceTime());
            serviceTimesConfidences.add(gasStation.getServiceTimeConfidence());
        }

        GasStation gasStation = new GasStation(numServers, queueLength, numStates, maxCars, 0.0, meanServiceTime);
        gasStation.drawGraph(arrivalRates, meanSystemTimes, systemTimesConfidences, meanQueueTimes, queueTimesConfidences, meanServiceTimes, serviceTimesConfidences);
    }
}
