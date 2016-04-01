package competition.fagprojekt.Debug;

import java.awt.*;

/**
 * Created by max on 01/04/16.
 */
public class DebugLine extends DebugGfx
{
    public int x0;
    public int y0;
    public int x1;
    public int y1;
    public Color color;

    public DebugLine(int x0, int y0, int x1, int y1, Color color) {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        this.color = color;
    }

    @Override
    public void render(Graphics g) {
        Color oldColor = g.getColor();
        g.setColor(color);
        g.drawLine(x0, y0, x1, y1);
        g.setColor(oldColor);
    }
}
