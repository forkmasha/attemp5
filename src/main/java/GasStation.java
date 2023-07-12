import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;


import javax.swing.*;

public class GasStation {
    private int numServers;
    private int queueLength;
    private int numStates;
    private int maxCars;
    private double meanArrivalInterval;
    private double meanServiceTime;

    private double totalCarsInSystem;
    private double totalCarsInQueue;

    private List<Double> systemTimes;
    private List<Double> queueTimes;
    private List<Double> serviceTimes;
    //private List<double[]> systemTimesConfidences;
    private List<Double> systemTimeList;
    private List<Double> queueTimeList;

    private EventStack eventStack;
    private List<Car> servicedCars;
    private int k;
    private int j;
    private int i;
    private double time;
    private double previousTime;
    private double[] systemTimeConfidence;

    private double[]queueTimesConfidence;
    private double[]serviceTimeConfidence;
    private List<Double> queueStartTimes;

    private List<Double> arrivalRateList;
    private List<Double> meanSystemTimeList;

    public double[] getSystemTimeConfidence() {
        return systemTimeConfidence;
    }

    public double[] getQueueTimeConfidence() {
        return systemTimeConfidence;
    }

    public double[] getServiceTimeConfidence() {
        return serviceTimeConfidence;
    }
    /* public List<double[]> getSystemTimesConfidences() {
        return systemTimesConfidences;
    }
    */

    public GasStation(int numServers, int queueLength, int numStates, int maxCars, double meanArrivalInterval, double meanServiceTime) {
        this.numServers = numServers;
        this.queueLength = queueLength;
        this.numStates = numStates;
        this.maxCars = maxCars;
        this.meanArrivalInterval = meanArrivalInterval;
        this.meanServiceTime = meanServiceTime;

        queueStartTimes = new ArrayList<>();
        systemTimeList = new ArrayList<>();

        systemTimes = new ArrayList<>();
        queueTimes = new ArrayList<>();
        serviceTimes = new ArrayList<>();
        //systemTimesConfidences = new ArrayList<>();

        queueTimeList = new ArrayList<>();

        arrivalRateList = new ArrayList<>();
        //meanSystemTimeList = new ArrayList<>();

        eventStack = new EventStack();
        servicedCars = new ArrayList<>();
        k = 0;
        j = 0;
        i = 0;
        time = 0.0;
        previousTime = 0.0;
    }

    public void simulate() {

        System.out.println("\n#########################");
        System.out.println("Simulation Parameters");
        System.out.println("#########################");
        System.out.println("MeanInterArrivalTime " + meanArrivalInterval);
        System.out.println("MeanServiceTime " + meanServiceTime);
        System.out.println("Number of servers " + numServers);
        System.out.println("Queue length " + queueLength);
        k = 0;
        j = 0;
        i = 0;
        time = 0.0;
        previousTime = 0.0;

        servicedCars.clear();
        queueStartTimes.clear();

        systemTimeList.clear();
        queueTimeList.clear();

        systemTimes.clear();
        queueTimes.clear();
        serviceTimes.clear();

        arrivalRateList.clear();
        //meanSystemTimeList.clear();

        eventStack.events.clear();
        eventStack.addEvent(new ArrivalEvent(0.0));   // insert initial arrival

        // Головний цикл моделювання
        while (!eventStack.isEmpty()) {
            // Отримуємо наступну подію зі стеку
            Event event = eventStack.getNextEvent();
            double eventTime = event.getTime();
            // Оновлюємо час моделювання та розраховуємо різницю з попереднім часом
            time = eventTime;
            double deltaTime = time - previousTime;

            // Оновлюємо часи перебування в системі та черзі для автомобілів
            updateCarTimes(deltaTime);
            // Обробляємо подію залежно від її типу
            if (event instanceof ArrivalEvent) {
                processArrivalEvent(eventTime);
            } else if (event instanceof DepartureEvent) {
                processDepartureEvent(eventTime);
            }
            // Оновлюємо попередній час
            previousTime = time;
        }
        // Генеруємо статистику симуляції
        generateStatistics();
    }

