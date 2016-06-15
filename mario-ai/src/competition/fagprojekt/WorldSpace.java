package competition.fagprojekt;

import ch.idsia.benchmark.mario.environments.Environment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WorldSpace implements Serializable
{
    // Cells are square, but this allows for any rectangular shape
    final static float CELL_WIDTH = 16f;
    final static float CELL_HEIGHT = 16f;

    private Vec2i tableSize = new Vec2i(50, 100);

    private Cell[][] cells;

    private int maxWalkableX = 0; // Largest x observed for a walkable
    private List<Vec2i> rightMostWalkables = new ArrayList<>();

    public WorldSpace() {
        cells = new Cell[tableSize.y][tableSize.x];
    }

    public void integrateObservation(Environment env) {
        // Mario's position in world space, in cell units
        Vec2i marioWorldPos = getMarioCellPos(env);

        // Mario's offset in the observation array
        int marioOffsetX = env.getMarioEgoPos()[0]; // Maybe need to be switched
        int marioOffsetY = env.getMarioEgoPos()[1];

        byte[][] levelObs = env.getLevelSceneObservationZ(2);

        if (marioWorldPos.x + levelObs[0].length >= tableSize.x) {
            expandWorldSpace();
        }

        // Write observed level into WorldSpace
        for(int i = levelObs.length - 1; i >= 0; i--) { // Row = Y. Iterate bottom to top
            for(int j = 0; j < levelObs[0].length; j++) { // Col = X
                int y = i - marioOffsetY + marioWorldPos.y;
                int x = j - marioOffsetX + marioWorldPos.x;

                if(x < 0 || y < 0) // Out of bounds
                    continue;

                CellType cellType = getCellType(levelObs, j, i);

                // Check if space is walkable
                if (0 < i && i < levelObs.length - 1) { // Don't look OOB
                    CellType cellBelow = getCellType(levelObs, j, i + 1);
                    CellType cellAbove = getCellType(levelObs, j, i - 1);

                    // A walkable is a passable cell, with a solid below and passable above
                    if (isPassable(cellType) && !isPassable(cellBelow) && isPassable(cellAbove)) {

                        // Update the right most walkable cells
                        if(x > maxWalkableX) {
                            rightMostWalkables.clear();
                        }
                        if(x >= maxWalkableX) {
                            maxWalkableX = x;

                            Vec2i p = new Vec2i(x, y);
                            if (!rightMostWalkables.contains(p))
                                rightMostWalkables.add(p);
                        }

                        cellType = CellType.Walkable;
                    }
                }

                cells[y][x] = new Cell(cellType);
            }
        }
    }

    private void expandWorldSpace() {
        tableSize.x = 2 * tableSize.x;
        Cell[][] copyCells = cells;
        cells = new Cell[tableSize.y][tableSize.x];
        for (int i = 0; i < cells.length; i++) {
            System.arraycopy(copyCells[i], 0, cells[i], 0, copyCells[i].length);
        }
    }

    private CellType getCellType(byte[][] levelObs, int x, int y) {
        return intToCellType(levelObs[y][x]);
    }

    // All ids can be found in GeneralizerLevelScene
    private CellType intToCellType(int n) {
        CellType type;
        switch (n) {
            case 0:
                type = CellType.Empty;
                break;

            case 2:
                type = CellType.Coin;
                break;

            default:
                type = CellType.Solid;
        }
        return type;
    }

    public static boolean isPassable(CellType ct) {
        return ct == CellType.Empty ||
               ct == CellType.Coin ||
               ct == CellType.Walkable;
    }

    public void setCellType(Vec2i p, CellType type) {
        getCell(p.x, p.y).type = type;
    }

    public void setCell(Vec2i p, Cell cell) {
        cells[p.y][p.x] = cell;
    }

    public Cell getCell(int x, int y) {
        if(0 <= y && y < cells.length && 0 <= x && x < cells[0].length)
            return cells[y][x];
        return null; // Maybe log a warning here? Might not matter
    }

    public Vec2i getSize() {
        return tableSize.clone();
    }

    public List<Vec2i> getRightMostWalkables() {
        // Suggested best practice for returning lists without copying
        return Collections.unmodifiableList(rightMostWalkables);
    }

    public static Vec2i getMarioCellPos(Environment env) {
        Vec2f p = getMarioFloatPos(env);
        int x = (int)(p.x / CELL_WIDTH);
        int y = (int)(p.y / CELL_HEIGHT);
        return new Vec2i(x, y);
    }
    public static Vec2f getMarioFloatPos(Environment env) {
        float[] floatPos = env.getMarioFloatPos();
        return new Vec2f(floatPos[0], floatPos[1]);
    }

    public static Vec2f cellToFloat(Vec2i p) {
        return new Vec2f(p.x * CELL_WIDTH, p.y * CELL_HEIGHT);
    }
    public static Vec2i floatToCell(Vec2f p) {
        return new Vec2i(
                (int)(p.x / CELL_WIDTH),
                (int)(p.y / CELL_HEIGHT));
    }

    // Test
    public void testExpandWorldSpace() {
        expandWorldSpace();
    }
}
