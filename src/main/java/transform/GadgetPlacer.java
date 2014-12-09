package transform;

import types.Direction;
import types.Gadget;
import types.Location;
import types.configuration.GridConfiguration;
import types.configuration.cells.*;
import types.configuration.placement.GadgetGroup;
import types.configuration.placement.OutputGrid;
import types.configuration.placement.OutputRect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kevin on 12/5/14.
 * Takes in list of Gadgets
 * Transforms GridConfiguration into a 2-d array of strings representing the actual cells
 */
public class GadgetPlacer {

    //input direction, output direction
    private Map<Direction, Map<Direction, Gadget>> turns;

    //most clockwise output direction
    private Map<Direction, Gadget> crossovers;

    //input direction
    private Map<Direction, List<Gadget>> directionalwires;

    private Gadget empty;

    private int[] wireLengths;
    private int[] wireWidths;
    private FrobeniusSolver frobSolver;
    private int minWireLength;
    private int maxWireWidth;
    private int maxTurnDim;

    private Map<String, Gadget> gadgets;
    private int cellSize;
    private int halfCellSize;

    public GadgetPlacer(List<Gadget> wires, Gadget turn, Gadget crossover, Gadget empty, List<Gadget> gadgets) throws Exception {
        //normalize turn south to east
        Gadget temp = normalizeTurn(turn);

        this.empty = empty;
        if(empty.getSizeX() != 1 || empty.getSizeY() != 1){
            throw new Exception("Non-1x1 empty gadgets are not supported yet");
        }
        //create map of turns
        Direction start = Direction.SOUTH;
        this.turns = new HashMap<>();
        for(int i = 0; i< 4; i++){
            Map<Direction, Gadget> outDirGadgets = new HashMap<>();
            outDirGadgets.put(start.anticlockwise(), temp);
            if(start.equals(Direction.SOUTH) || start.equals(Direction.NORTH)){
                outDirGadgets.put(start.clockwise(), reflectGadgetHorizontally(temp));
            }
            else{
                outDirGadgets.put(start.clockwise(), reflectGadgetVertically(temp));
            }
            turns.put(start, outDirGadgets);
            start = start.clockwise();
            temp = rotateGadget(temp);
        }

        //normalize a crossover (outs go north and east)
        temp = normalizeCrossover(crossover);
        start = Direction.EAST;
        for(int i = 0; i< 4; i++){
            crossovers.put(start, temp);
            start = start.clockwise();
            temp = rotateGadget(temp);
        }


        wireLengths = new int[wires.size()];
        wireWidths = new int[wires.size()];
        maxWireWidth = 0;
        List<Gadget> tempWires = new ArrayList<>();
        //normalize wires
        for(int i = 0; i<wires.size(); i++){
            tempWires.add(normalizeWire(wires.get(i)));
            wireLengths[i] = wires.get(i).getSizeY();
            wireWidths[i] = wires.get(i).getSizeX();
            maxWireWidth = Math.max(wires.get(i).getSizeX(), maxWireWidth);
        }
        start = Direction.SOUTH;
        directionalwires.put(start, tempWires);
        for(int i = 0; i< 3; i++){
            List<Gadget> rotatedWires = new ArrayList<>();
            for(Gadget wire: tempWires) {
                rotatedWires.add(rotateGadget(wire));
            }
            directionalwires.put(start, rotatedWires);
            tempWires = rotatedWires;
            start = start.clockwise();
        }

        frobSolver = new FrobeniusSolver(this.wireLengths);
        minWireLength = frobSolver.getSolvableCutoff()+1;
        minWireLength = Math.max(minWireLength, maxWireWidth);

        //determine size for each Cell
        this.gadgets = new HashMap<>();
        for( Gadget g : gadgets){
            this.gadgets.put(g.getName(), g);
        }
        maxTurnDim = 0;
        maxTurnDim = Math.max(maxTurnDim, Math.max(turn.getSizeX(), turn.getSizeY()));
        int maxGadgetDim = maxTurnDim;
        maxGadgetDim = Math.max(maxGadgetDim, maxWireWidth);
        maxGadgetDim = Math.max(maxGadgetDim, Math.max(crossover.getSizeX(), crossover.getSizeY()));
        int maxNumPorts = 0;
        for(Gadget gadget : gadgets){
            maxGadgetDim = Math.max(maxGadgetDim, Math.max(gadget.getSizeX(), gadget.getSizeY()));
            maxNumPorts = Math.max(maxNumPorts, gadget.getInputSize() + gadget.getOutputSize());
        }
        // max gadget size + minwireLength + (max#outputs/inputs)*(turnDim + wireWidth)
        this.cellSize = 6*(minWireLength + maxGadgetDim) + 4*((maxNumPorts+2)*(maxWireWidth + maxTurnDim));
        this.halfCellSize = cellSize/2;

    }