    private void updateCarTimes(double deltaTime) {
        for (Car car : servicedCars) {
            car.tinSys += deltaTime;
            // car.tinQueue += deltaTime;
        }

        totalCarsInSystem += servicedCars.size() * deltaTime;
        totalCarsInQueue += Math.max(0, servicedCars.size() - numServers) * deltaTime;

        // Зберігаємо часи перебування в системі та черзі для обліку середніх значень
        /* if (!servicedCars.isEmpty()) {
            // double lastCarTime = servicedCars.get(servicedCars.size() - 1).tinSys;
            // double queueTime = Math.max(0, lastCarTime - servicedCars.get(0).tinSys);
            //systemTimes.add(lastCarTime);
            // queueTimes.add(queueTime);

            // Зберігаємо час перебування в системі для кожного автомобіля
            for (Car car : servicedCars) {
                systemTimeList.add(car.tinSys);
            }
            for (Car car : servicedCars) {
                queueTimeList.add(car.tinQueue);
            }
        } */
    }

    private double calculateStandardDeviation(List<Double> values) {
        double mean = calculateMean(values);
        double sumSquaredDifferences = 0.0;
        for (Double value : values) {
            double difference = value - mean;
            sumSquaredDifferences += difference * difference;
        }
        double variance = sumSquaredDifferences / values.size();
        return Math.sqrt(variance);
    }

    private void processArrivalEvent(double eventTime) {
        if (i < maxCars) {
            double nextArrivalTime = eventTime + exponentialDistribution(meanArrivalInterval);
            eventStack.addEvent(new ArrivalEvent(nextArrivalTime));
        } else {
            return; // Повертаємося, якщо кількість автомобілів досягає максимального значення
        }
        // Збільшуємо лічильник обслужених автомобілі
        i++;
        if (numServers > servicedCars.size()) {   // directly enter servive
            double serviceTime = exponentialDistribution(meanServiceTime);
            eventStack.addEvent(new DepartureEvent(eventTime + serviceTime));
            servicedCars.add(new Car(eventTime, 0.0, serviceTime));
        } else if (queueStartTimes.size() < queueLength) {   // enter waiting queue
            queueStartTimes.add(eventTime);
            // Зберігаємо час початку очікування в черзі для автомобіля

        } else {  // arrival is blocked (deflected)
            k++;
        }
        arrivalRateList.add(1.0 / meanArrivalInterval); // Calculate arrival rate from mean arrival interval
        //meanSystemTimeList.add(calculateMean(systemTimeList));   // ?
    }

    private void processDepartureEvent(double eventTime) {
        // Збільшуємо лічильник обслуженихавтомобілів

        if (!servicedCars.isEmpty()) {
            Car departingCar = servicedCars.get(0);
            // systemTimes.add(departingCar.tinSys);
            // double carArrivalTime = departingCar.arrivalTime;
            systemTimes.add(departingCar.getTinQueue() + departingCar.getServiceTime());
            serviceTimes.add(departingCar.getServiceTime());
            servicedCars.remove(0); // Видаляємо перший автомобіль з черги
            j++;
        } else {
            System.out.println("Error: Departure from empty System!");
        }

        // Якщо стан системи перевищує кількість серверів, додаємо подію відправлення для автомобіля з черги до стеку подій та записуємо час обслуговування черги
        if (!queueStartTimes.isEmpty()) {
            double serviceTime = exponentialDistribution(meanServiceTime);
            eventStack.addEvent(new DepartureEvent(eventTime + serviceTime));
            // if (!queueStartTimes.isEmpty()) {
            // Додаємо час очікування в черзі до списку
            double queueTime = eventTime - queueStartTimes.get(0);
            queueTimes.add(queueTime);
            queueStartTimes.remove(0); // Видаляємо перший час початку очікування з черги
            servicedCars.add(new Car(eventTime, queueTime, serviceTime));
            // }
        }
    }

