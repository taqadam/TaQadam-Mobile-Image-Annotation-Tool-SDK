package com.recoded.taqadam;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.recoded.taqadam.models.Polygon;
import com.recoded.taqadam.models.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by wisam on Jan 18 18.
 */

public class SegmentationView extends View {

    //region: Fields and Consts

    public static final int POINT_RADIUS = 8; //The radius for point handle in dp
    private static final float CROSS_RECT_RATIO = 6 / 6;
    private final RectF mCrossRect = new RectF();
    private final PointF mDownTouch = new PointF();
    private List<Region> drawnRegions;
    private boolean isDrawing = false; //Holds the drawing state
    private boolean isDrawingEnabled = true;
    private boolean isDragging = false;
    private OnDrawingFinished drawingListener;
    private Region mCurrentDrawingShape; //Currently drawing shape
    private Region.Shape mSelectedTool;
    private int mSelectedRegion = -1; //for region manipulation
    private int mSelectedPoint = -1; //point manipulation
    private boolean hideDrawn = false; //to hide other regions while drawing
    private RectF mBoundingRectangle; //To snap the drawing to this
    private RectF mImageRect;
    private Paint mLinePaint;
    private float mScale = 1f;

    private Paint mPointPaint;

    private Paint mCrossPaint;

    private int mColorNormal, mColorSelected, mColorNotLabeled;

    /**
     * The radius of the touch zone (in pixels) around a given Handle.
     */
    private float mPointRadius;
    private OnRegionSelected selectionListener;
    private OnRegionDelete removingListener;


    //endregion

    //region: Public Methods

    public SegmentationView(Context context) {
        this(context, null);
    }

