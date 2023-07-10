import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
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

    private List<Double> systemTimeList;
    private List<Double> queueTimeList;

    private EventStack eventStack;
    private List<Car> servicedCars;
    private int k;
    private int j;
    private double time;
    private double previousTime;

    private List<Double> queueStartTimes;

    private List<Double> arrivalRateList;
    private List<Double> meanSystemTimeList;
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

        queueTimeList = new ArrayList<>();

        arrivalRateList = new ArrayList<>();
        meanSystemTimeList = new ArrayList<>();

        eventStack = new EventStack();
        servicedCars = new ArrayList<>();
        k = 0;
        j = 0;
        time = 0.0;
        previousTime = 0.0;
    }

    public void simulate() {
        // Ініціалізуємо початкові події
        eventStack.addEvent(new ArrivalEvent(0.0));

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
            car.tinQueue += deltaTime;
        }

        totalCarsInSystem += servicedCars.size() * deltaTime;
        totalCarsInQueue += Math.max(0, servicedCars.size() - numServers) * deltaTime;

        // Зберігаємо часи перебування в системі та черзі для обліку середніх значень
        if (!servicedCars.isEmpty()) {
            double lastCarTime = servicedCars.get(servicedCars.size() - 1).tinSys;
            double queueTime = Math.max(0, lastCarTime - servicedCars.get(0).tinSys);
            systemTimes.add(lastCarTime);
            queueTimes.add(queueTime);

            // Зберігаємо час перебування в системі для кожного автомобіля
            for (Car car : servicedCars) {
                systemTimeList.add(car.tinSys);
            }
            for (Car car : servicedCars) {
                queueTimeList.add(car.tinQueue);
            }
        }

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
            // Збільшуємо лічильник прибулих автомобілів
            int i = j + k + 1;
            // Якщо ще не всі автомобілі прибули на станцію, додаємо подію прибуття наступного автомобіля до стеку подій
            if (i <= maxCars) {
                double nextArrivalTime = eventTime + exponentialDistribution(meanArrivalInterval);
                eventStack.addEvent(new ArrivalEvent(nextArrivalTime));
            } else {
                return; // Повертаємося, якщо кількість автомобілів досягає максимального значення
            }
            // Збільшуємо лічильник обслужених автомобілів
            j++;

        // Якщо є вільні сервери, додаємо подію відправлення для цього автомобіля до стеку подій та записуємо час обслуговування
        if (numServers - (j - k) >= 0) {
            double serviceTime = exponentialDistribution(meanServiceTime);
            eventStack.addEvent(new DepartureEvent(eventTime + serviceTime));
            servicedCars.add(new Car(eventTime, 0.0, serviceTime));
        } else {
            if (j - k > numStates) {
                k++;
            }
            // Зберігаємо час початку очікування в черзі для автомобіля
            queueStartTimes.add(eventTime);
        }
        arrivalRateList.add(1.0 / meanArrivalInterval); // Calculate arrival rate from mean arrival interval
        meanSystemTimeList.add(calculateMean(systemTimeList));
    }
    private void processDepartureEvent(double eventTime) {
        // Збільшуємо лічильник обслуженихавтомобілів
        j--;
        // Якщо стан системи перевищує кількість серверів, додаємо подію відправлення для автомобіля з черги до стеку подій та записуємо час обслуговування черги
        if (j - k > numServers) {
            double serviceTime = exponentialDistribution(meanServiceTime);
            eventStack.addEvent(new DepartureEvent(eventTime + serviceTime));
            if (!servicedCars.isEmpty()) {
                servicedCars.remove(0); // Видаляємо перший автомобіль з черги
                // Додаємо час очікування в черзі до списку
                double queueTime = eventTime - queueStartTimes.get(0);
                queueTimes.add(queueTime);
                queueStartTimes.remove(0); // Видаляємо перший час початку очікування з черги
            }
        }
    }
    private void generateStatistics() {
        // Виводимо статистику симуляції
        System.out.println("Simulation Statistics");
        System.out.println("=====================");
        System.out.println("Number of Arrivals: " + (j + k));
        System.out.println("Number of Departures: " + j);
        System.out.println("Number of Blocked Cars: " + k);

        // Збираємо дані про часи перебування в системі та черзі

        double meanStime = calculateMean(systemTimes);
        double meanQtime = calculateMean(queueTimes);
        double stdDevSystemTime = calculateStandardDeviation(systemTimeList);

        System.out.println("-+-+-+-+-+-+-+-+-+-+-+-+");

        System.out.println("Mean Queue Time: " + meanQtime);
        System.out.println("Mean System Time: " + meanStime);
        System.out.println("*********************");
        System.out.println("Standard Deviation of System Time: " + stdDevSystemTime);
        double stdDevQueueTime = calculateStandardDeviation(queueTimeList);
        System.out.println("Standard Deviation of Queue Time: " + stdDevQueueTime);

        System.out.println("#########################");

        double[] stimeConfidenceInterval = calculateConfidenceInterval(systemTimes);
        double[] qtimeConfidenceInterval = calculateConfidenceInterval(queueTimes);

        System.out.println("Mean System Time: " + stimeConfidenceInterval[0]);
        System.out.println("Confidence Interval (Stime): " + stimeConfidenceInterval[0] + " - " + stimeConfidenceInterval[1]);

        System.out.println("Mean Queue Time: " + qtimeConfidenceInterval[0]);
        System.out.println("Confidence Interval (Qtime): " + qtimeConfidenceInterval[0] + " - " + qtimeConfidenceInterval[1]);

        drawGraph(arrivalRateList, meanSystemTimeList);
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

    void drawGraph(List<Double> arrivalRates, List<Double> meanSystemTimes) {
        DefaultXYDataset dataset = new DefaultXYDataset();
        double[][] data = new double[2][arrivalRates.size()];

        for (int i = 0; i < arrivalRates.size(); i++) {
            data[0][i] = arrivalRates.get(i);
            data[1][i] = meanSystemTimes.get(i);
        }

        dataset.addSeries("Mean System Time", data);

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

        // Customize the chart
        XYPlot plot = chart.getXYPlot();
        NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        xAxis.setTickUnit(new NumberTickUnit(0.2));

        ChartPanel chartPanel = new ChartPanel(chart);

        JFrame frame = new JFrame("Gas Station Simulation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(chartPanel);
        frame.pack();
        frame.setVisible(true);
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
        for (Double time : systemTimes) {
            sum += time;
        }
        return sum / systemTimes.size();
    }


}