    public String[][] place(GridConfiguration gridConfig) throws Exception {
        int sizeX = gridConfig.getSizeX();
        int sizeY = gridConfig.getSizeY();
        OutputGrid output = new OutputGrid(sizeX, sizeY, cellSize, empty);
        Boolean[][] visited = new Boolean[sizeX][sizeY];
        for(int i = 0; i < sizeX; i++){
            for(int j = 0; j < sizeY; j++){
                if(!visited[i][j]){
                    Cell curCell = gridConfig.getCell(i,j);
                    switch(curCell.getCellType()){
                        case EMPTY:
                            visited[i][j] = true;
                            break;
                        case WIRE:
                            visited[i][j] = true;
                            output.placeRect(getWireRect(curCell), i, j);
                            break;
                        case TURN:
                            visited[i][j] = true;
                            output.placeRect(getTurnRect(curCell), i,j);
                            break;
                        case CROSSOVER:
                            visited[i][j] = true;
                            output.placeRect(getCrossoverRect(curCell), i,j);
                            break;
                        case NODE:
                        case PORT:
                            String gadgetName = curCell.getName();
                            int x = 1;
                            int y = 1;
                            while(((i + x) <  sizeX)){
                                Cell temp = gridConfig.getCell(i+x,j);
                                if(temp.getCellType() == CellType.NODE || temp.getCellType() == CellType.PORT) {
                                    x++;
                                }
                                else{
                                    break;
                                }
                            }
                            while(((j + y) <  sizeY)){
                                Cell temp = gridConfig.getCell(i,j+y);
                                if(temp.getCellType() == CellType.NODE || temp.getCellType() == CellType.PORT) {
                                    y++;
                                }
                                else{
                                    break;
                                }
                            }
                            Cell[][] gadgetCells = new Cell[x][y];
                            for(int ii = 0; ii < x; ii++){
                                for(int jj = 0; jj< y; jj++){
                                    gadgetCells[ii][jj] = gridConfig.getCell(i+ii,j + jj);
                                    visited[i+ii][j+jj] = true;
                                }
                            }
                            output.placeRect(getGadgetRect(gadgetName, gadgetCells), i,j);
                            break;
                    }
                }
            }
        }


        return output.getOutputGrid();
    }


    /***
     * method for getting offset (rel to out) to line up an input and output cells on 2 gadgets
     * @param out gadget whose output is being lined up
     * @param outIdx which output
     * @param in gadget whose input is being lined up
     * @param inIdx which input
     * @param flip gets offset (rel to in)
     * @return offset relative to out that in should be at
     */
    private Location getAlignmentOffset(Gadget out, int outIdx, Gadget in, int inIdx, boolean flip) throws Exception {
        Location input = in.getInput(inIdx);
        Location output = out.getOutput(outIdx);
        Direction inDir = getEdge(input, in.getSizeX(), in.getSizeY());
        Direction outDir = getEdge(output, out.getSizeX(), out.getSizeY());
        if(inDir.opposite() != outDir){
            throw new Exception("Can't align gadget inputs and outputs that aren't on opposite sides");
        }
        if(!flip) {
            return new Location(output.getX() - input.getX(), output.getY() - input.getY());
        }
        else{
            return new Location(input.getX() - output.getX(), input.getY() - output.getY());
        }
    }

