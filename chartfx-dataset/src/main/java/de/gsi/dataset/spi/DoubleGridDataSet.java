package de.gsi.dataset.spi;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gsi.dataset.AxisDescription;
import de.gsi.dataset.GridDataSet;

/**
 * DoubleDataSet where all the values are allocated on a non-equidistant Cartesian grid.
 * The data storage is implemented as naive double arrays, which could change in the future.
 * For now the dataset is not editable, but provides some setter functionality
 * 
 * @author Alexander Krimm
 */
public class DoubleGridDataSet extends AbstractDataSet<DoubleGridDataSet> implements GridDataSet {
    private static final long serialVersionUID = 19092501;
    private static final Logger LOGGER = LoggerFactory.getLogger(DoubleGridDataSet.class);

    private int nGrid;
    private double[][] data;

    /**
     * @param name the name for the dataset
     * @param dimension the number of dimensions
     * @param nGrid the number of dimensions involved in spanning the grid (nGrid < dimension)
     */
    public DoubleGridDataSet(final String name, final int dimension, final int nGrid) {
        super(name, dimension);
        this.nGrid = nGrid;
        this.data = new double[dimension][];
        for (int i = 0; i < dimension; i++) {
            this.data[i] = new double[0];
        }
    }

    /**
     * @param name the name for the dataset
     * @param dimension the number of dimensions
     * @param shape the shape of the grid, number of points in x,y,... direction
     */
    public DoubleGridDataSet(final String name, final int dimension, final int[] shape) {
        super(name, dimension);
        this.nGrid = shape.length;
        this.data = new double[dimension][];
        int n = 1;
        for (int i = 0; i < dimension; i++) {
            if (i < this.nGrid) {
                this.data[i] = new double[shape[i]];
                n *= shape[i];
            } else {
                this.data[i] = new double[n];
            }
        }
    }

    /**
     * @param name the name for the dataset
     * @param dimension the number of dimensions
     * @param nGrid number of grid dimensions
     * @param data
     */
    public DoubleGridDataSet(final String name, final int nGrid, final double[][] data) {
        super(name, data.length);
        int n = 1;
        if (!(data.length > nGrid)) {
            throw new IllegalArgumentException("Dimensionality of data has to be higher than nGrid");
        }
        for (int i = 0; i < data.length; i++) {
            if (i < nGrid) {
                n *= data[i].length;
            } else {
                if (data[i].length != n) {
                    throw new IllegalArgumentException("The supplied data has inconsistent dimensionality");
                }
            }
        }
        this.nGrid = nGrid;
        this.data = data;
    }

    @Override
    public double get(final int dimIndex, final int index) {
        return data[dimIndex][dimIndex < nGrid ? decanonicaliseIndex(dimIndex, index) : index];
    }

    /**
     * @param dimIndex dimension
     * @param index canonical index
     * @return index in the array spanning dimension dimIndex
     */
    private final int decanonicaliseIndex(final int dimIndex, final int index) {
        int n = 1;
        for (int i = 0; i < dimIndex; i++) {
            n *= data[i].length;
        }
        return (index / n) % data[dimIndex].length;
    }

    /**
     * @param dimIndex dimension
     * @param indices full set of indices
     * @return canonical index
     */
    private final int canonicaliseIndex(final int[] indices) {
        int result = 0;
        int n = 1;
        for (int i = 0; i < getNGrid(); i++) {
            result += n * (i < indices.length ? indices[i] : 0); // assume missing indices to be 0
            n *= data[i].length;
        }
        return result;
    }

    @Override
    public int getIndex(int dimIndex, double value) {
        if (dimIndex < nGrid) {
            int result = Arrays.binarySearch(data[dimIndex], value);
            if (result >= 0) { // value is exact match, return index
                return result;
            }
            // return closest index
            if (result > -2)
                return 0;
            if (result <= -getDataCount(dimIndex))
                return getDataCount(dimIndex) - 1;
            final double diffhigh = data[dimIndex][-result - 1] - value;
            final double difflow = value - data[dimIndex][-result - 2];
            return (difflow > diffhigh) ? -result - 1 : -result - 2;
        }
        // TODO: not sure what should be returned here.
        // as it is dataSet interface it should return
        // canonical index, but that only works for a full set of values
        return 0;
    }

    @Override
    public double getValue(int dimIndex, double x) {
        // TODO: not really meaningful for high-dimensional data sets?
        return 0;
    }

    @Override
    public DoubleGridDataSet recomputeLimits(int dimension) {
        AxisDescription axis = getAxisDescription(dimension);
        if (dimension < getNGrid()) {
            axis.set(getGrid(dimension, 0), getGrid(dimension, getDataCount(dimension) - 1));
        } else {
            for (int j = 0; j < getDataCount(dimension); j++) {
                if (dimension < getNGrid()) {
                    axis.add(getGrid(dimension, j));
                } else {
                    axis.add(get(dimension, j));
                }
            }
        }
        return getThis();
    }

    @Override
    public int getNGrid() {
        return nGrid;
    }

    @Override
    public int getDataCount(int i) {
        return data[i].length;
    }

    @Override
    public int getDataCount() {
        return data[getNGrid()].length;
    }

    @Override
    public double getGrid(int dimIndex, int index) {
        return data[dimIndex][index];
    }

    @Override
    public double getValue(final int dimIndex, final int... indices) {
        return get(dimIndex, canonicaliseIndex(indices));
    }
}
