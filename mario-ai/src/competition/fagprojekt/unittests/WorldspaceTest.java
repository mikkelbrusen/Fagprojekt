package competition.fagprojekt.unittests;

import ch.idsia.benchmark.mario.environments.MarioEnvironment;
import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.benchmark.tasks.ReplayTask;
import ch.idsia.tools.MarioAIOptions;
import competition.fagprojekt.CellType;
import competition.fagprojekt.WorldSpace;
import junit.framework.TestCase;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Created by Mikkel on 07/06/16.
 */
public class WorldspaceTest extends TestCase{

    @BeforeTest
    public void setUp()
    {
    }

    @AfterTest
    public void tearDown()
    {
    }

    @Test
    public void testCreateWorldspace_FR1() throws Exception {
        MarioAIOptions marioAIOptions = new MarioAIOptions("-vis off -rfw 100 -rfh 100");
        MarioEnvironment env = MarioEnvironment.getInstance();

        assertNotNull(env);
        env.reset(marioAIOptions);

        WorldSpace worldspace = new WorldSpace();
        worldspace.integrateObservation(env);
        worldspace.printWorldSpace();

        assertEquals(worldspace.getCell(0,13).type, CellType.Walkable);
    }
}
