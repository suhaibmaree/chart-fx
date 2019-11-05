package de.gsi.dataset.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gsi.dataset.DataSet;

/**
 * @author akrimm
 */
public class DoubleGridDataSetTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(DoubleGridDataSet.class);

    @Test
    public void testGridDataSet() {
        // (0)| 1  2  3    (8)| 1  2  3
        // ---+--------    ---+--------
        //  4 | 1  2  3     4 |-3 -4 -5
        //  5 | 4  5  6     5 |-6 -7 -8
        //  6 | 7  8  9     6 |-9 11 22
        //  7 | 0 -1 -2     7 |33 44 55
        DoubleGridDataSet testGridDataSet = new DoubleGridDataSet("test", 3,
                new double[][] { { 1, 2, 3 }, { 4, 5, 6, 7 }, { 0, 8 } //
                        , { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, -1, -2, -3, -4, -5, -6, -7, -8, -9, 11, 22, 33, 44, 55 } });
        DataSet testDataSet = testGridDataSet;

        assertEquals(2, testDataSet.get(0, 7));
        assertEquals(6, testDataSet.get(1, 7));
        assertEquals(0, testDataSet.get(2, 7));
        assertEquals(8, testDataSet.get(3, 7));

        assertEquals(3, testGridDataSet.getValue(3, 2, 0));
        assertEquals(44, testGridDataSet.getValue(3, 1, 3, 1));
        assertEquals(6, testGridDataSet.getValue(1, 0, 2, 1));

        assertEquals(1, testGridDataSet.getIndex(1, 5));
        assertEquals(1, testGridDataSet.getIndex(1, 5.3));
        assertEquals(1, testGridDataSet.getIndex(1, 4.9));
        assertEquals(2, testGridDataSet.getIndex(1, 5.6));
    }

    @Test
    public void testConstructorExceptions() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            DoubleGridDataSet testGridDataSet = new DoubleGridDataSet("test", 3,
                    new double[][] { { 1, 2, 3 }, { 4, 5, 6, 7 }, { 0, 8 } //
                    , { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, -1, -2, -3, -4, -5, -6, -7, -8, -9, 11, 22, 33, 44 } });
        });
        assertEquals("The supplied data has inconsistent dimensionality", exception.getMessage());

        Exception exception2 = assertThrows(IllegalArgumentException.class, () -> {
            DoubleGridDataSet testGridDataSet = new DoubleGridDataSet("test", 4,
                    new double[][] { { 1, 2, 3 }, { 4, 5, 6, 7 }, { 0, 8 } //
                    , { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, -1, -2, -3, -4, -5, -6, -7, -8, -9, 11, 22, 33, 44, 55 } });
        });
        assertEquals("Dimensionality of data has to be higher than nGrid", exception2.getMessage());

    }

}
