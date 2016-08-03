package parser;

import com.google.common.base.*;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import transform.GridUtils;
import types.Direction;
import types.Grid;
import types.Location;
import types.MutableGrid;
import types.configuration.CellConfiguration;
import types.configuration.cells.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CellConfigurationParser implements Parser<CellConfiguration>, Writer<CellConfiguration> {
    private static final Logger logger = Logger.getLogger(CellConfigurationParser.class.getName());

    public static final ImmutableBiMap<CellType, String> ENCODING = getEncoding();
    public static final ImmutableBiMap<Direction, String> WIRING = getWiringSymbols();

    @Override
    public void write(CellConfiguration cellConfig, OutputStream stream) {
        PrintWriter out = new PrintWriter(stream);
        int x = cellConfig.getSizeX();
        int y = cellConfig.getSizeY();
        MutableGrid<String> grid = new MutableGrid<>("_", 3 * x, 3 * y);

        HashBiMap<String, Integer> seen = HashBiMap.create();
        HashMap<Location, Integer> macros = new HashMap<>();
        int id = 0;

        for (int i = 0; i < cellConfig.getSizeX(); i++) {
            for (int j = 0; j < cellConfig.getSizeY(); j++) {
                Location loc = new Location(i, j);
                Cell cell = cellConfig.getCell(loc);
                if (!requiresMacro(cell.getCellType())) {
                    continue;
                }

                String s = encodeNode(cell);
                if (!seen.containsKey(s)) {
                    seen.put(s, id);
                    id++;
                }

                macros.put(loc, seen.get(s));
            }
        }

        out.println(id);
        for (int i = 0; i < id; i++) {
            out.println(encodeMacro(encodeMacroID(i), seen.inverse().get(i)));
        }

        out.println(x + " " + y);

        for (int i = 0; i < cellConfig.getSizeX(); i++) {
            for (int j = 0; j < cellConfig.getSizeY(); j++) {
                Location loc = new Location(i, j);
                Cell cell = cellConfig.getCell(loc);

                String macro = requiresMacro(cell.getCellType()) ? encodeMacroID(macros.get(loc)) : null;
                MutableGrid<String> block = encodeCell(macro, cell);
                grid.put(block, 3 * i, 3 * j);
            }
        }

        for (int i = 0; i < 3 * y; i++) {
            out.println(Joiner.on("\t").join(GridUtils.sliceY(grid, i)));
        }

        out.flush();
    }

    @Override
    public CellConfiguration parse(File file) {
        try (FileReader reader = new FileReader(file)) {
            return parse(reader, file.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Error while opening file", e);
        }

        return null;
    }

    @Override
    public CellConfiguration parse(InputStream stream, String id) {
        return parse(new InputStreamReader(stream), id);
    }

    public CellConfiguration parse(Reader reader, String readerID) {
        try (BufferedReader in = new BufferedReader(reader)) {
            int numMacros = Integer.parseInt(in.readLine());

            HashMap<String, Macro> macros = new HashMap<>();
            Splitter splitter = Splitter.on(CharMatcher.BREAKING_WHITESPACE).omitEmptyStrings().trimResults();

            for (int i = 0; i < numMacros; i++) {
                String s = in.readLine();
                List<String> list = splitter.splitToList(s);
                Preconditions.checkArgument(list.get(0).equals("NODE"), "No macro of this type");

                List<Integer> id = Lists.transform(list.subList(3, list.size()), new Function<String, Integer>() {
                    @Override
                    public Integer apply(String input) {
                        return Integer.parseInt(input);
                    }
                });
                macros.put(list.get(1), new Macro(list.get(2), id));
            }

            List<String> dims = splitter.splitToList(in.readLine());
            int x = Integer.parseInt(dims.get(0));
            int y = Integer.parseInt(dims.get(1));

            MutableGrid<String> grid = new MutableGrid<>("_", x * 3, y * 3);
            for (int j = 0; j < y * 3; j++) {
                List<String> strings = splitter.splitToList(in.readLine());
                for (int i = 0; i < x * 3; i++) {
                    grid.put(strings.get(i), i, j);
                }
            }

            CellConfiguration cells = new CellConfiguration(EmptyCell.getInstance(), x, y);
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < y; j++) {
                    Grid<String> subGrid = GridUtils.subGrid(grid, new Location(i * 3, j * 3), 3, 3);
                    try {
                        Cell cell = decodeCell(subGrid, macros);
                        cells.put(cell, i, j);
                    } catch (Exception e) {
                        logger.error(String.format("Error reading %s at 3x3 block (%d, %d)", readerID, i, j), e);
                        return null;
                    }
                }
            }

            return cells;
        } catch (IOException e) {
            logger.error("Error while reading " + readerID, e);
            return null;
        } catch (Exception e) {
            logger.error("Error while reading " + readerID, e);
            return null;
        }
    }

    private static ImmutableBiMap<CellType, String> getEncoding() {
        ImmutableBiMap.Builder<CellType, String> builder = ImmutableBiMap.builder();
        builder.put(CellType.EMPTY, "E");
        builder.put(CellType.WIRE, "W");
        builder.put(CellType.TURN, "T");
        builder.put(CellType.CROSSOVER, "C");
        builder.put(CellType.NODE, "N");
        builder.put(CellType.PORT, "P");
        return builder.build();
    }

    private static ImmutableBiMap<Direction, String> getWiringSymbols() {
        ImmutableBiMap.Builder<Direction, String> builder = ImmutableBiMap.builder();
        builder.put(Direction.NORTH, "^");
        builder.put(Direction.EAST, ">");
        builder.put(Direction.SOUTH, "V");
        builder.put(Direction.WEST, "<");
        return builder.build();
    }

    private static String getInputSymbol(Direction d, int number) {
        return Strings.repeat(WIRING.get(d.opposite()), number + 1);
    }


    private static String getInputSymbol(Direction d) {
        return WIRING.get(d.opposite());
    }

    private static String getOutputSymbol(Direction d, int number) {
        return Strings.repeat(WIRING.get(d), number + 1);
    }

    private static String getOutputSymbol(Direction d) {
        return WIRING.get(d);
    }

    private static MutableGrid<String> encodeCell(String macro, Cell cell) {
        MutableGrid<String> grid = new MutableGrid<>("_", 3, 3);
        CellType type = cell.getCellType();
        Location center = new Location(1, 1);
        switch (type) {
            case EMPTY:
                grid.put(ENCODING.get(type), center);
                break;
            case WIRE:
            case TURN:
            case CROSSOVER:
                grid.put(ENCODING.get(type), center);
                for (int i = 0; i < cell.inputSize(); i++) {
                    Direction d = cell.getInputDirection(i);
                    grid.put(getInputSymbol(d, i), center.add(d));
                }

                for (int i = 0; i < cell.outputSize(); i++) {
                    Direction d = cell.getOutputDirection(i);
                    grid.put(getOutputSymbol(d, i), center.add(d));
                }
                break;
            case NODE:
                Preconditions.checkArgument(macro != null);
                grid.put(ENCODING.get(type) + macro, center);
                break;
            case PORT:
                Preconditions.checkArgument(macro != null);
                grid.put(ENCODING.get(type) + macro, center);

                for (int i = 0; i < cell.inputSize(); i++) {
                    Direction d = cell.getInputDirection(i);
                    grid.put(getInputSymbol(d, i) + cell.getPortNumber(d), center.add(d));
                }

                for (int i = 0; i < cell.outputSize(); i++) {
                    Direction d = cell.getOutputDirection(i);
                    grid.put(getOutputSymbol(d, i) + cell.getPortNumber(d), center.add(d));
                }
                break;
        }
        return grid;
    }

    private static String encodeNode(Cell cell) {
        Preconditions.checkArgument(requiresMacro(cell.getCellType()));
        return cell.getName() + " " + Joiner.on(" ").join(cell.getId());
    }

    private static String encodeMacro(String macro, String nodeEncoding) {
        return "NODE " + macro + " " + nodeEncoding;
    }

    private static String encodeMacroID(int id) {
        return Integer.toString(id, 36);
    }

    private static boolean requiresMacro(CellType type) {
        switch (type) {
            case EMPTY:
            case WIRE:
            case TURN:
            case CROSSOVER:
                return false;
            case NODE:
            case PORT:
                return true;
        }

        return false;
    }

    private static Port decodePort(String s, Direction d) {
        String symbol;
        boolean input;


        if (s.startsWith(getInputSymbol(d))) {
            symbol = getInputSymbol(d);
            input = true;
        } else if (s.startsWith(getOutputSymbol(d))) {
            symbol = getOutputSymbol(d);
            input = false;
        } else {
            return null;
        }

        int length = s.length() + 1;
        for (int i = 0; i < length; i++) {
            if (s.startsWith(symbol)) {
                s = s.substring(symbol.length());
            } else {
                if (s.length() == 0) {
                    return new Port(input, i - 1, 0);
                } else {
                    return new Port(input, i - 1, Integer.parseInt(s));
                }
            }
        }

        // should never reach here
        return null;
    }

    private static Map<Direction, Port> decodePorts(Grid<String> grid) {
        Location center = new Location(1, 1);
        ImmutableMap.Builder<Direction, Port> builder = ImmutableMap.builder();
        for (Direction d : Direction.values()) {
            String s = grid.getCell(center.add(d));
            Port p = decodePort(s, d);
            if (p != null) {
                builder.put(d, p);
            }
        }

        return builder.build();
    }

    private static Cell decodeCell(Grid<String> grid, Map<String, Macro> macros) {
        Location center = new Location(1, 1);
        String string = grid.getCell(center);
        CellType type = ENCODING.inverse().get(string.substring(0, 1));

        Map<Direction, Port> ports = decodePorts(grid);
        switch (type) {
            case EMPTY:
                return EmptyCell.getInstance();
            case WIRE:
                for (Direction d : Direction.values()) {
                    if (ports.containsKey(d) && ports.get(d).isOutput()) {
                        return WireCell.getWire(d);
                    }
                }

                Preconditions.checkArgument(false, "No output connection found");
            case TURN: {
                Direction input = null;
                Direction output = null;
                for (Direction d : Direction.values()) {
                    if (!ports.containsKey(d)) {
                        continue;
                    }

                    if (ports.get(d).isInput()) {
                        input = d;
                    } else {
                        output = d;
                    }
                }

                Preconditions.checkNotNull(input, "No input connection found");
                Preconditions.checkNotNull(output, "No output connection found");
                return new TurnCell(input, output);
            }
            case CROSSOVER: {
                Direction output1 = null;
                Direction output2 = null;
                for (Direction d : Direction.values()) {
                    if (!ports.containsKey(d) || ports.get(d).isInput()) {
                        continue;
                    }

                    if (ports.get(d).getIndex() == 0) {
                        output1 = d;
                    } else {
                        output2 = d;
                    }
                }

                Preconditions.checkNotNull(output1, "No input connection found");
                Preconditions.checkNotNull(output2, "No output connection found");
                return new CrossoverCell(output1, output2);
            }
            case NODE: {
                Macro macro = macros.get(string.substring(1));
                return new NodeCell(macro.getName(), macro.getId());
            }
            case PORT: {
                Macro macro = macros.get(string.substring(1));
                Map<Integer, Direction> inputs = new HashMap<>();
                Map<Integer, Direction> outputs = new HashMap<>();
                Map<Direction, Integer> portNumber = new HashMap<>();
                for (Direction d : Direction.values()) {
                    if (!ports.containsKey(d)) {
                        continue;
                    }

                    Port port = ports.get(d);
                    if (port.isInput()) {
                        inputs.put(port.getIndex(), d);
                    } else {
                        outputs.put(port.getIndex(), d);
                    }

                    portNumber.put(d, port.getPortNumber());
                }

                ArrayList<Direction> inputList = new ArrayList<>();
                ArrayList<Direction> outputList = new ArrayList<>();
                for (int i = 0; i < Direction.values().length; i++) {
                    if (!inputs.containsKey(i)) {
                        break;
                    }

                    inputList.add(inputs.get(i));
                }

                for (int i = 0; i < Direction.values().length; i++) {
                    if (!outputs.containsKey(i)) {
                        break;
                    }

                    outputList.add(outputs.get(i));
                }

                return new PortCell(macro.getName(), macro.getId(), inputList, outputList, portNumber);
            }
        }

        Preconditions.checkArgument(false, "Unknown type");
        return null;
    }

    private static class Macro {
        private String name;
        private List<Integer> id;

        public Macro(String name, List<Integer> id) {
            this.name = name;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public List<Integer> getId() {
            return id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Macro macro = (Macro) o;

            return name.equals(macro.name) && id.equals(macro.id);

        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + id.hashCode();
            return result;
        }
    }

    private static class Port {
        private final boolean input;
        private final int index;
        private final int portNumber;

        private Port(boolean input, int index, int portNumber) {
            this.input = input;
            this.index = index;
            this.portNumber = portNumber;
        }

        public boolean isInput() {
            return input;
        }

        public boolean isOutput() {
            return !input;
        }

        public int getIndex() {
            return index;
        }

        public int getPortNumber() {
            return portNumber;
        }
    }
}