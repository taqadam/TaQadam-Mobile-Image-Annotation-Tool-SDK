package com.recoded.taqadam.models;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by wisam on Jan 18 18.
 */

public abstract class Region implements Parcelable {

    protected final RectF shapeRect = new RectF(); //will be used for drawing
    protected Shape shape;
    protected List<PointF> points;
    protected HashMap<String, String> regionAttributes;
    protected boolean closed;
    private final Matrix mTransformMatrix = new Matrix();
    //private float scale = 1f;
    //protected RectF imageRect;

    //protected float pointRadius;

    protected Region(Shape shape) {
        this.shape = shape;
        points = new ArrayList<>();
        regionAttributes = new HashMap<>();
        closed = false;
    }

    public static Region copyRegion(Region in) {
        Region out = Region.newRegion(in.shape);
        for (PointF p : in.getPoints()) {
            out.addPoint(new PointF(p.x, p.y));
        }
        if (in.closed) {
            out.close();
        }
        out.setRegionAttributes(in.regionAttributes);
        return out;
    }

    protected boolean rectContains(PointF p) {
        return shapeRect.contains(p.x, p.y);
    }

    protected abstract void calculateShapeRect();

    public static Region newRegion(Shape shape) {
        switch (shape) {
            case POLYGON:
                return new Polygon();

            case ELLIPSE:
                return new Ellipse();
            case CIRCLE:
                return new Circle();
            case RECTANGLE:
                return new Rectangle();
            default:
                return new Polygon();
        }
    }

