package competition.fagprojekt.unittests;

import competition.fagprojekt.Vec2f;
import junit.framework.TestCase;
import org.testng.annotations.Test;

/**
 * Created by Jeppe on 08-06-2016.
 */
public class Vec2fTest extends TestCase{


    @Test
    public void testVec2i() {
        Vec2f vec = new Vec2f(2f,3f);

        assertEquals(2f, vec.x);
        assertEquals(3f, vec.y);

        assertTrue(Vec2f.add(vec, new Vec2f(3.5f, 2.5f)).equals(new Vec2f(5.5f, 5.5f)));
        assertTrue(Vec2f.subtract(vec, new Vec2f(3.5f, 2.5f)).equals(new Vec2f(-1.5f, 0.5f)));

        assertEquals((float)Math.sqrt(2*2 + 3*3), vec.magnitude());

        assertTrue(vec.equals(new Vec2f(2,3)));
        assertFalse(vec.equals(new Vec2f(5,5)));


        assertEquals("(2,000000, 3,000000)", vec.toString());
    }

}
