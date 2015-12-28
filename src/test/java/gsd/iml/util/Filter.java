/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.util;

/**
 *
 * @author leonardo
 */
public interface Filter<T> {
    boolean accepts(T elem) ;
}
