package competition.fagprojekt;

/**
 * Created by max on 28/04/16.
 */
public class Body2D {
    public Vec2f position;
    public Vec2f velocity;

    public Body2D(Vec2f position, Vec2f velocity) {
        this.position = position.clone();
        this.velocity = velocity.clone();
    }

    public boolean equals(Object other) {
        if (!(other instanceof Body2D)) {
            return false;
        }
        Body2D rhs = (Body2D) other;
        return this.position.equals(rhs.position) && this.velocity.equals(rhs.velocity);
    }
}
