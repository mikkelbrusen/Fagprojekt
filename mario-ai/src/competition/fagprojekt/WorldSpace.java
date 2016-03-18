package competition.fagprojekt;

import ch.idsia.benchmark.mario.environments.Environment;

import java.util.ArrayList;
import java.util.List;

public class WorldSpace
{
    // Cells are square, but this allows for any rectagular shape
    final static float CellWidth = 16f;
    final static float CellHeight = 16f;

    Cell[][] cells;

    int maxWalkableX = 0;
    public List<Vec2i> rightMostWalkables = new ArrayList<>();

    public WorldSpace() {
        cells = new Cell[100][100];
    }

    public void integrateObservation(Environment env) {

        // TODO: Possibly extend the cells array

        // Mario's position in world space, in cell units
        Vec2i marioWorldPos = getMarioCellPos(env);

        // Mario's offset in the observation array
        int marioOffsetX = env.getMarioEgoPos()[0]; // Maybe need to be switched
        int marioOffsetY = env.getMarioEgoPos()[1];

        byte[][] levelObs = env.getLevelSceneObservationZ(2);

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
                    if (cellType == CellType.Empty && cellBelow == CellType.Solid) {

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

                cells[y][x] = new Cell(cellType);
            }
        }
    }

    public Cell getCell(int x, int y) {
        if(0 <= y && y < cells.length && 0 <= x && x < cells[0].length)
            return cells[y][x];
        return null; // Maybe log a warning here? Might not matter
    }

    CellType getCellType(byte[][] levelObs, int x, int y)
    {
        CellType type = CellType.Empty;

        int v = levelObs[y][x];
        if(v == 0)
            type = CellType.Empty;
        else
            type = CellType.Solid;

        return type;
    }

    void printWorldSpace()
    {
         for(int i = 0; i < 20; i++) { // Row = Y
            String line = String.format("%2d:", i);
            for(int j = 0; j < 100; j++) { // Col = X
                Cell c = cells[i][j];
                String v = c == null ? "-1" :
                        (c.type == CellType.Empty ? "0" :
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
}
