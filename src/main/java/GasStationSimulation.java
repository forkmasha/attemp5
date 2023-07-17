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

    public void runSimulation() {
        GasStation gasStation = new GasStation(numServers, queueLength, numStates, maxCars, meanArrivalInterval, meanServiceTime,distributionType);
        gasStation.simulate();
    }
}