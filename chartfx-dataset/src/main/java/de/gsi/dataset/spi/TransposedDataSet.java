package de.gsi.dataset.spi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gsi.dataset.AxisDescription;
import de.gsi.dataset.DataSet;
import de.gsi.dataset.GridDataSet;
import de.gsi.dataset.event.AxisChangeEvent;
import de.gsi.dataset.event.EventListener;
import de.gsi.dataset.locks.DataSetLock;

/**
 * Allows permutation of the axes of an underlying DataSet, for applications like:
 * - transposed display
 * - reduction of multi-dimensional DataSets to lower dimensions
 * To be able to handle different DataSet Interface types, the constructors are private
 * and only acessible via the static class methods, which return the correct subtype.
 *
 * @author Alexander Krimm
 */
public class TransposedDataSet implements DataSet {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransposedDataSet.class);
    private static final long serialVersionUID = 2019092401;
    private final DataSet dataSet;
    private int[] permutation;
    private boolean transposed;

    private TransposedDataSet(final DataSet dataSet, final boolean transposed) {
        if (dataSet == null) {
            throw new IllegalArgumentException("DataSet is null");
        }
        this.dataSet = dataSet;
        permutation = new int[dataSet.getDimension()];
        this.transposed = transposed;
        for (int i = 0; i < permutation.length; i++) {
            permutation[i] = i;
        }
        if (transposed) {
            permutation[0] = 1;
            permutation[1] = 0;
        }
    }

    /**
     * @param dataSet reference to source data set
     * @param permutation array containing permutation of dimension index (e.g. '0,1,2,...' for normal and
     *            '1,0,2,...' for inverting the first with second axis)
     */
    private TransposedDataSet(final DataSet dataSet, final int[] permutation) {
        if (dataSet == null) {
            throw new IllegalArgumentException("DataSet is null");
        }
        
        if (permutation == null) {
            throw new IllegalArgumentException("permutation is null");
        }
        if (permutation.length < dataSet.getDimension()) {
            throw new IllegalArgumentException("insufficient permutation.lenght='" + permutation.length
                    + "' w.r.t. DataSet dimensions (" + dataSet.getDimension() + ")");
        }
        for (int i = 0; i < dataSet.getDimension(); i++) {
            if (permutation[i] >= dataSet.getDimension()) {
                throw new IndexOutOfBoundsException("permutation[" + i + "] contains dimIndex='" + permutation[i]
                        + "' outside DataSet dimension (" + dataSet.getDimension() + ")");
            }
        }

        this.dataSet = dataSet;
        this.permutation = Arrays.copyOf(permutation, this.dataSet.getDimension());
        this.transposed = false;
    }

    @Override
    public AtomicBoolean autoNotification() {
        return dataSet.autoNotification();
    }

    @Override
    public double get(int dimIndex, int index) {
        return dataSet.get(permutation[dimIndex], index);
    }

    @Override
    public List<AxisDescription> getAxisDescriptions() {
        ArrayList<AxisDescription> result = new ArrayList<>();
        for (int dimIndex : permutation) {
            result.add(dataSet.getAxisDescription(dimIndex));
        }
        return result;
    }

