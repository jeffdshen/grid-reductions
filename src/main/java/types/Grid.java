package types;

public interface Grid<E> {
    boolean isValid(Location loc);

    boolean isValid(int x, int y);

    E getCell(Location loc);

    E getCell(int x, int y);

    int getSizeX();

    int getSizeY();
}
