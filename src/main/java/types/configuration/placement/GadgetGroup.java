package types.configuration.placement;

import types.Gadget;
import types.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kevin on 12/6/14.
 * A gadget + collection of gadget groups with offsets from the top left corner of the gadget
 * Represents a collection of relatively positioned gadgets
 */
public class GadgetGroup {
    private Gadget baseGadget;
    private List<Location> offsets;
    private List<GadgetGroup> subGroups;

    public GadgetGroup(Gadget baseGadget){
        this.baseGadget = baseGadget;
        offsets = new ArrayList<>();
        subGroups = new ArrayList<>();
    }

    public void addGroup(GadgetGroup group, int offSetX, int offSetY){
        addGroup(group, new Location(offSetX, offSetY));
    }


    public void addGroup(GadgetGroup group, Location offset){
        offsets.add(offset);
        subGroups.add(group);
    }

    public Gadget getBaseGadget(){
        return baseGadget;
    }

    public int getSubGroupsSize(){
        return subGroups.size();
    }

    public Location getSubGroupOffset(int i){
        return offsets.get(i);
    }
    public GadgetGroup getSubGroup(int i){
        return subGroups.get(i);
    }

}
