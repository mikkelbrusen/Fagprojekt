package competition.fagprojekt;

public class Vec2i {
    public int x;
    public int y;

    public Vec2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vec2i clone() {
        return new Vec2i(x, y);
    }

    // Make this member methods instead? Might be confusing,
    // as they should return new vectors / have no side effects
    public static Vec2i add(Vec2i a, Vec2i b) {
        return new Vec2i(a.x + b.x, a.y + b.y);
    }
    public static Vec2i subtract(Vec2i a, Vec2i b) {
        return new Vec2i(a.x - b.x, a.y - b.y);
    }

    public int sqrMagnitude() {
        return x * x + y * y;
    }
    public float magnitude() {
        return (float)Math.sqrt(sqrMagnitude());
    }

    public Vec2f toVec2f() {
        return WorldSpace.cellToFloat(this);
    }

    @Override
    public boolean equals(Object other) {
        if(!(other instanceof Vec2i))
            return false;

        Vec2i rhs = (Vec2i)other;
        return x == rhs.x && y == rhs.y;
    }

    @Override
    public String toString() {
        return String.format("(%d, %d)", x, y);
    }

}
