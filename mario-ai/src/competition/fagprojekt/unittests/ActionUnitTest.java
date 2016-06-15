package competition.fagprojekt.unittests;

import competition.fagprojekt.ActionUnit;
import competition.fagprojekt.Vec2f;
import junit.framework.TestCase;
import org.testng.annotations.Test;

/**
 * Created by Jeppe on 08-06-2016.
 */
public class ActionUnitTest extends TestCase {

    @Test
    public void testActionUnit() {
        ActionUnit au = new ActionUnit(new Vec2f(0, 0), new Vec2f(0, 0));
        boolean[] bs = {true, true, true, true};

        assertEquals(0, au.getActions().size());
        au.add(bs);
        au.add(bs);
        assertEquals(2, au.getActions().size());
        assertEquals(bs, au.getActions().get(0));
    }

}
