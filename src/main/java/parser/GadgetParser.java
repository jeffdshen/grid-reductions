package parser;

import types.Gadget;
import types.Location;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kevin on 11/28/14.
 */
public class GadgetParser {

    public GadgetParser(){
    }

    /**
     * Parse a txt document and create a grid gadget
     * @param path path to the txt specifying the gadget
     * @return
     * @throws IOException
     *
     * The format of the txt should be space separated
     * line 1: width height
     * line 2: input1 input2 ...
     * line 3: output1 output2 ...
     * line 4: space separated things for cells
     *
     *
     * inputs and outputs have format xLoc,yLoc or (xLoc,yLoc)
     */
    public Gadget parseGadget(String name, String path) throws IOException{
        String[][] cells;
        List<Location> inputs = new ArrayList<Location>();
        List<Location> outputs = new ArrayList<Location>();

        try(BufferedReader br = new BufferedReader(new FileReader(path))){
            String[] dimensions = br.readLine().split(" ");
            int width = Integer.parseInt(dimensions[0]);
            int height = Integer.parseInt(dimensions[1]);

            cells = new String[width][height];
            for(String input : br.readLine().split(" ")){
                String[] loc = input.replaceAll("[()]","").split(",");
                inputs.add(new Location(Integer.parseInt(loc[0]), Integer.parseInt(loc[1])));
            }

            for(String output : br.readLine().split(" ")){
                String[] loc = output.replaceAll("[()]","").split(",");
                outputs.add(new Location(Integer.parseInt(loc[0]), Integer.parseInt(loc[1])));
            }

            int curRow = 0;
            String line;
            while((line = br.readLine()) != null){
                int curCol = 0;
                for(String cell : line.split(" ")){
                    if(inBounds(curRow, curCol, width, height)){
                        cells[curRow][curCol] = cell;
                        curCol ++;
                    }
                    else{
                        System.err.println("Warning: cell data exceeds provided dimensions. Skipping data.");
                    }
                }
                if(curCol < width){
                    System.err.println("Warning: only " + curCol + " entries specified for row " + (curRow + 1));
                }
                curRow ++;
            }
            if(curRow < height){
                System.err.println("Warning: only " + curRow + " rows have been specified");
            }
            return new Gadget(name, cells, inputs, outputs);
        }
        catch(Exception e){
            System.err.println("Error parsing the gadget file: " + e.getMessage());
        }
        return null;
    }

    private boolean inBounds(int x, int y, int width, int height){
        return 0 <= x && x < width && 0 <=y && y < height;
    }

}
