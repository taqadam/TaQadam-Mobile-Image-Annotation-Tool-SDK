package com.recoded.taqadam.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;
import com.recoded.taqadam.R;
import com.recoded.taqadam.models.Circle;
import com.recoded.taqadam.models.Ellipse;
import com.recoded.taqadam.models.Link;
import com.recoded.taqadam.models.Point;
import com.recoded.taqadam.models.Polygon;
import com.recoded.taqadam.models.Rectangle;
import com.recoded.taqadam.models.Region;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wisam on Jan 18 18.
 */

public class DrawingView extends View {

    //region: Fields and Consts

    public static final int POINT_RADIUS = 8; //The radius for point handle in dp
    private static final float MIN_SHAPE_RECT_SIZE = 2; //in dp
    private static final int TEXT_SIZE_PX = 16;

    private final PointF mDownTouch = new PointF();
    private List<Region> drawnRegions;

    private boolean isDrawing = false; //Holds the drawing state
    private boolean mEnabled = true;
    private boolean isDragging = false;
    private boolean isLinking = false;

    private Map<String, Link> links = new HashMap<>();

    private OnDrawingFinished drawingListener;
    private Region mCurrentDrawingShape; //Currently drawing shape
    private Region.Shape mSelectedTool;

    private int mSelectedRegion = -1; //for region manipulation
    private int mSelectedPoint = -1; //point manipulation

    private boolean hideDrawn = false; //to hide other regions while drawing
    private RectF mBoundingRectangle; //To snap the drawing to this

    private Paint mLinePaint;

    private float mScale = 1f;

    private final Matrix mDisplayMatrix = new Matrix();

    private Paint mPointPaint;

    private int mColorNormal, mColorSelected, mColorNotLabeled, mColorLinking;

    private float textSize;

    /**
     * The radius of the touch zone (in pixels) around a given Handle.
     */
    private float mPointRadius;
    private OnRegionSelected selectionListener;

    private GestureDetector mGestureDetector;
    private RectF imageRect;
    private boolean mContinuesDrawing = true;


    //endregion

    //region: Public Methods

    public void setActive(boolean active) {
        mEnabled = active;
    }

    public DrawingView(Context context) {
        this(context, null);
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mPointRadius = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                POINT_RADIUS,
                context.getResources().getDisplayMetrics());

        mColorNormal = context.getResources().getColor(R.color.colorBoundingBox);
        mColorSelected = context.getResources().getColor(R.color.colorBoundingBoxSelected);
        mColorLinking = getResources().getColor(R.color.colorLinking);
        mColorNotLabeled = Color.RED;

        mPointPaint = getNewPaint(mPointRadius, mColorNormal);

        mPointPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mLinePaint = getNewPaint(mPointRadius / 5, mColorNormal);

        drawnRegions = new ArrayList<>();

        textSize = TEXT_SIZE_PX * context.getResources().getDisplayMetrics().scaledDensity;

