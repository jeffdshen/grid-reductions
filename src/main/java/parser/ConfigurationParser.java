package parser;

import types.*;

import java.io.*;
import java.text.ParseException;
import java.util.*;

/**
 * Created by kevin on 11/28/14.
 */
public class ConfigurationParser {

    /**
     * Parse a configuration
     * @param file text file
     * @return
     *
     * File format should be "name" one of
     * node id# name #inputs #outputs
     * input id# #inputs=#outputs <- 1 number
     * output id# #inputs=#outputs
     *
     * followed by
     * id# output-port-connections
     *
     *
     * where id# is an int unique to each node
     * name is the node label (can be duplicate)
     * and output-port-connections are tuples of form: (node-id#,input_port#). The ith tuple
     * corresponds to the ith output port. input_port# should be 0 indexed
     *
     * The id# must be defined before setting output-port connections
     *
     * The number of inputs to input node assumed to be equal to # of outputs
     * The number of outputs from output node is assumed to be equal to # of inputs
     *
     */
    public Configuration parseConfiguration(File file) throws Exception {
        boolean hasInput = false;
        boolean hasOutput = false;
        int outputId = 0;
        Map<Integer, Node> nodes = new HashMap<Integer, Node>();
        Map<Integer, List<Port>> inputs = new HashMap<Integer, List<Port>>();
        Set<Integer> visitedNodes = new HashSet<Integer>();
        List<ConnectedNode> connectedNodes = new ArrayList<ConnectedNode>();
        String cfgName;

        try(BufferedReader br = new BufferedReader(new FileReader(file))){
            String line = br.readLine();
            cfgName = line;
            while((line = br.readLine()) != null){
                if(line.trim().length() == 0){
                    continue;
                }
                String[] tokens = line.split(" ");
                if(tokens[0].equals("node")){
                    if(tokens.length != 5){
                        throw new Exception("Parse Error: Invalid line " + line);
                    }
                    int id = Integer.parseInt(tokens[1]);
                    String name = tokens[2];
                    int numInputs = Integer.parseInt(tokens[3]);
                    int numOutputs = Integer.parseInt(tokens[4]);

                    Node n = new Node(NodeType.LABELLED, name, id, numInputs, numOutputs);

                    nodes.put(id, n);
                    inputs.put(id, getNullList(numInputs));
                }
                else if(tokens[0].equals("input")){
                    if(hasInput){
                        throw new Exception("Parse Error: Cannot have multiple inputs");
                    }
                    int id = Integer.parseInt(tokens[1]);
                    int numPorts = Integer.parseInt(tokens[2]);

                    Node n = new Node(NodeType.INPUT, null, id, 0, numPorts);
                    nodes.put(id, n);
                    inputs.put(id, getNullList(0));

                    hasInput = true;
                }
                else if (tokens[0].equals("output")){
                    if(hasOutput){
                        throw new Exception("Parse Error: Cannot have multiple outputs");
                    }
                    int id = Integer.parseInt(tokens[1]);
                    int numPorts = Integer.parseInt(tokens[2]);
                    Node n = new Node(NodeType.OUTPUT, null, id, numPorts, 0);
                    nodes.put(id, n);
                    inputs.put(id, getNullList(numPorts));
                    outputId = id;
                    hasOutput = true;
                }
                else{
                    // defining connections
                    int id = Integer.parseInt(tokens[0]);
                    List<Port> outputPorts = new ArrayList<Port>();
                    for(int i = 1; i<tokens.length; i++){
                        String[] info = tokens[i].replaceAll("[()]", "").split(",");
                        if(info.length != 2) {
                            throw new Exception("Parse Error: Invalid format for an output-port-connection " + line);
                        }
                        int outId = Integer.parseInt(info[0]);
                        int portNum = Integer.parseInt(info[1]);

                        //check topological ordering
                        if(visitedNodes.contains(outId)){
                            throw new Exception("Parse Error: output-port specs must be given in topological order");
                        }
                        outputPorts.add(nodes.get(outId).getInputPort(portNum));
                        inputs.get(outId).set(portNum, nodes.get(id).getOutputPort(i-1));
                    }
                    connectedNodes.add(new ConnectedNode(nodes.get(id), inputs.get(id), outputPorts));
                    visitedNodes.add(id);
                }
            }
            if(!hasOutput && !hasInput){
                throw new Exception("Parse Error: Must define an input and output node");
            }
        }
        if(!visitedNodes.contains(outputId)) {
            connectedNodes.add(new ConnectedNode(nodes.get(outputId), inputs.get(outputId), new ArrayList<Port>()));
        }
        return new Configuration(cfgName, connectedNodes);
    }

    public List<Port> getNullList(int length){
        List<Port> nullPorts = new ArrayList<Port>();
        for(int i = 0; i < length; i++){
            nullPorts.add(null);
        }
        return nullPorts;
    }

}
