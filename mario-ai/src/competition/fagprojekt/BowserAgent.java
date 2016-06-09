package competition.fagprojekt;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.benchmark.mario.environments.MarioEnvironment;
import competition.fagprojekt.Debug.Debug;
import competition.fagprojekt.Debug.DebugInput;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class BowserAgent extends BasicMarioAIAgent implements Agent
{
    WorldSpace worldSpace;
    JumpPathfinder jumpPathfinder;
    JumpTable jumpTable;
    Pathfinder pathfinder;
    MarioMove marioMove;

    Vec2i targetPos;
    ActionUnit currentUnit = new ActionUnit();

    public BowserAgent()
    {
        super("BowserAgent");
        reset();
    }

    public boolean[] getAction()
    {
        Debug debug = Debug.getInstance();

       // worldSpace.printWorldSpace();

        if (currentUnit.actions.isEmpty()) {
            System.out.println(debug.frameCount + ": Recalculating path");

            for(Vec2i targetCell : worldSpace.rightMostWalkables) {
                List<ActionUnit> path = pathfinder.searchAStar(marioMove.lastCell, marioMove.velocity, targetCell);
                if (path != null && !path.isEmpty()) {
                    targetPos = targetCell;
                    currentUnit = path.get(0).clone();

                    if (currentUnit.endPosition != null) {
                        Vec2i p1 = currentUnit.endPosition.toCell();
                        Vec2i p0 = marioMove.lastCell.clone();

                        JumpPath jumpPath = jumpTable.findPathAbsolute(p1, p0, marioMove.velocity.x, true);
                        if (jumpPath != null) {
                            System.out.printf("Found Path: %s -> %s\n", p0, p1);
                            BUtil.printActionUnit(jumpPath.actionUnit);
                        }
                    }

                    System.out.printf("Velocity: %s\n", marioMove.velocity);
                    System.out.printf("========== %s -> %s ==========\n", marioMove.lastCell, targetCell);
                    for (ActionUnit unit : path) {
                        System.out.printf("Unit to %s:\n", unit.endPosition == null ? "(x, x)" : unit.endPosition.toCell());
                        for (boolean[] a : unit.actions)
                            System.out.printf("  %s\n", BUtil.actionToString(a));
                    }

                    break;
                }
            }
        }

        if (currentUnit.actions.isEmpty())
            currentUnit.actions.add(MarioMove.newAction());

        action = currentUnit.actions.get(0);
        currentUnit.actions.remove(0);

        /*
        int w = 20;
        int h = 20;
        for(int i = 0; i < w; i++) {
            for(int j = 0; j < h; j++) {
                int x = marioMove.lastCell.x - (w / 2) + j;
                int y = marioMove.lastCell.y - (h / 2) + i;
                Vec2i p1 = new Vec2i(x, y);

                Color color = marioMove.canJumpToCell(p0, v0, p1)
                        ? Color.blue : Color.black;

                debug.drawCell(p1, color);
            }
        }
        */

        debug.update();

        debug.drawCell(marioMove.lastCell);
        for(Vec2i rightMost : worldSpace.rightMostWalkables)
            debug.drawCell(rightMost, Color.gray);

        if(targetPos != null)
            debug.drawCell(targetPos, Color.green);

        // Debug pathfinding
        if(DebugInput.keysPressed[DebugInput.KEY_K]) {
            Vec2i c0 = marioMove.lastCell;
            Vec2i c1 = debug.debugCell;
            System.out.println("DEBUG: " + c0 + " -> " + c1);

            List<ActionUnit> path = pathfinder.searchAStar(c0, marioMove.velocity, c1);

            float y0f = c0.y * WorldSpace.CellHeight;
            float y1f = c1.y * WorldSpace.CellHeight;
            int jumpFrames = MarioMove.minimumJumpFramesToEndAtHeight(y0f, y1f);
            System.out.println("JumpFrames: " + jumpFrames);

            if(path != null) {
                for(int i = 0; i < path.size(); i++) {
                    ActionUnit aFrame = path.get(i);
                    for(int j = 0; j < aFrame.actions.size(); j++) {
                        System.out.println(i + ": " + BUtil.actionToString(aFrame.actions.get(j)));
                    }
                }
            }
        }

        /*
        if(currentActions != null) {
            debug.drawActions(marioMove.lastFloatPos, marioMove.velocity, currentActions, Color.magenta);
        }
        */

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
        jumpPathfinder = new JumpPathfinder(worldSpace,marioMove);
        jumpTable = new JumpTable(jumpPathfinder);
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

