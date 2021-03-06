/* Copyright (c) 2001-2002, The HSQL Development Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the HSQL Development Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL HSQL DEVELOPMENT GROUP, HSQLDB.ORG, 
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package org.hsqldb.lib;

import java.lang.reflect.*;

/**
 * Collection of static methods for operations on arrays
 *
 * @author fredt@users
 * @version 1.7.2
 */
public class ArrayUtil {

    /**
     *   Basic sort for small arrays.
     */
    public static void sortArray(int intarr[]) {

        boolean swapped;

        do {
            swapped = false;

            for (int i = 0; i < intarr.length - 1; i++) {
                if (intarr[i] > intarr[i + 1]) {
                    int temp = intarr[i + 1];

                    intarr[i + 1] = intarr[i];
                    intarr[i]     = temp;
                    swapped       = true;
                }
            }
        } while (swapped);
    }

    /**
     * For sets == true returns true if a and b are the same length and
     * contain the same set of integers. For sets == false returns the result
     * of haveEqualArrays(a,b,count)
     *
     */
    public static boolean haveEquality(int[] a, int[] b, int count,
                                       boolean sets) {

        if (sets) {
            if (a.length == b.length && count == a.length
                    && ArrayUtil.haveEqualSets(a, b, count)) {
                return true;
            }
        } else {
            if (ArrayUtil.haveEqualArrays(a, b, count)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if the first count elements of a and b are identical sets
     * of integers
     *
     */
    public static boolean haveEqualSets(int[] a, int[] b, int count) {

        if (count > a.length || count > b.length) {
            return false;
        }

        if (count == 1) {
            return a[0] == b[0];
        }

        int[] tempa = new int[count];
        int[] tempb = new int[count];

        copyArray(a, tempa, count);
        copyArray(b, tempb, count);
        sortArray(tempa);
        sortArray(tempb);

        for (int j = 0; j < count; j++) {
            if (tempa[j] != tempb[j]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns true if the first count elements of a and b are identical
     * subarrays of integers
     *
     */
    public static boolean haveEqualArrays(int[] a, int[] b, int count) {

        if (count > a.length || count > b.length) {
            return false;
        }

        if (count == 1) {
            return a[0] == b[0];
        }

        for (int j = 0; j < count; j++) {
            if (a[j] != b[j]) {
                return false;
            }
        }

        return true;
    }

    /**
     *  Checks for any overlap between two arrays of column indexes.
     *  Limit check to lenb elements of b
     */
    public static boolean haveCommonElement(int[] a, int[] b, int lenb) {

        for (int i = 0; i < a.length; i++) {
            int c = a[i];

            for (int j = 0; j < lenb; j++) {
                if (c == b[j]) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Return the overlap between two arrays of column indexes.
     * Ordering of the result arrays will be the same as in array
     * a. The method silently assumes that each index is only listed
     * once in the two input arrays.
     * <p>
     * e.g.
     * </p>
     * <code>
     * <table width="90%" bgcolor="lightblue">
     * <tr><td colspane="3">The arrays</td></tr>
     * <tr><td>int []a</td><td>=</td><td>{2,11,5,8}</td></tr>
     * <tr><td>int []b</td><td>=</td><td>{20,8,10,11,28,12}</td></tr>
     * <tr><td colspane="3">will result in:</td></tr>
     * <tr><td>int []c</td><td>=</td><td>{11,8}</td></tr>
     * </table>
     *
     * @param a int[]; first column indexes
     *
     * @param b int[]; second column indexes
     *
     * @return int[] common indexes or <code>null</code> if there is no overlap.
     *
     * @short Return the overlap between two arrays of column indexes.
     */
    public static int[] commonElements(int[] a, int[] b) {

        int[] c = null;
        int   n = countCommonElements(a, b);

        if (n > 0) {
            c = new int[n];

            int k = 0;

            for (int i = 0; i < a.length; i++) {
                for (int j = 0; j < b.length; j++) {
                    if (a[i] == b[j]) {
                        c[k++] = a[i];
                    }
                }
            }
        }

        return c;
    }

    /**
     * Return the number of elements shared by two column index arrays.
     * This method silently assumes that each element index is only listed
     * once in each index array. Otherwise the returned number will NOT
     * represent the number of unique indexes shared by both index array.
     *
     * @param a int[]; first array of column indexes.
     *
     * @param b int[]; second array of column indexes
     *
     * @return int; number of elements shared by <code>a</code> and <code>b</code>
     */
    public static int countCommonElements(int[] a, int[] b) {

        int k = 0;

        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < b.length; j++) {
                if (a[i] == b[j]) {
                    k++;
                }
            }
        }

        return k;
    }

    public static void copyArray(Object source, Object dest, int count) {
        System.arraycopy(source, 0, dest, 0, count);
    }

    public static Object resizeArray(Object source, int newsize) {

        Object newarray =
            Array.newInstance(source.getClass().getComponentType(), newsize);
        int oldsize = Array.getLength(source);

        if (oldsize < newsize) {
            newsize = oldsize;
        }

        System.arraycopy(source, 0, newarray, 0, newsize);

        return newarray;
    }

    /**
     *  Copies elements of source to dest. If adjust is -1 the element at
     *  colindex is not copied. If adjust is +1 that element is filled with
     *  the Object addition. All the rest of the elements in source are
     *  shifted left or right accordingly when they are copied.
     *
     *  No checks are perfomed on array sizes and an exception is thrown
     *  if they are not consistent with the other arguments.
     *
     * @param  source
     * @param  dest
     * @param  addition
     * @param colindex
     * @param  adjust +1 or 0 or -1
     * return new, adjusted array or null if an element is removed
     */
    public static void copyAdjustArray(Object[] source, Object[] dest,
                                       Object addition, int colindex,
                                       int adjust) {

        int i;

        for (i = 0; i < colindex; i++) {
            dest[i] = source[i];
        }

        if (i == dest.length) {
            return;
        }

        if (adjust < 0) {
            i++;
        } else {
            dest[i] = addition;
        }

        for (; i < source.length; i++) {
            dest[i + adjust] = source[i];
        }
    }

    /**
     * Returns new array with the elements in collar ajusted to reflect
     * changes at colindex.
     *
     * Each element in collarr represents an index into another array
     * otherarr.
     * colindex is the index at which an element is added or removed form
     * otherarr. Each element in the result array represents the new,
     * adjusted index to otherarr.
     * For each element of collarr that represents an index equal to
     * colindex and adjust is -1, the result will not contain that element
     * and will be shorter than collar by one element.
     *
     *
     * @param  colarr
     * @param  colindex
     * @param  adjust +1 or 0 or -1
     * return new, adjusted array
     */
    public static int[] toAdjustedColumnArray(int[] colarr, int colindex,
            int adjust) {

        if (colarr == null) {
            return null;
        }

        int[] intarr = new int[colarr.length];
        int   j      = 0;

        for (int i = 0; i < colarr.length; i++) {
            if (colarr[i] > colindex) {
                intarr[j] = colarr[i] + adjust;

                j++;
            } else if (colarr[i] == colindex) {
                if (adjust < 0) {

                    // skip an element from colarr
                } else {
                    intarr[j] = colarr[i] + adjust;

                    j++;
                }
            } else {
                intarr[j] = colarr[i];

                j++;
            }
        }

        if (colarr.length != j) {
            int[] newarr = new int[j];

            copyArray(intarr, newarr, j);

            return newarr;
        }

        return intarr;
    }

    /**
     * Convenience wrapper for toAdjustedColumnArray() that creates the new
     * array.
     */
    public static int[] getAdjustedColumnArray(int[] colarr, int size,
            int colindex, int adjust) {

        int[] newarr = new int[size];

        copyArray(colarr, newarr, size);

        return toAdjustedColumnArray(newarr, colindex, adjust);
    }

    /**
     *  Copies some elements of row into colobject by using colindex as the
     *  list of indexes into row. colindex and colobject are of equal length
     *  and normally shorter than row;
     *
     */
    public static void copyColumnValues(Object row[], int colindex[],
                                        Object colobject[]) {

        for (int i = 0; i < colindex.length; i++) {
            colobject[i] = row[colindex[i]];
        }
    }
/*
    public static void main(String[] args) {

        int[] a = new int[] {
            23, 11, 37, 7, 1, 5
        };
        int[] b = new int[] {
            1, 3, 7, 11, 13, 17, 19, 3, 1
        };
        int[] c = toAdjustedColumnArray(a, 7, -1);
        int[] d = toAdjustedColumnArray(b, 11, 1);
        int[] e = new int[a.length];

        copyArray(a, e, a.length);
        sortArray(e);

        int[] f = new int[b.length];

        copyArray(b, f, b.length);
        sortArray(f);

        boolean x = haveEqualSets(a, e, a.length);
        boolean y = haveEqualSets(b, f, b.length);

        System.out.print("test passed: ");
        System.out.print(x == true && y == true && c.length == a.length - 1
                         && d.length == b.length);
    }
*/
}
