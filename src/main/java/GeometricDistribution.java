import java.util.Random;

class GeometricDistribution extends Distribution {
    @Override
    public double getSample(double mean) {
        Random random = new Random();
        return Math.round(exponentialDistribution(mean));
    }
    @Override
    public double[] getSamples(double mean, int count) {
        double[] samples = new double[count];
        for (int i = 0; i < count; i++) {
            samples[i] = getSample(mean);
        }
        return samples;
    }
    @Override
    public double[] getPDF(double mean, double xMax) {
        int numBins = 100; // Adjust the number of bins as needed
        double[] pdf = new double[numBins];
        double p = 1.0 / mean; // Calculate the probability of success (p)

        // Calculate the bin width to cover the range (1, xMax) with numBins
        double binWidth = (xMax - 1) / numBins;

        for (int i = 0; i < numBins; i++) {
            double x = 1 + i * binWidth; // Calculate the x value for the bin
            pdf[i] = Math.pow(1 - p, x - 1) * p * binWidth;
        }

        return pdf;
    }
    private double exponentialDistribution(double mean) {
        Random random = new Random();
        return mean * (-Math.log(1 - random.nextDouble()));
    }
}
