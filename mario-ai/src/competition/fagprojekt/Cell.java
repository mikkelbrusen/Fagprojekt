package competition.fagprojekt;

enum CellType { Empty, Solid, Walkable, Enemy };

public class Cell
{
    public CellType type = CellType.Empty;

    public Cell(CellType type) {
        this.type = type;
    }
}
