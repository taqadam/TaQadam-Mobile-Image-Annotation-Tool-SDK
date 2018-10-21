package com.recoded.taqadam.models;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;

import com.recoded.taqadam.R;
import com.recoded.taqadam.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by wisam on Jan 18 18.
 */

public abstract class Region implements Parcelable {
    public static final String ID_KEY = "ID";

    final RectF shapeRect = new RectF(); //will be used for drawing

    protected Shape shape;
    List<PointF> points;
    private boolean isLocked = false;
    private HashMap<String, String> regionAttributes;
    boolean closed;
    private final Matrix mTransformMatrix = new Matrix();
    private String label;
    private String linkId;
    private String id = Utils.getRandomString(5);

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
        out.setLocked(in.isLocked);
        out.setLabel(in.label);
        out.setLinkedBy(in.linkId);
        out.setId(in.id);
        return out;
    }

    public void setLinkedBy(String linkedBy) {
        this.linkId = linkedBy;
    }

    public String linkedBy() {
        return linkId;
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
            case POINT:
                return new Point();
            case ARC:
            case LINE:
            case SPLINE:
            default:
                return new Polygon();
        }
    }

    public static Region fromJSONObject(JSONObject json) {
        Region r;
        try {
            JSONObject shape = json.getJSONObject("shape_attributes");
            switch (shape.getString("name")) {
                case "rect":
                    r = Rectangle.makeRegionFromJson(shape);
                    break;
                case "ellipse":
                    r = Ellipse.makeRegionFromJson(shape);
                    break;
                case "polygon":
                    r = Polygon.makeRegionFromJson(shape);
                    break;
                case "circle":
                    r = Circle.makeRegionFromJson(shape);
                    break;
                case "point":
                    r = Point.makeRegionFromJson(shape);
                    break;
                default:
                    r = null;
                    break;
            }

            if (r != null) {
                JSONObject attribute = json.getJSONObject("region_attributes");
                for (Iterator<String> iter = attribute.keys(); iter.hasNext(); ) {
                    String attr = iter.next();
                    String val = attribute.getString(attr);
                    if (attr.equalsIgnoreCase("label")) {
                        r.setLabel(val);
                    } else if (attr.equalsIgnoreCase(ID_KEY)) {
                        r.setId(val);
                    } else if (attr.equalsIgnoreCase("linked_by")) {
                        r.setLinkedBy(val);
                    } else {
                        r.addRegionAttribute(attr, val);
                    }
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
            JSONObject regionAttr = new JSONObject(getRegionAttributes());
            if (label != null) {
                regionAttr.put("label", label);
            }

            regionAttr.put("linked_by", linkId);
            regionAttr.put("id", id);

            shapeAndRegionAttr.put("region_attributes", regionAttr);
        } catch (JSONException e) {
            e.printStackTrace();
            shapeAndRegionAttr = null;
        }
        return shapeAndRegionAttr;
    }

    public Map<String, String> getRegionAttributes() {
        return regionAttributes;
    }

    @Deprecated
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

    public int getPointIn(RectF rect) {
        int index = -1;
        final RectF pointRect = new RectF();
        for (int i = 0; i < points.size(); i++) {
            PointF p = points.get(i);
            if (rect.contains(p.x, p.y))
                index = i;
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
        calculateShapeRect();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        if (this.label != null) {
            return label;
        } else {
            return "NO LABEL";
        }
    }

    public String getTitle() {
        return "Region " + getId() + ": " + getLabel();
    }

    public int getIcon() {
        switch (shape) {
            case POINT:
                return R.drawable.ic_point;
            case SPLINE:
                return R.drawable.ic_spline;
            case ARC:
                return R.drawable.ic_arc;
            case LINE:
                return R.drawable.ic_line;
            case CIRCLE:
                return R.drawable.ic_circle;
            case ELLIPSE:
                return R.drawable.ic_ellipse_new;
            case POLYGON:
                return R.drawable.ic_polygon_new;
            case RECTANGLE:
                return R.drawable.ic_rect_new;
            default:
                return R.drawable.ic_polygon_new;
        }
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public boolean isLocked() {
        return isLocked;
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
        POLYGON("polygon"),
        POINT("point"),
        LINE("line"),
        ARC("arc"),
        SPLINE("spline");

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
        dest.writeInt(this.shape == null ? -1 : this.shape.ordinal()); //1
        dest.writeTypedList(this.points); //2
        dest.writeByte(this.closed ? (byte) 1 : (byte) 0); //3
        dest.writeSerializable(this.regionAttributes); //4
        dest.writeByte(this.isLocked ? (byte) 1 : (byte) 0); //5
        dest.writeString(label == null ? "NO_LABEL" : label); //6
        dest.writeString(id == null? "NO_ID":id); //7
        dest.writeString(linkId == null? "NO_LINK":linkId); //8
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

        if(in.readByte() != 0) { //5 locked
            r.isLocked = true;
        } else {
            r.isLocked = false;
        }

        String label = in.readString(); //6 label
        String id = in.readString(); //7 id
        String link = in.readString(); //8 link

        if(!label.equalsIgnoreCase("NO_LABEL")) r.label = label;
        if(!id.equalsIgnoreCase("NO_ID")) r.id = id;
        if(!link.equalsIgnoreCase("NO_LINK")) r.linkId = link;

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
