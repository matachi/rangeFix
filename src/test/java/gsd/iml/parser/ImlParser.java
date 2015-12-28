/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.parser;

import gsd.iml.ast.feature.Feature;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.List;

/**
 *
 * @author leonardo
 */
public class ImlParser {
    public static List<Feature> parse(String file) throws Exception {
        return parse(new FileReader(file)) ;
    }

    public static List<Feature> parse(File file) throws Exception {
        return parse(new FileReader(file)) ;
    }

    @SuppressWarnings("unchecked")
    public static List<Feature> parse(Reader reader) throws Exception {
        ImlCupParser parser = new ImlCupParser(new ImlLexer(reader)) ;
        return (List<Feature>) parser.parse().value ;
    }
}
