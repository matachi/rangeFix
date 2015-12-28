/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.parser;

/**
 *
 * @author leonardo
 */
class ImlParseMessage {
    public static String error(String message, int line, int column) {
        return "Error [line = " + line + ", " + "col = " + column + "]: " + message ;
    }
    public static String warning(String message, int line, int column) {
        return "Warning [line = " + line + ", " + "col = " + column + "]: " + message ;
    }
}
