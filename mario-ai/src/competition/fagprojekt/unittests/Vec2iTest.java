package competition.fagprojekt.unittests;

import competition.fagprojekt.Vec2i;
import junit.framework.TestCase;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Created by Jeppe on 08-06-2016.
 */
public class Vec2iTest extends TestCase{

    @Test
    public void testVec2i() {
        Vec2i vec = new Vec2i(2,3);

        assertEquals(2, vec.x);
        assertEquals(3, vec.y);

        assertTrue(Vec2i.add(vec, new Vec2i(3,2)).equals(new Vec2i(5,5)));
        assertTrue(Vec2i.subtract(vec, new Vec2i(3,2)).equals(new Vec2i(-1,1)));

        assertEquals((float)Math.sqrt(2*2 + 3*3), vec.magnitude());

        assertTrue(vec.equals(new Vec2i(2,3)));
        assertFalse(vec.equals(new Vec2i(5,5)));

        assertEquals("(2, 3)", vec.toString());
    }
}
