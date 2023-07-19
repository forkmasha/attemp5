package org.simulation.graph;

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
import java.awt.*;
import java.util.List;

import static java.awt.Color.*;
import static java.awt.Color.BLUE;

public class Graph {
    public void drawGraph(List<Double> xValues, List<Double> meanValues1, List<double[]> confidenceInterval1, List<Double> meanValues2, List<double[]> confidenceInterval2,
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
        chartPanel.setPreferredSize(new Dimension(800, 600));
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
