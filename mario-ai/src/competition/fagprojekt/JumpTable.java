package competition.fagprojekt;

import java.io.*;

/**
 * Created by Mikkel on 07/06/16.
 */
public class JumpTable implements Serializable{
    final static float maxSpeed = 20f;
    public final static int intervals = 40;
    public final static float stepSize = maxSpeed / intervals;

    int xRange = 14;
    int yRange = 8;
    int xMin = -xRange/2;
    int xMax = xRange/2;
    int yMin = -yRange/2;
    int yMax = yRange/2;
    public JumpPath[][][] jumpPathTable;

    public static final String JUMP_TABLE_PATH = "jumptable.ser";

    public static JumpTable getJumpTable(JumpPathfinder jumpPathfinder, boolean forceSerialize) {
        if (forceSerialize) {
            return serializeJumpTable(jumpPathfinder);
        }

        return checkForSerializedFile(jumpPathfinder);
    }

    private JumpTable(JumpPathfinder jumpPathfinder) {
        jumpPathTable = new JumpPath[xRange][yRange][intervals];
        for (int i = xMin; i < xMax; i++) {
            for (int j = yMin; j < yMax; j++) {
                for (int k = 0; k < intervals; k++) {
                    Vec2f start = new Vec2i(0, 0).toVec2f();
                    Vec2f end = new Vec2i(i, j).toVec2f();
                    start.x += 0.5f * WorldSpace.CellWidth;
                    end.x += 0.5f * WorldSpace.CellWidth;

                    Vec2f velocity = new Vec2f((-0.5f * intervals + k) * stepSize,0);

                    JumpPath path = jumpPathfinder.searchAStar(start,velocity,end);
                    jumpPathTable[i+xRange/2][j+yRange/2][k] = path;
                }
            }
        }
    }

    public int getVelocityIdx(float v) {
        //int velIndex = (int)(v.x / jumpTable.stepSize);
        float t = (v - -maxSpeed) / (maxSpeed - -maxSpeed);
        int idx = (int)t * (intervals-1);
        return idx;
    }
    public static JumpTable checkForSerializedFile(JumpPathfinder jumpPathfinder) {
        File f = new File(JUMP_TABLE_PATH);
        if(f.exists() && !f.isDirectory()) {
            JumpTable jumpTable = null;
            try {
                FileInputStream fileIn = new FileInputStream(JUMP_TABLE_PATH);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                jumpTable = (JumpTable) in.readObject();
                in.close();
                fileIn.close();
            } catch(Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
            System.out.println("Deserialized JumpTable");
            return jumpTable;

        } else {
            return serializeJumpTable(jumpPathfinder);
        }
    }

    public static JumpTable serializeJumpTable(JumpPathfinder jumpPathfinder) {
        JumpTable jumpTable = new JumpTable(jumpPathfinder);
        try {
            FileOutputStream fileOut =
                    new FileOutputStream(JUMP_TABLE_PATH);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(jumpTable);
            out.close();
            fileOut.close();
            System.out.printf("Serialized data is saved in " + JUMP_TABLE_PATH + "\n");
            return jumpTable;
        } catch(IOException i) {
            i.printStackTrace();
            return null;
        }
    }

    public JumpPath findPath(int x, int y, float velX) {
        if(xMin <= x && x <= xMax && yMin <= y && y <= yMax){
            return jumpPathTable[x][y][(int)(velX/stepSize)];
        }
        return null;
    }
}
