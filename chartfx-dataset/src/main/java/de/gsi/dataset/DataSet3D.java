package de.gsi.dataset;

/**
 * A <code>DataSet</code> extension used to represent 3-dimensional data points.
 * 
 * @deprecated DataSet now handles arbitrary numbers of dimensions. Additionally the get Method defined here assumes
 *             that the 3D Data is represented on a grid, which might not always be the case.
 * @author gkruk
 * @author rstein
 */
@Deprecated
public interface DataSet3D extends DataSet2D {

    @Override
    default int getDimension() {
        return 3; //TODO: get rid of this method
    }

    /**
     * Returns Z coordinate for the specified data point.
     *
     * @param xIndex
     *            index of X coordinate
     * @param yIndex
     *            index of Y coordinate
     * @return Z coordinate
     */
    double getZ(int xIndex, int yIndex);

}
