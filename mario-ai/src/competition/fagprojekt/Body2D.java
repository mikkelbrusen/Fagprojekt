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
}