        createGestureDetector();
    }

    public void setBoundingRect(RectF rect) {
        if (rect.width() == 0 && rect.height() == 0) return;
        transformRegions(rect);
        mBoundingRectangle = new RectF(rect);
    }

    public PointF getLastDownTouch() {
        return mDownTouch;
    }

    public Link[] getLinks() {
        Link[] ret = new Link[links.size()];
        return links.values().toArray(ret);
    }

    public void setLinks(Link[] links) {
        for (Link l : links) {
            this.links.put(l.id, l);
        }
    }

    public void clearAll() {
        drawnRegions.clear();
        links.clear();
        if (mSelectedRegion != -1)
            deselectRegion();
        else
            invalidate();
    }

    public void deleteRegion(int regionId) {
        if (isLinking) return; //or islinking = false;
        Region r = drawnRegions.get(regionId);
        if (r.linkedBy() != null) {
            Link link = links.get(r.linkedBy());
            link.regionIds.remove(r.getId());
            if (link.regionIds.size() == 1) {
                links.remove(link.id);
            }
        }
        drawnRegions.remove(regionId);
        if (regionId == mSelectedRegion) {
            deselectRegion();
        } else {
            invalidate();
        }
    }


    public RectF getBoundingRect() {
        return mBoundingRectangle;
    }

    public void setTool(Region.Shape tool) {
        this.mSelectedTool = tool;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // If this View is not enabled, don't allow for touch interactions.
        if (mEnabled) {
            //if (mGestureDetector.onTouchEvent(event)) return true;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    return onActionDown(event.getX(), event.getY());
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    getParent().requestDisallowInterceptTouchEvent(false);
                    return onActionUp();
                case MotionEvent.ACTION_MOVE:
                    if (onActionMove(event.getX(), event.getY())) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                        return true;
                    }
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    public List<Region> getDrawnRegions() {
        return drawnRegions;
    }

    public List<Region> getNormalizedRegions() {
        float scale = imageRect.width() / mBoundingRectangle.width();
        List<Region> ret = new ArrayList<>();
        for (Region r : drawnRegions) {
            Region n = Region.copyRegion(r);
            n.transform(scale);
            ret.add(n);
        }

        return ret;
    }

    public Region getRegion(int regionId) {
        return drawnRegions.get(regionId);
    }

    public int getRegionsCount() {
        return drawnRegions.size();
    }

    public void addRegion(Region region) {
        drawnRegions.add(region);
        invalidate();
    }

    public void addRegions(Collection<Region> regions) {
        //These are normalized regions so we scale them if bounding rect != null
        drawnRegions.clear();
        if(mBoundingRectangle != null) {
            float scale = mBoundingRectangle.width()/imageRect.width();
            for(Region r : regions) {
                r.transform(scale);
            }
        }

        drawnRegions.addAll(regions);
        invalidate();
    }

    public void transformRegions(RectF newRect) {
        if (mBoundingRectangle == null) {
            mScale = newRect.width() / imageRect.width();

        } else {
            mScale = newRect.width() / mBoundingRectangle.width();
        }

        for (Region r : drawnRegions) {
            r.transform(mScale);
        }
        //for zooming while drawing
        if (mCurrentDrawingShape != null) {
            mCurrentDrawingShape.transform(mScale);
        }
        invalidate();
    }

    public void setOnDrawingFinishedListener(OnDrawingFinished listener) {
        this.drawingListener = listener;
    }

    public void setOnRegionSelectedListener(OnRegionSelected listener) {
        this.selectionListener = listener;
    }

    //endregion

    // region: Private methods

    /**
     * Draw box overview by drawing background over image not in the boxing area, then borders and
     * guidelines.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawRegions(canvas);
    }

    private void createGestureDetector() {
        mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (mSelectedTool != null) {
                    cancelDrawing();
                    mSelectedTool = null;
                    invalidate();
                    return true;
                }
                return false;
            }
        });
    }

    private void drawRegions(Canvas c) {
        Region r;
        boolean selected;
        Paint paintForText = getPaintForText();
        for (int i = -1; i < drawnRegions.size(); i++) {
            if (i == -1) {
                if (mCurrentDrawingShape != null) {
                    if (mCurrentDrawingShape.getPoints().size() == 2
                            || mCurrentDrawingShape.getShape() == Region.Shape.POLYGON) {
                        r = mCurrentDrawingShape;
                        selected = true;
                    } else continue;
                } else continue;
            } else {
                if (isLinking) {
                    r = getRegion(i);
                    Region rSelected = getSelectedRegion();
                    if (rSelected.linkedBy() != null)
                        selected = rSelected.linkedBy().equals(r.linkedBy());
                    else selected = i == mSelectedRegion;
                } else {
                    r = hideDrawn ? null : drawnRegions.get(i);
                    selected = i == mSelectedRegion;
                }
            }

            if (r == null) return;

            if (r.getLabel().equals("NO LABEL")) {
                mLinePaint.setColor(mColorNotLabeled);
            } else {
                mLinePaint.setColor(mColorNormal);
            }

            if (selected) {
                if (isLinking)
                    mLinePaint.setColor(mColorLinking);
                else
                    mLinePaint.setColor(mColorSelected);
            }

            switch (r.getShape()) {
                case RECTANGLE:
                    drawRect((Rectangle) r, c, selected);
                    break;
                case CIRCLE:
                    drawCircle((Circle) r, c, selected);
                    break;
                case ELLIPSE:
                    drawEllipse((Ellipse) r, c, selected);
                    break;
                case POLYGON:
                    drawPolygon((Polygon) r, c, selected);
                    break;
                case POINT:
                    drawPoint((Point) r, c, selected);
                    break;
            }
            writeText(r, c, paintForText);
        }

    }

    private Paint getPaintForText() {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(textSize);
        return paint;
    }


    private void writeText(Region r, Canvas c, Paint paint) {
        String text = "";
        Map<String, String> regionAttributes = r.getRegionAttributes();
        for (String key : regionAttributes.keySet()) {
            if (!key.equals("__object_id")) {
                text = text + regionAttributes.get(key);
            }
        }

        float displaceX = 0f, displaceY = 0f;
        if (mBoundingRectangle != null) {
            displaceX = mBoundingRectangle.left;
            displaceY = mBoundingRectangle.top;
        }
        float left = r.getPoint(0).x + displaceX;
        float top = r.getPoint(0).y + displaceY;
        c.drawText(text, left, top, paint);
    }
    /**
     * Creates the Paint object for given thickness and color.
     */
    private static Paint getNewPaint(float thickness, int color) {
        Paint borderPaint = new Paint();
        borderPaint.setColor(color);
        borderPaint.setStrokeWidth(thickness);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setAntiAlias(true);
        return borderPaint;
    }

    private void drawPolygon(Polygon r, Canvas c, boolean selected) {
        Path path = new Path();
        int i = -1;
        //We need to move our image related points to their place
        float displaceX = 0f, displaceY = 0f;
        if (mBoundingRectangle != null) {
            displaceX = mBoundingRectangle.left;
            displaceY = mBoundingRectangle.top;
        }
        for (PointF p : r.getPoints()) {
            i++;
            if (i == 0) {
                path.moveTo(p.x + displaceX, p.y + displaceY);
                continue;
            }
            if (r.isClosed() && i == r.getPoints().size() - 1) {
                path.close();
                continue;
            }
            path.lineTo(p.x + displaceX, p.y + displaceY);
        }
        c.drawPath(path, mLinePaint);

        if (selected) {
            float[] points = r.getPointsArray();
            for (i = 0; i < points.length; i += 2) {
                points[i] += displaceX;
                points[i + 1] += displaceY;
            }
            c.drawPoints(points, mPointPaint);
        }
    }

    private void drawEllipse(Ellipse r, Canvas c, boolean selected) {
        float displaceX = 0f, displaceY = 0f;
        if (mBoundingRectangle != null) {
            displaceX = mBoundingRectangle.left;
            displaceY = mBoundingRectangle.top;
        }
        RectF drawingRect = new RectF(r.getShapeRect());
        drawingRect.offset(displaceX, displaceY);
        c.drawOval(drawingRect, mLinePaint);

        if (selected) {
            float rx = r.getShapeRect().width() / 2;
            float ry = r.getShapeRect().height() / 2;

            float left = Math.min(r.getPoint(0).x, r.getPoint(1).x);
            float top = Math.min(r.getPoint(0).y, r.getPoint(1).y);

            //top
            c.drawPoint(left + rx + displaceX, top + displaceY, mPointPaint);

            //left
            //c.drawPoint(left, top + ry, mPointPaint);

            //right
            c.drawPoint(left + rx + rx + displaceX, top + ry + displaceY, mPointPaint);

            //bottom
            //c.drawPoint(left + rx, top + ry + ry, mPointPaint);
        }
    }

    private void drawPoint(Point r, Canvas c, boolean selected) {
        float displaceX = 0f, displaceY = 0f;
        if (mBoundingRectangle != null) {
            displaceX = mBoundingRectangle.left;
            displaceY = mBoundingRectangle.top;
        }
        RectF drawingRect = new RectF(
                r.getCoords().x - (mPointRadius / 3),
                r.getCoords().y - (mPointRadius / 3),
                r.getCoords().x + (mPointRadius / 3),
                r.getCoords().y + (mPointRadius / 3)
        );
        drawingRect.offset(displaceX, displaceY);
        mLinePaint.setStyle(Paint.Style.FILL);
        c.drawOval(drawingRect, mLinePaint);
        mLinePaint.setStyle(Paint.Style.STROKE);
    }

    private void drawCircle(Circle r, Canvas c, boolean selected) {
        float displaceX = 0f, displaceY = 0f;
        if (mBoundingRectangle != null) {
            displaceX = mBoundingRectangle.left;
            displaceY = mBoundingRectangle.top;
        }

        PointF center = r.getPoint(0);
        c.drawCircle(center.x + displaceX, center.y + displaceY, r.getRadius(), mLinePaint);

        if (selected) {
            //rad
            c.drawPoint(r.getPoint(1).x + displaceX, r.getPoint(1).y + displaceY, mPointPaint);
        }
    }

    private void drawRect(Rectangle r, Canvas c, boolean selected) {
        float displaceX = 0f, displaceY = 0f;
        if (mBoundingRectangle != null) {
            displaceX = mBoundingRectangle.left;
            displaceY = mBoundingRectangle.top;
        }
        RectF drawingRect = new RectF(r.getShapeRect());
        drawingRect.offset(displaceX, displaceY);

        c.drawRect(drawingRect, mLinePaint);

        if (selected) {
            c.drawPoint(r.getPoint(0).x + displaceX, r.getPoint(0).y + displaceY, mPointPaint);
            c.drawPoint(r.getPoint(1).x + displaceX, r.getPoint(1).y + displaceY, mPointPaint);
        }
    }

    /*
        We first need to check if the user is drawing
        if not then check if there is a selected area and the touch is on it's points
        if not we need to check if a touch is on a drawn area
        if not then start drawing a new shape
        */
    private boolean onActionDown(float x, float y) {
        mDownTouch.set(x, y);
        if (isDrawing) { //if the user is drawing
            if (mCurrentDrawingShape.getShape() == Region.Shape.POLYGON) {
                PointF snapped = getSnappedToBoundsPoint(mDownTouch, true);
                RectF pointRect = getPointRect(snapped);
                int i = mCurrentDrawingShape.getPointIn(pointRect);
                //For closing the polygon
                if (i == 0) {
                    if (mCurrentDrawingShape.getPoints().size() >= 3) {
                        endDrawing();
                    } else return false;
                } else {
                    //check for validity
                    PointF last = mCurrentDrawingShape.getPoint(mCurrentDrawingShape.getPoints().size() - 1);
                    float dx = snapped.x - last.x;
                    float dy = snapped.y - last.y;
                    if ((float) Math.hypot(dx, dy) >= mPointRadius * 2) {
                        mCurrentDrawingShape.addPoint(snapped);
                    }
                }

                invalidate();
            }
            return true;
        } else if (mSelectedRegion != -1 && !isLinking) { //if the touch is on selected area
            //we need to check if is linking
            // or if touch on a point or on enclosed area
            //if region is selected no need to check for dragging
            Region selected = getSelectedRegion();
            PointF snapped = getSnappedToBoundsPoint(mDownTouch, false);
            RectF pointRect = getPointRect(snapped);
            if ((mSelectedPoint = selected.getPointIn(pointRect)) != -1) {
                isDragging = !selected.isLocked();
                return isDragging;
            } else if (!selected.contains(snapped)) { //no need to snap because already snapped
                deselectRegion();
                return onActionDown(x, y);
            } else {
                isDragging = !selected.isLocked();
                return isDragging;
            }
        } else if ((getRegionUnderTouch(mDownTouch)) != -1) { //if the touch is on a drawn region
            int newRegionIndex = getRegionUnderTouch(mDownTouch);
            if (isLinking) {
                if (newRegionIndex == mSelectedRegion) return false;
                linkOrDelinkRegions(mSelectedRegion, newRegionIndex);
                return true;
            } else {
                mSelectedRegion = newRegionIndex;
                invalidate();
                if (selectionListener != null)
                    selectionListener.onRegionSelected(getSelectedRegion(), mSelectedRegion);
                isDragging = !getSelectedRegion().isLocked();
                return true;
            }
        } else if (mSelectedTool != null && !isLinking) { //user is starting to draw
            deselectRegion();
            isDrawing = true;
            mCurrentDrawingShape = Region.newRegion(mSelectedTool);

            if (mCurrentDrawingShape.getShape() == Region.Shape.POLYGON) {
                PointF snapped = getSnappedToBoundsPoint(mDownTouch, true);
                mCurrentDrawingShape.addPoint(snapped);
                hideDrawn = true;
                invalidate();
            } else if (mCurrentDrawingShape.getShape() == Region.Shape.POINT) {
                PointF snapped = getSnappedToBoundsPoint(mDownTouch, true);
                mCurrentDrawingShape.addPoint(snapped);
                endDrawing();
                invalidate();
            }
            return true;
        } else {
            return false;
        }
    }

    private void linkOrDelinkRegions(int mSelectedRegion, int newRegionIndex) {
        Region first = getRegion(mSelectedRegion);
        Region second = getRegion(newRegionIndex);
        if (first.linkedBy() != null && second.linkedBy() != null) {
            //first check if both not nulls
            if (first.linkedBy().equals(second.linkedBy())) {
                //delink
                //we will seperate remove the second from the list and set link to null
                Link link = links.get(first.linkedBy());
                link.regionIds.remove(second.getId());
                second.setLinkedBy(null);
                if (link.regionIds.size() == 1) {
                    first.setLinkedBy(null);
                    links.remove(link.id);
                }
            } else {
                //Each one has different links, lets combine the links
                Link firstLink = links.get(first.linkedBy());
                Link secondLink = links.get(second.linkedBy());
                //Since each link has ids of all linked elements, if we just add all we have unique list of ids
                firstLink.regionIds.addAll(secondLink.regionIds);
                //remove then assign
                links.remove(second.linkedBy());
                second.setLinkedBy(firstLink.id);
            }
        } else if (first.linkedBy() == null && second.linkedBy() == null) {
            //check if both has no links
            Link link = new Link();
            link.regionIds.add(first.getId());
            link.regionIds.add(second.getId());
            first.setLinkedBy(link.id);
            second.setLinkedBy(link.id);
            links.put(link.id, link);
        } else if (second.linkedBy() == null && first.linkedBy() != null) {
            Link link = links.get(first.linkedBy());
            link.regionIds.add(second.getId());
            second.setLinkedBy(link.id);
        } else if (first.linkedBy() == null && second.linkedBy() != null) {
            Link link = links.get(second.linkedBy());
            link.regionIds.add(first.getId());
            first.setLinkedBy(link.id);
        }
        invalidate();
    }

    /**
     * We need to check if there is a selected point we drag it, else if there is selected region we drag it
     * then we need to check if the user drawing we draw it else return false
     */
    private boolean onActionMove(float x, float y) {
        if (isDragging) {
            //we need to calculate the offset dx, dy first
            float dx = x - mDownTouch.x;
            float dy = y - mDownTouch.y;
            mDownTouch.set(x, y);
            //if (getSelectedRegion().isLocked()) return false;
            if (mSelectedPoint != -1) {
                //this is for point offsetting
                if (mBoundingRectangle != null) {
                    PointF selected = drawnRegions.get(mSelectedRegion).getPoint(mSelectedPoint);
                    if (mBoundingRectangle.left >= selected.x + dx + mBoundingRectangle.left
                            || mBoundingRectangle.right <= selected.x + dx + mBoundingRectangle.left) {
                        dx = 0;
                    }
                    if (mBoundingRectangle.top >= selected.y + dy + mBoundingRectangle.top
                            || mBoundingRectangle.bottom <= selected.y + dy + mBoundingRectangle.top) {
                        dy = 0;
                    }
                }

                getSelectedRegion().offsetPoint(mSelectedPoint, dx, dy);

                //account for polygon last point which is the same as first one
                if (getSelectedRegion().getShape() == Region.Shape.POLYGON
                        && mSelectedPoint == 0) {
                    drawnRegions.get(mSelectedRegion).offsetPoint(
                            drawnRegions.get(mSelectedRegion).getPoints().size() - 1,
                            dx,
                            dy);
                }

                invalidate();
                return true;
            } else {
                //this is for shape offsetting
                //when we have linked objects we need to create a bigger rect that encompasses them all
                Region selected = getSelectedRegion();
                List<Region> regions = null;
                if (selected.linkedBy() != null) {
                    Link link = links.get(selected.linkedBy());
                    regions = new ArrayList<>();
                    for (String id : link.regionIds) {
                        regions.add(getRegionById(id));
                    }
                }
                if (mBoundingRectangle != null) {
                    RectF shapeRect;
                    if (selected.linkedBy() == null) {
                        shapeRect = selected.getShapeRect();
                    } else {
                        shapeRect = calculateCollectiveRect(regions);
                    }
                    if (mBoundingRectangle.left >= shapeRect.left + dx + mBoundingRectangle.left
                            || mBoundingRectangle.right <= shapeRect.right + dx + mBoundingRectangle.left) {
                        dx = 0;
                    }
                    if (mBoundingRectangle.top >= shapeRect.top + dy + mBoundingRectangle.top
                            || mBoundingRectangle.bottom <= shapeRect.bottom + dy + mBoundingRectangle.top) {
                        dy = 0;
                    }
                }

                if (regions != null) {
                    for (Region r : regions) {
                        r.offsetShape(dx, dy);
                    }
                } else {
                    selected.offsetShape(dx, dy);
                }
                invalidate();
                return true;
            }
        } else if (isDrawing) {
            if (mCurrentDrawingShape.getShape() == Region.Shape.POINT) return false;
            hideDrawn = true;
            PointF snapped = getSnappedToBoundsPoint(new PointF(x, y), true);
            RectF pointRect = getPointRect(snapped);
            if (mCurrentDrawingShape.getShape() == Region.Shape.POLYGON) {
                int i = mCurrentDrawingShape.getPointIn(pointRect);
                //For closing the polygon
                if (i == 0) {
                    if (mCurrentDrawingShape.getPoints().size() >= 3) { // we don't want to close a polygon on itself
                        endDrawing();
                        invalidate();
                        return true;
                    } else return false;
                } else {
                    //check for validity
                    PointF last = mCurrentDrawingShape.getPoint(mCurrentDrawingShape.getPoints().size() - 1);
                    float dx = snapped.x - last.x;
                    float dy = snapped.y - last.y;
                    if ((float) Math.hypot(dx, dy) >= mPointRadius * 2) {
                        mCurrentDrawingShape.addPoint(snapped);
                    }
                }
            } else {
                mCurrentDrawingShape.addPoint(snapped);
            }
            if (mCurrentDrawingShape.getPoints().size() == 2 || mCurrentDrawingShape.getShape() == Region.Shape.POLYGON)
                invalidate();
            return true;
        } else {
            return false;
        }
    }

    private RectF calculateCollectiveRect(List<Region> regions) {
        RectF ret = new RectF(regions.get(0).getShapeRect());
        for (Region reg : regions) {
            RectF rect = reg.getShapeRect();
            if (rect.left < ret.left) ret.left = rect.left;
            if (rect.top < ret.top) ret.top = rect.top;
            if (rect.right > ret.right) ret.right = rect.right;
            if (rect.bottom > ret.bottom) ret.bottom = rect.bottom;
        }
        return ret;
    }

    /**
     * Ends the drag drawing
     */
    private boolean onActionUp() {
        if (isDrawing && mCurrentDrawingShape.getShape() != Region.Shape.POLYGON) {
            if (mCurrentDrawingShape.getPoints().size() < 2) return false;
            endDrawing();
            invalidate();
            return true;
        } else if (isDragging) {
            isDragging = false;
            mSelectedPoint = -1;
            invalidate();
        }
        return false;
    }

    public Region getRegionById(String id) {
        for (Region r : drawnRegions) {
            if (r.getId().equals(id)) {
                return r;
            }
        }
        return null;
    }

    public void deselectRegion() {
        if (mSelectedRegion != -1) {
            mSelectedRegion = -1;
            mSelectedPoint = -1;
            if (selectionListener != null)
                selectionListener.onRegionSelected(null, mSelectedRegion);
            invalidate();
        }
    }

    private PointF getSnappedToBoundsPoint(PointF po, boolean move) {
        PointF p = new PointF(po.x, po.y);
        if (mBoundingRectangle != null) {
            if (move) {
                if (p.x < mBoundingRectangle.left) {
                    p.x = mBoundingRectangle.left;
                } else if (p.x > mBoundingRectangle.right) {
                    p.x = mBoundingRectangle.right;
                }

                if (p.y < mBoundingRectangle.top) {
                    p.y = mBoundingRectangle.top;
                } else if (p.y > mBoundingRectangle.bottom) {
                    p.y = mBoundingRectangle.bottom;
                }
            }
            //to make the coordinates per image coords
            p.x = p.x - mBoundingRectangle.left;
            p.y = p.y - mBoundingRectangle.top;
        }
        return p;
    }

    private RectF getPointRect(PointF p) {
        return new RectF(p.x - mPointRadius,
                p.y - mPointRadius,
                p.x + mPointRadius,
                p.y + mPointRadius);
    }

    private int getRegionUnderTouch(PointF downTouch) {
        PointF p = new PointF(downTouch.x, downTouch.y);
        if (mBoundingRectangle != null) {
            p.x = p.x - mBoundingRectangle.left;
            p.y = p.y - mBoundingRectangle.top;
        }
        for (int i = 0; i < drawnRegions.size(); i++) {
            if (drawnRegions.get(i).contains(p)) {
                return i;
            } else if (drawnRegions.get(i).getShape() == Region.Shape.POINT) {
                RectF r = getPointRect(drawnRegions.get(i).getPoint(0)); //Point has only 1 point
                if (r.contains(p.x, p.y)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void cancelDrawing() {
        mCurrentDrawingShape = null;
        isDrawing = false;
        hideDrawn = false;
    }

    private void endDrawing() {
        mCurrentDrawingShape.close();
        //this is for validating shape size, we don't want to use hypot since we might want to draw small shapes but while zoomed in
        if ((mCurrentDrawingShape.getShapeRect().width() > MIN_SHAPE_RECT_SIZE * mPointRadius
                && mCurrentDrawingShape.getShapeRect().height() > MIN_SHAPE_RECT_SIZE * mPointRadius)
                || mCurrentDrawingShape.getShape() == Region.Shape.POINT) {

            mSelectedRegion = drawnRegions.size();
            drawnRegions.add(mCurrentDrawingShape);
            if (drawingListener != null)
                drawingListener.onDrawingFinished(
                        drawnRegions.get(drawnRegions.size() - 1),
                        mSelectedRegion);

            if (selectionListener != null)
                selectionListener.onRegionSelected(drawnRegions.get(mSelectedRegion), mSelectedRegion);
        }
        if (!mContinuesDrawing)
            mSelectedTool = null;

        mCurrentDrawingShape = null;

        isDrawing = false;
        hideDrawn = false;

    }

    public Region getSelectedRegion() {
        if (mSelectedRegion != -1) {
            return drawnRegions.get(mSelectedRegion);
        }
        return null;
    }

    public void deleteSelected() {
        if (mSelectedRegion != -1) {
            deleteRegion(mSelectedRegion);
        }
    }

    public void setLinking(boolean isChecked) {
        isLinking = isChecked;
        invalidate();
    }

    public void setImageRect(RectF rectF) {
        this.imageRect = rectF;
    }

    public Region.Shape getTool() {
        return mSelectedTool;
    }

    public void setContinuesDrawing(boolean checked) {
        this.mContinuesDrawing = checked;
    }

    //endregion

    //region: Interfaces

    public interface OnDrawingFinished {
        void onDrawingFinished(Region drawnRegion, int index);
    }

    public interface OnRegionSelected {
        void onRegionSelected(Region r, int regionId);
    }

    public interface OnRegionDelete {
        void onRegionDelete(int regionId);
    }

    //endregion
}
