/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.ast.feature;

import gsd.iml.ast.ImlAstNode;
import gsd.iml.ast.constraint.ActiveIfConstraint;
import gsd.iml.ast.constraint.CalculatedConstraint;
import gsd.iml.ast.constraint.DefaultValueConstraint;
import gsd.iml.ast.constraint.ImplementsConstraint;
import gsd.iml.ast.constraint.LegalValuesConstraint;
import gsd.iml.ast.constraint.RequiresConstraint;
import gsd.iml.ast.flavor.Flavor;
import gsd.iml.util.CollectionsUtils;
import gsd.iml.util.StringUtils;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author leonardo
 */
public class Feature extends ImlAstNode {

    private String id ;
    private FeatureType type ;
    private String display ;
    private String description ;
    private Flavor flavor ;
    private DefaultValueConstraint defaultValue ;
    private CalculatedConstraint calculated ;
    private LegalValuesConstraint legalValues ;
    private List<RequiresConstraint> requires ;
    private List<ActiveIfConstraint> activeIfs ;
    private List<ImplementsConstraint> impls ;
    private List<Feature> subfeatures ;

    public Feature
            (FeatureType type,
            String id,
            String display,
            String description,
            Flavor flavor,
            DefaultValueConstraint defaultValue,
            CalculatedConstraint calculated,
            LegalValuesConstraint legalValues,
            List<RequiresConstraint> requires,
            List<ActiveIfConstraint> activeIfs,
            List<ImplementsConstraint> impls,
            List<Feature> subfeatures) {

        super("feature", subfeatures) ;
        this.id = id;
        this.type = type;
        this.display = display;
        this.description = description ;
        this.flavor = flavor;
        this.defaultValue = defaultValue;
        this.calculated = calculated;
        this.legalValues = legalValues;
        this.requires = requires;
        this.activeIfs = activeIfs;
        this.impls = impls;
        this.subfeatures = subfeatures ;
    }

    public List<ActiveIfConstraint> getActiveIfs() {
        return activeIfs;
    }

    public CalculatedConstraint getCalculated() {
        return calculated;
    }

    public DefaultValueConstraint getDefaultValue() {
        return defaultValue;
    }

    public String getDisplay() {
        return display;
    }

    public String getDescription() {
        return description;
    }

    public Flavor getFlavor() {
        return flavor;
    }

    public String getId() {
        return id;
    }

    public List<ImplementsConstraint> getImpls() {
        return impls;
    }

    public LegalValuesConstraint getLegalValues() {
        return legalValues;
    }

    public List<RequiresConstraint> getRequires() {
        return requires;
    }

    public FeatureType getType() {
        return type;
    }

    public List<Feature> getSubfeatures() {
        return subfeatures;
    }

    @Override
    public int hashCode() {
        return getId().hashCode() +
               getType().hashCode() +
               getDisplay().hashCode() +
               getFlavor().hashCode() +
               getDefaultValue().hashCode() +
               getCalculated().hashCode() +
               getLegalValues().hashCode() +
               getRequires().hashCode() +
               getActiveIfs().hashCode() +
               getImpls().hashCode() +
               getChildren().hashCode() ;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return false ;

        if (! (obj instanceof Feature))
            return false ;

        Feature f = (Feature) obj ;
        return f.getId().equals(getId()) &&
               f.getType().equals(getType()) &&
               f.getDisplay().equals(getDisplay()) &&
               f.getFlavor().equals(getFlavor()) &&
               f.getDefaultValue().equals(getDefaultValue()) &&
               f.getCalculated().equals(getCalculated()) &&
               f.getLegalValues().equals(getLegalValues()) &&
               f.getRequires().equals(getRequires()) &&
               f.getActiveIfs().equals(getActiveIfs()) &&
               f.getImpls().equals(getImpls()) &&
               f.getChildren().equals(getChildren()) ;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer() ;
        putInStringBuffer(this, 0, buffer) ;
        return buffer.toString() ;
    }

    private void putInStringBuffer(Object obj, int ident, StringBuffer buffer) {
        if (obj == null)
            return ;

        if (obj instanceof Collection) {
            for(Object o : (Collection) obj) {
                putInStringBuffer(o, ident, buffer) ;
            }
            return ;
        }

        if (obj instanceof Feature) {
            Feature f = (Feature) obj ;
            buffer.append(StringUtils.space(ident) + f.type + " " + f.id + " ") ;
            buffer.append("{\n");

            if (f.display != null)
                putInStringBuffer("display " + "\"" + f.display + "\"", ident + 2, buffer) ;
            
            putInStringBuffer(f.impls,           ident + 2, buffer) ;
            putInStringBuffer(f.activeIfs,       ident + 2, buffer) ;
            putInStringBuffer(f.requires,        ident + 2, buffer) ;
            putInStringBuffer(f.defaultValue,    ident + 2, buffer) ;
            putInStringBuffer(f.legalValues,     ident + 2, buffer) ;
            putInStringBuffer(f.calculated,      ident + 2, buffer) ;               
            putInStringBuffer(f.flavor,          ident + 2, buffer) ;
            
            if (f.hasChildren()) {
                for(ImlAstNode node : f.getChildren())
                    putInStringBuffer(node, ident + 2, buffer) ;
            }

            buffer.append(StringUtils.space(ident) + "}\n") ;

            return ;
        }
       
        buffer.append(StringUtils.space(ident) + obj + "\n") ;

    }
}
