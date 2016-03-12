package competition.fagprojekt;

public class Vector2i {
    public int x;
    public int y;

    public Vector2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vector2i clone() {
        return new Vector2i(x, y);
    }

    // Make this member methods instead? Might be confusing,
    // as they should return new vectors / have no side effects
    public static Vector2i add(Vector2i a, Vector2i b) {
        return new Vector2i(a.x + b.x, a.y + b.y);
    }
    public static Vector2i subtract(Vector2i a, Vector2i b) {
        return new Vector2i(a.x - b.x, a.y - b.y);
    }

    @Override
    public boolean equals(Object other) {
        if(!(other instanceof Vector2i))
            return false;

        Vector2i rhs = (Vector2i)other;
        return x == rhs.x && y == rhs.y;
    }

    @Override
    public String toString() {
        return String.format("(%d, %d)", x, y);
    }

}
