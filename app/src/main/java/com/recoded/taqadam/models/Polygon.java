package com.recoded.taqadam.models;

import android.graphics.PointF;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by wisam on Jan 18 18.
 */

public class Polygon extends Region {

    @Override
    protected void calculateShapeRect() {
        float left, top, right, bottom;

        left = Float.MAX_VALUE;
        top = Float.MAX_VALUE;
        right = Float.MIN_VALUE;
        bottom = Float.MIN_VALUE;
        for (PointF p : points) {
            //Top-Left should hold the lowest x and the highest y
            left = Math.min(p.x, left);
            top = Math.min(p.y, top);

            //Bottom-Right should hold the lowest y and the highest x
            right = Math.max(p.x, right);
            bottom = Math.max(p.y, bottom);
        }

        shapeRect.set(left, top, right, bottom);
    }

    @Override
    public void close() {
        points.add(new PointF(getPoint(0).x, getPoint(0).y));
        super.close();
    }

    //This is to filter the touch noise, needs to be moved to the custom view
    private boolean validPoint(PointF p) {
        if (points.size() == 0) return true;
        PointF last = points.get(points.size() - 1);
        float dx = p.x - last.x;
        float dy = p.y - last.y;

        return ((float) Math.hypot(dx, dy) >= 150);//pointRadius * 2);

    }

    /**
     * isLeft(): tests if a point is Left|On|Right of an infinite line.
     *
     * @param p0 A ref point on the infinite line
     * @param p1 A second ref point on the infinite line
     * @param pT The tested point
     * @return >0 for pT left of the line through p0 and p1
     * =0 for pT  on the line
     * <0 for pT  right of the line
     */
    private float isLeft(PointF p0, PointF p1, PointF pT) {
        return ((p1.x - p0.x) * (pT.y - p0.y)
                - (pT.x - p0.x) * (p1.y - p0.y));
    }

    public Polygon() {
        super(Shape.POLYGON);
    }

    @Override
    public void addPoint(PointF p) {
        if (closed) return;
        //if (validPoint(p)) {
        points.add(p);
        //}
    }

    @Override
    public JSONObject getShapeAttributes() {
        JSONObject ret = new JSONObject();
        try {
            //we need to do some calculation to set the points to the image viewport
            JSONArray
                    x = new JSONArray(),
                    y = new JSONArray();

            for (PointF p : points) {
                x.put((int) p.x);
                y.put((int) p.y);
            }

            ret.put("name", "polygon");
            ret.put("all_points_x", x);
            ret.put("all_points_y", y);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ret;
    }

    //Polygon containment is a hard geometric problem.
    //We are going to use the winding number algorithm to check for containment.
    //For this to work, the polygon needs to be closed, i.e. points[last] == points[0]
    //Ref: http://geomalgorithms.com/a03-_inclusion.html
    @Override
    public boolean contains(PointF p) {
        //To ease the computations we first check if the point is inside the shape rect
        if (shapeRect.contains(p.x, p.y)) {
            int wn = 0; //winding number. A point is inside a polygon only if wn != 0. Otherwise it's inside.

            for (int i = 0; i < points.size() - 1; i++) { //edge from points[i] to points[i+1]
                float isLeftValue = isLeft(points.get(i), points.get(i + 1), p);

                if (points.get(i).y <= p.y) {
                    if (points.get(i + 1).y > p.y && isLeftValue > 0f) {
                        wn++;
                    }
                } else {
                    if (points.get(i + 1).y <= p.y && isLeftValue < 0f) {
                        wn--;
                    }
                }
            }

            return !(wn == 0);
        }

        return false;
    }

    //https://stackoverflow.com/questions/9815699/how-to-calculate-centroid?noredirect=1&lq=1

    public PointF getCentroid() {
        /*
        PointF centroid = new PointF(0, 0);
        float signedArea = 0.0f; //signed area
        float x0 = 0.0f; // Current vertex X
        float y0 = 0.0f; // Current vertex Y
        float x1 = 0.0f; // Next vertex X
        float y1 = 0.0f; // Next vertex Y
        float a = 0.0f;  // Partial signed area

        // For all vertices except last
        int i = 0;
        for (i = 0; i < points.size() - 1; i++) {
            x0 = points.get(i).x;
            y0 = points.get(i).y;
            x1 = points.get(i + 1).x;
            y1 = points.get(i + 1).y;
            a = x0 * y1 - x1 * y0;
            signedArea += a;
            centroid.x += (x0 + x1) * a;
            centroid.y += (y0 + y1) * a;
        }

        // Do last vertex
        x0 = points.get(i).x;
        y0 = points.get(i).y;
        x1 = points.get(0).x;
        y1 = points.get(0).y;
        a = x0 * y1 - x1 * y0;
        signedArea += a;
        centroid.x += (x0 + x1) * a;
        centroid.y += (y0 + y1) * a;

        signedArea *= 0.5;
        centroid.x /= (6 * signedArea);
        centroid.y /= (6 * signedArea);

        return centroid;
        */
        return new PointF(shapeRect.centerX(), shapeRect.centerY());
    }

    public static Region makeRegionFromJson(JSONObject shape) {
        Region r = newRegion(Shape.POLYGON);
        try {
            JSONArray allX = shape.getJSONArray("all_points_x");
            JSONArray allY = shape.getJSONArray("all_points_y");

            if (allX.length() != allY.length()) return null; //broken array

            //add all points save the last. It will be added on close
            for (int i = 0; i < shape.getJSONArray("all_points_x").length() - 1; i++) {
                r.addPoint(new PointF(allX.getInt(i), allY.getInt(i)));
            }

            r.close();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return r;
    }
}
