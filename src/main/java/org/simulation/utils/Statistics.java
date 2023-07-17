package org.simulation.utils;

import java.util.List;

import static org.simulation.utils.General.*;
import static org.simulation.utils.General.calculateConfidenceInterval;

public class Statistics {
    private int numberOfArrivedCars;
    private int numberOfDeparturedCars;
    private int numberOfBlockedCars;
    private List<Double> queueTimes;
    private List<Double> serviceTimes;
    private List<Double> systemTimes;
    private double[] systemTimeConfidence;
    private double[] queueTimeConfidence;
    private double[] serviceTimeConfidence;

    public Statistics(int numberOfArrivedCars, int numberOfDeparturedCars, int numberOfBlockedCars, List<Double> queueTimes, List<Double> serviceTimes, List<Double> systemTimes) {
        this.numberOfArrivedCars = numberOfArrivedCars;
        this.numberOfDeparturedCars = numberOfDeparturedCars;
        this.numberOfBlockedCars = numberOfBlockedCars;
        this.queueTimes = queueTimes;
        this.serviceTimes = serviceTimes;
        this.systemTimes = systemTimes;
    }

    public double[] getSystemTimeConfidence() {
        return systemTimeConfidence;
    }

    public double[] getQueueTimeConfidence() {
        return queueTimeConfidence;
    }

    public double[] getServiceTimeConfidence() {
        return serviceTimeConfidence;
    }
    public void generateStatistics() {
        // Виводимо статистику симуляції
        System.out.println("Number of Arrivals: " + numberOfArrivedCars);
        System.out.println("Number of Departures: " + numberOfDeparturedCars);
        System.out.println("Number of Blocked Cars: " + numberOfBlockedCars);
        System.out.println("=====================");
        System.out.println("Simulation Statistics");

        // Збираємо дані про часи перебування в системі та черзі

        double meanQtime = calculateMean(queueTimes);
        double meanCtime = calculateMean(serviceTimes);
        double meanStime = calculateMean(systemTimes);
        // double stdDevSystemTime = calculateStandardDeviation(systemTimeList);
        // double stdDevQueueTime = calculateStandardDeviation(queueTimeList);
        double stdDevQueueTime = calculateStandardDeviation(queueTimes);
        double stdDevServiceTime = calculateStandardDeviation(serviceTimes);
        double stdDevSystemTime = calculateStandardDeviation(systemTimes);

        System.out.println("-+-+-+-+-+-+-+-+-+-+-+-+");

        System.out.println("Mean Queue Time (" + queueTimes.size() + "): " + meanQtime);
        System.out.println("Mean Service Time (" + serviceTimes.size() + "): " + meanCtime);
        System.out.println("Mean System Time (" + systemTimes.size() + "): " + meanStime);
        double calcMeanStime = meanCtime + (meanQtime * queueTimes.size() / serviceTimes.size());
        System.out.println("Calculated Mean System Time: " + calcMeanStime);
        System.out.println("*********************");
        System.out.println("Standard Deviation of Queue Time: " + stdDevQueueTime);
        System.out.println("Standard Deviation of Service Time: " + stdDevServiceTime);
        System.out.println("Standard Deviation of System Time: " + stdDevSystemTime);

        System.out.println("#########################");

        int confidLevel = 95; // set confidence level in percent (int: 80, 90, 95, 98, 99)
        double[] qtimeConfidenceInterval = calculateConfidenceInterval(queueTimes, confidLevel);
        double[] ctimeConfidenceInterval = calculateConfidenceInterval(serviceTimes, confidLevel);
        double[] stimeConfidenceInterval = calculateConfidenceInterval(systemTimes, confidLevel);

        systemTimeConfidence = stimeConfidenceInterval;
        queueTimeConfidence = qtimeConfidenceInterval;
        serviceTimeConfidence = ctimeConfidenceInterval;

        System.out.println("Mean Queue Time: " + qtimeConfidenceInterval[0]);
        System.out.println("Confidence Interval (Qtime): " + qtimeConfidenceInterval[0] + " - " + qtimeConfidenceInterval[1]);

        System.out.println("Mean Service Time: " + ctimeConfidenceInterval[0]);
        System.out.println("Confidence Interval (Ctime): " + ctimeConfidenceInterval[0] + " - " + ctimeConfidenceInterval[1]);

        System.out.println("Mean System Time: " + stimeConfidenceInterval[0]);
        System.out.println("Confidence Interval (Stime): " + stimeConfidenceInterval[0] + " - " + stimeConfidenceInterval[1]);

        //drawGraph(arrivalRateList, meanSystemTimeList);
    }

}
