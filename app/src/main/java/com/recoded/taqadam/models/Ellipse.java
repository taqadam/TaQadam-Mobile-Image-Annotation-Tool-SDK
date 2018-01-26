package com.recoded.taqadam.models;

import android.graphics.PointF;
import android.graphics.RectF;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by wisam on Jan 18 18.
 */

public class Ellipse extends Region {

    private final PointF center = new PointF();
    private float radiusX, radiusY;

    public Ellipse() {
        super(Shape.ELLIPSE);
        radiusX = 0f;
        radiusY = 0f;
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

            ret.put("name", "ellipse");
            ret.put("cx", cx);
            ret.put("cy", cy);
            ret.put("rx", (int) radiusX);
            ret.put("ry", (int) radiusY);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public boolean contains(PointF p) {
        float dx = center.x - p.x; //cx - px
        float dy = center.y - p.y; //cy - py

        return ((dx * dx) / (radiusX * radiusX)) + ((dy * dy) / (radiusY * radiusY)) < 1;
    }

    @Override
    public void calculateShapeRect() {
        shapeRect.set(
                Math.min(points.get(0).x, points.get(1).x),
                Math.min(points.get(0).y, points.get(1).y),
                Math.max(points.get(0).x, points.get(1).x),
                Math.max(points.get(0).y, points.get(1).y)
        );
        radiusX = shapeRect.width() / 2;
        radiusY = shapeRect.height() / 2;
        center.set(shapeRect.left + radiusX, shapeRect.top + radiusY);
    }

    @Override
    public int getPointUnder(PointF p, float pointRadius) {
        //we should create an imaginary point around the handles
        int index = -1;
        final RectF pointR = new RectF();
        sortPoints();

        //Top point
        pointR.set(
                (shapeRect.left + radiusX) - pointRadius * 2,
                shapeRect.top - pointRadius * 2,
                (shapeRect.left + radiusX) + pointRadius * 2,
                shapeRect.top + pointRadius * 2
        );

        if (pointR.contains(p.x, p.y)) {
            index = 0;
        } else {
            //right point
            pointR.set(
                    (shapeRect.left + radiusX * 2) - pointRadius * 2,
                    (shapeRect.top + radiusY) - pointRadius * 2,
                    (shapeRect.left + radiusX * 2) + pointRadius * 2,
                    (shapeRect.top + radiusY) + pointRadius * 2
            );
            if (pointR.contains(p.x, p.y)) {
                index = 1;
            }
        }

        return index;
    }

    @Override
    public void offsetPoint(int index, float dx, float dy) {
        if (index == 0) {

            getPoint(index).y += dy;

        } else {
            getPoint(index).x += dx;

        }

        calculateShapeRect();
    }

    private void sortPoints() {
        if (getPoint(0).x > getPoint(1).x) {
            float temp = getPoint(0).x;
            getPoint(0).x = getPoint(1).x;
            getPoint(1).x = temp;
        }
        if (getPoint(0).y > getPoint(1).y) {
            float temp = getPoint(0).y;
            getPoint(0).y = getPoint(1).y;
            getPoint(1).y = temp;
        }
    }


    public float getRadiusX() {
        return radiusX;
    }

    public float getRadiusY() {
        return radiusY;
    }

    public PointF getCenter() {
        return center;
    }

    public static Region makeRegionFromJson(JSONObject shape) {
        Region r = newRegion(Shape.ELLIPSE);
        int cx, cy, rx, ry;
        PointF topLeft = new PointF();
        PointF bottomRight = new PointF();
        try {
            cx = shape.getInt("cx");
            cy = shape.getInt("cy");
            rx = shape.getInt("rx");
            ry = shape.getInt("ry");

            topLeft.set(cx - rx, cy - ry);
            bottomRight.set(cx + rx, cy + ry);
            r.addPoint(topLeft);
            r.addPoint(bottomRight);
            r.close();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return r;
    }
}
