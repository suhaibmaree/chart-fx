package de.gsi.dataset;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gsi.dataset.AxisDescription;
import de.gsi.dataset.DataSet;
import de.gsi.dataset.spi.DefaultAxisDescription;
import de.gsi.dataset.spi.DefaultDataSet;
import de.sandec.jmemorybuddy.JMemoryBuddy;

/**
 * First memory leak testing stub
 * 
 * @author rstein
 */
public class DataSetMemoryLeakTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSetMemoryLeakTests.class);

    @Test
    public void LeakCheckerTest() {
        final Object referenced = new Object();
        JMemoryBuddy.doMemTest(checker -> {
            Object notReferenced = new Object();
            checker.assertCollectable(notReferenced); // not referenced should be collectable
        });

        boolean caughtException = false;
        try {
            JMemoryBuddy.doMemTest(checker -> {
                checker.assertCollectable(referenced); // not collectable and should throw an exception
            });
        } catch (Exception ex) {
            caughtException = true;
            if (LOGGER.isDebugEnabled()) {
                LOGGER.atDebug().log("intercepted correct exception");
            }
        }
        assertTrue(caughtException, "did not intercept correct exception");
    }

    @Test
    public void MemoryLeakTest() {
        DataSet dataSet = new DefaultDataSet("TestAxisDescription");

        JMemoryBuddy.doMemTest(checker -> {
            AxisDescription notReferenced = dataSet.getAxisDescription(0);
            dataSet.getAxisDescriptions().set(0, new DefaultAxisDescription(dataSet, "axis name", "axis unit"));
            checker.assertCollectable(notReferenced); // not referenced should be collectable
        });
    }
}
