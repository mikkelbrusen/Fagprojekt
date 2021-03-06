package competition.fagprojekt.unittests;

import competition.fagprojekt.ActionUnit;
import junit.framework.TestCase;
import org.testng.annotations.Test;

/**
 * Created by Jeppe on 08-06-2016.
 */
public class ActionUnitTest extends TestCase {

    @Test
    public void testActionUnit() {
        ActionUnit au = new ActionUnit();
        boolean[] bs = {true, true, true, true};

        assertEquals(0, au.actions.size());
        au.add(bs);
        au.add(bs);
        assertEquals(2, au.actions.size());
        assertEquals(bs, au.actions.get(0));
    }

}
