package competition.fagprojekt.unittests;

import com.sun.corba.se.impl.orb.ParserTable;
import competition.fagprojekt.Body1D;
import competition.fagprojekt.Body2D;
import competition.fagprojekt.Vec2f;
import junit.framework.TestCase;
import org.testng.annotations.Test;

/**
 * Created by Jeppe on 10-06-2016.
 */
public class BodyTest extends TestCase {

    @Test
    public void testBody1D() {
        Body1D b1 = new Body1D(2f, 3f);

        assertEquals(b1.position, 2f);
        assertEquals(b1.velocity, 3f);

        Body1D b2 = new Body1D(2f, 3f);
        assertTrue(b1.equals(b2));
    }

    @Test
    public void testBody2D() {
        Vec2f vec1 = new Vec2f(2f, 3f);
        Vec2f vec2 = new Vec2f(4f, 5f);

        Body2D b1 = new Body2D(vec1, vec2);

        assertEquals(vec1, b1.position);
        assertEquals(vec2, b1.velocity);

        Body2D b2 = new Body2D(vec1, vec2);
        assertTrue(b1.equals(b2));
    }
}
