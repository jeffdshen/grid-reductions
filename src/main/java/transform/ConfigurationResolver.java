package transform;

import types.configuration.AtomicConfiguration;
import types.configuration.Configuration;
import types.configuration.nodes.Node;
import types.configuration.nodes.NodeType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by kevin on 11/29/14.
 * This class takes in a problem configuration and an Iterable of
 * possible substitution configurations (name of config is label it can substitute)
 * and a list of available atomic nodes
 *
 * Outputs an AtomicConfiguration with an appropriate set of substitution configurations
 * or throws an error if not all labels in the problem can be constructed
 *
 */
public class ConfigurationResolver {

    public AtomicConfiguration resolve(Configuration problem, Iterable<Configuration> subs, Set<String> atoms){
        //a map of label name to chosen substitute
        Map<String, Configuration> chosenSubs = new HashMap<String, Configuration>();
        Set<String> available = new HashSet<String>(atoms);
        // maps a label to all the labels used in its sub if we've added it to chosenSubs
        Map<String, Set<String>> dependencies = new HashMap<String, Set<String>>();

        //build all the different labels we can make.
        boolean shouldRetry = true;
        while(shouldRetry) {
            shouldRetry = false;
            for (Configuration sub : subs) {
                String subName = sub.getName();
                if (available.contains(subName)){
                    continue;
                }
                boolean canBuild = true;
                Set<String> neededNames = new HashSet<String>();
                for (Node node : sub.getNodes()) {
                    if (node.getType().equals(NodeType.LABELLED)) {
                        String nodeName = node.getName();
                        if(!available.contains(nodeName)){
                            canBuild = false;
                            break;
                        }
                        neededNames.add(nodeName);
                    }
                }
                if(canBuild){
                    available.add(subName);
                    dependencies.put(subName, neededNames);
                    chosenSubs.put(subName, sub);
                    shouldRetry = true;
                }
            }
        }


        Set<String> needed = new HashSet<String>();
        //travel the problem config to find all labels needed. keep only the necessary labels
        for( Node node: problem.getNodes()){
            if(node.getType().equals(NodeType.LABELLED)){
                addDependencies(node.getName(), dependencies, needed);
            }
        }
        Set<Configuration> usedSubs = new HashSet<Configuration>();
        for(String name: needed){
            if(chosenSubs.containsKey(name)){
                usedSubs.add(chosenSubs.get(name));
            }
        }

        return new AtomicConfiguration(problem, usedSubs, atoms);
    }

    // helper method to recursively update needed labels for an input label given the dependency map
    private void addDependencies(String name, Map<String, Set<String>> dependencies, Set<String> needed){
        if(!needed.contains(name)) {
            needed.add(name);
            if (dependencies.containsKey(name)) {
                for (String subName : dependencies.get(name)) {
                    addDependencies(subName, dependencies, needed);
                }
            }
        }
        //assume dependencies have already been added
    }

}
