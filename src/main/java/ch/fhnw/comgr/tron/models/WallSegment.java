package ch.fhnw.comgr.tron.models;

import ch.fhnw.util.math.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Serquet on 15.01.2017.
 */
public class WallSegment {
    // thos can be used to quickly check if each edge should even be checked!
    private float maxX;
    private float minX;
    private float maxY;
    private float minY;
    
    private List<Vec3> edges = new ArrayList<>();

    public WallSegment(Vec3 start, Vec3 end) {
        if (start.x > end.x) {
            maxX = start.x;
            minX = end.x;
        } else {
            maxX = end.x;
            minX = start.x;
        }
        
        if (start.y > end.y) {
            maxY = start.y;
            minY = end.y;
        } else {
            maxY = end.y;
            minY = start.y;
        }

        edges.add(start);
        edges.add(end);
    }
    
    public void addEdge(Vec3 edge)
    {
        edges.add(edge);

        maxX = Math.max(edge.x, maxX);
        minX = Math.min(edge.x, minX);
        maxY = Math.max(edge.y, maxY);
        minY = Math.min(edge.y, minY);
    }

    public boolean checkCollision(Player p) {
        for(Vec3 v : edges) {
        	if(p.pointInPlayer(v.x, v.y)) return true;
        }
        return false;
    }
}
