package de.gsi.dataset.utils;

/**
 * Utility class used to examine function parameters. All the methods throw <code>IllegalArgumentException</code> if the
 * argument doesn't fulfil constraints.
 *
 * @author rstein
 */
public final class AssertUtils {
    private static final String MUST_BE_GREATER_THAN_OR_EQUAL_TO_0 = " must be greater than or equal to 0!";
    private static final String MUST_BE_NON_EMPTY = " must be non-empty!";

    private AssertUtils() {
    }

    /**
     * Checks if the object is not null.
     * @param <T> generics object to be checked
     *
     * @param name name to be included in exception message.
     * @param obj object to be checked
     */
    public static <T> void notNull(final String name, final T obj) {
        if (obj == null) {
            throw new IllegalArgumentException("The " + name + " must be non-null!");
        }
    }

    /**
     * Checks if the index is  &gt;= 0 and &lt; bounds
     * @param index index to be checked
     * @param bounds maximum bound
     */
    public static void indexInBounds(final int index, final int bounds) {
        AssertUtils.indexInBounds(index, bounds, "The index is out of bounds: 0 <= " + index + " < " + bounds);
    }

    /**
     * Checks if the index is  &gt;= 0 and &lt; bounds
     * @param index index to be checked
     * @param bounds maximum bound
     * @param message exception message
     */
    public static void indexInBounds(final int index, final int bounds, final String message) {
        if ((index < 0) || (index >= bounds)) {
            throw new IndexOutOfBoundsException(message);
        }
    }

    /**
     * Checks if the index1 &lt;= index2
     * 
     * @param index1 index1 to be checked
     * @param name1 name of index1
     * @param index2 index1 to be checked
     * @param name2 name of index2
     */
    public static void indexOrder(final int index1, final String name1, final int index2, final String name2) {
        if (index1 > index2) {
            throw new IndexOutOfBoundsException(
                    "Index " + name1 + "(" + index1 + ") is greated than index " + name2 + "(" + index2 + ")");
        }
    }

    /**
     * Checks if the index1 &lt;= index2
     *
     * @param index1 index1 to be checked
     * @param index2 index1 to be checked  
     * @param msg exception message
     */
    public static void indexOrder(final int index1, final int index2, final String msg) {
        if (index1 > index2) {
            throw new IndexOutOfBoundsException(msg);
        }
    }

    /**
     * Checks if the value is  &gt;= 0
     * @param <T> generics object to be checked
     *
     * @param name name to be included in the exception message
     * @param value to be checked
     */
    public static <T extends Number> void gtThanZero(final String name, final T value) {
        if (value.doubleValue() <= 0) {
            throw new IllegalArgumentException("The " + name + " must be greater than 0!");
        }
    }

    /**
     * Checks if the value is  &gt;= 0
     * @param <T> generics object to be checked
     *
     * @param name name to be included in the exception message
     * @param value to be checked
     */
    public static <T extends Number> void gtEqThanZero(final String name, final T value) {
        if (value.doubleValue() < 0) {
            throw new IllegalArgumentException("The " + name + MUST_BE_GREATER_THAN_OR_EQUAL_TO_0);
        }
    }

    /**
     * Asserts that the specified arrays have the same length.
     * @param array1 to be checked
     * @param array2 to be checked
     */
    public static void equalDoubleArrays(final double[] array1, final double[] array2) {
        if (array1.length != array2.length) {
            throw new IllegalArgumentException("The double arrays must have the same length! length1 = " + array1.length
                    + " vs. length2 = " + array2.length);
        }
    }

    /**
     * Asserts that the specified arrays have the same length or are at least min size.
     * 
     * @param array1 to be checked
     * @param array2 to be checked
     * @param nMinSize minimum required size
     */
    public static void equalDoubleArrays(final double[] array1, final double[] array2, final int nMinSize) {
        final int length1 = Math.min(nMinSize, array1.length);
        final int length2 = Math.min(nMinSize, array2.length);
        if (length1 != length2) {
            throw new IllegalArgumentException("The double arrays must have the same length! length1 = " + array1.length
                    + " vs. length2 = " + array2.length + " (nMinSize = " + nMinSize + ")");
        }
    }

    /**
     * Asserts that the specified arrays have the same length.
     * @param <T> generics object to be checked
     * 
     * @param array1 to be checked
     * @param array2 to be checked
     */
    public static <T> void equalDoubleArrays(final T[] array1, final T[] array2) {
        if (array1.length != array2.length) {
            throw new IllegalArgumentException("The double arrays must have the same length!");
        }
    }