    /***
     * Generate a gadget group representing a wire to target location
     * The base gadget contains the input/output to the wire.
     *
     * Target location should not be opposite of inDir
     *
     * @param inDir initial wire direction
     * @param target the offset from gadget group input to output
     * @param flipped whether the base gadget contains the output of wire  and wire direction is flipped
     * @return gadget representing wire to target location. Wire output direction same as input
     */
    private GadgetGroup generateSameDirWire(Direction inDir, Location target, boolean flipped) throws Exception {
        //assert conditions
        int cutoff = minWireLength + maxTurnDim + maxTurnDim;
        Direction finalTurnDir;
        switch(inDir){
            case NORTH:
            case SOUTH:
                if(Math.abs(target.getX()) == 0){ //change for EW
                    if(flipped){
                        return generateStraightWire(inDir.opposite(), Math.abs(target.getY()), true);
                    }
                    return generateStraightWire(inDir, Math.abs(target.getY()), false); //change for EW
                }
                else if(Math.abs(target.getX()) > cutoff){  //change for EW
                    //2-turn
                    finalTurnDir = Direction.getClosestDirection(target.getX(), 0); //change for EW
                    if(flipped){
                        inDir = inDir.opposite();
                        finalTurnDir = finalTurnDir.opposite();
                    }
                    Gadget turn = turns.get(inDir).get(finalTurnDir);
                    Location turnIn = turn.getInput(0);
                    Location turnOut = turn.getOutput(0);
                    int x = target.getX() - (turnOut.getX() - turnIn.getX());
                    int y = target.getY() - (turnOut.getY() - turnIn.getY());
                    if(flipped){
                        x = target.getX() - (turnIn.getX() - turnOut.getX());
                        y = target.getY() - (turnIn.getY() - turnOut.getY());
                        GadgetGroup ret = new GadgetGroup(turn);
                        GadgetGroup sub = generateTurnWire(finalTurnDir.opposite(), inDir.opposite(), new Location(x, y), true);
                        ret.addGroup(sub, getAlignmentOffset( sub.getBaseGadget(), 0, turn, 0, true));
                        return ret;
                    }
                    GadgetGroup ret = new GadgetGroup(turn);
                    GadgetGroup sub = generateTurnWire(finalTurnDir, inDir, new Location(x, y));
                    ret.addGroup(sub, getAlignmentOffset(turn, 0, sub.getBaseGadget(), 0, false));
                    return ret;
                }
                else{
                    if(Math.abs(target.getY()) < cutoff + minWireLength){ //change for EW
                        throw new Exception("can't handle this yet when turning");
                    }

                    finalTurnDir = Direction.getClosestDirection(target.getX(), 0); //change for EW
                }
                break;
            case EAST:
            case WEST:
                if(Math.abs(target.getY()) == 0){
                    if(flipped){
                        return generateStraightWire(inDir.opposite(), Math.abs(target.getX()), true);
                    }
                    return generateStraightWire(inDir, Math.abs(target.getX()), false);
                }
                else if(Math.abs(target.getY()) > cutoff){
                    //2-turn
                    finalTurnDir = Direction.getClosestDirection(0, target.getY());
                    if(flipped){
                        inDir = inDir.opposite();
                        finalTurnDir = finalTurnDir.opposite();
                    }
                    Gadget turn = turns.get(inDir).get(finalTurnDir);
                    Location turnIn = turn.getInput(0);
                    Location turnOut = turn.getOutput(0);
                    int x = target.getX() - (turnOut.getX() - turnIn.getX());
                    int y = target.getY() - (turnOut.getY() - turnIn.getY());
                    if(flipped){
                        x = target.getX() - (turnIn.getX() - turnOut.getX());
                        y = target.getY() - (turnIn.getY() - turnOut.getY());
                        GadgetGroup ret = new GadgetGroup(turn);
                        GadgetGroup sub = generateTurnWire(finalTurnDir.opposite(), inDir.opposite(), new Location(x, y), true);
                        ret.addGroup(sub, getAlignmentOffset( sub.getBaseGadget(), 0, turn, 0, true));
                        return ret;
                    }
                    GadgetGroup ret = new GadgetGroup(turn);
                    GadgetGroup sub = generateTurnWire(finalTurnDir, inDir, new Location(x, y));
                    ret.addGroup(sub, getAlignmentOffset(turn, 0, sub.getBaseGadget(), 0, false));
                    return ret;
                }
                else{
                    if(Math.abs(target.getX()) < cutoff + minWireLength){
                        throw new Exception("can't handle this yet when turning");
                    }

                    finalTurnDir = Direction.getClosestDirection(0, target.getY()); //change for EW
                }
                break;
            default:
                throw new Exception("What is this direction even?");
        }
        if(flipped){
            finalTurnDir = finalTurnDir.opposite();
            inDir = inDir.opposite();
        }
        //4-turn case
        Direction initTurnDir = finalTurnDir.opposite();
        Gadget firstTurn = turns.get(inDir).get(initTurnDir);
        Gadget secondTurn = turns.get(initTurnDir).get(inDir);
        Gadget thirdTurn = turns.get(inDir).get(finalTurnDir);
        if(flipped){
            finalTurnDir = finalTurnDir.opposite();
            inDir = inDir.opposite();
        }
        int thirdTurnOutX = target.getX()
                - getOutInDist(firstTurn, true, flipped)
                - (cutoff*initTurnDir.getX())
                - getOutInDist(secondTurn, true, flipped)
                - (minWireLength * inDir.getX())
                - getOutInDist(thirdTurn, true, flipped);
        int thirdTurnOutY = target.getY()
                - getOutInDist(firstTurn, false, flipped)
                - (cutoff*initTurnDir.getY())
                - getOutInDist(secondTurn, false, flipped)
                - (minWireLength * inDir.getY())
                - getOutInDist(thirdTurn, false, flipped);
        if(!flipped) {
            GadgetGroup sub = generateTurnWire(finalTurnDir, inDir, new Location(thirdTurnOutX, thirdTurnOutY));
            GadgetGroup turn3 = new GadgetGroup(thirdTurn);
            turn3.addGroup(sub, getAlignmentOffset(thirdTurn, 0, sub.getBaseGadget(), 0, false));
            GadgetGroup temp = appendToStraightWire(inDir, minWireLength, turn3, 0, false);
            GadgetGroup turn2 = new GadgetGroup(secondTurn);
            turn2.addGroup(temp, getAlignmentOffset(secondTurn, 0, temp.getBaseGadget(), 0, false));
            GadgetGroup temp2 = appendToStraightWire(initTurnDir, cutoff, turn2, 0, false);
            GadgetGroup turn1 = new GadgetGroup(firstTurn);
            turn1.addGroup(temp2, getAlignmentOffset(firstTurn, 0, temp2.getBaseGadget(), 0, false));
            return turn1;
        }
        else{
            GadgetGroup sub = generateTurnWire(finalTurnDir, inDir, new Location(thirdTurnOutX, thirdTurnOutY), true);
            GadgetGroup turn3 = new GadgetGroup(thirdTurn);
            turn3.addGroup(sub, getAlignmentOffset(sub.getBaseGadget(), 0, thirdTurn, 0, true));
            GadgetGroup temp = appendToStraightWire(inDir.opposite(), minWireLength, turn3, 0, true);
            GadgetGroup turn2 = new GadgetGroup(secondTurn);
            turn2.addGroup(temp, getAlignmentOffset(temp.getBaseGadget(), 0, secondTurn, 0, true));
            GadgetGroup temp2 = appendToStraightWire(initTurnDir.opposite(), cutoff, turn2, 0, true);
            GadgetGroup turn1 = new GadgetGroup(firstTurn);
            turn1.addGroup(temp2, getAlignmentOffset(temp2.getBaseGadget(), 0, firstTurn, 0, true));
            return turn1;
        }
    }

