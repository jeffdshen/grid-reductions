package types.configuration.placement;

import types.Location;

/**
 * Created by kevin on 12/6/14.
 * Utility class. Represents a rectangle of cells to be placed on the OutputGrid
 *
 * Methods for getting offsets to a cell.
 *
 * Can Tracks a GadgetGroup that is offset inside
 */
public class OutputRect {
    //number of cells in each dimension of the rect
    private int cellX;
    private int cellY;
    //size of each cell
    private int cellDim;

    private GadgetGroup contents;
    private Location contentsOffset;


    public OutputRect(int cellX, int cellY, int cellDim){
        this.cellX = cellX;
        this.cellY = cellY;
        this.cellDim = cellDim;
    }

    public void setContents(GadgetGroup group, Location offset){
        this.contents = group;
        this.contentsOffset = offset;
    }

    public GadgetGroup getContents(){
        return contents;
    }

    public Location getContentsOffset(){
        return contentsOffset;
    }

}
