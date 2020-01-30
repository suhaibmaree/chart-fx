package de.gsi.dataset.spi;

import de.gsi.dataset.AxisDescription;
import de.gsi.dataset.DataSet;
import de.gsi.dataset.EditableDataSet;
import de.gsi.dataset.event.AddedDataEvent;
import de.gsi.dataset.event.RemovedDataEvent;
import de.gsi.dataset.event.UpdatedDataEvent;
import de.gsi.dataset.utils.AssertUtils;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

/**
 * Implementation of the {@code DataSet} interface which stores x,y,... values in nDim separate arrays. It provides
 * methods allowing easy manipulation of data points.
 *
 * @author rstein
 * @author akrimm
 */
@SuppressWarnings("PMD.TooManyMethods") // part of the flexible class nature
public class MultiDimDoubleDataSet extends AbstractDataSet<MultiDimDoubleDataSet> implements EditableDataSet {
    private static final long serialVersionUID = -493232313124620828L;
    protected DoubleArrayList[] values; // way faster than java default lists

    /**
     * Creates a new instance of <code>DoubleDataSet</code> as copy of another (deep-copy).
     *
     * @param another name of this DataSet.
     */
    public MultiDimDoubleDataSet(final DataSet another) {
        super(another.getName(), another.getDimension());
        this.set(another); // NOPMD by rstein on 25/06/19 07:42
    }

    /**
     * Creates a new instance of <code>DoubleDataSet</code>.
     *
     * @param name name of this DataSet.
     * @param nDim the number of dimensions
     * @throws IllegalArgumentException if {@code name} is {@code null}
     */
    public MultiDimDoubleDataSet(final String name, final int nDim) {
        this(name, nDim, 0);
    }

    /**
     * <p>
     * Creates a new instance of <code>DoubleDataSet</code>.
     * </p>
     * The user can specify via the copy parameter, whether the dataset wraps the input arrays themselves or on a
     * copies of the input arrays.
     *
     * @param name name of this data set.
     * @param values the values for the new dataset double[nDims][nDataPoints]
     * @param initalSize how many data points are relevant to be taken
     * @param deepCopy if true, the input array is copied
     * @throws IllegalArgumentException if any of the parameters is {@code null} or if arrays with coordinates have
     *             different lengths
     */
    public MultiDimDoubleDataSet(final String name, final double[][] values, final int initalSize,
            final boolean deepCopy) {
        super(name, values.length);
        int dataMaxIndex = initalSize;
        for (int i = 0; i < values.length; i++) {
            AssertUtils.notNull("data for dimension " + i, values[i]);
            dataMaxIndex = Math.min(dataMaxIndex, values[i].length);
        }
        this.values = new DoubleArrayList[values.length];
        for (int i = 0; i < values.length; i++) {
            if (deepCopy) {
                this.values[i] = new DoubleArrayList(dataMaxIndex);
                resize(dataMaxIndex);
                System.arraycopy(values[i], 0, this.values[i].elements(), 0, dataMaxIndex);
            } else {
                this.values[i] = DoubleArrayList.wrap(values[i]);
            }
        }
    }

    /**
     * Creates a new instance of <code>DoubleDataSet</code>.
     *
     * @param name name of this DataSet.
     * @param nDims the number of dimensions
     * @param initalSize initial capacity of buffer (N.B. size=0)
     * @throws IllegalArgumentException if {@code name} is {@code null}
     */
    public MultiDimDoubleDataSet(final String name, final int nDims, final int initalSize) {
        super(name, nDims);
        AssertUtils.gtEqThanZero("initalSize", initalSize);
        values = new DoubleArrayList[nDims];
        for (int i = 0; i < nDims; i++) {
            values[i] = new DoubleArrayList(initalSize);
        }
    }

    /**
     * Add point to the end of the data set
     *
     * @param values the coordinate of the new value
     * @return itself
     */
    public MultiDimDoubleDataSet add(final double... values) {
        return add(this.getDataCount(), values, null);
    }