    /***
     * helper method, get the difference from output 0  to output 1 for a gadget
     * @param g gadget
     * @param isXDist whether to return x distance or y distance
     * @param flipped whether we should flip in and out
     * @return distance between input/ouptut for gadget
     */
    private int getOutInDist(Gadget g, boolean isXDist, boolean flipped){
        if(flipped){
            return -getOutInDist(g, isXDist, false);
        }
        if(isXDist){
            return g.getOutput(0).getX() - g.getInput(0).getX();
        }
        return g.getOutput(0).getY() - g.getInput(0).getY();
    }

    private GadgetGroup generateTurnWire(Direction inDir, Direction outDir, Location target) throws Exception{
        return generateTurnWire(inDir, outDir, target, false);
    }
    /***
     * generate gadget group representing a turn using a single turn
     * Precondition that distance to target is > minWireLength + turn dimension in both dimensions
     * @param inDir input direction
     * @param outDir output direction
     * @param target location to go to.
     * @param flipped whether input/output direction should be flipped and target is where the wire comes from
     * @return gadget group representing a turn with 1 turn gadget
     */
    private GadgetGroup generateTurnWire(Direction inDir, Direction outDir, Location target, boolean flipped) throws Exception {
        //assert conditions
        if(target.getX() == 0 || target.getY() == 0){
            throw new Exception("Invalid target for generating turn wire " + target );
        }
        Direction xDir = Direction.getClosestDirection(target.getX(),0);
        Direction yDir = Direction.getClosestDirection(0,target.getY());
        if(!((xDir == inDir || xDir == outDir) && (yDir == inDir || yDir == outDir))){
            throw new Exception("Invalid input and output Directions " + inDir + ", " + outDir +" for target " + target);
        }
        int cutoff = minWireLength + maxTurnDim;
        if(Math.abs(target.getX()) < cutoff || Math.abs(target.getY()) < cutoff ){
            throw new Exception("Target " + target + " for turn wire has dimensions below cutoff " + cutoff);
        }
        if(flipped){
            inDir = inDir.opposite();
            outDir = outDir.opposite();
        }
        Gadget turn = turns.get(inDir).get(outDir);
        Location turnIn = turn.getInput(0);
        Location turnOut = turn.getOutput(0);
        int length = 0;
        switch(outDir){
            case NORTH:
            case SOUTH:
                length = Math.abs(target.getY());
                length = length - Math.abs(turnOut.getY() - turnIn.getY());
                break;
            case EAST:
            case WEST:
                length = Math.abs(target.getX());
                length = length - Math.abs(turnOut.getY() - turnIn.getX());
                break;
        }
        GadgetGroup turnAndWire = new GadgetGroup(turn);
        GadgetGroup wireOut = generateStraightWire(outDir, length, flipped);
        if(!flipped){
            turnAndWire.addGroup(wireOut, getAlignmentOffset(turn, 0, wireOut.getBaseGadget(), 0, false));
        }else{
            turnAndWire.addGroup(wireOut, getAlignmentOffset(wireOut.getBaseGadget(), 0, turn, 0, true));
        }

        switch(inDir){
            case NORTH:
            case SOUTH:
                length = Math.abs(target.getY());
                length = length - Math.abs(turnOut.getY() - turnIn.getY());
                break;
            case EAST:
            case WEST:
                length = Math.abs(target.getX());
                length = length - Math.abs(turnOut.getY() - turnIn.getX());
                break;
        }
        return appendToStraightWire(inDir, length, turnAndWire, 0, flipped);
    }


    // TODO assumes wires are straight
    /***
     * Generate a gadget group representing a straight wire
     * Input given by the base gadget, output is length away from the input in direction d.
     * @param d direction from input to output
     * @param length length of wire. Should not be 0
     * @param flipped true if the base gadget is the output and input is length away in direction d.
     * @return gadget group for straight wire
     */
    private GadgetGroup generateStraightWire(Direction d, int length, boolean flipped) throws Exception {
        return appendToStraightWire(d, length, null, 0, flipped);
    }

    /***
     * append input idx of gadget group to output of straight wire of length in direction d
     * if flipped, appends output idx of gadget group to input of wire length in direction d
     * @param d direction the wire goes (input to output)
     * @param length length of the wire
     * @param g gadget whose input the wire output is connected to
     * @param idx idx of the input wire
     * @param flipped change all input -> output and vice versa in description. Behaves like a prepend
     * @return gadget group of straight wire attached to specified group. base gadget is input of wire
     * @throws Exception
     */
    private GadgetGroup appendToStraightWire(Direction d, int length, GadgetGroup g, int idx,boolean flipped) throws Exception {
        List<Gadget> wireList = directionalwires.get(d);
        int[] coeff = frobSolver.getCoefficients(length);
        if(coeff == null){
            throw new Exception("Requested wire length can't be generated");
        }
        GadgetGroup currentGroup = g; //Better way to do this?
        int curIdx = idx;
        for(int i = 0; i < coeff.length; i ++){
            for(int j = 0; j < coeff[i]; j ++){
                Gadget wire = wireList.get(i);
                GadgetGroup nextGroup = new GadgetGroup(wire);
                if(currentGroup != null){
                    //figure out offsets
                    Location offset = getAlignmentOffset(wire, 0, currentGroup.getBaseGadget(), curIdx, flipped);
                    if(flipped){
                        offset = getAlignmentOffset(currentGroup.getBaseGadget(), curIdx, wire, 0, true);
                    }
                    nextGroup.addGroup(currentGroup, offset);
                }
                currentGroup = nextGroup;
                curIdx = 0;
            }
        }
        return currentGroup;
    }

