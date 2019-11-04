package com.recoded.taqadam.models;

import android.graphics.PointF;
import android.graphics.RectF;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by wisam on Jan 18 18.
 */

public class Point extends Region {

    private final PointF center = new PointF();
    //private float radiusX, radiusY;

    public Point() {
        super(Shape.POINT);
        //radiusX = 0f;
        //radiusY = 0f;
    }

    @Override
    public JSONObject getShapeAttributes() {
        JSONObject ret = new JSONObject();
        try {
            //we need to do some calculation to set the points to the image viewport
            int cx, cy;
            /*if (imageRect != null) {
                cx = (int) (center.x - imageRect.left);
                cy = (int) (center.y - imageRect.top);
            } else {
                cx = (int) (center.x);
                cy = (int) (center.y);
            }*/

            cx = (int) (center.x);
            cy = (int) (center.y);

            ret.put("name", shape.getShapeName());
            ret.put("x", cx);
            ret.put("y", cy);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public boolean contains(PointF p) {
        return false; //The point will not have an area
    }

    @Override
    public void calculateShapeRect() {
        shapeRect.set(
                points.get(0).x,
                points.get(0).y,
                points.get(0).x,
                points.get(0).y
        );

        center.set(points.get(0));
    }

    @Override
    public int getPointUnder(PointF p, float pointRadius) {
        final RectF centerRect = new RectF();

        centerRect.set(
                center.x - pointRadius * 2,
                center.y - pointRadius * 2,
                center.x + pointRadius * 2,
                center.y + pointRadius * 2
        );

        if (centerRect.contains(p.x, p.y)) return 0;

        return -1;
    }

    @Override
    public int getPointIn(RectF rect) {
        if (rect.contains(center.x, center.y))
            return 0;

        return -1;
    }

    public PointF getCoords() {
        return center;
    }

    public static Region makeRegionFromJson(JSONObject shape) {
        Region r = newRegion(Shape.POINT);
        int x,y;
        try {
            x = shape.getInt("x");
            y = shape.getInt("y");
            r.addPoint(new PointF(x,y));
            r.close();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return r;
    }
}
