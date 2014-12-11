package parser;

import types.Gadget;
import types.Location;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GadgetParser {

    public GadgetParser(){
    }

    /**
     * Parse a txt document and create a grid gadget
     * @param file file object for the txt specifying the gadget
     * @return a gadget
     * @throws IOException
     *
     * The format of the txt should be space separated
     * line 1: name
     * line 2: width height
     * line 3: input1 input2 ...
     * line 4: output1 output2 ...
     * line 5: space separated things for cells
     *
     * inputs and outputs have format xLoc,yLoc or (xLoc,yLoc)
     */
    public Gadget parseGadget(File file) throws IOException {
        String[][] cells;
        List<Location> inputs = new ArrayList<>();
        List<Location> outputs = new ArrayList<>();

        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            String name = br.readLine();
            String[] dimensions = br.readLine().split(" ");
            int width = Integer.parseInt(dimensions[0]);
            int height = Integer.parseInt(dimensions[1]);

            cells = new String[width][height];
            for(String input : br.readLine().split(" ")){
                if(input.length() == 0){
                    continue;
                }
                String[] loc = input.replaceAll("[()]","").split(",");
                inputs.add(new Location(Integer.parseInt(loc[0]), Integer.parseInt(loc[1])));
            }

            for(String output : br.readLine().split(" ")){
                if(output.length() == 0){
                    continue;
                }
                String[] loc = output.replaceAll("[()]","").split(",");
                outputs.add(new Location(Integer.parseInt(loc[0]), Integer.parseInt(loc[1])));
            }

            int curY = 0;
            String line;
            while((line = br.readLine()) != null){
                int curX = 0;
                for(String cell : line.split(" ")){
                    if(inBounds(curX, curY, width, height)){
                        cells[curX][curY] = cell;
                        curX++;
                    } else{
                        System.err.println("Warning: cell data exceeds provided dimensions. Skipping data.");
                    }
                }
                if(curX < width){
                    System.err.println(
                        String.format("File %s - Warning: only %s entries specified for row %s", file, curX, (curY + 1))
                    );
                }
                curY++;
            }
            if(curY < height){
                System.err.println("Warning: only " + curY + " rows have been specified");
            }
            return new Gadget(name, cells, inputs, outputs);
        }
    }

    private boolean inBounds(int x, int y, int width, int height){
        return 0 <= x && x < width && 0 <=y && y < height;
    }

}