    //Method for creating rects. has no contents.
    private OutputRect makeOutputRect(int cellX, int cellY){
        return new OutputRect(cellX,cellY, cellSize);
    }

    //Input should be a wire cell
    private OutputRect getWireRect(Cell c) throws Exception {
        OutputRect rect = makeOutputRect(1,1);
        //determine direction.
        Direction inDir = c.getInputDirection(0);
        GadgetGroup wire = generateStraightWire(inDir.opposite(), cellSize, false);
        Location wireInput = wire.getBaseGadget().getInput(0);
        Gadget baseWire = wire.getBaseGadget();
        switch(inDir){
            case NORTH:
                rect.setContents(wire, new Location(halfCellSize - wireInput.getX(), 0));
                break;
            case SOUTH:
                rect.setContents(wire, new Location(halfCellSize - wireInput.getX(), cellSize - baseWire.getSizeY()));
                break;
            case EAST:
                rect.setContents(wire, new Location(cellSize - baseWire.getSizeX(), halfCellSize - wireInput.getY()));
                break;
            case WEST:
                rect.setContents(wire, new Location(0, halfCellSize - wireInput.getY()));
                break;
        }
        return rect;
    }

    //Input should be turn cell
    private OutputRect getTurnRect(Cell c) throws Exception{
        OutputRect rect = makeOutputRect(1,1);
        Direction inDir = c.getInputDirection(0);
        Direction outDir = c.getOutputDirection(0);
        GadgetGroup turn;
        Location wireInput;
        switch(inDir){
            case NORTH:
                if(outDir == Direction.EAST){
                    turn = generateTurnWire(inDir.opposite(), outDir, new Location(halfCellSize, -halfCellSize));
                }
                else{
                    turn = generateTurnWire(inDir.opposite(), outDir, new Location(-halfCellSize, -halfCellSize));
                }
                wireInput = turn.getBaseGadget().getInput(0);
                rect.setContents(turn, new Location(halfCellSize - wireInput.getX(), 0));
                break;
            case SOUTH:
                if(outDir == Direction.EAST){
                    turn = generateTurnWire(inDir.opposite(), outDir, new Location(halfCellSize, halfCellSize));
                }
                else{
                    turn = generateTurnWire(inDir.opposite(), outDir, new Location(-halfCellSize, halfCellSize));
                }
                wireInput = turn.getBaseGadget().getInput(0);
                rect.setContents(turn, new Location(halfCellSize - wireInput.getX(), cellSize - turn.getBaseGadget().getSizeY()));
                break;
            case EAST:
                if(outDir == Direction.NORTH){
                    turn = generateTurnWire(inDir.opposite(), outDir, new Location(-halfCellSize, halfCellSize));
                }
                else{
                    turn = generateTurnWire(inDir.opposite(), outDir, new Location(-halfCellSize, -halfCellSize));
                }
                wireInput = turn.getBaseGadget().getInput(0);
                rect.setContents(turn, new Location(cellSize - turn.getBaseGadget().getSizeX(), halfCellSize - wireInput.getY()));
                break;
            case WEST:
                if(outDir == Direction.NORTH){
                    turn = generateTurnWire(inDir.opposite(), outDir, new Location(halfCellSize, halfCellSize));
                }
                else{
                    turn = generateTurnWire(inDir.opposite(), outDir, new Location(halfCellSize, -halfCellSize));
                }
                wireInput = turn.getBaseGadget().getInput(0);
                rect.setContents(turn, new Location(0, halfCellSize - wireInput.getY()));
                break;
        }
        return rect;
    }

    //input should be crossover cell
    private OutputRect getCrossoverRect(Cell c) throws Exception {
        OutputRect rect = makeOutputRect(1,1);
        //determine direction.
        Direction outDir1 = c.getOutputDirection(0);
        Direction outDir2 = c.getOutputDirection(1);
        if(outDir1.clockwise() == outDir2){
            outDir1 = outDir2;
        }
        Gadget crossover = crossovers.get(outDir1);
        //place in center of cell
        Location center = new Location(halfCellSize, halfCellSize);
        GadgetGroup crossoverGroup = new GadgetGroup(crossover);
        for(int i = 0; i < 2; i ++){
            Location input = crossover.getInput(i);
            Direction inputDir = getEdge(input, crossover.getSizeX(),crossover.getSizeY());
            Location start = center.offset(input);
            Location target;
            switch(inputDir){
                case NORTH:
                    target = new Location(halfCellSize, 0);
                    break;
                case SOUTH:
                    target = new Location(halfCellSize, cellSize);
                    break;
                case EAST:
                    target = new Location(cellSize, halfCellSize);
                    break;
                case WEST:
                    target = new Location(0, halfCellSize);
                    break;
                default:
                    throw new Exception("Not a direction");
            }
            GadgetGroup inWire = generateSameDirWire(inputDir, new Location(target.getX() - start.getX(), target.getY() - start.getY()),true);
            crossoverGroup.addGroup(inWire, getAlignmentOffset(inWire.getBaseGadget(),0, crossover, i, true));
        }
        for(int i = 0; i < 2; i ++){
            Location output = crossover.getOutput(i);
            Direction outputDir = getEdge(output, crossover.getSizeX(),crossover.getSizeY());
            Location start = center.offset(output);
            Location target;
            switch(outputDir){
                case NORTH:
                    target = new Location(halfCellSize, 0);
                    break;
                case SOUTH:
                    target = new Location(halfCellSize, cellSize);
                    break;
                case EAST:
                    target = new Location(cellSize, halfCellSize);
                    break;
                case WEST:
                    target = new Location(0, halfCellSize);
                    break;
                default:
                    throw new Exception("Not a direction");
            }
            GadgetGroup outWire = generateSameDirWire(outputDir, new Location(target.getX() - start.getX(), target.getY() - start.getY()),false);
            crossoverGroup.addGroup(outWire, getAlignmentOffset(crossover, i, outWire.getBaseGadget(), 0, false));
        }
        rect.setContents(crossoverGroup, center);
        return rect;
    }

