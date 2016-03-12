package competition.fagprojekt;

public class Vec2f {
    public float x;
    public float y;

    public Vec2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vec2f clone() {
        return new Vec2f(x, y);
    }

    // Make this member methods instead? Might be confusing,
    // as they should return new vectors / have no side effects
    public static Vec2f add(Vec2f a, Vec2f b) {
        return new Vec2f(a.x + b.x, a.y + b.y);
    }
    public static Vec2f subtract(Vec2f a, Vec2f b) {
        return new Vec2f(a.x - b.x, a.y - b.y);
    }

    @Override
    public boolean equals(Object other) {
        if(!(other instanceof Vec2f))
            return false;

        Vec2f rhs = (Vec2f)other;
        return x == rhs.x && y == rhs.y;
    }

    @Override
    public String toString() {
        return String.format("(%d, %d)", x, y);
    }

}
