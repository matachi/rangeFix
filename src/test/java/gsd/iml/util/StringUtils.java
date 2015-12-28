/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.util;

import java.util.Arrays;

/**
 *
 * @author leonardo
 */
public class StringUtils {
    public static String space(int amount) {
        char[] ss = new char[amount] ;
        Arrays.fill(ss, ' ') ;
        return new String(ss) ;
    }
}
