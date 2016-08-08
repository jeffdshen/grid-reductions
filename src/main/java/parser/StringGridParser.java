package parser;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.MutableClassToInstanceMap;
import org.apache.log4j.Logger;
import transform.GridUtils;
import types.Gadget;
import types.Grid;
import types.MutableGrid;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.apache.log4j.Level.ERROR;

public class StringGridParser implements Parser<Grid<String>>, Writer<Grid<String>>{
    private static final Logger logger = Logger.getLogger(StringGridParser.class.getName());

    @Override
    public Grid<String> parse(File file) {
        try (FileReader reader = new FileReader(file)) {
            return parse(reader, file.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Error while opening file", e);
        }

        return null;
    }

    @Override
    public Grid<String> parse(InputStream stream, String id) {
        return parse(new InputStreamReader(stream), id);
    }

    public Grid<String> parse(Reader reader, String id) {
        try (BufferedReader in = new BufferedReader(reader)) {
            Splitter splitter = Splitter.on(CharMatcher.BREAKING_WHITESPACE).omitEmptyStrings().trimResults();
            ArrayList<List<String>> list = new ArrayList<>();

            int x = 0;
            for (String s = in.readLine(); s != null; s = in.readLine()) {
                List<String> line = splitter.splitToList(s);
                list.add(line);
                x = line.size();
            }

            int y = list.size();

            try {
                MutableGrid<String> grid = new MutableGrid<>("", x, y);
                for (int i = 0; i < x; i++) {
                    for (int j = 0; j < y; j++) {
                        if (list.size() > j && list.get(j).size() > i) {
                            grid.put(list.get(j).get(i), i, j);
                        }
                    }
                }

                return grid;
            } catch (Exception e) {
                logger.error("Error while parsing " + id, e);
                return null;
            }
        } catch (IOException e) {
            logger.error("Error while opening " + id, e);
        }

        return null;
    }

    public void write(Grid<String> grid, File file) {
        try (OutputStream stream = new FileOutputStream(file)) {
            write(grid, stream);
        } catch (IOException e) {
            logger.log(ERROR, e.getMessage(), e);
        }
    }

    @Override
    public void write(Grid<String> grid, OutputStream stream) {
        PrintWriter out = new PrintWriter(stream);
        for (int i = 0; i < grid.getSizeY(); i++) {
            out.println(Joiner.on(" ").join(GridUtils.sliceY(grid, i)));
        }
        out.flush();
    }
}