//    @Override
//    public AxisDescription getAxisDescription(final int dimIndex) {
//        return dataSet.getAxisDescription(permutation[dimIndex]);
//    }

    @Override
    public int getDataCount(int dimIndex) {
        return dataSet.getDataCount(permutation[dimIndex]);
    }

    @Override
    public String getDataLabel(int index) {
        return dataSet.getDataLabel(index);
    }

    @Override
    public int getDimension() {
        return permutation.length;
    }

    @Override
    public int getIndex(int dimIndex, double value) {
        return dataSet.getIndex(permutation[dimIndex], value);
    }

    @Override
    public String getName() {
        return dataSet.getName();
    }

    public int[] getPermutation() {
        return Arrays.copyOf(permutation, permutation.length);
    }

    @Override
    public String getStyle() {
        return dataSet.getStyle();
    }

    @Override
    public String getStyle(int index) {
        return dataSet.getStyle(index);
    }

    @Override
    public double getValue(int dimIndex, double x) {
        return dataSet.getValue(permutation[dimIndex], x);
    }

    /**
     * @param dimIndex the dimension index (ie. '0' equals 'X', '1' equals 'Y')
     * @return the x value array
     */
    @Override
    public double[] getValues(final int dimIndex) {
        final int n = getDataCount(permutation[dimIndex]);
        final double[] retValues = new double[n];
        for (int i = 0; i < n; i++) {
            retValues[i] = get(dimIndex, i);
        }
        return retValues;
    }

    public boolean isTransposed() {
        return transposed;
    }

    @Override
    public <D extends DataSet> DataSetLock<D> lock() {
        return dataSet.lock();
    }

    @Override
    public DataSet recomputeLimits(int dimension) {
        return dataSet.recomputeLimits(permutation[dimension]);
    }

    public void setPermutation(final int[] permutation) {
        if (permutation == null) {
            throw new IllegalArgumentException("permutation is null");
        }
        this.lock().writeLockGuard(() -> {
            if (permutation.length < dataSet.getDimension()) {
                throw new IllegalArgumentException("insufficient permutation.lenght='" + permutation.length
                        + "' w.r.t. DataSet dimensions (" + dataSet.getDimension() + ")");
            }
            for (int i = 0; i < dataSet.getDimension(); i++) {
                if (permutation[i] >= dataSet.getDimension()) {
                    throw new IndexOutOfBoundsException("permutation[" + i + "] contains dimIndex='" + permutation[i]
                            + "' outside DataSet dimension (" + dataSet.getDimension() + ")");
                }
            }

            this.permutation = Arrays.copyOf(permutation, dataSet.getDimension());
            if (transposed) {
                final int tmp = this.permutation[1];
                this.permutation[1] = this.permutation[0];
                this.permutation[0] = tmp;
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.atDebug().addArgument(this.permutation).log("applied permutation: {}");
            }
        });
        this.invokeListener(new AxisChangeEvent(this, "Permutation changed", -1));
    }

    @Override
    public DataSet setStyle(String style) {
        return this.lock().writeLockGuard(() -> dataSet.setStyle(style));
    }

    public void setTransposed(final boolean transposed) {
        this.lock().writeLockGuard(() -> {
            if (this.transposed != transposed) {
                final int tmp = this.permutation[1];
                this.permutation[1] = this.permutation[0];
                this.permutation[0] = tmp;
                this.transposed = transposed;
            }
        });
        this.invokeListener(new AxisChangeEvent(this, "(Un)transposed", -1));
    }

    @Override
    public List<EventListener> updateEventListener() {
        return dataSet.updateEventListener();
    }

    public static TransposedDataSet permute(DataSet dataSet, int[] permutation) {
        if (dataSet instanceof GridDataSet) {
            return new TransposedGridDataSet((GridDataSet) dataSet, permutation);
        }
        return new TransposedDataSet(dataSet, permutation);
    }

    public static TransposedDataSet transpose(DataSet dataSet) {
        return TransposedDataSet.transpose(dataSet, true);
    }

    public static TransposedDataSet transpose(DataSet dataSet, boolean transpose) {
        if (dataSet instanceof GridDataSet) {
            return new TransposedGridDataSet((GridDataSet) dataSet, transpose);
        }
        return new TransposedDataSet(dataSet, transpose);
    }

    /**
     * TODO: allow permutations to change number of grid dimensions, while enforcing contract, that all
     * grid axes must come before data axes.
     * 
     * @author Alexander Krimm
     */
    public static class TransposedGridDataSet extends TransposedDataSet implements GridDataSet {
        private static final long serialVersionUID = 19092601;
        private int nGrid;

        private TransposedGridDataSet(final GridDataSet dataSet, final boolean transposed) {
            super(dataSet, transposed);
            nGrid = dataSet.getNGrid();
        }

        /**
         * @param dataSet the source DataSet to initialise from
         * @param permutation the initial permutation index

         */
        private TransposedGridDataSet(final GridDataSet dataSet, final int[] permutation) {
            super(dataSet, permutation);
            for (int i = 0; i < dataSet.getNGrid(); i++) {
                if (permutation[i] >= dataSet.getNGrid()) {
                    throw new IllegalArgumentException(
                            "Permuting the non-grid dimensions to grid-dimensions is not supported");
                }
            }
        }

        @Override
        public void setPermutation(final int[] permutation) {
            boolean inGrid = true;
            for (int i = 0; i < permutation.length; i++) {
                if (inGrid) {
                    if (permutation[i] >= ((GridDataSet) super.dataSet).getNGrid()) {
                        inGrid = false;
                        nGrid = i;
                    }
                } else {
                    if (permutation[i] < ((GridDataSet) super.dataSet).getNGrid()) {
                        throw new IllegalArgumentException(
                                "All grid dimensions must be before value dimensions in the permutation");
                    }
                }
            }
            super.setPermutation(permutation);
        }

        @Override
        public int getNGrid() {
            return nGrid;
        }

        @Override
        public double getGrid(final int dimIndex, final int index) {
            return ((GridDataSet) super.dataSet).getGrid(super.permutation[dimIndex], index);
        }

        @Override
        public double getValue(final int dimIndex, final int... index) {
            GridDataSet gridDataSet = (GridDataSet) super.dataSet;
            int[] permutedIndex = new int[gridDataSet.getNGrid()];
            for (int i = 0; i < gridDataSet.getNGrid(); i++) {
                int perm = i < super.permutation.length ? super.permutation[i] : i;
                int idx = perm < index.length ? index[perm] : 0;
                permutedIndex[i] = idx;
            }
            return gridDataSet.getValue(super.permutation[dimIndex], permutedIndex);
        }
    }
}
