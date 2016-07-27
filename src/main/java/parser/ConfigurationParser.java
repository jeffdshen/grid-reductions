package parser;

import org.apache.log4j.Logger;
import types.configuration.Configuration;
import types.configuration.nodes.*;

import java.io.*;
import java.util.*;

import static org.apache.log4j.Level.ERROR;

public class ConfigurationParser implements Parser<Configuration> {
    private static final Logger logger = Logger.getLogger(GadgetParser.class.getName());

    @Override
    public Configuration parse(File file) {
        try (FileReader reader = new FileReader(file)) {
            return parse(reader, file.getAbsolutePath());
        } catch (IOException e) {
            logger.log(ERROR, e.getMessage(), e);
        }

        return null;
    }

    @Override
    public Configuration parse(InputStream stream, String id) {
        return parse(new InputStreamReader(stream), id);
    }

    /**
     * Parse a configuration
     * @param reader the input
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
    public Configuration parse(Reader reader, String readerID) {
        boolean hasInput = false;
        boolean hasOutput = false;
        int outputId = 0;
        Map<Integer, Node> nodes = new HashMap<>();
        Map<Integer, List<Port>> inputs = new HashMap<>();
        Set<Integer> visitedNodes = new HashSet<>();
        List<ConnectedNode> connectedNodes = new ArrayList<>();
        String cfgName;

        String errorPrefix = "While reading configuration " + readerID + ": ";

        try(BufferedReader br = new BufferedReader(reader)){
            String line = br.readLine();
            cfgName = line;
            while((line = br.readLine()) != null){
                if(line.trim().length() == 0){
                    continue;
                }
                String[] tokens = line.split(" ");
                if(tokens[0].equals("node")){
                    if(tokens.length != 5){
                        logger.log(ERROR, errorPrefix + "Invalid line - " + line);
                        return null;
                    }
                    int id = Integer.parseInt(tokens[1]);
                    String name = tokens[2];
                    int numInputs = Integer.parseInt(tokens[3]);
                    int numOutputs = Integer.parseInt(tokens[4]);

                    Node n = new Node(NodeType.LABELLED, name, id, numInputs, numOutputs);

                    nodes.put(id, n);
                    inputs.put(id, getNullList(numInputs));
                } else if(tokens[0].equals("input")){
                    if(hasInput){
                        logger.log(ERROR, errorPrefix + "Cannot have multiple inputs");
                        return null;
                    }
                    int id = Integer.parseInt(tokens[1]);
                    int numPorts = Integer.parseInt(tokens[2]);

                    Node n = new Node(NodeType.INPUT, null, id, 0, numPorts);
                    nodes.put(id, n);
                    inputs.put(id, getNullList(0));

                    hasInput = true;
                } else if (tokens[0].equals("output")){
                    if(hasOutput){
                        logger.log(ERROR, errorPrefix + "Cannot have multiple outputs");
                        return null;
                    }
                    int id = Integer.parseInt(tokens[1]);
                    int numPorts = Integer.parseInt(tokens[2]);
                    Node n = new Node(NodeType.OUTPUT, null, id, numPorts, 0);
                    nodes.put(id, n);
                    inputs.put(id, getNullList(numPorts));
                    outputId = id;
                    hasOutput = true;
                } else {
                    // defining connections
                    int id = Integer.parseInt(tokens[0]);
                    List<Port> outputPorts = new ArrayList<>();
                    for(int i = 1; i<tokens.length; i++){
                        String[] info = tokens[i].replaceAll("[()]", "").split(",");
                        if(info.length != 2) {
                            logger.log(ERROR, errorPrefix + "Invalid format for an output-port-connection - " + line);
                            return null;
                        }
                        int outId = Integer.parseInt(info[0]);
                        int portNum = Integer.parseInt(info[1]);

                        //check topological ordering
                        if(visitedNodes.contains(outId)){
                            logger.log(ERROR, errorPrefix + "Output-port specs must be given in topological order");
                            return null;
                        }
                        outputPorts.add(nodes.get(outId).getInputPort(portNum));
                        inputs.get(outId).set(portNum, nodes.get(id).getOutputPort(i-1));
                    }
                    connectedNodes.add(new ConnectedNode(nodes.get(id), inputs.get(id), outputPorts));
                    visitedNodes.add(id);
                }
            }
            if(!hasOutput && !hasInput){
                logger.log(ERROR, errorPrefix + "Must define an input and output node");
                return null;
            }
        } catch (IOException e) {
            logger.log(ERROR, e.getMessage(), e);
            return null;
        }
        if(!visitedNodes.contains(outputId)) {
            connectedNodes.add(new ConnectedNode(nodes.get(outputId), inputs.get(outputId), new ArrayList<Port>()));
        }
        return new Configuration(cfgName, connectedNodes);
    }

    private List<Port> getNullList(int length){
        List<Port> nullPorts = new ArrayList<>();
        for(int i = 0; i < length; i++){
            nullPorts.add(null);
        }
        return nullPorts;
    }
}
