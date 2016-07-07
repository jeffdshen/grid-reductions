package types.configuration;

import types.Direction;
import types.MutableGrid;
import types.configuration.cells.Cell;
import types.configuration.cells.EmptyCell;

public class CellConfiguration extends MutableGrid<Cell> {
    public CellConfiguration(int initialSizeX, int initialSizeY) {
        this(EmptyCell.getInstance(), initialSizeX, initialSizeY);
    }

    public CellConfiguration(Cell background, int initialSizeX, int initialSizeY) {
        super(background, initialSizeX, initialSizeY);
    }

    @Override
    public String toString() {
        int sizeX = this.getSizeX();
        int sizeY = this.getSizeY();
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("CellConfiguration(sizeX=%s, sizeY=%s, cells=", sizeX, sizeY));
        builder.append(System.lineSeparator());
        char[][] grid = new char[sizeX * 3][sizeY * 3];

        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < sizeY; j++) {
                Cell cell = this.getCell(i, j);
                int x = i * 3 + 1;
                int y = j * 3 + 1;

                // set center
                grid[x][y] = cell.getCellType().name().charAt(0);

                // set corners
                grid[x - 1][y - 1] = grid[x + 1][y + 1] = grid[x + 1][y - 1] = grid[x - 1][y + 1] = ' ';

                // set sides
                for (Direction dir : Direction.values()) {
                    Direction flow = null;
                    if (cell.isInput(dir)) {
                        flow = dir.opposite();
                    }
                    if (cell.isOutput(dir)) {
                        flow = dir;
                    }
                    char c = ' ';
                    if (flow != null) {
                        switch (flow) {
                            case NORTH:
                                c = '^';
                                break;
                            case SOUTH:
                                c = 'V';
                                break;
                            case EAST:
                                c = '>';
                                break;
                            case WEST:
                                c = '<';
                                break;
                        }
                    }
                    grid[x + dir.getX()][y + dir.getY()] = c;
                }
            }
        }

        // print transposed
        for (int i = 0; i < sizeY * 3; i++) {
            for (int j = 0; j < sizeX * 3; j++) {
                builder.append(grid[j][i]);
            }
            builder.append(System.lineSeparator());
        }
        builder.append(")");
        return builder.toString();
    }
}