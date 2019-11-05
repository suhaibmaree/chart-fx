package de.gsi.dataset.spi;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gsi.dataset.DataSet;
import de.gsi.dataset.GridDataSet;
import de.gsi.dataset.spi.TransposedDataSet.TransposedGridDataSet;

/**
 * @author akrimm
 */
public class TransposedDataSetTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransposedDataSetTest.class);

    @Test
    public void testWithDataSet2D() {
        DataSet dataSet = new DataSetBuilder().setName("Test Default Data Set")
                .setXValuesNoCopy(new double[] { 1, 2, 3 }).setYValuesNoCopy(new double[] { 4, 7, 6 }).build();

        TransposedDataSet transposed0 = TransposedDataSet.transpose(dataSet, true);
        assertArrayEquals(new int[] { 1, 0 }, transposed0.getPermutation());

        assertEquals(dataSet.get(0, 0), transposed0.get(1, 0));
        assertEquals(dataSet.get(1, 0), transposed0.get(0, 0));

        TransposedDataSet transposed1 = TransposedDataSet.transpose(dataSet, false);
        assertArrayEquals(new int[] { 0, 1 }, transposed1.getPermutation());

        assertEquals(dataSet.get(0, 0), transposed1.get(0, 0));
        assertEquals(dataSet.get(1, 0), transposed1.get(1, 0));

        TransposedDataSet transposed2 = TransposedDataSet.permute(dataSet, new int[] { 1, 0 });
        assertArrayEquals(new int[] { 1, 0 }, transposed2.getPermutation());

        assertEquals(dataSet.get(0, 0), transposed2.get(1, 0));
        assertEquals(dataSet.get(1, 0), transposed2.get(0, 0));

        transposed2.setTransposed(true);
        assertArrayEquals(new int[] { 0, 1 }, transposed2.getPermutation());

        transposed2.setPermutation(new int[] { 1, 0 });
        assertArrayEquals(new int[] { 0, 1 }, transposed2.getPermutation());

        transposed2.setTransposed(false);
        assertArrayEquals(new int[] { 1, 0 }, transposed2.getPermutation());

        assertEquals(dataSet.get(0, 0), transposed2.get(1, 0));
        assertEquals(dataSet.get(1, 0), transposed2.get(0, 0));

    }

    @Test
    public void testWithGridDataSet() {
        GridDataSet dataSet = new DoubleGridDataSet("Test 3D Dataset", 2,
                new double[][] { { 1.0, 2.0, 3.0 }, { 0.001, 4.2 }, { 1.3, 3.7, 4.2, 2.3, 1.8, 5.0 } });

        TransposedDataSet transposedDataSet = TransposedDataSet.transpose(dataSet);
        
        assertTrue(transposedDataSet instanceof GridDataSet);
        TransposedGridDataSet transposedGridDataSet = (TransposedGridDataSet) transposedDataSet;
        
        assertEquals(dataSet.getValue(2, 2, 1), transposedGridDataSet.getValue(2, 1, 2));
    }

}
