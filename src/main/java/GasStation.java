import java.awt.*;
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
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import static java.awt.Color.*;
import static java.lang.Math.ceil;
import static java.lang.Math.log;

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
    private DistributionType distributionType;
    private double[] systemTimeConfidence;

    private double[] queueTimeConfidence;
    private double[] serviceTimeConfidence;
    private List<Double> queueStartTimes;

    private List<Double> arrivalRateList;
    private List<Double> meanSystemTimeList;

    public double[] getSystemTimeConfidence() {
        return systemTimeConfidence;
    }

    public double[] getQueueTimeConfidence() {
        return queueTimeConfidence;
    }

    public double[] getServiceTimeConfidence() {
        return serviceTimeConfidence;
    }
    /* public List<double[]> getSystemTimesConfidences() {
        return systemTimesConfidences;
    }
    */

    public GasStation(int numServers, int queueLength, int numStates, int maxCars, double meanArrivalInterval, double meanServiceTime,DistributionType distributionType) {
        this.numServers = numServers;
        this.queueLength = queueLength;
        this.numStates = numStates;
        this.maxCars = maxCars;
        this.meanArrivalInterval = meanArrivalInterval;
        this.meanServiceTime = meanServiceTime;
        this.distributionType=distributionType;

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

 /*   private void processArrivalEvent(double eventTime) {
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
*/
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
        // double serviceTime = exponentialDistribution(meanServiceTime);
         double serviceTime = calculateServiceTime();
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

 private double calculateServiceTime(){
     if(distributionType==DistributionType.GEOMETRIC){
         return geometricDistribution(meanServiceTime);
     }
     else if(distributionType==DistributionType.ERLANG){
         return erlangDistribution(meanServiceTime,2);
     }
     else if(distributionType==DistributionType.EXPONENTIAL){
         return exponentialDistribution(meanServiceTime);
     }
     else {
         System.out.println("Error: Distribution is not implemented");
     }
     return 1;
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
            //double serviceTime = exponentialDistribution(meanServiceTime);
           // double serviceTime = geometricDistribution(meanServiceTime);
            double serviceTime = calculateServiceTime();
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

    private double calculateMean(List<Double> values) {
        double sum = 0.0;
        for (Double value : values) {
            sum += value;
        }
        return sum / values.size();
    }

    private double exponentialDistribution(double mean) {
        Random random = new Random();
        return mean * -log(1 - random.nextDouble());
    }

    private double erlangDistribution(double mean, int k) {
        Random random = new Random();
        double sample = exponentialDistribution(mean);
        for(int i=1; i<k; i++) {
            sample +=exponentialDistribution(mean);
        }
        return sample/k;
    }

    private double geometricDistribution(double mean) {
        Random random = new Random();
        //return (int) Math.ceil(Math.log(1-random.nextDouble())/Math.log(1-p));

        return Math.round(exponentialDistribution(mean));// correct
        // return mean * Math.ceil(Math.log(1-random.nextDouble())/Math.log(1-0.999999));
        // return mean * ceil(log(1-random.nextDouble()) / log(1-mean));
    }

    private double[] calculateConfidenceInterval(List<Double> values, int level) {

        //SwingUtilities.invokeLater(() -> {
        //  JFrame frame = new JFrame("CriticalValueTable");
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        double[][] data = {
                {1, 3.078, 6.314, 12.706, 31.821, 63.65},
                {2, 1.886, 2.920, 4.303, 6.965, 9.925},
                {3, 1.638, 2.353, 3.182, 4.541, 5.841},
                {4, 1.533, 2.132, 2.776, 3.747, 4.604},
                {5, 1.476, 2.015, 2.571, 3.365, 4.032},
                {6, 1.440, 1.943, 2.447, 3.143, 3.707},
                {7, 1.415, 1.895, 2.365, 2.998, 3.499},
                {8, 1.397, 1.860, 2.306, 2.896, 3.355},
                {9, 1.383, 1.833, 2.262, 2.821, 3.250},
                {10, 1.372, 1.812, 2.228, 2.764, 3.169},
                {11, 1.363, 1.796, 2.201, 2.718, 3.106},
                {12, 1.356, 1.782, 2.179, 2.681, 3.055},
                {13, 1.350, 1.771, 2.160, 2.650, 3.012},
                {14, 1.345, 1.761, 2.145, 2.624, 2.977},
                {15, 1.341, 1.753, 2.131, 2.602, 2.947},
                {16, 1.337, 1.746, 2.120, 2.583, 2.921},
                {17, 1.333, 1.740, 2.110, 2.567, 2.898},
                {18, 1.330, 1.734, 2.101, 2.552, 2.878},
                {19, 1.328, 1.729, 2.093, 2.539, 2.861},
                {20, 1.325, 1.725, 2.086, 2.528, 2.845},
                {21, 1.323, 1.721, 2.080, 2.518, 2.831},
                {22, 1.321, 1.717, 2.074, 2.508, 2.819},
                {23, 1.319, 1.714, 2.069, 2.500, 2.807},
                {24, 1.318, 1.711, 2.064, 2.492, 2.797},
                {25, 1.316, 1.708, 2.060, 2.485, 2.787},
                {26, 1.315, 1.706, 2.056, 2.479, 2.779},
                {27, 1.314, 1.703, 2.052, 2.473, 2.771},
                {28, 1.313, 1.701, 2.048, 2.467, 2.763},
                {29, 1.311, 1.699, 2.045, 2.462, 2.756},
                {30, 1.310, 1.697, 2.042, 2.457, 2.750},
                {31, 1.309, 1.696, 2.040, 2.453, 2.744},
                {32, 1.309, 1.694, 2.037, 2.449, 2.738},
                {33, 1.308, 1.692, 2.035, 2.445, 2.733},
                {34, 1.307, 1.691, 2.032, 2.441, 2.728},
                {35, 1.306, 1.690, 2.030, 2.438, 2.724},
                {36, 1.306, 1.688, 2.028, 2.434, 2.719},
                {37, 1.305, 1.687, 2.026, 2.431, 2.715},
                {38, 1.304, 1.686, 2.024, 2.429, 2.712},
                {39, 1.304, 1.685, 2.023, 2.426, 2.708},
                {40, 1.303, 1.684, 2.021, 2.423, 2.704},

                {41, 1.303, 1.683, 2.020, 2.421, 2.701},
                {42, 1.302, 1.682, 2.018, 2.418, 2.698},
                {43, 1.302, 1.681, 2.017, 2.416, 2.695},
                {44, 1.301, 1.680, 2.015, 2.414, 2.692},
                {45, 1.301, 1.679, 2.014, 2.412, 2.690},
                {46, 1.300, 1.679, 2.013, 2.410, 2.687},
                {47, 1.300, 1.678, 2.012, 2.408, 2.685},
                {48, 1.299, 1.677, 2.011, 2.407, 2.682},
                {49, 1.299, 1.677, 2.010, 2.405, 2.680},
                {50, 1.299, 1.676, 2.009, 2.403, 2.678},
                {51, 1.298, 1.675, 2.008, 2.402, 2.676},
                {52, 1.298, 1.675, 2.007, 2.400, 2.674},
                {53, 1.298, 1.674, 2.006, 2.399, 2.672},
                {54, 1.297, 1.674, 2.005, 2.397, 2.670},
                {55, 1.297, 1.673, 2.004, 2.396, 2.668},
                {56, 1.297, 1.673, 2.003, 2.395, 2.667},
                {57, 1.297, 1.672, 2.002, 2.394, 2.665},
                {58, 1.296, 1.672, 2.002, 2.392, 2.663},
                {59, 1.296, 1.671, 2.001, 2.391, 2.662},
                {60, 1.296, 1.671, 2.000, 2.390, 2.660},
                {61, 1.296, 1.670, 2.000, 2.389, 2.659},
                {62, 1.295, 1.670, 1.999, 2.388, 2.657},
                {63, 1.295, 1.669, 1.998, 2.387, 2.656},
                {64, 1.295, 1.669, 1.998, 2.386, 2.655},
                {65, 1.295, 1.669, 1.997, 2.385, 2.654},
                {66, 1.295, 1.668, 1.997, 2.384, 2.652},
                {67, 1.294, 1.668, 1.996, 2.383, 2.651},
                {68, 1.294, 1.668, 1.995, 2.382, 2.650},
                {69, 1.294, 1.667, 1.995, 2.382, 2.649},
                {70, 1.294, 1.667, 1.994, 2.381, 2.648},
                {71, 1.294, 1.667, 1.994, 2.380, 2.647},
                {72, 1.293, 1.666, 1.993, 2.379, 2.646},
                {73, 1.293, 1.666, 1.993, 2.379, 2.645},
                {74, 1.293, 1.666, 1.993, 2.378, 2.644},
                {75, 1.293, 1.665, 1.992, 2.377, 2.643},
                {76, 1.293, 1.665, 1.992, 2.376, 2.642},
                {77, 1.293, 1.665, 1.991, 2.376, 2.641},
                {78, 1.292, 1.665, 1.991, 2.375, 2.640},
                {79, 1.292, 1.664, 1.990, 2.374, 2.640},
                {80, 1.292, 1.664, 1.990, 2.374, 2.639},
                {81, 1.292, 1.664, 1.990, 2.373, 2.638},
                {82, 1.292, 1.664, 1.989, 2.373, 2.637},
                {83, 1.292, 1.663, 1.989, 2.372, 2.636},
                {84, 1.292, 1.663, 1.989, 2.372, 2.636},
                {85, 1.292, 1.663, 1.988, 2.371, 2.635},
                {86, 1.291, 1.663, 1.988, 2.370, 2.634},
                {87, 1.291, 1.663, 1.988, 2.370, 2.634},
                {88, 1.291, 1.662, 1.987, 2.369, 2.633},
                {89, 1.291, 1.662, 1.987, 2.369, 2.632},
                {90, 1.291, 1.662, 1.987, 2.368, 2.632},
                {91, 1.291, 1.662, 1.986, 2.368, 2.631},
                {92, 1.291, 1.662, 1.986, 2.368, 2.630},
                {93, 1.291, 1.661, 1.986, 2.367, 2.630},
                {94, 1.291, 1.661, 1.986, 2.367, 2.629},
                {95, 1.291, 1.661, 1.985, 2.366, 2.629},
                {96, 1.290, 1.661, 1.985, 2.366, 2.628},
                {97, 1.290, 1.661, 1.985, 2.365, 2.627},
                {98, 1.290, 1.661, 1.984, 2.365, 2.627},
                {99, 1.290, 1.660, 1.984, 2.365, 2.626},
                {100, 1.290, 1.660, 1.984, 2.364, 2.626},
                {101, 1.290, 1.660, 1.984, 2.364, 2.625},
                {102, 1.290, 1.660, 1.983, 2.363, 2.625},
                {103, 1.290, 1.660, 1.983, 2.363, 2.624},
                {104, 1.290, 1.660, 1.983, 2.363, 2.624},
                {105, 1.290, 1.659, 1.983, 2.362, 2.623},
                {999, 1.280, 1.645, 1.960, 2.330, 2.575},
        };
        String[] columnNames = {"Degrees of Freedom", "80%", "90%", "95%", "98%", "99%"};
        // DefaultTableModel model = new DefaultTableModel(data, columnNames);
        //   JTable criticalValueTable = new JTable(model);
        //JScrollPane scrollPane = new JScrollPane(criticalValueTable);
        //  frame.add(scrollPane);
        //frame.setSize(500, 300);
        //frame.setVisible(true);

        double mean = calculateMean(values);
        double stdDev = calculateStandardDeviation(values);
        //int level = 95;
        int levelID = 0;
        double zScore = 100;

        switch(level) {
            case 80:
                levelID = 1;
                break;
            case 90:
                levelID = 2;
                break;
            case 95:
                levelID = 3;
                break;
            case 98:
                levelID = 4;
                break;
            case 99:
                levelID = 5;
                break;
            default:
                System.out.println("Error: Confidence level is not available in table");
        }

        // Calculate the confidence interval
        if ( values.size() > 105 ) {
            zScore = data[105][levelID]; // For a 95% confidence interval (assuming a large enough sample size)
        }
        else {
            zScore = data[values.size()][levelID]; // For a 95% confidence interval (assuming a large enough sample size)
        }
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
    void drawGraph(List<Double> xValues, List<Double> meanValues1, List<double[]> confidenceInterval1, List<Double> meanValues2, List<double[]> confidenceInterval2,
                   List<Double> meanValues3, List<double[]> confidenceInterval3) {
        DefaultXYDataset dataset = new DefaultXYDataset();

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Queueing Model Results for EV Charging Sites", // Chart title
                "Arrival Rate [1/h]", // X-axis label
                "Mean Times [h]", // Y-axis label
                dataset, // Dataset
                PlotOrientation.VERTICAL,
                false, // Show legend
                true, // Use tooltips
                false // Generate URLs
        );

        XYPlot plot = chart.getXYPlot();
        NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        xAxis.setTickUnit(new NumberTickUnit(5));

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
        // frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);

        // Adding mean queuing time line to the graph
        double[][] lineData2 = new double[2][xValues.size()];
        for (int i = 0; i < xValues.size(); i++) {
            lineData2[0][i] = xValues.get(i);
            lineData2[1][i] = meanValues2.get(i);
        }
        dataset.addSeries(1000, lineData2);  // "Mean Queue Time"
        renderer.setSeriesPaint(0, MAGENTA);

        for (int i = 0; i < xValues.size(); i++) {
            double[][] barData = new double[2][2];
            barData[0][0] = xValues.get(i);
            barData[0][1] = xValues.get(i);
            barData[1][0] = confidenceInterval2.get(i)[0];
            barData[1][1] = confidenceInterval2.get(i)[1];
            dataset.addSeries(i + 1, barData);
            renderer.setSeriesPaint(i + 1, MAGENTA);
        }

        // Adding mean system time line to the graph
        double[][] lineData1 = new double[2][xValues.size()];
        for (int i = 0; i < xValues.size(); i++) {
            lineData1[0][i] = xValues.get(i);
            lineData1[1][i] = meanValues1.get(i);
        }
        dataset.addSeries(0, lineData1); // "Mean System Time"
        renderer.setSeriesPaint(xValues.size() + 1, BLACK);

        for (int i = 0; i < xValues.size(); i++) {
            double[][] barData = new double[2][2];
            barData[0][0] = xValues.get(i);
            barData[0][1] = xValues.get(i);
            barData[1][0] = confidenceInterval1.get(i)[0];
            barData[1][1] = confidenceInterval1.get(i)[1];
            dataset.addSeries(xValues.size() + i + 2, barData);
            renderer.setSeriesPaint(xValues.size() + i + 2, BLACK);
        }

        // Adding mean service time line to the graph
        double[][] lineData3 = new double[2][xValues.size()];
        for (int i = 0; i < xValues.size(); i++) {
            lineData3[0][i] = xValues.get(i);
            lineData3[1][i] = meanValues3.get(i);
        }
        dataset.addSeries(2000, lineData3); // "Mean Service Time"
        renderer.setSeriesPaint(2 * xValues.size() + 2, BLUE);

        for (int i = 0; i < xValues.size(); i++) {
            double[][] barData = new double[2][2];
            barData[0][0] = xValues.get(i);
            barData[0][1] = xValues.get(i);
            barData[1][0] = confidenceInterval3.get(i)[0];
            barData[1][1] = confidenceInterval3.get(i)[1];
            dataset.addSeries(2 * xValues.size() + i + 3, barData);
            renderer.setSeriesPaint(2 * xValues.size() + i + 3, BLUE);
        }

        chartPanel.repaint();
    }
}



