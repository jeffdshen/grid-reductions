package transform.planar;

import transform.GadgetConverter;
import transform.GadgetUtils;
import types.Gadget;
import types.Location;
import types.Side;
import types.configuration.AtomicConfiguration;
import types.configuration.CellConfiguration;
import types.configuration.nodes.AtomicNode;
import types.configuration.nodes.AtomicPort;

import java.util.List;
import java.util.Map;

class GadgetPlanarizerInstance {
    private static final int INITIAL_SIZE = 10;
    private final GadgetSet gadgets;

    private CellConfiguration grid;
    private AtomicConfiguration config;
    private final GridExpander expander;
    private final GadgetConverter converter;

    private boolean incrCost = false;

    GadgetPlanarizerInstance(AtomicConfiguration config, GadgetSet gadgets) {
        this.config = config;
        this.gadgets = gadgets;
        this.grid = new CellConfiguration(INITIAL_SIZE, INITIAL_SIZE);
        this.expander = new GridExpander();
        this.converter = new GadgetConverter();
    }

    public CellConfiguration getGrid() {
        return this.grid;
    }

    public void place() {
        Iterable<AtomicNode> nodes = config.getNodes();
        // assume nodes are in topological order
        for (AtomicNode node : nodes) {
            CellConfiguration nodeGrid = converter.toGridConfiguration(gadgets.gadgets.get(node.getName()), node.getId());
            List<Location> locs = PlanarUtils.getPlacements(grid, nodeGrid);
            if (locs.isEmpty()) {
                grid.resize(grid.getSizeX() + nodeGrid.getSizeX() + 2, grid.getSizeY() + nodeGrid.getSizeY() + 2);
                locs = PlanarUtils.getPlacements(grid, nodeGrid);
            }

            Location best = locs.get(0);
            if (!grid.isEmpty(nodeGrid.getSizeX() + 2, nodeGrid.getSizeY() + 2, best.subtract(1, 1))) {
                throw new IllegalStateException(
                    String.format("Configuration put at node %s would overwrite cells", best)
                );
            }
            grid.put(nodeGrid, best);
            connectInputs(node);
        }

        replace();
        expander.expandLast(grid);
    }


    // TODO - replace 2 at a time
    public void replace() {
        expander.expandDouble(grid);
        Iterable<AtomicNode> nodes = config.getNodes();
        for (int i = 15; i >= 0; i--) {
            grid.expand(5, 5); // TODO non hard code
            if (i < 4) {
                incrCost = true;
            }
            System.out.println(String.format(
                "re-placing step %d, cost - %d, size - %d, %d.",
                i,
                getNewCostFunction().getTotalCost(),
                grid.getSizeX(),
                grid.getSizeY()
            ));

            for (AtomicNode node : nodes) {
                PlanarUtils.deleteNode(grid, node);
                CellConfiguration nodeGrid = converter.toGridConfiguration(gadgets.gadgets.get(node.getName()), node.getId());
                Location best = PlanarUtils.findReplacement(
                    grid, config, node, nodeGrid, getNewCostFunction(), i * 0.04
                );

                // TODO - expand grid
                if (best == null) {
                    throw new IllegalStateException(
                        String.format("Couldn't find location to re-place node %s", node)
                    );
                }

                if (!grid.isEmpty(nodeGrid.getSizeX() + 2, nodeGrid.getSizeY() + 2, best.subtract(1, 1))) {
                    throw new IllegalStateException(
                        String.format("CellConfiguration put at node %s would overwrite cells", best)
                    );
                }
                grid.put(nodeGrid, best);
                connectInputs(node);
                connectOutputs(node);
            }

            PlanarUtils.deleteSlices(grid);
            System.out.println(grid);
        }
    }

    private void connectInputs(AtomicNode node) {
        for (int i = 0; i < node.inputSize(); i++) {
            AtomicPort port = node.getInputPort(i);
            Side start = PlanarUtils.findPort(grid, port);
            Side end = PlanarUtils.findPort(grid, config.getConnectingPort(port));

            if (start == null || end == null) {
                port = config.getConnectingPort(port);
                String nodeName = config.getNode(port.getContext(), port.getPort().getId()).getName();

                throw new IllegalArgumentException(
                    String.format("Configuration with node %s is not topologically sorted", nodeName)
                );
            }

            List<Side> path = new DijkstrasPather(grid, start, getNewCostFunction(), true).getPath(end);
            PlanarUtils.putInputPath(grid, path);
            expander.expand(grid);
        }
    }

    private void connectOutputs(AtomicNode node) {
        for (int i = 0; i < node.outputSize(); i++) {
            AtomicPort port = node.getOutputPort(i);
            Side start = PlanarUtils.findPort(grid, port);
            Side end = PlanarUtils.findPort(grid, config.getConnectingPort(port));

            if (start == null || end == null) {
                port = config.getConnectingPort(port);
                String nodeName = config.getNode(port.getContext(), port.getPort().getId()).getName();

                throw new IllegalArgumentException(
                    String.format("Configuration with node %s is not topologically sorted", nodeName)
                );
            }

            List<Side> path = new DijkstrasPather(grid, start, getNewCostFunction(), false).getPath(end);
            PlanarUtils.putOutputPath(grid, path);
            expander.expand(grid);
        }
    }

    private CostFunction getNewCostFunction() {
        if (incrCost) {
            return new IncrementalCost(grid, gadgets);
        }

        return new BaseCost(grid, gadgets);
    }
}
