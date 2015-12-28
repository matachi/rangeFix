/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.util;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author leonardo
 */
public class CollectionsUtils {
    public static <T> Collection<T> filter(Filter<T> filter, Collection<T> collection) {
        List<T> res = new LinkedList<T>() ;
        for(T elem : collection)
            if (filter.accepts(elem))
                res.add(elem) ;
        return res ;
    }


    public static <T> boolean isEmpty(Collection<T> c) {
        return c == null || c.isEmpty() ;
    }

    @SuppressWarnings("unchecked")
    public static <U, T> T foldRight(Collection<U> list, Function<U,T> f, T value) {
        return foldRight((U[]) list.toArray(), f, value, 0, list.size()) ;
    }

    @SuppressWarnings("unchecked")
    public static <T> T foldRight(Collection<T> list, Function<T, T> f) {
        if (list.size() == 0)
            return null ;

        T[] arr = (T[]) list.toArray() ;
        return foldRight(arr, f, arr[arr.length - 1], 0, arr.length - 1) ;
    }

    private static <U, T> T foldRight(U[] collection, Function<U,T> f, T value, int i, int size) {
        if (i == size)
            return value ;

        return f.apply(collection[i], foldRight(collection, f, value, i + 1, size)) ;
    }
}
