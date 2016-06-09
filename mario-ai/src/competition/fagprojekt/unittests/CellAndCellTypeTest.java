package competition.fagprojekt.unittests;

import competition.fagprojekt.Cell;
import competition.fagprojekt.CellType;
import junit.framework.TestCase;
import org.testng.annotations.Test;

/**
 * Created by Jeppe on 08-06-2016.
 */
public class CellAndCellTypeTest extends TestCase {

    @Test
    public void testCell() {
        Cell cell = new Cell(CellType.Empty);

        assertEquals(CellType.Empty, cell.type);
    }
}
