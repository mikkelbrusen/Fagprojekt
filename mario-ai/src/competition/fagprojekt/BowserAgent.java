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
import java.util.List;
import java.util.Random;

public class BowserAgent extends BasicMarioAIAgent implements Agent
{
    WorldSpace worldSpace;
    JumpPathfinder jumpPathfinder;
    JumpTable jumpTable;
    Pathfinder pathfinder;
    MarioMove marioMove;

    Vec2i targetPos;
    ActionUnit currentUnit = new ActionUnit();

    Body2D lastBody = new Body2D(new Vec2f(0, 0), new Vec2f(0, 0));
    List<ActionUnit> lastPath = new ArrayList<>();

    Vec2f floatPosLastFrame = new Vec2f(0, 0);
    int standstillFrames = 0;

    public BowserAgent()
    {
        super("BowserAgent");
        reset();
    }

    boolean doOnce = true;
    public boolean[] getAction()
    {
        Debug debug = Debug.getInstance();

        if (doOnce) {
            doOnce = false;
            for (int y = jumpTable.yMin; y < jumpTable.yMax; y++) {
                String line = "";
                for (int x = jumpTable.xMin; x < jumpTable.xMax; x++) {
                    String c = " .";
                    JumpPath jp = jumpTable.findPathRelative(x, y, 0, false);
                    if (jp != null)
                        c = String.format("%2d", jp.actionUnit.actions.size());
                    if (x == 0 && y == 0)
                        c = " x";
                    line += c + " ";
                }
                System.out.println(line);
            }
        }

        //System.exit(0);

       // worldSpace.printWorldSpace();

        if (currentUnit.actions.isEmpty()) {
            //System.out.println(debug.frameCount + ": Recalculating path");

            for(Vec2i targetCell : worldSpace.rightMostWalkables) {
                List<ActionUnit> path = pathfinder.searchAStar(marioMove.lastFloatPos, marioMove.velocity, targetCell);
                if (path != null && !path.isEmpty()) {
                    targetPos = targetCell.clone();
                    currentUnit = path.get(0).clone();

                    lastBody = new Body2D(marioMove.lastFloatPos, marioMove.velocity);
                    lastPath = path;

                    /*
                    if (currentUnit.endPosition != null) {
                        Vec2i p1 = currentUnit.endPosition.toCell();
                        Vec2i p0 = marioMove.lastCell.clone();

                        JumpPath jumpPath = jumpTable.findPathAbsolute(p1, p0, marioMove.velocity.x, true);
                        if (jumpPath != null) {
                            System.out.printf("Found Path: %s -> %s\n", p0, p1);
                            BUtil.printActionUnit(jumpPath.actionUnit);
                        }
                    }
                    */

                    System.out.printf("Velocity: %s\n", marioMove.velocity);
                    System.out.printf("========== %s -> %s ==========\n", marioMove.lastFloatPos.toCell(), targetCell);
                    for (ActionUnit unit : path) {
                        Vec2i dp = unit.endPosition == null ? new Vec2i(0, 0) : Vec2i.add(marioMove.lastCell, unit.endPosition.toCell());
                        System.out.printf("Unit to %s:\n", unit.endPosition == null ? "(x, x)" : dp);
                        for (boolean[] a : unit.actions)
                            System.out.printf("  %s\n", BUtil.actionToString(a));
                    }

                    break;
                }
                else
                    pathfinder.lastPathCells.clear();
            }
        }

        if (currentUnit.actions.isEmpty())
            currentUnit.actions.add(MarioMove.newAction());

        action = currentUnit.actions.get(0);
        currentUnit.actions.remove(0);

        debug.update();

        debug.drawCell(marioMove.lastCell);
        for(Vec2i rightMost : worldSpace.rightMostWalkables)
            debug.drawCell(rightMost, Color.gray);

        if(targetPos != null)
            debug.drawCell(targetPos, Color.green);

        // Draw path via SimMario
        SimMario debugMario = new SimMario(lastBody.position, lastBody.velocity, worldSpace);
        Vec2f lastP = lastBody.position.clone();
        for (ActionUnit unit : lastPath) {
            for (boolean[] action : unit.actions) {
                debugMario.move(action);

                Color color = unit == lastPath.get(0) ? Color.blue : Color.magenta;
                debug.drawLine(lastP, debugMario.body.position, color);
                lastP = debugMario.body.position.clone();
            }
        }

        if (currentUnit.actions.isEmpty() && DebugInput.keysPressed[DebugInput.KEY_K]) {
            Vec2i targetCell = debug.debugCell;
            List<ActionUnit> path = pathfinder.searchAStar(marioMove.lastFloatPos, marioMove.velocity, targetCell);
            if (path != null && !path.isEmpty()) {
                for (ActionUnit unit : path)
                    currentUnit.actions.addAll(unit.actions);
            }
        }

        for (Vec2i c : pathfinder.lastPathCells)
            debug.drawCell(c, Color.ORANGE);

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
                currentUnit.actions.add(MarioMove.moveAction(dir, true));
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
        jumpPathfinder = new JumpPathfinder(worldSpace, marioMove);
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
}