    public static void nonEmptyArray(final String name, final Object[] array) {
        AssertUtils.notNull(name, array);
        if (array.length == 0) {
            throw new IllegalArgumentException("The " + name + MUST_BE_NON_EMPTY);
        }

        for (final Object element : array) {
            if (element == null) {
                throw new NullPointerException("Elements of the " + name + " must be non-null!"); // #NOPMD
            }
        }
    }

    public static void nonEmptyArray(final String name, final double[] array) {
        AssertUtils.notNull(name, array);
        if (array.length == 0) {
            throw new IllegalArgumentException("The " + name + MUST_BE_NON_EMPTY);
        }
    }

    public static void nonEmptyArray(final String name, final int[] array) {
        AssertUtils.notNull(name, array);
        if (array.length == 0) {
            throw new IllegalArgumentException("The " + name + MUST_BE_NON_EMPTY);
        }
    }

    public static void nonEmptyArray(final String name, final boolean[] array) {
        AssertUtils.notNull(name, array);
        if (array.length == 0) {
            throw new IllegalArgumentException("The " + name + MUST_BE_NON_EMPTY);
        }
    }

    public static void belongsToEnum(final String name, final int[] allowedElements, final int value) {
        for (final int allowedElement : allowedElements) {
            if (value == allowedElement) {
                return;
            }
        }
        throw new IllegalArgumentException("The " + name + " has incorrect value!");
    }

    /**
     * Asserts if the specified object is an instance of the specified type.
     * @param obj to be checked
     * @param type required class type
     *
     * @throws IllegalArgumentException in case of problems
     */
    public static void assertType(final Object obj, final Class<?> type) {
        if (!type.isInstance(obj)) {
            throw new IllegalArgumentException(
                    "The argument has incorrect type. The correct type is " + type.getName());
        }
    }

    /**
     * The method returns true if both values area equal. The method differs from simple == compare because it takes
     * into account that both values can be Double.NaN, in which case == operator returns <code>false</code>.
     * @param v1 to be checked
     * @param v2 to be checked
     *
     * @return <code>true</code> if v1 and v2 are Double.NaN or v1 == v2.
     */
    public static boolean areEqual(final double v1, final double v2) {
        return ((v1 != v1) && (v2 != v2)) || (v1 == v2);
    }

    /**
     * Checks if the int value is &gt;= 0
     *
     * @param name name to be included in the exception message
     * @param value to be checked
     */
    public static void gtThanZero(final String name, final int value) {
        if (value <= 0) {
            throw new IllegalArgumentException("The " + name + " must be greater than 0!");
        }
    }

    /**
     * Checks if the int value is  &gt;= 0
     *
     * @param name name to be included in the exception message
     * @param value to be checked
     */
    public static void gtEqThanZero(final String name, final int value) {
        if (value < 0) {
            throw new IllegalArgumentException("The " + name + MUST_BE_GREATER_THAN_OR_EQUAL_TO_0);
        }
    }

    /**
     * Checks if the int value is  &gt;= 0
     *
     * @param name name to be included in the exception message
     * @param value to be checked
     */
    public static void gtEqThanZero(final String name, final double value) {
        if (value < 0) {
            throw new IllegalArgumentException("The " + name + MUST_BE_GREATER_THAN_OR_EQUAL_TO_0);
        }
    }

    public static void checkArrayDimension(final String name, final double[] array, final int defaultLength) {
        AssertUtils.notNull(name, array);
        AssertUtils.nonEmptyArray(name, array);
        if (array.length != defaultLength) {
            throw new IllegalArgumentException("The " + name + " double array must have a length of " + defaultLength);
        }
    }

    public static void checkArrayDimension(final String name, final int[] array, final int defaultLength) {
        AssertUtils.notNull(name, array);
        AssertUtils.nonEmptyArray(name, array);
        if (array.length != defaultLength) {
            throw new IllegalArgumentException("The " + name + " int array must have a length of " + defaultLength);
        }
    }

    public static void checkArrayDimension(final String name, final boolean[] array, final int defaultLength) {
        AssertUtils.notNull(name, array);
        AssertUtils.nonEmptyArray(name, array);
        if (array.length != defaultLength) {
            throw new IllegalArgumentException("The " + name + " boolean array must have a length of " + defaultLength);
        }
    }
}