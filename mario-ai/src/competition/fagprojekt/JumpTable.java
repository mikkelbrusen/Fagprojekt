package competition.fagprojekt;

/**
 * Created by Mikkel on 07/06/16.
 */
public class JumpTable {
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

    public JumpTable(JumpPathfinder jumpPathfinder) {
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
        int idx = (int)t * intervals;
        return idx;
    }

    public JumpPath findPath(int x, int y, float velX) {
        if(xMin <= x && x <= xMax && yMin <= y && y <= yMax){
            return jumpPathTable[x][y][(int)(velX/stepSize)];
        }
        return null;
    }
}