    private void generateStatistics() {
        // Виводимо статистику симуляції
        System.out.println("Number of Arrivals: " + i);
        System.out.println("Number of Departures: " + j);
        System.out.println("Number of Blocked Cars: " + k);
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

        double[] qtimeConfidenceInterval = calculateConfidenceInterval(queueTimes);
        double[] ctimeConfidenceInterval = calculateConfidenceInterval(serviceTimes);
        double[] stimeConfidenceInterval = calculateConfidenceInterval(systemTimes);
        systemTimeConfidence = stimeConfidenceInterval;
        queueTimesConfidence = qtimeConfidenceInterval;
        serviceTimeConfidence = ctimeConfidenceInterval;

        System.out.println("Mean Queue Time: " + qtimeConfidenceInterval[0]);
        System.out.println("Confidence Interval (Qtime): " + qtimeConfidenceInterval[0] + " - " + qtimeConfidenceInterval[1]);

        System.out.println("Mean Service Time: " + ctimeConfidenceInterval[0]);
        System.out.println("Confidence Interval (Ctime): " + ctimeConfidenceInterval[0] + " - " + ctimeConfidenceInterval[1]);

        System.out.println("Mean System Time: " + stimeConfidenceInterval[0]);
        System.out.println("Confidence Interval (Stime): " + stimeConfidenceInterval[0] + " - " + stimeConfidenceInterval[1]);

        //drawGraph(arrivalRateList, meanSystemTimeList);
    }

    private double calculateMean(List<Double> values) {
        double sum = 0.0;
        for (Double value : values) {
            sum += value;
        }
        return sum / values.size();
    }

    private double exponentialDistribution(double mean) {
        Random random = new Random();
        return -mean * Math.log(1 - random.nextDouble());
    }

    private double[] calculateConfidenceInterval(List<Double> values) {
        double mean = calculateMean(values);
        double stdDev = calculateStandardDeviation(values);

        // Calculate the confidence interval
        double zScore = 1.96; // For a 95% confidence interval (assuming a large enough sample size)
        double marginOfError = zScore * (stdDev / Math.sqrt(values.size()));
        double lowerBound = mean - marginOfError;
        double upperBound = mean + marginOfError;

        return new double[]{lowerBound, upperBound};
    }

    public List<Double> getArrivalRateList() {
        return arrivalRateList;
    }

    public List<Double> getMeanSystemTimeList() {
        return meanSystemTimeList;
    }

    public void setMeanArrivalInterval(double meanArrivalInterval) {
        this.meanArrivalInterval = meanArrivalInterval;
    }

    public double getMeanArrivalInterval() {
        return meanArrivalInterval;
    }

    public double getMeanSystemTime() {
        double sum = 0.0;
        int i = 0;
        for (Double time : systemTimes) {
            sum += time;
            i++;
        }
        // sum /= systemTimes.size();
        sum /= i;
        return sum;
    }

    public double getMeanQueueTime() {
        double sum = 0.0;
        int i = 0;
        for (Double time : queueTimes) {
            sum += time;
            i++;
        }
        // sum /= systemTimes.size();
        sum /= i;
        return sum;
    }
    public double getMeanServiceTime() {
        double sum = 0.0;
        int i = 0;
        for (Double time : serviceTimes) {
            sum += time;
            i++;
        }
        // sum /= systemTimes.size();
        sum /= i;
        return sum;
    }


    void drawGraph(List<Double> xValues, List<Double> meanValues, List<double[]> confidenceInterval, List<Double> meanValues2, List<double[]> confidenceInterval2,
                   List<Double> meanValues3, List<double[]> confidenceInterval3) {
        DefaultXYDataset dataset = new DefaultXYDataset();

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Mean System Time vs. Arrival Rate", // Chart title
                "Arrival Rate", // X-axis label
                "Mean System Time", // Y-axis label
                dataset, // Dataset
                PlotOrientation.VERTICAL,
                false, // Show legend
                true, // Use tooltips
                false // Generate URLs
        );

        XYPlot plot = chart.getXYPlot();
        NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        xAxis.setTickUnit(new NumberTickUnit(1));

