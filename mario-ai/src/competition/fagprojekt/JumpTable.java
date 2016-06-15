package competition.fagprojekt;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Mikkel on 07/06/16.
 */
public class JumpTable implements Serializable{
    final static float maxSpeed = 11f;
    public final static int intervals = 40;
    public final static float stepSize = maxSpeed / (float)intervals;

    int xRange = 14;
    int yRange = 8;
    int xMin = -xRange/2;
    int xMax = xRange/2;
    int yMin = -yRange/2;
    int yMax = yRange/2;
    public JumpPath[][][] jumpPathTable;

    public static final String JUMP_TABLE_PATH = "jumptable.ser";
    public static boolean forceNextSerialization = true;

    WorldSpace worldSpace;

    public static JumpTable getJumpTable(JumpPathfinder jumpPathfinder, boolean forceSerialize) {
        if (forceNextSerialization) {
            forceNextSerialization = false;
            return serializeJumpTable(jumpPathfinder);
        }

        return checkForSerializedFile(jumpPathfinder);
    }

    private JumpTable(JumpPathfinder jumpPathfinder) {
        worldSpace = jumpPathfinder.getWorldSpace();

        Vec2i marioOffset = new Vec2i(xMax, yMax);

        Vec2f start = new Vec2i(0, 0).toVec2f();
        start.x += 0.5f * WorldSpace.CELL_WIDTH;
        start.y += WorldSpace.CELL_HEIGHT;
        start = Vec2f.add(start, marioOffset.toVec2f());

        // Insert block below
        Vec2i cellPosBelowStart = start.toCell();
        Cell cellBelowStart = worldSpace.getCell(cellPosBelowStart.x, cellPosBelowStart.y);
        if (cellBelowStart != null)
            worldSpace.setCellType(cellPosBelowStart, CellType.Solid);
        else
            worldSpace.setCell(cellPosBelowStart, new Cell(CellType.Solid));

        jumpPathTable = new JumpPath[xRange][yRange][intervals];
        for (int i = xMin; i < xMax; i++) {
            for (int j = yMin; j < yMax; j++) {
                for (int k = 0; k < intervals; k++) {
                    boolean inMario =
                        i == 0 && j == 0 ||
                        i == 0 && j == -1;
                    boolean walkable =
                        i == 1 && j == 0 ||
                        i == -1 && j == 0;
                    if (inMario || walkable)
                        continue;

                    Vec2f end = new Vec2i(i, j).toVec2f();
                    end.x += 0.5f * WorldSpace.CELL_WIDTH;
                    end.y += WorldSpace.CELL_HEIGHT;
                    end = Vec2f.add(end, marioOffset.toVec2f());

                    Vec2f velocity = new Vec2f((-0.5f * intervals + k) * stepSize,0);

                    // Insert block below
                    Vec2i cellPosBelow = end.toCell();
                    //cellPosBelow.y += 1; // Not needed because of +CELL_HEIGHT
                    Cell cellBelow = worldSpace.getCell(cellPosBelow.x, cellPosBelow.y);
                    if (cellBelow != null)
                        worldSpace.setCellType(cellPosBelow, CellType.Solid);
                    else
                        worldSpace.setCell(cellPosBelow, new Cell(CellType.Solid));

                    JumpPath path = jumpPathfinder.searchAStar(start, velocity, end);

                    // Subtract offset again
                    if (path != null) {
                        path.actionUnit.endPosition.x -= marioOffset.toVec2f().x;
                        path.actionUnit.endPosition.y -= marioOffset.toVec2f().y;
                    }

                    jumpPathTable[i+xRange/2][j+yRange/2][k] = path;

                    // Remove block below
                    if (cellBelow != null)
                        worldSpace.setCellType(cellPosBelow, cellBelow.type);
                    else
                        worldSpace.setCell(cellPosBelow, null);
                }
            }

            // Remove block below
            if (cellBelowStart != null)
                worldSpace.setCellType(cellPosBelowStart, cellBelowStart.type);
            else
                worldSpace.setCell(cellPosBelowStart, null);
        }
    }

    public int getVelocityIdx(float v) {
        float t = (v - -maxSpeed) / (maxSpeed - -maxSpeed);
        return (int)(t * (float)(intervals - 1));
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

    // TODO: Remove debug parameter
    public JumpPath findPathRelative(int x, int y, float velX, boolean debug) {
        final int xOffset = xRange / 2;
        final int yOffset = yRange / 2;
        int vIx = getVelocityIdx(velX);
        boolean isBad =
                x < xMin || x >= xMax ||
                y < yMin || y >= yMax ||
                vIx < 0 || vIx >= intervals;

        if (isBad)
            return null;

        return jumpPathTable[x + xOffset][y + yOffset][vIx];
    }

    public JumpPath findPathAbsolute(Vec2i pos, Vec2i origin, float velX, boolean debug) {
        Vec2i relative = Vec2i.subtract(pos, origin);
        return findPathRelative(relative.x, relative.y, velX, debug);
    }

    // TODO: FIX: DOESNT FUCKNG WORK
    public List<JumpPath> getRelativeJumpsForVelocity(float velocity) {
        List<JumpPath> jumps = new LinkedList<>();
        for (int i = xMin; i < xMax; i++) {
            for (int j = yMin; j < yMax; j++) {
                JumpPath jp = findPathRelative(i, j, velocity, false);

                if (jp == null)
                    continue;

                jumps.add(jp);
            }
        }

        return jumps;
    }

    public void printJumpTable(float v) {
       for (int y = yMin; y < yMax; y++) {
            String line = "";
            for (int x = xMin; x < xMax; x++) {
                String c = " .";
                JumpPath jp = findPathRelative(x, y, v, false);
                if (jp != null)
                    c = String.format("%2d", jp.actionUnit.getActions().size());
                if (x == 0 && y == 0)
                    c = " x";
                line += c + " ";
            }
            System.out.println(line);
        }
    }
}
