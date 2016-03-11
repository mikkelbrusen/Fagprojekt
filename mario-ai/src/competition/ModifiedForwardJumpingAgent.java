/*
 * Copyright (c) 2009-2010, Sergey Karakovskiy and Julian Togelius
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Mario AI nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package ch.idsia.agents.controllers;

import ch.idsia.agents.Agent;
import ch.idsia.benchmark.mario.engine.GlobalOptions;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.benchmark.mario.environments.MarioEnvironment;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy
 * Date: Apr 25, 2009
 * Time: 12:27:07 AM
 * Package: ch.idsia.agents.controllers;
 */

public class ModifiedForwardJumpingAgent extends BasicMarioAIAgent implements Agent
{
    enum JumpState { Falling, Jumping, OnGround};
    JumpState jumpState = JumpState.OnGround;

    final int maxJumpFrames = 10;
    int jumpFrames = 0;

public ModifiedForwardJumpingAgent()
{
    super("ForwardJumpingAgent");
    reset();
}

public boolean[] getAction()
{
    MarioEnvironment env;
    for(int i = 0; i < 100; i++)
    {
        env = MarioEnvironment.getInstance();
    }
    boolean wallInFront = false;
    boolean holeInFront = false;

    GlobalOptions.isShowReceptiveField = true;

    int x = marioEgoRow;
    int y = marioEgoCol;
    for(int i = 1; i < 5; i++)
    {
        int v = getReceptiveFieldCellValue(y - 1, x + i);
        if(v != 0)
        {
            wallInFront = false;
        }

        System.out.println(i + ": " + v);
    }

    boolean willJump = wallInFront;
    if(jumpState == JumpState.OnGround && willJump)
    {
        jumpState = JumpState.Jumping;
        jumpFrames = 0;
    }
    if(jumpState == JumpState.Jumping)
    {
        jumpFrames++;

        if(jumpFrames >= maxJumpFrames)
            jumpState = JumpState.Falling;
    }
    if(jumpState == JumpState.Falling && isMarioOnGround)
    {
        jumpState = JumpState.OnGround;
    }

    action[Mario.KEY_SPEED] = isMarioAbleToJump || !isMarioOnGround;
    action[Mario.KEY_JUMP] = jumpState == JumpState.Jumping;
    return action;
}

public void reset()
{
    action = new boolean[Environment.numberOfKeys];
    action[Mario.KEY_RIGHT] = true;
    action[Mario.KEY_SPEED] = true;
}
}
