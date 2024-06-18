package com.xiaohe.nacos.common.utils;

import java.util.Arrays;

/**
 * Array utils.
 *
 * @author zzq
 */
public class ArrayUtils {

    private ArrayUtils() {
    }

    /**
     * <p>Checks if an array of Objects is empty or {@code null}.</p>
     *
     * @param array  the array to test
     * @return {@code true} if the array is empty or {@code null}
     */
    public static boolean isEmpty(final Object[] array) {
        return array == null || array.length == 0;
    }

    /**
     * <p>Checks if the object is in the given array.</p>
     *
     * <p>The method returns {@code false} if a {@code null} array is passed in.</p>
     *
     * @param array  the array to search through
     * @param objectToFind  the object to find
     * @return {@code true} if the array contains the object
     */
    public static boolean contains(final Object[] array, final Object objectToFind) {
        if (array == null) {
            return false;
        }

        return Arrays.asList(array).contains(objectToFind);
    }

}