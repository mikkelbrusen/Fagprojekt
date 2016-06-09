package competition.fagprojekt.unittests;

import competition.fagprojekt.PathNode;
import competition.fagprojekt.Vec2f;
import competition.fagprojekt.Vec2i;
import junit.framework.TestCase;
import org.testng.annotations.Test;

/**
 * Created by Jeppe on 08-06-2016.
 */
public class PathNodeTest extends TestCase{

    @Test
    public void testPathNode() {
        Vec2i vec = new Vec2i(20, 10);

        PathNode pn = new PathNode(vec);

        assertEquals(vec, pn.position);
        assertEquals(null, pn.parent);
        assertEquals(0f, pn.marioVelocity.magnitude());
        assertEquals(0, pn.actions.actions.size());
        assertEquals(0, pn.fitness.getFitness());

        PathNode pn2 = new PathNode(vec, pn, 17, 42, new Vec2f(3f, 4f));

        assertEquals(vec, pn2.position);
        assertEquals(pn, pn2.parent);
        assertEquals(5f, pn2.marioVelocity.magnitude());
        assertEquals(0, pn2.actions.actions.size());
        assertEquals(17 + 42, pn2.fitness.getFitness());

        assertTrue(pn2.compareTo(pn) > 0);

    }

}
