import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

import javax.swing.*;
import java.util.Random;

import static java.lang.Math.log;
public class GasStationSimulation {
    //public static void main(String[] args) {
       // int count = 100000;
       // double samples[]=new double[count];
        //double pdf[]=new double[count];
        //for (int i=0;i<count;i++){
            //samples[i]=exponentialDistribution(1.0);
            //samples[i]=uniformDistribution(0,2);
           // samples[i]=erlangDistribution(1,2);
            //samples[i]=geometricDistribution(1);
     //  }
       // pdf=calculateExponentialPD(1.0);
        //generateServiceTimeHistogram(samples,pdf);
    //}
    public static void generateServiceTimeHistogram(double samples[],double curve[]) {

        HistogramDataset dataset = new HistogramDataset();
        dataset.setType(HistogramType.FREQUENCY);
        dataset.addSeries("Histogram", samples, 100); // 10 is the number of bins
        JFreeChart chart = ChartFactory.createHistogram("Distribution", "Values", "Frequency", dataset, PlotOrientation.VERTICAL, true, true, false);

        chart.getPlot().setForegroundAlpha(0.6f); // Adjust transparency (0.0f - fully transparent, 1.0f - fully opaque)
        chart.getPlot().setBackgroundPaint(ChartColor.WHITE); // Set background color
        chart.getXYPlot().getRenderer().setSeriesPaint(0, new ChartColor(0, 122, 255)); // Exponential - Blue


        // Create a chart panel and display the chart in a frame
        ChartPanel chartPanel = new ChartPanel(chart);
        JFrame frame = new JFrame("Histogram");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(chartPanel);
        frame.pack();
        frame.setVisible(true);
    }
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


    public void runSimulation() {
        GasStation gasStation = new GasStation(numServers, queueLength, numStates, maxCars, meanArrivalInterval, meanServiceTime,distributionType);
        gasStation.simulate();
    }
}