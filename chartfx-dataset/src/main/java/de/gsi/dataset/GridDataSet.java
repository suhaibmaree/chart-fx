package de.gsi.dataset;

/**
 * A dataset for multidimensional data on cartesian grids.
 * The first nGrid dimensions span a grid, while the remaining
 * dimensions contain values associated with the grid points.
 *
 * @author Alexander Krimm
 */
public interface GridDataSet extends DataSet {

    /**
     * @return the number of dimensions spanning the grid
     */
    public int getNGrid();

    // get data count for individual dimensions should be removed from DataSet
    // or be a default implementation returning dataCount for each dimension
    @Override
    public int getDataCount(int i);

    /**
     * @param dimIndex
     * @param index
     * @return
     */
    public double getGrid(int dimIndex, int index);

    /**
     * Get Values on specific grid multiindices
     * 
     * @param dimIndex dimension to query. Usually bigger than nGrid, for < nGrid returns the specific index
     * @param index multi-index for the grid, missing dimensions are assumed to be zero.
     * @return value on the given grid index
     */
    public double getValue(int dimIndex, int... index);

    /**
     * @param dimX
     * @return
     */
    default double[] getGrid(int dimIndex) {
        final int n = getDataCount(dimIndex);
        final double[] result = new double[n];
        for (int i = 0; i < n; i++) {
            result[i] = getGrid(dimIndex, i);
        }
        return result;
    }
}