        // Disable points on the lines
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setBaseShapesVisible(false);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        chartPanel.setDomainZoomable(true);
        chartPanel.setRangeZoomable(true);
        chartPanel.setMouseWheelEnabled(true);

        JFrame frame = new JFrame("Gas Station Simulation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(chartPanel);
        frame.pack();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);

        // Adding mean system time line to the graph
        double[][] lineData = new double[2][xValues.size()];
        for (int i = 0; i < xValues.size(); i++) {
            lineData[0][i] = xValues.get(i);
            lineData[1][i] = meanValues.get(i);
        }
        dataset.addSeries("Mean System Time", lineData);
        //chartPanel.repaint();
        for (int i = 0; i < xValues.size(); i++) {
            double[][] barData = new double[2][2];
            barData[0][0] = xValues.get(i);
            barData[0][1] = xValues.get(i);
            barData[1][0] = confidenceInterval.get(i)[0];
            barData[1][1] = confidenceInterval.get(i)[1];
            dataset.addSeries("sys"+i, barData);
        }
        //chartPanel.repaint();

        // Adding mean queuing time line to the graph
        double[][] lineData2 = new double[2][xValues.size()];
        for (int i = 0; i < xValues.size(); i++) {
            lineData2[0][i] = xValues.get(i);
            lineData2[1][i] = meanValues2.get(i);
        }
        dataset.addSeries("Mean Queue Time", lineData2);
        //chartPanel.repaint();
        for (int i = 0; i < xValues.size(); i++) {
            double[][] barData2 = new double[2][2];
            barData2[0][0] = xValues.get(i);
            barData2[0][1] = xValues.get(i);
            barData2[1][0] = confidenceInterval2.get(i)[0];
            barData2[1][1] = confidenceInterval2.get(i)[1];
            dataset.addSeries("que"+i, barData2);
        }
        //chartPanel.repaint();

        // Adding mean service time line to the graph
        double[][] lineData3 = new double[2][xValues.size()];
        for (int i = 0; i < xValues.size(); i++) {
            lineData3[0][i] = xValues.get(i);
            lineData3[1][i] = meanValues3.get(i);
        }
        dataset.addSeries("Mean Service Time", lineData3);
        //chartPanel.repaint();
        for (int i = 0; i < xValues.size(); i++) {
            double[][] barData3 = new double[2][2];
            barData3[0][0] = xValues.get(i);
            barData3[0][1] = xValues.get(i);
            barData3[1][0] = confidenceInterval3.get(i)[0];
            barData3[1][1] = confidenceInterval3.get(i)[1];
            dataset.addSeries("cha"+i, barData3);
        }
        chartPanel.repaint();


   /* void drawGraph(List<Double> arrivalRates, List<Double> meanSystemTimes) {
        DefaultXYDataset dataset = new DefaultXYDataset();

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Mean System Time vs. Arrival Rate", // Chart title
                "Arrival Rate", // X-axis label
                "Mean System Time", // Y-axis label
                dataset, // Dataset
                PlotOrientation.VERTICAL,
                true, // Show legend
                true, // Use tooltips
                false // Generate URLs
        );

        XYPlot plot = chart.getXYPlot();
        NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        xAxis.setTickUnit(new NumberTickUnit(0.2));

        // Turn off points on the lines
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setBaseShapesVisible(false);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        chartPanel.setDomainZoomable(true);
        chartPanel.setRangeZoomable(true);
        chartPanel.setMouseWheelEnabled(true);

        JFrame frame = new JFrame("Gas Station Simulation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(chartPanel);
        frame.pack();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);

        // Adding lines to the graph
        for (int i = 0; i < arrivalRates.size(); i++) {
            double[][] lineData = new double[2][2];
            lineData[0][0] = arrivalRates.get(i);
            lineData[0][1] = arrivalRates.get(i);
            lineData[1][0] = meanSystemTimes.get(i) - confid...;
            lineData[1][1] = meanSystemTimes.get(i) + confid...;
            dataset.addSeries("Car " + (i + 1), lineData);
            chartPanel.repaint();
        }*/
    }
}



