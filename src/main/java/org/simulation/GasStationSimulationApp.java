package org.simulation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.simulation.entity.DistributionType;
import org.simulation.entity.GasStation;
import org.simulation.graph.Graph;
import org.simulation.utils.PropertiesReader;

import java.util.ArrayList;
import java.util.List;

public class GasStationSimulationApp {
    protected static final PropertiesReader repo = new PropertiesReader("src/main/resources/Base.properties");
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int NUMBER_OF_SERVERS = Integer.parseInt(repo.getBy("number.of.servers"));
    private static final int LENGTH_OF_QUEUE = Integer.parseInt(repo.getBy("length.of.queue"));
    private static final int NUM_STATES = NUMBER_OF_SERVERS + LENGTH_OF_QUEUE + 1;
    private static final int MAX_COUNT_OF_CARS = Integer.parseInt(repo.getBy("max.count.of.cars"));
    private static final double MIN_SERVICE_TIME = Double.parseDouble(repo.getBy("mean.service.time"));
    private final List<Double> arrivalRates = new ArrayList<>();
    private final List<Double> meanSystemTimes = new ArrayList<>();
    private final List<Double> meanQueueTimes = new ArrayList<>();
    private final List<Double> meanServiceTimes = new ArrayList<>();

    private final List<double[]> systemTimesConfidences = new ArrayList<>();
    private final List<double[]> queueTimesConfidences = new ArrayList<>();
    private final List<double[]> serviceTimesConfidences = new ArrayList<>();

    private DistributionType distributionType= DistributionType.GEOMETRIC;

    final Graph graph = new Graph();

    void process() {
        for (double arrivalRate = 0.5; arrivalRate <= 50; arrivalRate += 0.5) {
            GasStation gasStation = new GasStation(NUMBER_OF_SERVERS, LENGTH_OF_QUEUE, NUM_STATES, MAX_COUNT_OF_CARS, 1.0 / arrivalRate, MIN_SERVICE_TIME, distributionType);
            gasStation.simulate();
            arrivalRates.add(arrivalRate);

            meanSystemTimes.add(gasStation.getMeanSystemTime());
            systemTimesConfidences.add(gasStation.getStatistics().getSystemTimeConfidence());

            meanQueueTimes.add(gasStation.getMeanQueueTime());
            queueTimesConfidences.add(gasStation.getStatistics().getQueueTimeConfidence());

            meanServiceTimes.add(gasStation.getMeanServiceTime());
            serviceTimesConfidences.add(gasStation.getStatistics().getServiceTimeConfidence());
        }
    }

    public static void main(String[] args) {
        LOGGER.info("Starting");
        GasStationSimulationApp app = new GasStationSimulationApp();
        app.process();
        app.graph.drawGraph(app.arrivalRates,
                app.meanSystemTimes,
                app.systemTimesConfidences,
                app.meanQueueTimes,
                app.queueTimesConfidences,
                app.meanServiceTimes,
                app.serviceTimesConfidences);
        LOGGER.warn("Warning as a test");
        LOGGER.error("Error as a test");
        LOGGER.info("Finishing");
    }
}
