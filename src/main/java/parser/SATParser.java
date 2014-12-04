package parser;

import com.google.common.base.Preconditions;
import types.configuration.Configuration;
import types.configuration.nodes.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kevin on 11/30/14.
 */
public class SATParser {

    private SATTokenizer tokenizer;
    private int id = 0;
    private Map<String, Integer> labelCount;
    private Node inputNode;
    private Node outputNode;
    private Node endNode;


    //Parses a SAT expression and outputs a configuration.
    // Uses SPLIT nodes to duplicate variables from VARIABLE nodes
    // Requires: AND, SPLIT, NOT, OR, VARIABLE, END
    public Configuration parseSAT(String expr) throws Exception {
        tokenizer = new SATTokenizer(expr);
        id = 0;

        SATNode root = parseExpression();

        labelCount = new HashMap<String, Integer>();
        //count variable nodes
        countVariables(root);

        //Create VARIABLE and SPLIT nodes
        //maps from var id to list of nodes corresponding to VARIABLE node + SPLIT nodes
        Map<String, List<Node>> variableNodes = new HashMap<String, List<Node>>();
        for(String key: labelCount.keySet()){
            int num = labelCount.get(key);
            List<Node> varNodeList = new ArrayList<Node>();
            varNodeList.add(new Node(NodeType.LABELLED, "VARIABLE", id, 0, 1));
            id++;
            for(int i = 1; i < num; i++){
                varNodeList.add(new Node(NodeType.LABELLED, "SPLIT", id, 1, 2));
                id++;
            }
            variableNodes.put(key, varNodeList);
        }

        //zero labelCount to use as a counter
        for(String key: labelCount.keySet()){
            labelCount.put(key, 0);
        }

        //build configuration
        Map<SATNode, Node> nodes = new HashMap<SATNode, Node>();
        makeNodes(root, nodes, variableNodes);
        inputNode = new Node(NodeType.INPUT, null, id, 0, 0);
        id++;
        outputNode = new Node(NodeType.OUTPUT, null, id, 0, 0);
        id++;
        endNode = new Node(NodeType.LABELLED, "END", id, 1, 0);
        id++;

        List<ConnectedNode> connectedNodes = new ArrayList<ConnectedNode>();
        //init inputnode and variable/split nodes;
        //add Input Node
        connectedNodes.add(new ConnectedNode(inputNode, new ArrayList<Port>(), new ArrayList<Port>()));
        Map<String, List<Port>> varOutPorts = new HashMap<>();
        for(String key : labelCount.keySet()){
            varOutPorts.put(key, new ArrayList<Port>());
        }
        //get ports used by var/split nodes
        getVarNodePorts(root, nodes, varOutPorts,true);
        for(String key: varOutPorts.keySet()){
            List<Port> outPorts = varOutPorts.get(key);
            List<Node> varNodeList = variableNodes.get(key);
            //no splits
            if(varNodeList.size() == 1){
                connectedNodes.add(new ConnectedNode(varNodeList.get(0),
                        new ArrayList<Port>(), outPorts));
            }
            else{
                //handle VARIABLE Node
                List<Port> outs = new ArrayList<Port>();
                outs.add(varNodeList.get(1).getInputPort(0));
                connectedNodes.add(new ConnectedNode(varNodeList.get(0), new ArrayList<Port>(), outs));
                int numVars = varNodeList.size();
                for(int i = 1; i < numVars; i++){
                    List<Port> in = new ArrayList<Port>();
                    in.add(varNodeList.get(i-1).getOutputPort(0));
                    List<Port> out = new ArrayList<Port>();
                    if(i < numVars-1) {
                        //outport 0
                        out.add(varNodeList.get(i+1).getInputPort(0));
                        out.add(outPorts.get(i-1));
                    }
                    else{
                        out.add(outPorts.get(i));
                        out.add(outPorts.get(i-1));
                    }
                    connectedNodes.add(new ConnectedNode(varNodeList.get(i), in, out));
                }
            }
        }

        //create rest of nodes
        buildConnectedNodes(root, nodes, connectedNodes, true);

        //create output
        List<Port> endNodeIn = new ArrayList<Port>();
        endNodeIn.add(nodes.get(root).getOutputPort(0));
        connectedNodes.add(new ConnectedNode(endNode, endNodeIn, new ArrayList<Port>()));
        connectedNodes.add(new ConnectedNode(outputNode, new ArrayList<Port>(), new ArrayList<Port>()));

        return new Configuration("problem", connectedNodes);
    }

