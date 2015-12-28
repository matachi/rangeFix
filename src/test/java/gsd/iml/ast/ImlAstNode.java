/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor. blabla
 */

package gsd.iml.ast;

import java.util.List;

/**
 *
 * @author leonardo
 */
public abstract class ImlAstNode extends ImlAstObject {
    private List<? extends ImlAstNode> children ;

    public ImlAstNode(String name, List<? extends ImlAstNode> children) {
        super(name) ;
        this.children = children ;
    }

    public boolean hasChildren() {
        List<? extends ImlAstNode> _children = getChildren() ;
        return (_children != null) && (_children.size() > 0) ;
    }

    public List<? extends ImlAstNode> getChildren() {
        return children ;
    }

    @Override
    abstract public boolean equals(Object obj) ;

    @Override
    abstract public int hashCode() ;

    @Override
    public abstract String toString() ;
}
