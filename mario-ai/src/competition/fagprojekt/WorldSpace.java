package competition.fagprojekt;

import ch.idsia.benchmark.mario.environments.Environment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WorldSpace implements Serializable
{
    // Cells are square, but this allows for any rectagular shape
    final static float CellWidth = 16f;
    final static float CellHeight = 16f;
    public static int length = 50;
    static int height = 100;

    Cell[][] cells;
    Cell[][] copyCells;

    int maxWalkableX = 0;
    public List<Vec2i> rightMostWalkables = new ArrayList<>();

    public WorldSpace() {
        cells = new Cell[height][length]; // TODO: Dynamic resizing
    }

    public void integrateObservation(Environment env) {

        // Mario's position in world space, in cell units
        Vec2i marioWorldPos = getMarioCellPos(env);

        // Mario's offset in the observation array
        int marioOffsetX = env.getMarioEgoPos()[0]; // Maybe need to be switched
        int marioOffsetY = env.getMarioEgoPos()[1];

        byte[][] levelObs = env.getLevelSceneObservationZ(2);

        if (marioWorldPos.x + levelObs[0].length >= length) {
            expandWorldSpace();
        }
        // TODO: Only perform observation check when reaching new x, for optimization
        for(int i = levelObs.length - 1; i >= 0; i--) { // Row = Y. Iterate bottom to top
            for(int j = 0; j < levelObs[0].length; j++) { // Col = X
                int y = i - marioOffsetY + marioWorldPos.y;
                int x = j - marioOffsetX + marioWorldPos.x;

                if(x < 0 || y < 0)
                    continue; // TODO: Make sure this is correct. We assume this is out of bounds

                // TODO: Create method for converting int value to CellType
                CellType cellType = getCellType(levelObs, j, i);
                // Check if space is walkable
                if(i != levelObs.length - 1) {
                    CellType cellBelow = getCellType(levelObs, j, i + 1);
                    if(i>0){
                        CellType cellAbove = getCellType(levelObs, j, i - 1);
                        if (isPassable(cellType) && !isPassable(cellBelow) && isPassable(cellAbove)) {

                            // Update the right most walkable cells
                            if(x > maxWalkableX)
                                rightMostWalkables.clear();
                            if(x >= maxWalkableX) {
                                maxWalkableX = x;
                                rightMostWalkables.add(new Vec2i(x, y));
                            }

                            cellType = CellType.Walkable;
                        }
                    }
                }

                cells[y][x] = new Cell(cellType);
            }
        }
    }

    public void expandWorldSpace() {
        length = 2 * length;
        copyCells = cells;
        cells = new Cell[height][length];
        for (int i = 0; i < cells.length; i++) {
            System.arraycopy(copyCells[i], 0, cells[i], 0, copyCells[i].length);
        }

    }

    public boolean isPassable(CellType ct) {
        return (ct == CellType.Empty ||
                ct == CellType.Coin ||
                ct == CellType.Walkable);
    }

    public Cell getCell(int x, int y) {
        if(0 <= y && y < cells.length && 0 <= x && x < cells[0].length)
            return cells[y][x];
        return null; // Maybe log a warning here? Might not matter
    }

    public void setCellType(Vec2i p, CellType type) {
        getCell(p.x, p.y).type = type;
    }

    public void setCell(Vec2i p, Cell cell) {
        cells[p.y][p.x] = cell;
    }

    // All ids can be found GeneralizerLevelScene
    CellType getCellType(byte[][] levelObs, int x, int y) { return intToCellType(levelObs[y][x]);}
    CellType intToCellType(int n) {
        CellType type;
        switch (n) {
            case 0:     type = CellType.Empty;
                break;

            case 2:     type = CellType.Coin;
                break;

            default:    type = CellType.Solid;
        }
        return type;
    }

    public void printWorldSpace()
    {
         for(int i = 0; i < 16; i++) { // Row = Y
            String line = String.format("%2d:", i);
            for(int j = 0; j < length; j++) { // Col = X
                Cell c = cells[i][j];
                String v = c == null ? "-1" :
                        (c.type == CellType.Empty || c.type == CellType.Coin ? "0" :
                                (c.type == CellType.Walkable ? "X" : "1"));

                line += v + " ";
            }
            System.out.println(line);
        }

        System.out.println();
    }
    
    public static Vec2i getMarioCellPos(Environment env) {
        Vec2f p = getMarioFloatPos(env);
        int x = (int)(p.x / CellWidth);
        int y = (int)(p.y / CellHeight);
        return new Vec2i(x, y);
    }
    public static Vec2f getMarioFloatPos(Environment env) {
        float[] floatPos = env.getMarioFloatPos();
        return new Vec2f(floatPos[0], floatPos[1]);
    }

    public static Vec2f cellToFloat(Vec2i p) {
        return new Vec2f(p.x * CellWidth, p.y * CellHeight);
    }
    public static Vec2i floatToCell(Vec2f p) {
        return new Vec2i(
                (int)(p.x / CellWidth),
                (int)(p.y / CellHeight));
    }

    public Vec2i getSize() {
        return new Vec2i(
                cells[0].length,
                cells.length);
    }
}
