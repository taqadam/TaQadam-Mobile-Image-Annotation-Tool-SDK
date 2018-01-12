package com.recoded.taqadam;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.ImageButton;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

public class TaskActivity extends AppCompatActivity {
    private static final String TAG = TaskActivity.class.getSimpleName();
    private final static int TOOLBOX_HIDE_DURATION = 600; //Animate in ms
    private final static int TOOLBOX_SHOW_DURATION = 200; //Animate in ms
    private final static int TOOLBOX_HIDE_TIMEOUT = 2000; //Hide after ms
    private final static int TOOLBOX_HIDE_Y = 100; //Offset out of screen
    private static final int SWIPE_THRESHOLD_VELOCITY = 1500;

    private ImageViewTouch mTaskImage;
    private ViewGroup mToolbox;
    private ImageButton mRectButton, mCircleButton, mEllipseButton, mPolygonButton;

    private View.OnClickListener mToolsClickListener;
    private Runnable mToolboxHider;
    private Runnable mToolboxShower;
    private ViewPropertyAnimator mToolboxAnimator;
    private boolean mAnimationCancelled;

    private int mSelectedId = R.id.button_polygon; //Selected tool id
    private boolean mToolboxVisible = true;
    private GestureDetectorCompat mDetector;
    private int mScreenHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        //get display height to dispatch gestures within a selected height
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mScreenHeight = dm.heightPixels;

        initToolbox();
        mDetector = new GestureDetectorCompat(this, new GestureListener());

        mTaskImage = findViewById(R.id.task_image);
        mTaskImage.setDisplayType(ImageViewTouchBase.DisplayType.FIT_IF_BIGGER);
        mTaskImage.setImageResource(R.drawable.cubes);

        //This library has an issue that it intercepts all touch events so I changed it's touch listener to dispatch other gestures as well
        mTaskImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!mDetector.onTouchEvent(event))
                    v.onTouchEvent(event); //if the toolbox swipe was unsuccessful then dispatch the image touch events
                return true;
            }
        });
        toggleToolbox(); //hide or show the toolbox
    }

    private void initToolbox() {
        mToolbox = findViewById(R.id.toolbox);
        mRectButton = mToolbox.findViewById(R.id.button_rect);
        mCircleButton = mToolbox.findViewById(R.id.button_circle);
        mPolygonButton = mToolbox.findViewById(R.id.button_polygon);
        mEllipseButton = mToolbox.findViewById(R.id.button_ellipse);
        mToolboxAnimator = mToolbox.animate();

        mToolboxHider = new Runnable() {
            @Override
            public void run() {
                mToolboxAnimator.setDuration(TOOLBOX_HIDE_DURATION)
                        .setStartDelay(TOOLBOX_HIDE_TIMEOUT)
                        .alpha(0)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                if (!mAnimationCancelled) {
                                    mToolbox.setVisibility(View.GONE);
                                    mToolboxVisible = false;
                                } else {
                                    mToolbox.setAlpha(1);
                                    mAnimationCancelled = false;
                                }
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                                mAnimationCancelled = true;
                            }
                        });
            }
        };

        mToolboxShower = new Runnable() {
            @Override
            public void run() {
                if (!mToolboxVisible) {
                    mToolbox.setTranslationY(TOOLBOX_HIDE_Y);
                    mToolbox.setVisibility(View.VISIBLE);
                    mToolboxAnimator.setDuration(TOOLBOX_SHOW_DURATION)
                            .setStartDelay(0)
                            .alpha(1)
                            .translationYBy(-TOOLBOX_HIDE_Y)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    mToolboxVisible = true;
                                    toggleToolbox();
                                }
                            });
                }

            }
        };

        mToolsClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mToolboxAnimator != null) {
                    mToolboxAnimator.cancel();
                    toggleToolbox();
                }
                View selected = findViewById(mSelectedId);
                selected.setAlpha(1);
                mSelectedId = v.getId();
                v.setAlpha(0.8f);
            }
        };

        mRectButton.setOnClickListener(mToolsClickListener);
        mCircleButton.setOnClickListener(mToolsClickListener);
        mPolygonButton.setOnClickListener(mToolsClickListener);
        mEllipseButton.setOnClickListener(mToolsClickListener);

        findViewById(mSelectedId).performClick();
    }

    private void toggleToolbox() {
        if (mToolboxVisible) {
            mToolboxHider.run();
        } else {
            mToolboxShower.run();
        }
    }

    class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (mToolboxVisible) return false; //Toolbox is already visible
            if (e1.getY() < (mScreenHeight * 2 / 3))
                return false; //Swipe didn't start at the bottom of screen
            if (e1.getY() < e2.getY()) return false; //Swipe was downward
            if (Math.abs(velocityX) > Math.abs(velocityY)) return false; //Swipe in the x axis
            if (Math.abs(velocityY) < SWIPE_THRESHOLD_VELOCITY)
                return false; //Swipe velocity is low (Not for toolbox)
            toggleToolbox();
            return true;
        }
    }
}
