package parser.sat;

import com.google.common.base.Preconditions;

/**
 * Created by kevin on 11/30/14.
 * represent a SAT Expression upside down. Parents are outputs of the node
 * The root node is the output of the SAT and leaves are variables.
 *
 *
 */

public class SATNode {


    //NOT node uses only left child
    public enum SATNodeType{
        AND, NOT, OR, VAR
    }
    
    public final SATNodeType type;
    private SATNode parent;
    private SATNode leftChild;
    private SATNode rightChild;

    private String varID;

    public SATNode(SATNodeType type){
        this.type = type;
    }

    public SATNode(String varID){
        this.type = SATNodeType.VAR;
        this.varID = varID;
    }

    public String getVarID(){
        Preconditions.checkArgument(this.type == SATNodeType.VAR, "Not a variable node");
        return this.varID;
    }

    public SATNode getLeftChild() {
        Preconditions.checkArgument(this.type != SATNodeType.VAR, "Var nodes have no inputs");
        return leftChild;
    }

    public void setLeftChild(SATNode leftChild) {
        Preconditions.checkArgument(this.type != SATNodeType.VAR, "Var nodes have no inputs");
        this.leftChild = leftChild;
    }

    public SATNode getRightChild() {
        Preconditions.checkArgument(this.type != SATNodeType.NOT, "Not nodes have 1 input");
        Preconditions.checkArgument(this.type != SATNodeType.VAR, "Var nodes have no inputs");
        return rightChild;
    }

    public void setRightChild(SATNode rightChild) {
        Preconditions.checkArgument(this.type != SATNodeType.NOT, "Not nodes have 1 input");
        Preconditions.checkArgument(this.type != SATNodeType.VAR, "Var nodes have no inputs");
        this.rightChild = rightChild;
    }

    public boolean isVariable(){
        return this.type == SATNodeType.VAR;
    }


    public void setParent(SATNode parent) {
        this.parent = parent;
    }

    public SATNode getParent() {
        return parent;
    }
    
    public boolean isRoot() {
        return parent==null;
    }

    @Override
    public String toString(){
        switch(this.type){
            case AND:
                return "("+ this.getLeftChild() + " && " + this.getRightChild() + ")";
            case OR:
                return "("+ this.getLeftChild() + " || " + this.getRightChild() + ")";
            case NOT:
                return "!(" + this.getLeftChild() + ")";
            case VAR:
                return this.getVarID();
            default:
                return "";
        }
    }
    
}
