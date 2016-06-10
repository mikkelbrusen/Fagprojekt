package competition.fagprojekt;

public class Body1D {
    public float position;
    public float velocity;

    public Body1D(float position, float velocity) {
        this.position = position;
        this.velocity = velocity;
    }

    public boolean equals(Object other) {
        if (!(other instanceof Body1D)) {
            return false;
        }
        Body1D rhs = (Body1D) other;
        return this.position == rhs.position && this.velocity == rhs.velocity;
    }
}
