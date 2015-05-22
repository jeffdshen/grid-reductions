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
    private Gadget empty;

    public OutputGrid(int cellX, int cellY, int cellDim, Gadget empty){
        this.cellX = cellX;
        this.cellY = cellY;
        this.cellDim = cellDim;
        this.empty = empty;
        outputGrid = new String[cellX*cellDim][cellY*cellDim];
//        System.out.println(cellDim);
    }

    public void placeRect(OutputRect rect, int cellOffsetX, int cellOffsetY){
        placeGadgetGroup(rect.getContents(),
                         rect.getContentsOffset().add(cellOffsetX * cellDim, cellOffsetY * cellDim));
    }

    private void placeGadgetGroup(GadgetGroup group, Location offset){
        placeGadgetGroup(group, offset.getX(), offset.getY());
    }
    private void placeGadgetGroup(GadgetGroup group, int offsetX, int offsetY){
//        System.out.println("Placing gadgetgroup " + "" + " at " + offsetX + ", " + offsetY);
        placeGadget(group.getBaseGadget(), offsetX, offsetY);
        for(int i = 0; i < group.getSubGroupsSize(); i++){
            placeGadgetGroup(group.getSubGroup(i),
                             group.getSubGroupOffset(i).add(offsetX, offsetY));
        }
    }

    private void placeGadget(Gadget g, int offsetX, int offsetY){
//        System.out.println("Placing gadget " + g.getName() + " "+ g.getSizeX() + ", "+ g.getSizeY());
//        System.out.println("at "  + offsetX + ", " + offsetY);
        for(int i = 0; i < g.getSizeX(); i ++){
            for(int j = 0; j < g.getSizeY(); j ++){
                placeString(g.getCell(i,j), offsetX + i, offsetY + j);
            }
        }
    }

    // checks that no gridcell written to twice when writing
    private void placeString(String s, int x, int y){
        if(x < 0 || x >= cellDim*cellX || y < 0 || y>=cellDim*cellY){
            System.err.println("TRYING TO WRITE OUTSIDE OF BOUNDS " + x + " " + y);
            return;
        }
        if(outputGrid[x][y] == null){
            outputGrid[x][y] = s;
        }
        else{
            System.err.println("Overwriting cell " + x + " , " + y + " during output");
            System.err.println("Old value = " + outputGrid[x][y] + " new value = " + s);
        }
    }

    private void fillEmpty(){
        for(int i = 0; i < outputGrid.length; i ++ ){
            for(int j = 0; j < outputGrid[0].length; j++){
                if(outputGrid[i][j]==null){
                    placeGadget(empty, i, j);
                }
            }
        }
    }

    /***
     * Fills in "empty" squares and returns the grid.
     *
     * @return
     */
    public String[][] getOutputGrid(){
        fillEmpty();
        return outputGrid;
    }

}
