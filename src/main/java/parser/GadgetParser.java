package parser;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Ordering;
import org.apache.log4j.Logger;
import transform.GridUtils;
import types.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.log4j.Level.ERROR;
import static org.apache.log4j.Level.WARN;

public class GadgetParser implements Parser<Gadget> {
    private static final Logger logger = Logger.getLogger(GadgetParser.class.getName());

    public GadgetParser() {
    }

    /**
     * Parse a txt document and create a grid gadget
     * @param file file object for the txt specifying the gadget
     * @return a gadget
     *
     * The format of the txt should be whitespace separated
     * line 1: name
     * line 2: width height
     * line 3: space separated cells.
     *
     * The outer boundary of the cells must either be an _ to represent no input or output port,
     * an I_ followed by the input number, or an O_ followed by the output number
     */
    public Gadget parse(File file) {
        try (FileReader reader = new FileReader(file)) {
            return parse(reader, file.getAbsolutePath());
        } catch (IOException e) {
            logger.log(ERROR, e.getMessage(), e);
        }
        return null;
    }

    @Override
    public Gadget parse(InputStream stream, String id) {
        return parse(new InputStreamReader(stream), id);
    }

    public void write(Gadget g, File file) {
        try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
            out.println(g.getName());

            int x = g.getSizeX();
            int y = g.getSizeY();
            out.println(y + " " + x);

            final MutableGrid<String> output = new MutableGrid<>("_", x + 2, y + 2);
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < y; j++) {
                    output.put(g.getCell(i ,j), i + 1, j + 1);
                }
            }

            for (int i = 0; i < g.getInputSize(); i++) {
                Side side = g.getInput(i);
                Location loc = side.opposite().getLocation();
                output.put("I_" + i, loc.add(1, 1));
            }

            for (int i = 0; i < g.getOutputSize(); i++) {
                Side side = g.getOutput(i);
                Location loc = side.opposite().getLocation();
                output.put("O_" + i, loc.add(1, 1));
            }

            Joiner joiner = Joiner.on(" ");
            for (int i = 0; i < output.getSizeX(); i++) {
                out.println(joiner.join(GridUtils.sliceY(output, i)));
            }
        } catch (IOException e) {
            logger.log(ERROR, e.getMessage(), e);
        }
    }

    public Gadget parse(Reader reader, String gadgetId) {
        return new GadgetParserInstance(reader, gadgetId).parseGadget();
    }

    private static class GadgetParserInstance {
        private final Reader reader;
        private final String gadgetId;

        private int width;
        private int height;
        private String[][] cells;
        private Map<Integer, Side> inputs;
        private Map<Integer, Side> outputs;


        private GadgetParserInstance (Reader reader, String gadgetId) {
            this.reader = reader;
            this.gadgetId = gadgetId;
            inputs = new HashMap<>();
            outputs = new HashMap<>();
        }

        private void warn(String format, Object... objects) {
            logger.log(WARN, String.format("While reading gadget " + gadgetId + ": " + format, objects));
        }

        private void error(String format, Object... objects) {
            logger.log(ERROR, String.format("While reading gadget " + gadgetId + ": " + format, objects));
        }

        private boolean checkDimensions(List<String> dimensions) {
            if (dimensions.size() <  2) {
                error("Not enough dimensions specified - found %d, expected 2.", dimensions.size());
                return true;
            }

            if (dimensions.size() > 2) {
                warn("Too many dimensions specified - found %d, expected 2.", dimensions.size());
                return false;
            }

            return false;
        }

        private boolean checkY(String s, int rowNumber) {
            if (s == null) {
                error("Not enough rows - found %d", rowNumber);
                return true;
            }

            return false;
        }

        private boolean checkX(List<String> row, int rowNumber, int width) {
            if (row.size() < width + 2) {
                error("Not enough columns in row %d - found %d, expected %d", rowNumber, row.size(), width + 2);
                return true;
            }

            if (row.size() > width + 2) {
                warn("Too many columns in row %d - found %d, expected %d", rowNumber, row.size(), width + 2);
                return false;
            }

            return false;
        }

        private boolean parseBoundaryCell(String s, int x, int y) {
            if (s.equals("_")) {
                return false;
            }

            if (s.startsWith("I_")) {
                int port = Integer.parseInt(s.substring(2));
                if (inputs.containsKey(port)) {
                    error("Duplicate input number %d", port);
                    return true;
                }

                Location loc = new Location(x, y);
                for (Direction dir : Direction.values()) {
                    if (inBounds(loc.add(dir))) {
                        inputs.put(port, new Side(loc, dir).opposite());
                        return false;
                    }
                }

                error("Input cell at (%d, %d) not adjacent: \"%s\"", x + 1, y + 1, s);
                return true;
            }

            if (s.startsWith("O_")) {
                int port = Integer.parseInt(s.substring(2));
                if (outputs.containsKey(port)) {
                    error("Duplicate output number %d", port);
                    return true;
                }

                Location loc = new Location(x, y);
                for (Direction dir : Direction.values()) {
                    if (inBounds(loc.add(dir))) {
                        outputs.put(port, new Side(loc, dir).opposite());
                        return false;
                    }
                }

                error("Output cell at (%d, %d) not adjacent: \"%s\"", x + 1, y + 1, s);
                return true;
            }

            error("Unrecognized boundary cell: \"%s\"", s);
            return true;
        }

        private boolean parseInnerCell(String s, int x, int y) {
            cells[x][y] = s;
            return false;
        }

        private boolean checkInputs(int i) {
            if (inputs.containsKey(i)) {
                return false;
            }

            error("Missing input number %d", i);
            return true;
        }

        private boolean checkOutputs(int i) {
            if (outputs.containsKey(i)) {
                return false;
            }

            error("Missing output number %d", i);
            return true;
        }


        private Gadget parseGadget() {
            Splitter splitter = Splitter.on(CharMatcher.BREAKING_WHITESPACE).omitEmptyStrings().trimResults();

            try(BufferedReader in = new BufferedReader(reader)) {
                String name = in.readLine().trim();
                List<String> dimensions = splitter.splitToList(in.readLine());

                if (checkDimensions(dimensions)) {
                    return null;
                }

                width = Integer.parseInt(dimensions.get(0));
                height = Integer.parseInt(dimensions.get(1));

                cells = new String[width][height];
                for (int i = 0; i < height + 2; i++) {
                    String s = in.readLine();
                    if (checkY(s, i)) {
                        return null;
                    }

                    List<String> row = splitter.splitToList(s);

                    if (checkX(row, i, width)) {
                        return null;
                    }

                    for (int j = 0; j < width + 2; j++) {
                        String cell = row.get(j);
                        if (inBounds(j - 1, i - 1)) {
                            if (parseInnerCell(cell, j - 1, i - 1)) {
                                return null;
                            }
                        } else {
                            if (parseBoundaryCell(cell, j - 1, i - 1)) {
                                return null;
                            }
                        }
                    }
                }

                List<Side> inputList = new ArrayList<>();
                List<Side> outputList = new ArrayList<>();

                int maxInput = inputs.isEmpty() ? -1 : Ordering.<Integer>natural().max(inputs.keySet());
                for (int i = 0; i <= maxInput; i++) {
                    if (checkInputs(i)) {
                        return null;
                    }
                    inputList.add(inputs.get(i));
                }

                int maxOutput = outputs.isEmpty() ? -1 : Ordering.<Integer>natural().max(outputs.keySet());
                for (int i = 0; i <= maxOutput; i++) {
                    if (checkOutputs(i)) {
                        return null;
                    }
                    outputList.add(outputs.get(i));
                }

                return new Gadget(name, cells, inputList, outputList);
            } catch (IOException e) {
                logger.log(ERROR, e.getMessage(), e);
                return null;
            } catch (Exception e) {
                logger.log(ERROR, e.getMessage(), e);
                return null;
            }
        }

        private boolean inBounds(Location loc) {
            return inBounds(loc.getX(), loc.getY());
        }

        private boolean inBounds(int x, int y){
            return 0 <= x && x < width && 0 <=y && y < height;
        }
    }
}
