package types.configuration.placement;

import types.Gadget;
import types.Location;

/**
 * Created by kevin on 12/6/14.
 * Represents final output file in terms of square cells of a fixed
 */
public class OutputGrid {
    private String[][] outputGrid;
    //number of cells in each dimension
    private int cellX;
    private int cellY;
    //size of each cell
    private int cellDim;

    public OutputGrid(int cellX, int cellY, int cellDim){
        this.cellX = cellX;
        this.cellY = cellY;
        this.cellDim = cellDim;
        outputGrid = new String[cellX*cellDim][cellY*cellDim];
    }

    public void placeRect(OutputRect rect, int cellOffsetX, int cellOffsetY){
        placeGadgetGroup(rect.getContents(),
                         rect.getContentsOffset().offset(cellOffsetX*cellDim, cellOffsetY*cellDim));
    }

    private void placeGadgetGroup(GadgetGroup group, Location offset){
        placeGadgetGroup(group, offset.getX(), offset.getY());
    }
    private void placeGadgetGroup(GadgetGroup group, int offsetX, int offsetY){
        placeGadget(group.getBaseGadget(), offsetX, offsetY);
        for(int i = 0; i < group.getSubGroupsSize(); i++){
            placeGadgetGroup(group.getSubGroup(i),
                             group.getSubGroupOffset(i).offset(offsetX, offsetY));
        }
    }

    private void placeGadget(Gadget g, int offsetX, int offsetY){
        for(int i = 0; i < g.getSizeX(); i ++){
            for(int j = 0; j < g.getSizeY(); j ++){
                placeString(g.getCell(i,j), offsetX + i, offsetY + j);
            }
        }
    }

    // checks that no gridcell written to twice when writing
    private void placeString(String s, int x, int y){
        if(outputGrid[x][y] == null){
            outputGrid[x][y] = s;
        }
        else{
            System.err.println("Overwriting cell " + x + " , " + y + " during output");
        }
    }

    public String[][] getOutputGrid(){
        return outputGrid;
    }

}
