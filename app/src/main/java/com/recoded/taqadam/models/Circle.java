package com.recoded.taqadam.models;

import android.graphics.PointF;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by wisam on Jan 18 18.
 */

public class Circle extends Region {

    public Circle() {
        super(Shape.CIRCLE);
    }

    @Override
    public JSONObject getShapeAttributes() {
        JSONObject ret = new JSONObject();
        try {
            //we need to do some calculation to set the points to the image viewport
            int x, y;
            /*if (imageRect != null) {
                x = (int) (getPoint(0).x - imageRect.left);
                y = (int) (getPoint(0).y - imageRect.top);
            } else {
                x = (int) (getPoint(0).x);
                y = (int) (getPoint(0).y);
            }*/
            x = (int) (getPoint(0).x);
            y = (int) (getPoint(0).y);

            ret.put("name", "circle");
            ret.put("cx", x);
            ret.put("cy", y);
            ret.put("r", (int) shapeRect.width() / 2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public boolean contains(PointF p) {
        float dx = p.x - points.get(0).x;
        float dy = p.y - points.get(0).y;

        return (dx * dx + dy * dy) < Math.pow(getRadius(), 2);
    }

    public PointF getCenter() {
        return points.size() > 0 ? points.get(0) : null;
    }

    @Override
    public void calculateShapeRect() {
        float r = getRadius();
        shapeRect.set(
                points.get(0).x - r,
                points.get(0).y - r,
                points.get(0).x + r,
                points.get(0).y + r
        );
    }

    public float getRadius() {
        if (points.size() < 2) return 0f;
        return (float) Math.hypot(
                points.get(1).x - points.get(0).x,
                points.get(1).y - points.get(0).y);
    }

    public float calculateRadius(PointF p) {
        if (points.size() == 0) return 0f;
        return (float) Math.hypot(
                p.x = points.get(0).x,
                p.y - points.get(0).y);
    }

    public static Region makeRegionFromJson(JSONObject shape) {
        Region r = newRegion(Shape.CIRCLE);
        int cx, cy, rad;
        PointF center = new PointF();
        PointF radius = new PointF();
        try {
            cx = shape.getInt("cx");
            cy = shape.getInt("cy");
            rad = shape.getInt("r");

            center.set(cx, cy);
            radius.set(cx + rad, cy);
            r.addPoint(center);
            r.addPoint(radius);
            r.close();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return r;
    }
}