    /**
     * Add point to the data set.
     *
     * @param values the coordinate of the new value
     * @param label the data label
     * @return itself (fluent design)
     */
    public MultiDimDoubleDataSet add(final double[] values, final String label) {
        lock().writeLockGuard(() -> {
            for (int i = 0; i < this.values.length; i++) {
                this.values[i].add(values[i]);
                getAxisDescription(i).add(values[i]);
            }
            if ((label != null) && !label.isEmpty()) {
                addDataLabel(this.values[0].size() - 1, label);
            }
        });
        return fireInvalidated(new UpdatedDataEvent(this, "add"));
    }

    /**
     * Add array vectors to data set.
     *
     * @param valuesNew the coordinates of the new value
     * @return itself
     */
    public MultiDimDoubleDataSet add(final double[][] valuesNew) {
        int nPoints = valuesNew[0].length;
        for (int i = 0; i < this.values.length; i++) {
            AssertUtils.notNull("coordinates dim " + i, valuesNew[i]);
            AssertUtils.checkArrayDimension("New Data for dim " + i, valuesNew[i], nPoints);
        }

        lock().writeLockGuard(() -> {
            for (int i = 0; i < this.values.length; i++) {
                this.values[i].addElements(values[i].size(), valuesNew[i], 0, nPoints);
                getAxisDescription(i).add(valuesNew[i]);
            }
        });

        return fireInvalidated(new AddedDataEvent(this));
    }

    /**
     * add point to the data set
     *
     * @param index data point index at which the new data point should be added
     * @param newValues the coordinates for the new point
     * @return itself (fluent design)
     */
    @Override
    public MultiDimDoubleDataSet add(final int index, final double... newValues) {
        return add(index, newValues, null);
    }

    /**
     * add point to the data set
     *
     * @param index data point index at which the new data point should be added
     * @param values coordinate of the new data point
     * @param label data point label (see CategoryAxis)
     * @return itself (fluent design)
     */
    public MultiDimDoubleDataSet add(final int index, final double[] values, final String label) {
        lock().writeLockGuard(() -> {
            final int indexAt = Math.max(0, Math.min(index, getDataCount() + 1));

            for (int i = 0; i < this.values.length; i++) {
                this.values[i].add(indexAt, values[i]);
                getAxisDescription(i).add(values[i]);
            }
            getDataLabelMap().addValueAndShiftKeys(indexAt, this.values[0].size(), label);
            getDataStyleMap().shiftKeys(indexAt, this.values[0].size());
        });
        return fireInvalidated(new AddedDataEvent(this));
    }

    /**
     * add point to the data set
     *
     * @param index data point index at which the new data points should be added
     * @param values the coordinates of the new points
     * @return itself (fluent design)
     */
    public MultiDimDoubleDataSet add(final int index, final double[][] values) {
        int nPoints = 0;
        for (int i = 0; i < this.values.length; i++) {
            AssertUtils.notNull("coordinates in dim " + i, values[i]);
            final int min = Math.min(nPoints, values[i].length);
        }

        lock().writeLockGuard(() -> {
            final int indexAt = Math.max(0, Math.min(index, getDataCount() + 1));
            for (int i = 0; i < this.values.length; i++) {
                this.values[i].addElements(indexAt, values[i], 0, nPoints);
                getAxisDescription(0).add(values[i], nPoints);
            }
            getDataLabelMap().shiftKeys(indexAt, this.values[0].size());
            getDataStyleMap().shiftKeys(indexAt, this.values[0].size());
        });
        return fireInvalidated(new AddedDataEvent(this));
    }

    /**
     * clear all data points
     *
     * @return itself (fluent design)
     */
    public MultiDimDoubleDataSet clearData() {
        lock().writeLockGuard(() -> {
            for (int i = 0; i < this.values.length; i++) {
                this.values[i].clear();
            }
            getDataLabelMap().clear();
            getDataStyleMap().clear();
            clearMetaInfo();

            getAxisDescriptions().forEach(AxisDescription::clear);
        });
        return fireInvalidated(new RemovedDataEvent(this, "clearData()"));
    }

    @Override
    public final double get(final int dimIndex, final int index) {
        return values[dimIndex].elements()[index];
    }

    /**
     * @return storage capacity of dataset
     */
    public int getCapacity() {
        int cap = 0;
        for (int i = 0; i < this.values.length; i++) {
            cap = Math.min(cap, values[i].elements().length);
        }
        return cap;
    }

