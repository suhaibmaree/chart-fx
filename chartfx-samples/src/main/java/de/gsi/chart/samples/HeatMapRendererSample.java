package de.gsi.chart.samples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gsi.chart.XYChart;
import de.gsi.chart.axes.spi.ColorGradientAxis;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.plugins.ColormapSelector;
import de.gsi.chart.plugins.EditAxis;
import de.gsi.chart.plugins.Zoomer;
import de.gsi.chart.renderer.spi.HeatMapRenderer;
import de.gsi.chart.ui.geometry.Side;
import de.gsi.dataset.GridDataSet;
import de.gsi.dataset.spi.DoubleGridDataSet;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Simple Example for the HeatmapRenderer
 * 
 * @author akrimm
 */
public class HeatMapRendererSample extends Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(HeatMapRendererSample.class);

    @Override
    public void start(final Stage primaryStage) {
        final XYChart chart = new XYChart(new DefaultNumericAxis(), new DefaultNumericAxis());
        Zoomer zoomer = new Zoomer();
        chart.getPlugins().add(zoomer);
        chart.getPlugins().add(new EditAxis());
        ColormapSelector colormapSelector = new ColormapSelector();
        chart.getPlugins().add(colormapSelector);

        chart.getXAxis().setAutoRanging(true);
        chart.getYAxis().setAutoRanging(true);

        // Manually Add Z Axis, so we can setup bindings (otherwise z-axis is only populated on first render)
        ColorGradientAxis gradientAxis = new ColorGradientAxis("Test", "wow");
        gradientAxis.setSide(Side.RIGHT);
        gradientAxis.setAutoRanging(true);
        gradientAxis.setGradientWidth(40);
        gradientAxis.colorGradientProperty().bind(colormapSelector.colormapProperty());
        chart.getAxes().add(gradientAxis);

        HeatMapRenderer heatMapRenderer = new HeatMapRenderer();
        chart.getRenderers().setAll(heatMapRenderer);

        // final GridDataSet dataSet = new DoubleGridDataSet("test 3D", 2, new double[][] { { 0, 1.5, 2, 3 }, { 0, 1, 2, 3 },
        //      { 0.1, 0, 0, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0, 0, 1, 1, 1, 1, 1 } });
        final GridDataSet dataSet = createTestData();
        heatMapRenderer.getDatasets().add(dataSet);

        final Scene scene = new Scene(chart, 800, 600);
        primaryStage.setTitle(getClass().getSimpleName());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private static GridDataSet createTestData() {
        final int nPoints = 1000;
        final double f = 0.1;
        final double[] x = new double[nPoints];
        final double[] y = new double[nPoints];
        for (int i = 0; i < x.length; i++) {
            final double val = (i / (double) x.length - 0.5) * 10;
            x[i] = val;
            y[i] = val;
        }
        final double[] z = new double[x.length * y.length];
        for (int yIndex = 0; yIndex < y.length; yIndex++) {
            for (int xIndex = 0; xIndex < x.length; xIndex++) {
                z[xIndex + x.length * yIndex] = Math.sin(2.0 * Math.PI * f * x[xIndex])
                        * Math.cos(2.0 * Math.PI * f * y[yIndex]);
            }
        }
        return new DoubleGridDataSet("demoDataSet", 2, new double[][] { x, y, z });
    }

    /**
     * @param args
     *            the command line arguments
     */
    public static void main(final String[] args) {
        Application.launch(args);
    }
}
