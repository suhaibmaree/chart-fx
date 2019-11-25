package de.gsi.dataset;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        JMemoryBuddy.memoryTest(checker -> {
            Object notReferenced = new Object();
            checker.assertCollectable(notReferenced); // not referenced should be collectable
        });

        AssertionError error = assertThrows(AssertionError.class, () -> {
            JMemoryBuddy.memoryTest(checker -> {
                checker.assertCollectable(referenced); // not collectable and should throw an exception
            });
        });
        assertTrue(error.getMessage().startsWith("Content of WeakReference was not collected"));
    }

    @Test
    public void MemoryLeakTest() {
        DataSet dataSet = new DefaultDataSet("TestAxisDescription");

        JMemoryBuddy.memoryTest(checker -> {
            AxisDescription notReferenced = dataSet.getAxisDescription(0);
            dataSet.getAxisDescriptions().set(0, new DefaultAxisDescription(dataSet, "axis name", "axis unit"));
            checker.assertCollectable(notReferenced); // not referenced should be collectable
        });
    }
}
