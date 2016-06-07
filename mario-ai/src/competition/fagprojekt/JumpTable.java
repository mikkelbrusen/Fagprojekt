package competition.fagprojekt;

/**
 * Created by Mikkel on 07/06/16.
 */
public class JumpTable {
    final static float maxSpeed = 9.7f;
    final static int intervals = 50;
    final static float stepSize = 9.7f/intervals;

    int xRange = 14;
    int yRange = 8;
    int xMin = -xRange/2;
    int xMax = xRange/2;
    int yMin = -yRange/2;
    int yMax = yRange/2;
    JumpPath[][][] jumptable;

    public JumpTable() {
        jumptable = new JumpPath[xRange][yRange][intervals];
    }

    public void initializeTable(JumpPathfinder jumpPathFinder){
        for (int i = -xMin; i < xMax; i++) {
            for (int j = yMin; j < yMax; j++) {
                for (int k = 0; k < intervals; k++) {
                    Vec2i start = new Vec2i(0,0);
                    Vec2i end = new  Vec2i(i,j);
                    Vec2f velocity = new Vec2f(k*stepSize,0);
                    jumptable[i][j][k]=jumpPathFinder.searchAStar(start,velocity,end);
                }
            }
        }
    }

    public JumpPath findPath(int x, int y, float velX){
        if(xMin <= x && x <= xMax && yMin <= y && y <= yMax){
            return jumptable[x][y][(int)(velX/stepSize)];
        }
        return null;
    }
}
