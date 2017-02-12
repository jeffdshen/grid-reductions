package transform.planar;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import transform.GadgetUtils;
import transform.wiring.FrobeniusWirer;
import transform.wiring.Shifter;
import transform.wiring.TurnShifter;
import transform.wiring.Wirer;
import types.Direction;
import types.Gadget;
import types.configuration.CellConfiguration;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class GadgetSet {
    public final Multimap<Direction, Gadget> wires;
    public final Gadget empty;
    public final Map<List<Direction>, Gadget> turns;
    public final Map<Set<Direction>, Gadget> crossovers;
    public final Map<String, Gadget> gadgets;

    public final Wirer wirer;
    public final Shifter shifter;

    public GadgetSet(
        Iterable<Gadget> wires,
        Iterable<Gadget> turns,
        Iterable<Gadget> crossovers,
        Gadget empty,
        Iterable<Gadget> gadgets
    ) {
        this.empty = empty;
        Preconditions.checkArgument(
            empty.getSizeX() == 1 || empty.getSizeY() == 1, "Non-1x1 empty gadgets are not supported yet"
        );

        this.wires = GadgetUtils.getWireMap(wires);
        this.gadgets = GadgetUtils.getGadgetMap(gadgets);
        this.turns = GadgetUtils.getTurnMap(turns);
        this.crossovers = GadgetUtils.getCrossoverMap(crossovers);
        this.wirer = new FrobeniusWirer(wires);
        this.shifter = new TurnShifter(turns, wires, wirer);
    }
}