    public static Region fromJSONObject(JSONObject json) {
        Region r;
        try {
            JSONObject shape = json.getJSONObject("shape_attributes");
            if (shape.getString("name").equals("rect")) {
                r = Rectangle.makeRegionFromJson(shape);
            } else if (shape.getString("name").equals("ellipse")) {
                r = Ellipse.makeRegionFromJson(shape);
            } else if (shape.getString("name").equals("polygon")) {
                r = Polygon.makeRegionFromJson(shape);
            } else if (shape.getString("name").equals("circle")) {
                r = Circle.makeRegionFromJson(shape);
            } else r = null;

            if (r != null) {
                JSONObject attribute = json.getJSONObject("region_attributes");
                for (Iterator<String> iter = attribute.keys(); iter.hasNext(); ) {
                    String attr = iter.next();
                    String val = attribute.getString(attr);
                    r.addRegionAttribute(attr, val);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
            r = null;
        }

        return r;
    }

    public void addPoint(PointF p) {
        if (closed) return;
        if (points.isEmpty()) {
            points.add(p);
            return;
        } else if (points.size() == 1) {
            points.add(p);
            calculateShapeRect();
            return;
        }
        points.set(1, p);
        calculateShapeRect();
    }

    public void close() {
        closed = true;
        calculateShapeRect();
    }

    /*public void setPointRadius(float pointRadius) {
        this.pointRadius = pointRadius;
    }*/

    public boolean isClosed() {
        return closed;
    }

    public void addRegionAttribute(String attrib, String value) {
        regionAttributes.put(attrib, value);
    }

    public void setRegionAttributes(HashMap<String, String> attr) {
        regionAttributes.clear();
        regionAttributes.putAll(attr);
    }

    public JSONObject toJSONObject() {
        JSONObject shapeAndRegionAttr;
        try {
            shapeAndRegionAttr = new JSONObject();
            shapeAndRegionAttr.put("shape_attributes", getShapeAttributes());
            shapeAndRegionAttr.put("region_attributes", new JSONObject(getRegionAttributes()));
        } catch (JSONException e) {
            e.printStackTrace();
            shapeAndRegionAttr = null;
        }
        return shapeAndRegionAttr;
    }

    public Map<String, String> getRegionAttributes() {
        return regionAttributes;
    }

    public int getPointUnder(PointF p, float pointRadius) {
        int index = -1;
        final RectF pointRect = new RectF();
        for (int i = 0; i < points.size(); i++) {
            if (i == 0 && shape == Shape.CIRCLE) continue;
            pointRect.set(
                    points.get(i).x - pointRadius * 2,
                    points.get(i).y - pointRadius * 2,
                    points.get(i).x + pointRadius * 2,
                    points.get(i).y + pointRadius * 2
            );

            if (pointRect.contains(p.x, p.y)) {
                index = i;
                break;
            }
        }

        return index;
    }

    public RectF getShapeRect() {
        return shapeRect;
    }

    public void offsetPoint(int index, float dx, float dy) {
        points.get(index).offset(dx, dy);
        calculateShapeRect();
    }

    public void offsetShape(float dx, float dy) {
        for (PointF p : points) {
            p.x += dx;
            p.y += dy;
        }
        calculateShapeRect();
    }

    public List<PointF> getPoints() {
        return points;
    }

    public float[] getPointsArray() {
        float[] ret = new float[points.size() * 2];
        for (int i = 0; i < points.size(); i++) {
            ret[i * 2] = points.get(i).x;
            ret[i * 2 + 1] = points.get(i).y;
        }

        return ret;
    }

    public PointF getPoint(int i) {
        return points.get(i);
    }

    public Shape getShape() {
        return shape;
    }

    public abstract JSONObject getShapeAttributes();

    public abstract boolean contains(PointF p);


    public void transform(float mScale) {
        //scale = mScale;
        mTransformMatrix.reset();
        mTransformMatrix.postScale(mScale, mScale);
        float[] pts = getPointsArray();
        mTransformMatrix.mapPoints(pts);
        points.clear();
        for (int i = 0; i < pts.length; i += 2) {
            points.add(new PointF(pts[i], pts[i + 1]));
        }
        close();
    }

    /*
    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getScale() {
        return scale;
    }
    */

    /*public void setImageRect(RectF imageRect) {
        this.imageRect = imageRect;
    }*/

    public enum Shape {
        RECTANGLE("rect"),
        CIRCLE("circle"),
        ELLIPSE("ellipse"),
        POLYGON("polygon");

        private String shapeName;

        Shape(String name) {
            shapeName = name;
        }

        public String getShapeName() {
            return shapeName;
        }
    }


    /*
    protected boolean exceedsBounds(PointF p) {
        //first determine to which edge the center is nearest to then calculate max_r
        //we should determine the quadrant of the center
        PointF c = points.get(0);
        float max_rx, max_ry, max_r;
        if (c.x < boundsRect.width() / 2 + boundsRect.left) {
            max_rx = c.x + boundsRect.left;
        } else {
            max_rx = boundsRect.right - c.x;
        }
        if (c.y < boundsRect.height() / 2 + boundsRect.top) {
            max_ry = c.y - boundsRect.top;
        } else {
            max_ry = boundsRect.bottom - c.y;
        }

        max_r = Math.min(max_rx, max_ry);
        float r = (float) Math.hypot(p.x - points.get(0).x, p.y - points.get(0).y);

        //snap p to max_r
        return r > max_r;
    }
    */

    @Override
    public int describeContents() {
        return this.hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.shape == null ? -1 : this.shape.ordinal());
        dest.writeTypedList(this.points);
        dest.writeByte(this.closed ? (byte) 1 : (byte) 0);
        dest.writeSerializable(this.regionAttributes);
        //dest.writeFloat(this.scale);
    }

    public static Region createFromParcel(Parcel in) {
        Region r = Region.newRegion(Shape.values()[in.readInt()]); //1 shape
        List<PointF> points = in.createTypedArrayList(PointF.CREATOR); //2 points
        for (PointF p : points) {
            r.addPoint(p);
        }
        if (in.readByte() != 0) { //3 close
            r.close();
        }

        r.setRegionAttributes((HashMap<String, String>) in.readSerializable()); //4 attributes
        //r.setScale(in.readFloat()); //5 scale
        //if (r.getScale() != 1f) {
        //    r.transform(r.getScale());
        //}
        return r;

    }

    public static final Parcelable.Creator<Region> CREATOR = new Parcelable.Creator<Region>() {
        @Override
        public Region createFromParcel(Parcel source) {
            return Region.createFromParcel(source);
        }

        @Override
        public Region[] newArray(int size) {
            return new Region[size];
        }
    };
}