    //Assumes Port cells are in order and same side as port on gadget
    private OutputRect getGadgetRect(String gadgetName, Cell[][] cells) throws Exception {
        int sizeX = cells.length; int sizeY = cells[0].length;
        OutputRect rect = makeOutputRect(sizeX, sizeY);
        if(!gadgets.containsKey(gadgetName)){
            throw new Exception("missing gadget " + gadgetName);
        }
        Gadget g = gadgets.get(gadgetName);
        //place in center of cell (0 0)
        Location center = new Location(halfCellSize, halfCellSize);
        GadgetGroup gadgetGroup = new GadgetGroup(g);
        int buffer = maxTurnDim + maxWireWidth;
        //iterate through sides
        //WEST
        int numTurn = 0;
        int straightLength = minWireLength;
        Direction curDir = Direction.WEST;
        for(int i = sizeY-1; i >= 0; i--){ //change
            if(cells[0][i].isInput(curDir)){ //change access
                int portIdx = cells[0][i].getPortNumber(curDir);
                Location input = g.getInput(portIdx);
                Location start = center.offset(input);
                Location target = new Location(0, cellSize*i + halfCellSize); // change
                int length = numTurn*buffer + straightLength;
                GadgetGroup temp = generateSameDirWire(curDir, //change loc
                        new Location(target.getX()- start.getX() + length, target.getY() - start.getY()),
                        true);
                GadgetGroup inWire = appendToStraightWire(curDir.opposite(), length, temp, 0, true);
                gadgetGroup.addGroup(inWire, getAlignmentOffset(inWire.getBaseGadget(), 0, g, portIdx, true));
                numTurn ++;
            }
            if(cells[0][i].isOutput(curDir)){ //change access
                int portIdx = cells[0][i].getPortNumber(curDir);
                Location output = g.getOutput(portIdx);
                Location start = center.offset(output);
                Location target = new Location(0, cellSize*i + halfCellSize); //change
                int length = numTurn*buffer + straightLength;
                GadgetGroup temp = generateSameDirWire(curDir, //change loc
                        new Location(target.getX()- start.getX() + length, target.getY() - start.getY()),
                        false);
                GadgetGroup outWire = appendToStraightWire(curDir, length, temp, 0, false);
                gadgetGroup.addGroup(outWire, getAlignmentOffset(g, portIdx, outWire.getBaseGadget(), 0, true));
                numTurn ++;
            }
        }
        //EAST
        numTurn = 0;
        straightLength = cellSize*(sizeX - 1) + minWireLength;
        curDir = Direction.EAST;
        for(int i = sizeY-1; i >= 0; i--){ //change
            if(cells[sizeX - 1][i].isInput(curDir)){ //change access
                int portIdx = cells[sizeX - 1][i].getPortNumber(curDir);
                Location input = g.getInput(portIdx);
                Location start = center.offset(input);
                Location target = new Location(cellSize*sizeX, cellSize*i + halfCellSize); // change
                int length = numTurn*buffer + straightLength;
                GadgetGroup temp = generateSameDirWire(curDir, //change loc
                        new Location(target.getX()- start.getX() - length, target.getY() - start.getY()),
                        true);
                GadgetGroup inWire = appendToStraightWire(curDir.opposite(), length, temp, 0, true);
                gadgetGroup.addGroup(inWire, getAlignmentOffset(inWire.getBaseGadget(), 0, g, portIdx, true));
                numTurn ++;
            }
            if(cells[sizeX - 1][i].isOutput(curDir)){ //change access
                int portIdx = cells[sizeX - 1][i].getPortNumber(curDir);
                Location output = g.getOutput(portIdx);
                Location start = center.offset(output);
                Location target = new Location(cellSize*sizeX, cellSize*i + halfCellSize); //change
                int length = numTurn*buffer + straightLength;
                GadgetGroup temp = generateSameDirWire(curDir, //change loc
                        new Location(target.getX()- start.getX() - length, target.getY() - start.getY()),
                        false);
                GadgetGroup outWire = appendToStraightWire(curDir, length, temp, 0, false);
                gadgetGroup.addGroup(outWire, getAlignmentOffset(g, portIdx, outWire.getBaseGadget(), 0, true));
                numTurn ++;
            }
        }

        //SOUTH
        numTurn = 0;
        straightLength = cellSize*(sizeY - 1) + minWireLength;
        curDir = Direction.SOUTH;
        for(int i = sizeX-1; i >= 0; i--){ //change
            if(cells[i][sizeY - 1].isInput(curDir)){ //change access
                int portIdx = cells[i][sizeY - 1].getPortNumber(curDir);
                Location input = g.getInput(portIdx);
                Location start = center.offset(input);
                Location target = new Location(cellSize*i + halfCellSize, cellSize*sizeY); // change
                int length = numTurn*buffer + straightLength;
                GadgetGroup temp = generateSameDirWire(curDir, //change loc
                        new Location(target.getX()- start.getX(), target.getY() - start.getY() - length),
                        true);
                GadgetGroup inWire = appendToStraightWire(curDir.opposite(), length, temp, 0, true);
                gadgetGroup.addGroup(inWire, getAlignmentOffset(inWire.getBaseGadget(), 0, g, portIdx, true));
                numTurn ++;
            }
            if(cells[i][sizeY - 1].isOutput(curDir)){ //change access
                int portIdx = cells[i][sizeY - 1].getPortNumber(curDir);
                Location output = g.getOutput(portIdx);
                Location start = center.offset(output);
                Location target = new Location(cellSize*i + halfCellSize, cellSize*sizeY); //change
                int length = numTurn*buffer + straightLength;
                GadgetGroup temp = generateSameDirWire(curDir, //change loc
                        new Location(target.getX()- start.getX(), target.getY() - start.getY() - length),
                        false);
                GadgetGroup outWire = appendToStraightWire(curDir, length, temp, 0, false);
                gadgetGroup.addGroup(outWire, getAlignmentOffset(g, portIdx, outWire.getBaseGadget(), 0, true));
                numTurn ++;
            }
        }

        //NORTH
        numTurn = 0;
        straightLength = minWireLength;
        curDir = Direction.NORTH;
        for(int i = sizeX-1; i >= 0; i--){ //change
            if(cells[i][0].isInput(curDir)){ //change access
                int portIdx = cells[i][0].getPortNumber(curDir);
                Location input = g.getInput(portIdx);
                Location start = center.offset(input);
                Location target = new Location(cellSize*i + halfCellSize, 0); // change
                int length = numTurn*buffer + straightLength;
                GadgetGroup temp = generateSameDirWire(curDir, //change loc
                        new Location(target.getX()- start.getX(), target.getY() - start.getY() + length),
                        true);
                GadgetGroup inWire = appendToStraightWire(curDir.opposite(), length, temp, 0, true);
                gadgetGroup.addGroup(inWire, getAlignmentOffset(inWire.getBaseGadget(), 0, g, portIdx, true));
                numTurn ++;
            }
            if(cells[i][0].isOutput(curDir)){ //change access
                int portIdx = cells[i][0].getPortNumber(curDir);
                Location output = g.getOutput(portIdx);
                Location start = center.offset(output);
                Location target = new Location(cellSize*i + halfCellSize, 0); //change
                int length = numTurn*buffer + straightLength;
                GadgetGroup temp = generateSameDirWire(curDir, //change loc
                        new Location(target.getX()- start.getX(), target.getY() - start.getY() + length),
                        false);
                GadgetGroup outWire = appendToStraightWire(curDir, length, temp, 0, false);
                gadgetGroup.addGroup(outWire, getAlignmentOffset(g, portIdx, outWire.getBaseGadget(), 0, true));
                numTurn ++;
            }
        }
        rect.setContents(gadgetGroup, center);
        return rect;
    }
    // place gadgetgroup with just the gadget.
    // "place" gadget in center of cell 0 0
    // iterate through sides of gadget to construct wires
    //     create gadget group representing wires
    //         Add straight wire from output and go straight to halfway + # turn offsets of last cell in side.
    //         generateSameDirWire to output loc on portcell
    //     add wire gadgetgroup to original gadget subgroups


