import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

public class GasStationSimulationApp {
    public static void main(String[] args) {

        int numServers = 5;
        int queueLength = 10;
        int numStates = numServers + queueLength + 1;
        int maxCars = 50000;
        double meanServiceTime = 0.5;

        int count = 100000;
        double samples[] = new double[count];
        double pdf[] = new double[count];
        Distribution distribution = new ErlangDistribution(); // Choose the desired distribution type here
        double mean = 1.0;
        double xMax = 10.0; // Set the maximum x value for the PDF here
        samples = distribution.getSamples(mean, count);
        pdf = distribution.getPDF(mean, xMax);
        GasStationSimulation.generateServiceTimeHistogram(samples, pdf);


        List<Double> arrivalRates = new ArrayList<>();
        List<Double> meanSystemTimes = new ArrayList<>();
        List<Double> meanQueueTimes = new ArrayList<>();
        List<Double> meanServiceTimes = new ArrayList<>();

        List<double[]> systemTimesConfidences = new ArrayList<>();
        List<double[]> queueTimesConfidences = new ArrayList<>();
        List<double[]> serviceTimesConfidences = new ArrayList<>();

        DistributionType distributionType=DistributionType.BETA;

        for (double arrivalRate = 0.5; arrivalRate <= 50; arrivalRate += 0.5) {
            GasStationSimulation gasStationSimulation = new GasStationSimulation(numServers, queueLength, numStates, maxCars, 1.0 / arrivalRate, meanServiceTime,distributionType);
            GasStation gasStation = new GasStation(numServers, queueLength, numStates, maxCars, 1.0 / arrivalRate, meanServiceTime,distributionType);
            gasStation.simulate();
            arrivalRates.add(arrivalRate);

            meanSystemTimes.add(gasStation.getMeanSystemTime());
            systemTimesConfidences.add(gasStation.getSystemTimeConfidence());

            meanQueueTimes.add(gasStation.getMeanQueueTime());
            queueTimesConfidences.add(gasStation.getQueueTimeConfidence());

            meanServiceTimes.add(gasStation.getMeanServiceTime());
            serviceTimesConfidences.add(gasStation.getServiceTimeConfidence());
        }

        GasStation gasStation = new GasStation(numServers, queueLength, numStates, maxCars, 0.0, meanServiceTime,distributionType);
        gasStation.drawGraph(arrivalRates, meanSystemTimes, systemTimesConfidences, meanQueueTimes, queueTimesConfidences, meanServiceTimes, serviceTimesConfidences);
    }
}
