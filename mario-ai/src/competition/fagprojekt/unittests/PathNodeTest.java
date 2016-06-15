package competition.fagprojekt.unittests;

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


        /*
        PathNode pn = new PathNode(vec);

        assertEquals(vec, pn.position);
        assertEquals(null, pn.parent);
        assertEquals(0f, pn.actions.getEndVelocity().magnitude());
        assertEquals(0, pn.actions.getActionUnit().size());
        assertEquals(0, pn.fitness.getFitness());

        PathNode pn2 = new PathNode(vec, pn, 17, 42, new Vec2f(3f, 4f), new Vec2f(2f, 2f));
        Body2D body = new Body2D(new Vec2f(3f, 4f), new Vec2f(2f, 2f));

        assertEquals(vec, pn2.position);
        assertEquals(pn, pn2.parent);
        //assertTrue(pn2.endBody.equals(body));
        assertEquals(0, pn2.actions.getActionUnit().size());
        assertEquals(17 + 42, pn2.fitness.getFitness());

        assertTrue(pn2.compareTo(pn) > 0);
        */


    }

}