    /*****
     * Get the Direction of the edge a point lies on (north is top edge)
     * @param edgePoint - a Location. Assumed to be on the edge of a gadget
     * @param width The width of the gadget
     * @param height The height of the gadget
     * @return Direction of the edge the point lies on
     */
    private Direction getEdge(Location edgePoint, int width, int height){
        if(edgePoint.getX() == 0){
            return Direction.WEST;
        }
        if(edgePoint.getY() == 0){
            return Direction.NORTH;
        }
        if(edgePoint.getX() >= width -1){
            return Direction.EAST;
        }
        return Direction.SOUTH;
    }

    /**
     * Normalize a crossover - more clockwise output east
     * @param g crossover gadget
     * @return normalized crossover
     */
    private Gadget normalizeCrossover(Gadget g){
        Gadget out = g;
        Location output1 = out.getOutput(0);
        Location output2 = out.getOutput(1);
        Direction output1Dir = getEdge(output1, out.getSizeX(), out.getSizeY());
        Direction output2Dir = getEdge(output2, out.getSizeX(), out.getSizeY());
        if(output1Dir.clockwise().equals(output2Dir)){
            output1Dir = output2Dir;
        }
        switch(output1Dir){
            case WEST:
                out = rotateGadget(out);
            case NORTH:
                return rotateGadget(out);
            case SOUTH:
                return rotateAntiGadget(out);
            case EAST:
                return out;
        }
        return out;
    }

    /***
     * Normalize a turn. input south, output east
     * @param g turn gadget
     * @return normalized turn
     */
    private Gadget normalizeTurn(Gadget g){
        Gadget out = g;
        Location input = out.getInput(0);
        Location output = out.getOutput(0);
        Direction inputDir = getEdge(input, out.getSizeX(), out.getSizeY());
        Direction outputDir = getEdge(output, out.getSizeX(), out.getSizeY());
        switch(inputDir){
            case NORTH:
                out = rotateGadget(out);
            case EAST:
                out = rotateGadget(out);
                break;
            case WEST:
                out = rotateAntiGadget(out);
                break;
            case SOUTH:
                break;
        }
        if(inputDir.clockwise().equals(outputDir)){
            out = reflectGadgetHorizontally(out);
        }
        return out;
    }