    // converts a SATNode tree into a list of connectedNodes, bottom up
    private void buildConnectedNodes(SATNode node, Map<SATNode, Node> nodes, List<ConnectedNode> connectedNodes, boolean isLeft){
        List<Port> outPorts = new ArrayList<>();
        List<Port> inPorts = new ArrayList<>();
        switch(node.type){
            case AND:
            case OR:
                buildConnectedNodes(node.getLeftChild(), nodes, connectedNodes, true);
                if(node.getLeftChild().isVariable()){
                    String varId = node.getLeftChild().getVarID();
                    if(labelCount.get(varId) == 0){
                        inPorts.add(nodes.get(node.getLeftChild()).getOutputPort(0));
                    }else{
                        inPorts.add(nodes.get(node.getLeftChild()).getOutputPort(1));
                    }

                }else{
                    inPorts.add(nodes.get(node.getLeftChild()).getOutputPort(0));
                }
                buildConnectedNodes(node.getRightChild(), nodes, connectedNodes, false);
                if(node.getRightChild().isVariable()){
                    String varId = node.getRightChild().getVarID();
                    if(labelCount.get(varId) == 0){
                        inPorts.add(nodes.get(node.getRightChild()).getOutputPort(0));
                    }else{
                        inPorts.add(nodes.get(node.getRightChild()).getOutputPort(1));
                    }

                }else{
                    inPorts.add(nodes.get(node.getRightChild()).getOutputPort(0));
                }
                if(!node.isRoot()) {
                    if (isLeft) {
                        outPorts.add(nodes.get(node.getParent()).getInputPort(0));
                    }
                    else{
                        outPorts.add(nodes.get(node.getParent()).getInputPort(1));
                    }
                }else{
                    outPorts.add(endNode.getInputPort(0));
                }
                break;
            case NOT:
                buildConnectedNodes(node.getLeftChild(), nodes, connectedNodes, true);
                if(node.getLeftChild().isVariable()){
                    String varId = node.getLeftChild().getVarID();
                    if(labelCount.get(varId) == 0){
                        inPorts.add(nodes.get(node.getLeftChild()).getOutputPort(0));
                    }else{
                        inPorts.add(nodes.get(node.getLeftChild()).getOutputPort(1));
                    }

                }else{
                    inPorts.add(nodes.get(node.getLeftChild()).getOutputPort(0));
                }
                if(!node.isRoot()) {
                    if (isLeft) {
                        outPorts.add(nodes.get(node.getParent()).getInputPort(0));
                    }
                    else{
                        outPorts.add(nodes.get(node.getParent()).getInputPort(1));
                    }
                }else{
                    outPorts.add(endNode.getInputPort(0));
                }
                break;
            case VAR:
                //decrement counter
                String varId = node.getVarID();
                labelCount.put(varId, labelCount.get(varId) - 1);
                break;
            default:
                break;
        }
        connectedNodes.add(new ConnectedNode(nodes.get(node), inPorts, outPorts));
    }

    private void getVarNodePorts(SATNode node, Map<SATNode, Node> nodes, Map<String, List<Port>> varOutPorts, boolean isLeft){
        switch(node.type){
            case AND:
            case OR:
                getVarNodePorts(node.getLeftChild(), nodes, varOutPorts, true);
                getVarNodePorts(node.getRightChild(), nodes, varOutPorts, false);
                break;
            case NOT:
                getVarNodePorts(node.getLeftChild(), nodes, varOutPorts, true);
                break;
            case VAR:
                List<Port> ports = varOutPorts.get(node.getVarID());
                Node parentNode = nodes.get(node.getParent());
                if(isLeft) {
                    ports.add(parentNode.getInputPort(0));
                }
                else{
                    ports.add(parentNode.getInputPort(1));
                }
                break;
            default:
                break;
        }
    }

