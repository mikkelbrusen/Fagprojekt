package competition.fagprojekt.Debug;

import ch.idsia.benchmark.mario.engine.GlobalOptions;
import ch.idsia.benchmark.mario.engine.LevelScene;
import ch.idsia.benchmark.mario.engine.MarioVisualComponent;
import ch.idsia.benchmark.mario.environments.MarioEnvironment;
import competition.fagprojekt.SimMario;
import competition.fagprojekt.Vec2f;
import competition.fagprojekt.Vec2i;
import competition.fagprojekt.WorldSpace;

import java.awt.*;
import java.util.List.*;
import java.util.ArrayList;

/**
 * Created by max on 01/04/16.
 */
public class Debug
{
    private static Debug _instance;

    private LevelScene _level;
    private WorldSpace _worldSpace;

    public Vec2i debugCell = new Vec2i(5, 10);

    public int frameCount = 0;

    // To make sure the debug stuff doesn't get drawn over,
    // we collect all entities to be drawn and render them at once,
    // via a call from MarioVisualComponent, after it has rendered
    // everything else.
    private ArrayList<DebugGfx> _graphicsThisFrame = new ArrayList<>();

    private Debug(LevelScene levelScene, WorldSpace worldSpace) {
        _level = levelScene;
        _worldSpace = worldSpace;
    }

    public static void initialize(LevelScene levelScene, WorldSpace worldSpace) {
        _instance = new Debug(levelScene, worldSpace);
    }

    public static Debug getInstance() {
        return _instance;
    }

    public void update() {
        if(DebugInput.keysPressed[DebugInput.KEY_LEFT])
            debugCell.x--;
        if(DebugInput.keysPressed[DebugInput.KEY_RIGHT])
            debugCell.x++;
        if(DebugInput.keysPressed[DebugInput.KEY_UP])
            debugCell.y--;
        if(DebugInput.keysPressed[DebugInput.KEY_DOWN])
            debugCell.y++;

        drawCell(debugCell, Color.magenta);

        frameCount++;
    }

    public void renderFrame(Graphics g) {
        for(DebugGfx gfx : _graphicsThisFrame)
            gfx.render(g);

        _graphicsThisFrame.clear();
    }

    public void drawLine(Vec2f p0, Vec2f p1) {
        drawLine(p0, p1, Color.red);
    }
    public void drawLine(Vec2f p0, Vec2f p1, Color color) {
        Vec2i pp0 = toScreenPixel(p0);
        Vec2i pp1 = toScreenPixel(p1);
        DebugLine line = new DebugLine(pp0.x, pp0.y, pp1.x, pp1.y, color);
        _graphicsThisFrame.add(line);
    }

    public void drawCell(Vec2i cp0) {
        drawCell(cp0, Color.red);
    }
    public void drawCell(Vec2i cp0, Color color) {
        Vec2f p0 = WorldSpace.cellToFloat(cp0); // Top left
        Vec2f p1 = WorldSpace.cellToFloat(new Vec2i(cp0.x + 1, cp0.y)); // Top right
        Vec2f p2 = WorldSpace.cellToFloat(new Vec2i(cp0.x + 1, cp0.y + 1)); // Bottom right
        Vec2f p3 = WorldSpace.cellToFloat(new Vec2i(cp0.x, cp0.y + 1)); // Bottom left

        drawLine(p0, p1, color);
        drawLine(p1, p2, color);
        drawLine(p2, p3, color);
        drawLine(p3, p0, color);
    }

    public void drawActions(Vec2f startPosition, Vec2f startVelocity, java.util.List<boolean[]> actions, Color color) {
        Vec2f lastPosition = startPosition.clone();
        SimMario mario = new SimMario(startPosition, startVelocity, _worldSpace);
        for(boolean[] a : actions) {
            mario.move(a);
            drawLine(lastPosition, mario.position, color);
            lastPosition = mario.position.clone();
        }
    }

    public Vec2i toScreenPixel(Vec2f floatPos) {
        float x = floatPos.x - _level.xCam;
        float y = floatPos.y - _level.yCam;
        return new Vec2i((int)x, (int)y);
    }

    public boolean inScreen(Vec2i pixelPos) {
        return pixelPos.x > 0 && pixelPos.y > 0
            && pixelPos.x < GlobalOptions.VISUAL_COMPONENT_WIDTH
            && pixelPos.y < GlobalOptions.VISUAL_COMPONENT_HEIGHT;
    }
}