    /***
     * Normalize a wire gadget (input south, output north)
     * @param g wire gadget
     * @return normalized wire
     */
    private Gadget normalizeWire(Gadget g){
        Location input = g.getInput(0);
        Direction inputDir = getEdge(input, g.getSizeX(), g.getSizeY());
        switch(inputDir){
            case NORTH:
                return reflectGadgetVertically(g);
            case SOUTH:
                return g;
            case EAST:
                return rotateGadget(g);
            case WEST:
                return rotateAntiGadget(g);
        }
        return g;
    }

    /***
     * Rotate a gadget anti-clockwise
     * @param g gadget to rotate
     * @return rotated gadget
     */
    private Gadget rotateAntiGadget(Gadget g){
        int sizeX = g.getSizeX();
        int sizeY = g.getSizeY();
        String[][] newCells = new String[sizeY][sizeX];
        for(int i = 0; i< sizeX; i ++){
            for(int j = 0; j < sizeY; j++){
                newCells[j][sizeX-1-i] = g.getCell(i,j);
            }
        }
        List<Location> newInputs = new ArrayList<>();
        for(int i = 0; i < g.getInputSize(); i++){
            Location oldInput = g.getInput(i);
            newInputs.add(new Location(oldInput.getY(), sizeX - oldInput.getX() - 1));
        }
        List<Location> newOutputs = new ArrayList<>();
        for(int i= 0; i < g.getOutputSize(); i++){
            Location oldOutput = g.getOutput(i);
            newOutputs.add(new Location(oldOutput.getY(), sizeX - oldOutput.getX() - 1));
        }
        return new Gadget(g.getName(), newCells, newInputs, newOutputs);
    }

    /***
     * Rotate a gadget clockwise
     * @param g gadget to rotate
     * @return rotated gadget
     */
     private Gadget rotateGadget(Gadget g){
        int sizeX = g.getSizeX();
        int sizeY = g.getSizeY();
        String[][] newCells = new String[sizeY][sizeX];
        for(int i = 0; i< sizeX; i ++){
            for(int j = 0; j < sizeY; j++){
                newCells[sizeY-1-j][i] = g.getCell(i,j);
            }
        }
        List<Location> newInputs = new ArrayList<>();
        for(int i = 0; i < g.getInputSize(); i++){
            Location oldInput = g.getInput(i);
            newInputs.add(new Location(sizeY - oldInput.getY() - 1, oldInput.getX()));
        }
        List<Location> newOutputs = new ArrayList<>();
        for(int i= 0; i < g.getOutputSize(); i++){
            Location oldOutput = g.getOutput(i);
            newOutputs.add(new Location(sizeY - oldOutput.getY() - 1, oldOutput.getX()));
        }
        return new Gadget(g.getName(), newCells, newInputs, newOutputs);
    }

    /***
     * Reflect a gadget along the Y-axis
     * @param g gadget to reflect
     * @return reflected gadget
     */
    private Gadget reflectGadgetHorizontally(Gadget g){
        int sizeX = g.getSizeX();
        int sizeY = g.getSizeY();
        String[][] newCells = new String[sizeX][sizeY];
        for(int i = 0; i < sizeX; i ++){
            for(int j = 0; j < sizeY; j ++){
                newCells[i][j] = g.getCell(sizeX - i - 1,j);
            }
        }
        List<Location> newInputs = new ArrayList<>();
        for(int i = 0; i < g.getInputSize(); i++){
            Location oldInput = g.getInput(i);
            newInputs.add(new Location(sizeX - oldInput.getX() - 1, oldInput.getY()));
        }
        List<Location> newOutputs = new ArrayList<>();
        for(int i= 0; i < g.getOutputSize(); i++){
            Location oldOutput = g.getOutput(i);
            newOutputs.add(new Location(sizeX - oldOutput.getX() - 1, oldOutput.getY()));
        }
        return new Gadget(g.getName(), newCells, newInputs, newOutputs);
    }
    /***
     * Reflect a gadget along the X-axis
     * @param g gadget to reflect
     * @return reflected gadget
     */
    private Gadget reflectGadgetVertically(Gadget g){
        int sizeX = g.getSizeX();
        int sizeY = g.getSizeY();
        String[][] newCells = new String[sizeX][sizeY];
        for(int i = 0; i < sizeX; i ++){
            for(int j = 0; j < sizeY; j ++){
                newCells[i][j] = g.getCell(i,sizeY - j - 1);
            }
        }
        List<Location> newInputs = new ArrayList<>();
        for(int i = 0; i < g.getInputSize(); i++){
            Location oldInput = g.getInput(i);
            newInputs.add(new Location(oldInput.getX(), sizeY - oldInput.getY() - 1));
        }
        List<Location> newOutputs = new ArrayList<>();
        for(int i= 0; i < g.getOutputSize(); i++){
            Location oldOutput = g.getOutput(i);
            newOutputs.add(new Location(oldOutput.getX(), sizeY - oldOutput.getY() - 1));
        }
        return new Gadget(g.getName(), newCells, newInputs, newOutputs);
    }

}