package de.gsi.dataset.spi;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gsi.dataset.AxisDescription;
import de.gsi.dataset.DataSet;
import de.gsi.dataset.DataSet3D;
import de.gsi.dataset.GridDataSet;
import de.gsi.dataset.event.EventListener;
import de.gsi.dataset.locks.DataSetLock;

/**
 *
 * @author Alexander Krimm
 */
public class Legacy3DGridDataSetWrapper implements GridDataSet {
    private static final Logger LOGGER = LoggerFactory.getLogger(Legacy3DGridDataSetWrapper.class);
    private static final long serialVersionUID = 1L;

    private final DataSet3D wrappedDataSet3D;
    
    /**
     * 
     */
    public Legacy3DGridDataSetWrapper(final DataSet3D wrappedDataSet3D) {
        this.wrappedDataSet3D = wrappedDataSet3D;
    }

    @Override
    public double get(int dimIndex, int index) {
        switch (dimIndex) {
        case DIM_X:
            return wrappedDataSet3D.get(DIM_X, index % getDataCount(DIM_X));
        case DIM_Y:
            return wrappedDataSet3D.get(DIM_Y, index / getDataCount(DIM_X));
        case DIM_Z:
            return wrappedDataSet3D.getZ(index % getDataCount(DIM_X), index / getDataCount(DIM_X));
        default:
            throw new IllegalArgumentException("Dimension Index out of Bounds");
        }
    }

    @Override
    public List<AxisDescription> getAxisDescriptions() {
        return wrappedDataSet3D.getAxisDescriptions();
    }

    @Override
    public String getDataLabel(int index) {
        return wrappedDataSet3D.getDataLabel(index);
    }

    @Override
    public int getDimension() {
        return 3;
    }

    @Override
    public int getIndex(int dimIndex, double value) {
        return wrappedDataSet3D.getIndex(dimIndex, value);
    }

    @Override
    public String getName() {
        return wrappedDataSet3D.getName();
    }

    @Override
    public String getStyle() {
        return wrappedDataSet3D.getStyle();
    }

    @Override
    public String getStyle(int index) {
        return wrappedDataSet3D.getStyle(index);
    }

    @Override
    public double getValue(int dimIndex, double x) {
        throw new UnsupportedOperationException("Deprecated Method not implemented");
    }

    @Override
    public <D extends DataSet> DataSetLock<D> lock() {
        return wrappedDataSet3D.lock();
    }

    @Override
    public DataSet recomputeLimits(int dimension) {
        return wrappedDataSet3D.recomputeLimits(dimension);
    }

    @Override
    public DataSet setStyle(String style) {
        return wrappedDataSet3D.setStyle(style);
    }

    @Override
    public AtomicBoolean autoNotification() {
        return wrappedDataSet3D.autoNotification();
    }

    @Override
    public List<EventListener> updateEventListener() {
        return wrappedDataSet3D.updateEventListener();
    }

    @Override
    public int getNGrid() {
        return 2;
    }

    @Override
    public int getDataCount(int i) {
        return wrappedDataSet3D.getDataCount(i);
    }

    @Override
    public double getGrid(int dimIndex, int index) {
        switch (dimIndex) {
        case DIM_X:
            return wrappedDataSet3D.get(DIM_X, index);
        case DIM_Y:
            return wrappedDataSet3D.get(DIM_Y, index / getDataCount(DIM_X));
        default:
            throw new IllegalArgumentException("Dimension Index out of Bounds 2");
        }
    }

    @Override
    public double getValue(int dimIndex, int... index) {
        if (index.length != 2) {
            throw new IllegalArgumentException("wrong number of Indices, should be 2");
        }
        switch (dimIndex) {
        case DIM_X:
            return wrappedDataSet3D.get(DIM_X, index[DIM_X]);
        case DIM_Y:
            return wrappedDataSet3D.get(DIM_Y, index[DIM_Y]);
        case DIM_Z:
            return wrappedDataSet3D.getZ(index[DIM_X], index[DIM_Y]);
        default:
            throw new IllegalArgumentException("Dimension Index out of Bounds");
        }
    }
}

