package com.recoded.taqadam.models;

import android.graphics.PointF;
import android.graphics.RectF;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by wisam on Jan 18 18.
 */

public class Rectangle extends Region {

    public final static int LEFT=3,TOP=4,RIGHT=5,BOTTOM=6;

    public Rectangle() {
        super(Shape.RECTANGLE);
    }

    @Override
    public JSONObject getShapeAttributes() {
        JSONObject ret = new JSONObject();
        try {
            //we need to do some calculation to set the points to the image viewport
            int x, y;
            x = (int) (shapeRect.left);
            y = (int) (shapeRect.top);
            ret.put("name", "rect");
            ret.put("x", x);
            ret.put("y", y);
            ret.put("width", (int) shapeRect.width());
            ret.put("height", (int) shapeRect.height());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public boolean contains(PointF p) {
        return rectContains(p);
    }

    /*@Override //if you activate, make sure to add offset point override
    public int getPointIn(RectF rect) {
        float left = Math.min(points.get(0).x, points.get(1).x);
        float top = Math.min(points.get(0).y, points.get(1).y);
        float right = Math.max(points.get(0).x, points.get(1).x);
        float bottom = Math.max(points.get(0).y, points.get(1).y);
        float hw = shapeRect.width()/2;
        float hh = shapeRect.height()/2;
        if(rect.contains(left, top + hh)) return LEFT;
        if(rect.contains(left+hw, top)) return TOP;
        if(rect.contains(right, top+hh)) return RIGHT;
        if(rect.contains(left+hw, bottom)) return BOTTOM;

        return -1;
    }*/

    @Override
    public void calculateShapeRect() {
        shapeRect.set(
                Math.min(points.get(0).x, points.get(1).x),
                Math.min(points.get(0).y, points.get(1).y),
                Math.max(points.get(0).x, points.get(1).x),
                Math.max(points.get(0).y, points.get(1).y)
        );
    }

    public static Region makeRegionFromJson(JSONObject shape) {
        Region r = newRegion(Shape.RECTANGLE);
        int x, y, w, h;
        PointF topLeft = new PointF();
        PointF bottomRight = new PointF();
        try {
            x = shape.getInt("x");
            y = shape.getInt("y");
            w = shape.getInt("width");
            h = shape.getInt("height");

            topLeft.set(x, y);
            bottomRight.set(x + w, y + h);
            r.addPoint(topLeft);
            r.addPoint(bottomRight);
            r.close();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return r;
    }
}
