import java.util.Random;

import static java.lang.Math.log;
public class GasStationSimulation {

    private int numServers;
    private int queueLength;
    private int numStates;
    private int maxCars;
    private double meanArrivalInterval;
    private double meanServiceTime;
    private DistributionType distributionType;


    public GasStationSimulation(int numServers, int queueLength, int numStates, int maxCars,
                                double meanArrivalInterval, double meanServiceTime,DistributionType distributionType) {
        this.numServers = numServers;
        this.queueLength = queueLength;
        this.numStates = numStates;
        this.maxCars = maxCars;
        this.meanArrivalInterval = meanArrivalInterval;
        this.meanServiceTime = meanServiceTime;
        this.distributionType=distributionType;
    }

    public static double exponentialDistribution(double mean) {
        Random random = new Random();
        return mean * -log(1 - random.nextDouble());
    }

    public static double erlangDistribution(double mean, int k) {
        Random random = new Random();
        double sample = exponentialDistribution(mean);
        for (int i = 1; i < k; i++) {
            sample += exponentialDistribution(mean);
        }
        return sample / k;
    }

    public static double geometricDistribution(double mean) {
        Random random = new Random();
        //return (int) Math.ceil(Math.log(1-random.nextDouble())/Math.log(1-p));
        return Math.round(exponentialDistribution(mean));// correct
        // return mean * Math.ceil(Math.log(1-random.nextDouble())/Math.log(1-0.999999));
        // return mean * ceil(log(1-random.nextDouble()) / log(1-mean));
    }
    public static double uniformDistribution(double min, double max) {
        Random random = new Random();
        return min + (max - min) * random.nextDouble();
    }
    public static double gammaDistribution(double shape, double scale) {
        Random random = new Random();
        double shapeFloor = Math.floor(shape);
        double fraction = shape - shapeFloor;
        double result = 0.0;
        for (int i = 0; i < shapeFloor; i++) {
            result += -Math.log(random.nextDouble());
        }
        if (fraction > 0) {
            result += -Math.log(random.nextDouble()) * fraction;
        }
        return result * scale;
    }

    public void runSimulation() {
        GasStation gasStation = new GasStation(numServers, queueLength, numStates, maxCars, meanArrivalInterval, meanServiceTime,distributionType);
        gasStation.simulate();
    }
}