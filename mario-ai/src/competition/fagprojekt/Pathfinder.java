package competition.fagprojekt;

import java.util.*;

public class Pathfinder {
    WorldSpace worldSpace;

    public Pathfinder(WorldSpace worldSpace) {
        this.worldSpace = worldSpace;
    }

    public List<Vec2i> searchBfs(Vec2i start, Vec2i end) {
        Queue<PathNode> toBeSearched = new LinkedList<>();
        List<Vec2i> hasSearched = new LinkedList<>(); // TODO: Should be hash table for best complexity, but we need to override hashCode() then
        PathNode current = new PathNode(start.clone(), null);

        boolean hasFoundEnd = false;
        hasSearched.add(current.position.clone());
        toBeSearched.add(current);
        while (!toBeSearched.isEmpty()) {
            current = toBeSearched.poll();
            hasSearched.add(current.position.clone());

            if (current.position.equals(end)) {
                hasFoundEnd = true;
                break;
            }

            for (Vec2i n : getNeighbours(current.position)) {
                if (!hasSearched.contains(n))
                    toBeSearched.add(new PathNode(n, current));
            }
        }

        if (!hasFoundEnd)
            return null;

        List<Vec2i> path = new ArrayList<>();
        while (current.parent != null) {
            path.add(current.position);
            current = current.parent;
        }
        Collections.reverse(path);
        return path;
    }

    List<Vec2i> getNeighbours(Vec2i p) {
        return getNeighbours(p.x, p.y);
    }

    List<Vec2i> getNeighbours(int x, int y) {
        List<Vec2i> neighbours = new ArrayList<Vec2i>();

        if (isWalkable(x - 1, y))
            neighbours.add(new Vec2i(x - 1, y));

        if (isWalkable(x + 1, y))
            neighbours.add(new Vec2i(x + 1, y));

        if (isEmpty(x, y + 1)) // Falling
            neighbours.add(new Vec2i(x, y + 1));

        // TODO: Add jumps

        return neighbours;
    }

    boolean isEmpty(int x, int y) {
        return isType(x, y, CellType.Empty);
    }
    boolean isWalkable(int x, int y) {
        return isType(x, y, CellType.Walkable);
    }

    boolean isType(int x, int y, CellType type) {
        return isType(worldSpace.getCell(x, y), type);
    }
    boolean isType(Cell cell, CellType type) {
        return cell != null && cell.type == type;
    }
}