    //create Nodes, bottom up. Associates variables with correct VAR/SPLIT
    private void makeNodes(SATNode node, Map<SATNode, Node> nodes, Map<String, List<Node>> varMap){
        switch(node.type){
            case AND:
                makeNodes(node.getLeftChild(), nodes, varMap);
                makeNodes(node.getRightChild(), nodes, varMap);
                nodes.put(node, new Node(NodeType.LABELLED, "AND", id, 2, 1));
                id ++;
                break;
            case OR:
                makeNodes(node.getLeftChild(), nodes, varMap);
                makeNodes(node.getRightChild(), nodes, varMap);
                nodes.put(node, new Node(NodeType.LABELLED, "OR", id, 2, 1));
                id ++;
                break;
            case NOT:
                makeNodes(node.getLeftChild(), nodes, varMap);
                nodes.put(node, new Node(NodeType.LABELLED, "NOT", id, 1, 1));
                id ++;
                break;
            case VAR:
                String varID = node.getVarID();
                Preconditions.checkArgument(varMap.containsKey(varID), "varMap not properly initialized");
                List<Node> varNodeList = varMap.get(varID);

                int idx = labelCount.get(varID);
                if(varNodeList.size() == 1) {
                    nodes.put(node, varNodeList.get(Math.min(idx + 1, varNodeList.size())));
                }
                labelCount.put(varID, idx + 1);
                break;
            default:
                break;
        }
    }

    private void countVariables(SATNode node){
        switch(node.type){
            case AND:
            case OR:
                countVariables(node.getLeftChild());
                countVariables(node.getRightChild());
                break;
            case NOT:
                countVariables(node.getLeftChild());
                break;
            case VAR:
                String varId = node.getVarID();
                if(!labelCount.containsKey(varId)){
                    labelCount.put(varId, 1);
                }else{
                    labelCount.put(varId, labelCount.get(varId) + 1);
                }
                break;
            default:
                break;
        }
    }

    // (x and !y or !z) - > x into and, (y into not) into and. and into or
    public SATNode parseExpression() throws Exception {
        if(!tokenizer.hasMoreTokens()){
            throw new Exception("Parser Error: Expecting more tokens when parsing expression");
        }
        return parseGate(parseSingleExpression());
    }

    public SATNode parseSingleExpression() throws Exception {
        if(!tokenizer.hasMoreTokens()){
            throw new Exception("Parser Error: Expecting more tokens when parsing single expression");
        }
        String token = tokenizer.getNextToken();
        if(token.equals("(")){
            return parseExpression();
        }
        else if(token.equals(")")){
            throw new Exception("Parser Error: invalid token ) when parsing expression");
        }
        else if(token.equals("&&")){
            throw new Exception("Parser Error: invalid token && when parsing expression");
        }
        else if(token.equals("||")){
            throw new Exception("Parser Error: invalid token || when parsing expression");
        }
        else if(token.equals("!")){
            SATNode next = parseSingleExpression();
            SATNode notNode = new SATNode(SATNode.SATNodeType.NOT);
            notNode.setLeftChild(next);
            next.setParent(notNode);
            return notNode;
        }else{
            return parseVar(token);
        }
    }



    public SATNode parseGate(SATNode currentNode) throws Exception {
        if(!tokenizer.hasMoreTokens()){
            return currentNode;
        }
        String token = tokenizer.getNextToken();
        if(token.equals(")")){
            return currentNode;
        }
        else if(token.equals("&&")){
            SATNode andNode = new SATNode(SATNode.SATNodeType.AND);
            andNode.setLeftChild(currentNode);
            currentNode.setParent(andNode);
            SATNode rightChild = parseSingleExpression();
            rightChild.setParent(andNode);
            andNode.setRightChild(rightChild);
            return parseGate(andNode);
        }
        else if(token.equals("||")){
            SATNode orNode = new SATNode(SATNode.SATNodeType.OR);
            orNode.setLeftChild(currentNode);
            currentNode.setParent(orNode);
            SATNode rightChild = parseSingleExpression();
            rightChild.setParent(orNode);
            orNode.setRightChild(rightChild);
            return parseGate(orNode);
        }else{
            throw new Exception("Parser Error: Expecting && or || or )");
        }
    }

    //assumes that whatevers passed is an id or !
    public SATNode parseVar(String token) throws Exception{
        return new SATNode(token);
    }
}
