package competition.fagprojekt;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.benchmark.mario.environments.MarioEnvironment;
import competition.fagprojekt.Debug.Debug;
import competition.fagprojekt.Debug.DebugInput;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class BowserAgent extends BasicMarioAIAgent implements Agent
{
    WorldSpace worldSpace;
    JumpPathfinder jumpPathfinder;
    JumpTable jumpTable;
    Pathfinder pathfinder;
    MarioMove marioMove;

    List<boolean[]> currentActions = new LinkedList<>(); // Constant add/remove at ends

    // Debug
    Vec2i targetPos;
    Body2D lastBody = new Body2D(new Vec2f(0, 0), new Vec2f(0, 0));
    List<ActionUnit> lastPath = new ArrayList<>();

    // Anti-stuck
    Vec2f floatPosLastFrame = new Vec2f(0, 0);
    int standstillFrames = 0;

    public BowserAgent()
    {
        super("BowserAgent");
        reset();
    }

    public boolean[] getAction()
    {
        if (currentActions.isEmpty()) {
            //System.out.println(debug.frameCount + ": Recalculating path");

            for(Vec2i targetCell : worldSpace.getRightMostWalkables()) {
                List<ActionUnit> path = pathfinder.searchAStar(marioMove.lastFloatPos, marioMove.velocity, targetCell);
                if (path != null && !path.isEmpty()) {
                    targetPos = targetCell.clone(); // Debug
                    currentActions.addAll(path.get(0).getActions());

                    // Debug
                    lastBody = new Body2D(marioMove.lastFloatPos, marioMove.velocity);
                    lastPath = path;

                    //printFoundPath(path);

                    break;
                }
                else {
                    // Debug
                    pathfinder.lastPathCells.clear();
                    lastPath.clear();
                }
            }
        }

        // Fallback
        if (currentActions.isEmpty())
            currentActions.add(MarioMove.emptyAction());

        // Consume next action
        action = currentActions.get(0);
        currentActions.remove(0);

        // Anti-stuck
        if (floatPosLastFrame.equals(marioMove.lastFloatPos))
            standstillFrames++;
        else
            standstillFrames = 0;
        floatPosLastFrame = marioMove.lastFloatPos.clone();

        if (standstillFrames > 24) {
            Random rng = new Random((int)marioMove.lastFloatPos.x);
            for (int i = 0; i < 5; i++) {
                int dir = rng.nextInt() % 2 == 0 ? -1 : 1;
                currentActions.add(MarioMove.moveAction(dir, true));
            }
            standstillFrames = 0;
        }

        return action;
    }

    @Override
    public void integrateObservation(Environment environment)
    {
        super.integrateObservation(environment);

        worldSpace.integrateObservation(environment);
        marioMove.integrateObservation(environment);
    }

    public void reset()
    {
        worldSpace = new WorldSpace();
        marioMove = new MarioMove();
        jumpPathfinder = new JumpPathfinder(worldSpace);
        jumpTable = JumpTable.getJumpTable(jumpPathfinder, false);
        pathfinder = new Pathfinder(worldSpace, marioMove,jumpTable);

        Debug.initialize(MarioEnvironment.getInstance().getLevelScene(), worldSpace);
    }

    // Debug
    public void keyPressed(KeyEvent e) {
        DebugInput.keyPressed(e);
    }
    public void keyReleased(KeyEvent e) {
        DebugInput.keyReleased(e);
    }

    void updateDebug() {
        Debug debug = Debug.getInstance();

        debug.update();

        worldSpace.getRightMostWalkables().clear();

        debug.drawCell(marioMove.lastCell);
        for(Vec2i rightMost : worldSpace.getRightMostWalkables())
            debug.drawCell(rightMost, Color.gray);

        if(targetPos != null)
            debug.drawCell(targetPos, Color.green);

        // Draw path via SimMario
        SimMario debugMario = new SimMario(lastBody.position, lastBody.velocity, worldSpace);
        Vec2f lastP = lastBody.position.clone();
        for (ActionUnit unit : lastPath) {
            for (boolean[] action : unit.getActions()) {
                debugMario.move(action);

                Color color = unit == lastPath.get(0) ? Color.blue : Color.magenta;
                debug.drawLine(lastP, debugMario.body.position, color);
                lastP = debugMario.body.position.clone();
            }
        }

        if (currentActions.isEmpty() && DebugInput.keysPressed[DebugInput.KEY_K]) {
            Vec2i targetCell = debug.debugCell;
            List<ActionUnit> path = pathfinder.searchAStar(marioMove.lastFloatPos, marioMove.velocity, targetCell);
            if (path != null && !path.isEmpty()) {
                for (ActionUnit unit : path)
                    currentActions.addAll(unit.getActions());
            }
        }

        for (Vec2i c : pathfinder.lastPathCells)
            debug.drawCell(c, Color.ORANGE);
    }

    void printFoundPath(List<ActionUnit> path) {
        System.out.printf("========== %s -> %s ==========\n", marioMove.lastFloatPos.toCell(), targetPos);
        BUtil.printPath(path);
    }
}

