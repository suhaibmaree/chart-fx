package de.gsi.chart.renderer.spi;

import static de.gsi.dataset.DataSet.DIM_X;
import static de.gsi.dataset.DataSet.DIM_Y;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gsi.chart.Chart;
import de.gsi.chart.XYChart;
import de.gsi.chart.axes.Axis;
import de.gsi.chart.axes.spi.ColorGradientAxis;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.renderer.Renderer;
import de.gsi.chart.ui.geometry.Side;
import de.gsi.chart.utils.FXUtils;
import de.gsi.dataset.DataSet;
import de.gsi.dataset.GridDataSet;
import de.gsi.dataset.utils.ProcessingProfiler;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * An alternative Implementation to the heat-map implementation contained in ContourDataSetRenderer.
 * Correctly handles zooming out of the data range and non uniformly spaced grids.
 * Also uses the new ByteBuffer backed WritableImage implementation from openJFX13.
 * TODO:
 * - Handle Unstructured Data 3D data
 * 
 * @author Alexander Krimm
 */
public class HeatMapRenderer extends AbstractDataSetManagement<HeatMapRenderer> implements Renderer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContourDataSetRenderer.class);

    private BooleanProperty smooth = new SimpleBooleanProperty(this, "smooth", false);
    private ObjectProperty<Axis> xAxis = new SimpleObjectProperty<>(this, "xAxis", null);
    private ObjectProperty<Axis> yAxis = new SimpleObjectProperty<>(this, "yAxis", null);
    private ObjectProperty<ColorGradientAxis> zAxis = new SimpleObjectProperty<>(this, "zAxis", null);

    @Override
    public void render(final GraphicsContext gc, final Chart chart, final int dataSetOffset,
            final ObservableList<DataSet> datasets) {
        addMissingAxes(chart);

        final long start = ProcessingProfiler.getTimeStamp();

        // make local copy and add renderer specific data sets
        final List<DataSet> localDataSetList = new ArrayList<>(datasets);
        localDataSetList.addAll(getDatasets());

        // N.B. importance of reverse order: start with last index, so that
        // most(-like) important DataSet is drawn on top of the others
        for (int dataSetIndex = localDataSetList.size() - 1; dataSetIndex >= 0; dataSetIndex--) {
            final DataSet dataSet = localDataSetList.get(dataSetIndex);

            if (dataSet.getDimension() < 3) {
                LOGGER.atInfo().addArgument(dataSet.getName()).addArgument(dataSet.getDimension())
                        .log("Skipped dataset {}: heatMap renderer needs at least 3 dimensions, is {}.");
                continue;
            }

            ProcessingProfiler.getTimeDiff(start, "drawing DataSet");

            if (dataSet instanceof GridDataSet) {
                renderGridData(gc, (GridDataSet) dataSet);
            } else {
                renderUnstructured(gc, dataSet);
            }
            ProcessingProfiler.getTimeDiff(start, "finished drawing");
        }
        ProcessingProfiler.getTimeDiff(start);
    }

    /**
     * TODO: implement rendering of unstructured Data
     * 
     * @param gc
     * @param dataSet
     */
    private void renderUnstructured(GraphicsContext gc, DataSet dataSet) {
        LOGGER.atWarn().addArgument(dataSet.getName())
                .log("skipping dataSet {}: rendering unstructured data not implemented yet");
    }

    /**
     * Draw Heat map from grid structured data
     * 
     * @param gc
     * @param axisTransform
     * @param lCache
     */
    private void renderGridData(final GraphicsContext gc, final GridDataSet dataSet) {
        final long start = ProcessingProfiler.getTimeStamp();

        // get position and extend of the visible part of the heatmap
        final double dataMinX = getXAxis().getDisplayPosition(dataSet.getGrid(DIM_X, 0));
        final double outMinX;
        final int dataMinXIndex;
        if (dataMinX > 0) {
            outMinX = dataMinX;
            dataMinXIndex = 0;
        } else {
            outMinX = 0;
            dataMinXIndex = dataSet.getIndex(DIM_X, getXAxis().getValueForDisplay(0));
        }
        final double dataMaxX = getXAxis().getDisplayPosition(dataSet.getGrid(DIM_X, dataSet.getDataCount(DIM_X) - 1));
        final double outMaxX;
        final int dataMaxXIndex;
        if (dataMaxX < getXAxis().getWidth()) {
            outMaxX = dataMaxX;
            dataMaxXIndex = dataSet.getDataCount(DIM_X) - 1;
        } else {
            outMaxX = getXAxis().getWidth();
            dataMaxXIndex = dataSet.getIndex(DIM_X, getXAxis().getValueForDisplay(outMaxX));
        }
        final double dataMinY = getYAxis().getDisplayPosition(dataSet.getGrid(DIM_Y, 0));
        final double outMinY;
        final int dataMinYIndex;
        if (dataMinY < getYAxis().getHeight()) {
            outMinY = dataMinY;
            dataMinYIndex = 0;
        } else {
            outMinY = getYAxis().getHeight();
            dataMinYIndex = dataSet.getIndex(DIM_Y, getYAxis().getValueForDisplay(outMinX));
        }
        final double dataMaxY = getYAxis().getDisplayPosition(dataSet.getGrid(DIM_Y, dataSet.getDataCount(DIM_Y) - 1));
        final double outMaxY;
        final int dataMaxYIndex;
        if (dataMaxY > 0) {
            outMaxY = dataMaxY;
            dataMaxYIndex = dataSet.getDataCount(DIM_Y) - 1;
        } else {
            outMaxY = 0;
            dataMaxYIndex = dataSet.getIndex(DIM_Y, getYAxis().getValueForDisplay(0));
        }
        final int outWidth = (int) outMaxX - (int) outMinX;
        final int outHeight = (int) outMinY - (int) outMaxY;

        if (outWidth <= 0 || outHeight <= 0) {
            return;
        }

        dataSet.recomputeLimits(DataSet.DIM_Z);
        getZAxis().getAutoRange().set(dataSet.getAxisDescription(DataSet.DIM_Z).getMin(),
                dataSet.getAxisDescription(DataSet.DIM_Z).getMax());
        // get an image backed by a byte buffer (new in openjfx13)
        PixelBuffer<IntBuffer> pixelBuffer = new PixelBuffer<>(outWidth, outHeight,
                IntBuffer.allocate(outWidth * outHeight), PixelFormat.getIntArgbPreInstance());
        final WritableImage image = new WritableImage(pixelBuffer);

        pixelBuffer.updateBuffer(pBuf -> {
            FXUtils.assertJavaFxThread();
            IntBuffer buf = pBuf.getBuffer();
            int r = dataMaxYIndex + 1;
            double nextrow = 0;
            for (int j = 0; j < outHeight; j++) {
                int accumulatedRows = 0;
                while (j >= nextrow) {
                    accumulatedRows++;
                    r--;
                    nextrow = r > 0
                            ? (0.5 * (getYAxis().getDisplayPosition(dataSet.getGrid(DIM_Y, r))
                                    + getYAxis().getDisplayPosition(dataSet.getGrid(DIM_Y, r - 1)))) - outMaxY
                            : Double.MAX_VALUE;
                }
                if (accumulatedRows > 0) { // Last row (image origin inverts y)
                    int c = dataMinXIndex - 1;
                    double nextcol = 0;
                    int intcolor = 0;
                    for (int i = 0; i < outWidth; i++) {
                        int accumulatedCols = 0;
                        while (i >= nextcol) {
                            accumulatedCols++;
                            c++;
                            nextcol = c < dataMaxXIndex
                                    ? getXAxis().getDisplayPosition(
                                            0.5 * (dataSet.getGrid(DIM_X, c) + dataSet.getGrid(DIM_X, c + 1))) - outMinX
                                    : Double.MAX_VALUE;
                        }
                        if (accumulatedCols > 0) {
                            double alpha = 0;
                            double red = 0;
                            double green = 0;
                            double blue = 0;
                            for (int dr = 0; dr < accumulatedRows; dr++) {
                                for (int dc = 0; dc < accumulatedCols; dc++) {
                                    Color color = getZAxis()
                                            .getColor(dataSet.getValue(dataSet.getNGrid(), c - dc, r + dr));
                                    alpha += color.getOpacity();
                                    red += color.getRed();
                                    green += color.getGreen();
                                    blue += color.getBlue();
                                }
                            }
                            double factor = 255 / (accumulatedRows * accumulatedCols);
                            intcolor = ((byte) (alpha * factor) << 24) + ((byte) (red * factor) << 16)
                                    + ((byte) (green * factor) << 8) + ((byte) (blue * factor));
                        }
                        buf.put(intcolor);
                    }
                } else { // consecutive row (bulk copy previous line)
                    IntBuffer currentLine = buf.duplicate();
                    currentLine.position(buf.position() - outWidth);
                    currentLine.limit(buf.position());
                    buf.put(currentLine);
                }
            }
            buf.flip(); // setup buffer for reading
            return null; // could also return new Rectangle2D(x, y, w, h) specifying which is dirty
        });
        gc.drawImage(image, outMinX, outMaxY, outWidth, outHeight);
        ProcessingProfiler.getTimeDiff(start, "drawHeatMap");
    }
    
    private void addMissingAxes(Chart chart) {
        if (getXAxis() == null && chart instanceof XYChart) {
            setXAxis(((XYChart) chart).getXAxis());
        }
        if (getXAxis() == null) {
            Axis newXAxis = new DefaultNumericAxis("y-Axis");
            newXAxis.setSide(Side.LEFT);
            chart.getAxes().add(newXAxis);
            setXAxis(newXAxis);
        }
        if (getYAxis() == null && chart instanceof XYChart) {
            setYAxis(((XYChart) chart).getYAxis());
        }
        if (getYAxis() == null) {
            Axis newYAxis = new DefaultNumericAxis("y-Axis");
            newYAxis.setSide(Side.BOTTOM);
            chart.getAxes().add(newYAxis);
            setXAxis(newYAxis);
        }
        if (getZAxis() == null) {
            setZAxis((ColorGradientAxis) (chart.getAxes().stream()
                    .filter(axis -> (axis.getSide() != null && axis instanceof ColorGradientAxis)).findFirst()
                    .orElseGet(() -> {
                        Axis newZAxis = new ColorGradientAxis("z-Axis");
                        newZAxis.setSide(Side.RIGHT);
                        newZAxis.setAutoRanging(true);
                        chart.getAxes().add(newZAxis);
                        return newZAxis;
                    })));
        }
    }

    @Override
    public Canvas drawLegendSymbol(DataSet dataSet, int dsIndex, int width, int height) {
        // TODO not implemented yet
        return null;
    }

    @Override
    protected HeatMapRenderer getThis() {
        return this;
    }

    public BooleanProperty smoothProperty() {
        return smooth;
    }

    public boolean isSmooth() {
        return smooth.get();
    }

    public void setSmooth(final boolean newSmooth) {
        smooth.set(newSmooth);
    }

    public ObjectProperty<Axis> xAxisProperty() {
        return xAxis;
    }

    public Axis getXAxis() {
        return xAxis.get();
    }

    public void setXAxis(final Axis xAxis) {
        this.xAxis.set(xAxis);
    }

    public ObjectProperty<Axis> yAxisProperty() {
        return yAxis;
    }

    public Axis getYAxis() {
        return yAxis.get();
    }

    public void setYAxis(final Axis yAxis) {
        this.yAxis.set(yAxis);
    }

    public ObjectProperty<ColorGradientAxis> zAxisProperty() {
        return zAxis;
    }

    public ColorGradientAxis getZAxis() {
        return zAxis.get();
    }

    public void setZAxis(final ColorGradientAxis zAxis) {
        this.zAxis.set(zAxis);
    }
}
