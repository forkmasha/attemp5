import java.util.Random;

class UniformDistribution extends Distribution {
    @Override
    public double getSample(double mean) {
        Random random = new Random();
        return mean * (random.nextDouble());
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
        double binWidth = xMax / numBins;
        double constantPDF = 1.0 / xMax; // Calculate the constant PDF value

        double[] pdf = new double[numBins];

        for (int i = 0; i < numBins; i++) {
            pdf[i] = constantPDF;
        }

        return pdf;
    }
}