    @Override
    public int getDataCount() {
        int size = 0;
        for (int i = 0; i < this.values.length; i++) {
            size = Math.min(size, values[i].size());
        }
        return size;
    }

    @Override
    public final double[] getValues(final int dimIndex) {
        return values[dimIndex].elements();
    }

    /**
     * @param amount storage capacity increase
     * @return itself (fluent design)
     */
    public MultiDimDoubleDataSet increaseCapacity(final int amount) {
        lock().writeLockGuard(() -> {
            final int size = getDataCount();
            resize(getCapacity() + amount);
            resize(size);
        });
        return getThis();
    }

    /**
     * remove point from data set
     *
     * @param index data point which should be removed
     * @return itself (fluent design)
     */
    @Override
    public EditableDataSet remove(final int index) {
        return remove(index, index + 1);
    }

    /**
     * removes sub-range of data points
     *
     * @param fromIndex start index
     * @param toIndex stop index
     * @return itself (fluent design)
     */
    public MultiDimDoubleDataSet remove(final int fromIndex, final int toIndex) {
        lock().writeLockGuard(() -> {
            AssertUtils.indexInBounds(fromIndex, getDataCount(), "fromIndex");
            AssertUtils.indexInBounds(toIndex, getDataCount(), "toIndex");
            AssertUtils.indexOrder(fromIndex, "fromIndex", toIndex, "toIndex");

            for (int i = 0; i < this.values.length; i++) {
                this.values[i].removeElements(fromIndex, toIndex);
            }

            // remove old label and style keys
            getDataLabelMap().remove(fromIndex, toIndex);
            getDataStyleMap().remove(fromIndex, toIndex);

            // invalidate ranges
            // -> fireInvalidated calls computeLimits for autoNotification
            getAxisDescriptions().forEach(AxisDescription::clear);
        });
        return fireInvalidated(new RemovedDataEvent(this));
    }

    /**
     * ensures minimum size, enlarges if necessary
     *
     * @param size the actually used array lengths
     * @return itself (fluent design)
     */
    public MultiDimDoubleDataSet resize(final int size) {
        lock().writeLockGuard(() -> {
            for (int i = 0; i < this.values.length; i++) {
                values[i].size(size);
            }
        });
        return fireInvalidated(new UpdatedDataEvent(this, "increaseCapacity()"));
    }

    /**
     * clear old data and overwrite with data from 'other' data set (deep copy)
     *
     * @param other the source data set
     * @return itself (fluent design)
     */
    public MultiDimDoubleDataSet set(final DataSet other) {
        lock().writeLockGuard(() -> other.lock().writeLockGuard(() -> {

            // deep copy data point labels and styles
            getDataLabelMap().clear();
            for (int index = 0; index < other.getDataCount(); index++) {
                final String label = other.getDataLabel(index);
                if ((label != null) && !label.isEmpty()) {
                    addDataLabel(index, label);
                }
            }
            getDataStyleMap().clear();
            for (int index = 0; index < other.getDataCount(); index++) {
                final String style = other.getStyle(index);
                if ((style != null) && !style.isEmpty()) {
                    addDataStyle(index, style);
                }
            }
            setStyle(other.getStyle());

            // copy data
            double[][] data = new double[other.getDimension()][];
            for (int i = 0; i < other.getDimension(); i++) {
                data[i] = other.getValues(i);
            }
            this.set(data, false);
        }));
        return fireInvalidated(new UpdatedDataEvent(this));
    }

    /**
     * <p>
     * Initialises the data set with specified data.
     * </p>
     * Note: The method copies values from specified double arrays.
     *
     * @param values coordinates
     * @return itself
     */
    public MultiDimDoubleDataSet set(final double[][] values) {
        return set(values, true);
    }

