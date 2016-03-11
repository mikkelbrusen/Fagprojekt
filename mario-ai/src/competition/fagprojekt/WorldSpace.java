package competition.fagprojekt;

import ch.idsia.benchmark.mario.environments.Environment;

public class WorldSpace
{
    // Cells are square, but this allows for any rectagular shape
    final static float CellWidth = 16f;
    final static float CellHeight = 16f;

    Cell[][] cells;

    public WorldSpace() {
        cells = new Cell[100][100];
    }

    public void integrateObservation(Environment env) {

        // TODO: Possibly extend the cells array


        float[] marioFloatPos = env.getMarioFloatPos();

        // Mario's position in world space, in cell units
        int marioWorldX = (int)(marioFloatPos[0] / CellWidth);
        int marioWorldY = (int)(marioFloatPos[1] / CellHeight);

        // Mario's offset in the observation array
        int marioOffsetX = env.getMarioEgoPos()[0]; // Maybe need to be switched
        int marioOffsetY = env.getMarioEgoPos()[1];

        byte[][] levelObs = env.getLevelSceneObservationZ(2);

        // TODO: Only perform observation check when reaching new x, for optimization
        for(int i = 0; i < levelObs.length; i++) { // Row = Y
            for(int j = 0; j < levelObs[0].length; j++) { // Col = X
                int y = i - marioOffsetY + marioWorldY;
                int x = j - marioOffsetX + marioWorldX;

                if(x < 0 || y < 0)
                    continue; // TODO: Make sure this is correct. We assume this is out of bounds

                // TODO: Create method for converting int value to CellType
                CellType cellType = levelObs[i][j] == 0 ? CellType.Empty : CellType.Solid;
                cells[y][x] = new Cell(cellType);
            }
        }

        printWorldSpace();
    }

    void printWorldSpace()
    {
         for(int i = 0; i < 20; i++) { // Row = Y
            String line = String.format("%2d:", i);
            for(int j = 0; j < 100; j++) { // Col = X
                Cell c = cells[i][j];
                int v = c == null ? -1 : (c.type == CellType.Empty ? 0 : 1);
                line += v + " ";
            }
            System.out.println(line);
        }

        System.out.println();
    }
}