    public SegmentationView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mPointRadius = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                POINT_RADIUS,
                context.getResources().getDisplayMetrics());

        mColorNormal = context.getResources().getColor(R.color.colorSegmentation);
        mColorSelected = context.getResources().getColor(R.color.colorSegmentationSelected);
        mColorNotLabeled = Color.RED;

        mPointPaint = getNewPaint(mPointRadius / 4, mColorNormal);

        mPointPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mLinePaint = getNewPaint(mPointRadius / 5, mColorNormal);

        mCrossPaint = getNewPaint(mPointRadius / 2, Color.RED);

        drawnRegions = new ArrayList<>();
    }

    public void setBoundingRect(RectF rect) {
        mBoundingRectangle = new RectF(rect);
    }

    public void setImageRect(RectF rect) {
        mImageRect = new RectF(rect);
    }

    public RectF getImageRect() {
        return mImageRect;
    }

    public RectF getBoundingRect() {
        return mBoundingRectangle;
    }

    public void setTool(Region.Shape tool) {
        this.mSelectedTool = tool;
    }

    public void deleteRegion(int regionId) {
        drawnRegions.remove(regionId);
        if (regionId == mSelectedRegion) {
            deselectRegion();
        } else {
            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // If this View is not enabled, don't allow for touch interactions.
        if (isEnabled()) {
            switch (event.getActionMasked()) {
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
        drawnRegions.clear();
        drawnRegions.addAll(regions);
        invalidate();
    }

    public void transformRegions(RectF newRect) {
        mScale = newRect.width() / mBoundingRectangle.width();
        mBoundingRectangle.set(newRect);
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

    public void setOnRegionDeleteListener(OnRegionDelete listener) {
        this.removingListener = listener;
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

    private void drawRegions(Canvas c) {
        Region r;
        boolean selected;
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
                r = hideDrawn ? null : drawnRegions.get(i);
                selected = i == mSelectedRegion;
            }

            if (r == null) return;

            if (!r.getRegionAttributes().containsKey("label")) {
                mLinePaint.setColor(mColorNotLabeled);
            } else {
                mLinePaint.setColor(mColorNormal);
            }

            if (selected) {
                mLinePaint.setColor(mColorSelected);
            }

            switch (r.getShape()) {
                case POLYGON:
                    drawPolygon((Polygon) r, c, selected);
                    break;
            }
        }
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

    private void drawCross(PointF center, Canvas c) {
        float displaceX = 0f, displaceY = 0f;
        if (mBoundingRectangle != null) {
            displaceX = mBoundingRectangle.left;
            displaceY = mBoundingRectangle.top;
        }
        center.x += displaceX;
        center.y += displaceY;

        mCrossRect.set(
                center.x - mPointRadius * CROSS_RECT_RATIO,
                center.y - mPointRadius * CROSS_RECT_RATIO,
                center.x + mPointRadius * CROSS_RECT_RATIO,
                center.y + mPointRadius * CROSS_RECT_RATIO
        );

        c.drawLine(mCrossRect.left, mCrossRect.top, mCrossRect.right, mCrossRect.bottom, mCrossPaint);
        c.drawLine(mCrossRect.right, mCrossRect.top, mCrossRect.left, mCrossRect.bottom, mCrossPaint);
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
            if (r.isClosed()) drawCross(r.getCentroid(), c);
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

                int i = mCurrentDrawingShape.getPointUnder(snapped, mPointRadius);
                //For closing the polygon
                if (i == 0) {
                    if (mCurrentDrawingShape.getPoints().size() >= 3) { // we don't want to close a polygon on itself
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
        } else if (isDrawingEnabled && mSelectedTool != null) { //user is starting to draw
            hideDrawn = true;
            deselectRegion();
            isDrawing = true;
            mCurrentDrawingShape = Region.newRegion(mSelectedTool);
            //mCurrentDrawingShape.setPointRadius(mPointRadius);
            //mCurrentDrawingShape.setImageRect(mBoundingRectangle);

            if (mCurrentDrawingShape.getShape() == Region.Shape.POLYGON) {
                PointF snapped = getSnappedToBoundsPoint(mDownTouch, true);
                mCurrentDrawingShape.addPoint(snapped);
                invalidate();
            }
            return true;
        } else if (mSelectedRegion != -1) { //if the touch is on selected area
            //we need to check if the touch is on the cross, on a point or on enclosed area
            PointF snapped = getSnappedToBoundsPoint(mDownTouch, false);
            if (mCrossRect.contains(mDownTouch.x, mDownTouch.y)) {
                if (removingListener != null) removingListener.onRegionDelete(mSelectedRegion);
                else deleteRegion(mSelectedRegion);
                return true;
            } else if ((mSelectedPoint = drawnRegions.get(mSelectedRegion)
                    .getPointUnder(snapped, mPointRadius)) != -1) {
                isDragging = true;
                return true;
            } else if (!drawnRegions.get(mSelectedRegion).contains(snapped)) { //no need to snap because already snapped
                deselectRegion();
                return onActionDown(x, y);
            } else {
                isDragging = true;
                return true;
            }
        } else if ((mSelectedRegion = getRegionUnderTouch(mDownTouch)) != -1) { //if the touch is on a drawn region
            invalidate();
            if (selectionListener != null) selectionListener.onRegionSelected(mSelectedRegion);
            isDragging = true;
            return true;
        } else {
            return false;
        }
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
                drawnRegions.get(mSelectedRegion).offsetPoint(mSelectedPoint, dx, dy);

                //account for polygon
                if (drawnRegions.get(mSelectedRegion).getShape() == Region.Shape.POLYGON
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
                if (mBoundingRectangle != null) {
                    RectF shapeRect = drawnRegions.get(mSelectedRegion).getShapeRect();
                    if (mBoundingRectangle.left >= shapeRect.left + dx + mBoundingRectangle.left
                            || mBoundingRectangle.right <= shapeRect.right + dx + mBoundingRectangle.left) {
                        dx = 0;
                    }
                    if (mBoundingRectangle.top >= shapeRect.top + dy + mBoundingRectangle.top
                            || mBoundingRectangle.bottom <= shapeRect.bottom + dy + mBoundingRectangle.top) {
                        dy = 0;
                    }
                }
                drawnRegions.get(mSelectedRegion).offsetShape(dx, dy);
                invalidate();
                return true;
            }
        } else if (isDrawing) {
            PointF snapped = getSnappedToBoundsPoint(new PointF(x, y), true);
            if (mCurrentDrawingShape.getShape() == Region.Shape.POLYGON) {
                int i = mCurrentDrawingShape.getPointUnder(snapped, mPointRadius);
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

    private void deselectRegion() {
        if (mSelectedRegion != -1) {
            mSelectedRegion = -1;
            mSelectedPoint = -1;
            if (selectionListener != null) selectionListener.onRegionSelected(mSelectedRegion);
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

    private int getRegionUnderTouch(PointF downTouch) {
        PointF p = new PointF(downTouch.x, downTouch.y);
        if (mBoundingRectangle != null) {
            p.x = p.x - mBoundingRectangle.left;
            p.y = p.y - mBoundingRectangle.top;
        }
        for (int i = 0; i < drawnRegions.size(); i++) {
            if (drawnRegions.get(i).contains(p)) {
                return i;
            }
        }
        return -1;
    }

    private void endDrawing() {
        mCurrentDrawingShape.close();
        mSelectedRegion = drawnRegions.size();
        drawnRegions.add(mCurrentDrawingShape);
        mCurrentDrawingShape = null;
        mSelectedTool = null;

        isDrawing = false;
        hideDrawn = false;
        if (drawingListener != null)
            drawingListener.onDrawingFinished(
                    drawnRegions.get(drawnRegions.size() - 1),
                    mSelectedRegion);

        if (selectionListener != null)
            selectionListener.onRegionSelected(mSelectedRegion);
    }

    //endregion

    //region: Interfaces

    public interface OnDrawingFinished {
        void onDrawingFinished(Region drawnRegion, int index);
    }

    public interface OnRegionSelected {
        void onRegionSelected(int regionId);
    }

    public interface OnRegionDelete {
        void onRegionDelete(int regionId);
    }

    //endregion
}