    /**
     * <p>
     * Initialises the data set with specified data.
     * </p>
     * Note: The method copies values from specified double arrays.
     *
     * @param values coordinates
     * @param copy true: makes an internal copy, false: use the pointer as is (saves memory allocation
     * @return itself
     */
    public MultiDimDoubleDataSet set(final double[][] values, final boolean copy) {
        int dataMaxIndex = 0;
        for (int i = 0; i < this.values.length; i++) {
            AssertUtils.notNull("X coordinates", values[i]);
            dataMaxIndex = Math.min(dataMaxIndex, values[i].length);
        }

        lock().writeLockGuard(() -> {
            getDataLabelMap().clear();
            getDataStyleMap().clear();
            if (copy) {
                for (int i = 0; i < this.values.length; i++) {
                    if (this.values[i] == null) {
                        this.values[i] = new DoubleArrayList();
                    }
                }
                resize(0);
                for (int i = 0; i < this.values.length; i++) {
                    this.values[i].addElements(0, values[i]);
                }
            } else {
                for (int i = 0; i < this.values.length; i++) {
                    this.values[i] = DoubleArrayList.wrap(values[i]);
                }
            }

            for (int i = 0; i < this.values.length; i++) {
                recomputeLimits(i);
            }
        });
        return fireInvalidated(new UpdatedDataEvent(this));
    }

    /**
     * replaces point coordinate of existing data point
     *
     * @param index data point index at which the new data point should be added
     * @param newValue new data point coordinate
     * @return itself (fluent design)
     */
    @Override
    public MultiDimDoubleDataSet set(final int index, final double... newValue) {
        lock().writeLockGuard(() -> {
            final int dataCount = Math.max(index + 1, this.getDataCount());
            for (int i = 0; i < this.values.length; i++) {
                this.values[i].size(dataCount);
                values[i].elements()[index] = newValue[i];
            }
            getDataLabelMap().remove(index);
            getDataStyleMap().remove(index);

            // invalidate ranges
            // -> fireInvalidated calls computeLimits for autoNotification
            getAxisDescriptions().forEach(AxisDescription::clear);
        });
        return fireInvalidated(new UpdatedDataEvent(this, "set - single"));
    }

    /**
     * Sets the values of the DataSet from index onwards.
     * Clears all labels in the overwritten section of data.
     * 
     * @param index start index of the data
     * @param values coordinates for the new points
     * @return itself
     */
    public MultiDimDoubleDataSet set(final int index, final double[][] values) {
        lock().writeLockGuard(() -> {
            resize(Math.max(index + values[0].length, this.values[0].size()));
            for (int i = 0; i < this.values.length; i++) {
                System.arraycopy(values[i], 0, this.values[i].elements(), index, values[i].length);
            }
            getDataLabelMap().remove(index, index + values[0].length);
            getDataStyleMap().remove(index, index + values[0].length);

            // invalidate ranges
            // -> fireInvalidated calls computeLimits for autoNotification
            getAxisDescriptions().forEach(AxisDescription::clear);
        });
        return fireInvalidated(new UpdatedDataEvent(this, "set - via arrays"));
    }

    /**
     * Trims the arrays list so that the capacity is equal to the size.
     *
     * @see java.util.ArrayList#trimToSize()
     * @return itself (fluent design)
     */
    public MultiDimDoubleDataSet trim() {
        lock().writeLockGuard(() -> {
            for (int i = 0; i < this.values.length; i++) {
                values[i].trim(i);
            }
        });
        return fireInvalidated(new UpdatedDataEvent(this, "increaseCapacity()"));
    }

    @Override
    public int getDataCount(int dimIndex) {
        return values[dimIndex].size();
    }

    @Override
    public int getIndex(int dimIndex, double value) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getValue(int dimIndex, double x) {
        final int index1 = getIndex(DIM_X, x);
        final double x1 = get(DIM_X, index1);
        final double y1 = get(dimIndex, index1);
        int index2 = x1 < x ? index1 + 1 : index1 - 1;
        index2 = Math.max(0, Math.min(index2, this.getDataCount() - 1));
        final double y2 = get(dimIndex, index2);
        if (Double.isNaN(y1) || Double.isNaN(y2)) {
            // case where the function has a gap (y-coordinate equals to NaN
            return Double.NaN;
        }

        final double x2 = get(DIM_X, index2);
        if (x1 == x2) {
            return y1;
        }

        return y1 + (((y2 - y1) * (x - x1)) / (x2 - x1));
    }
}
