package com.recoded.taqadam;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.recoded.taqadam.databinding.FragBoundingBoxBinding;
import com.recoded.taqadam.models.Answer;
import com.recoded.taqadam.models.Region;
import com.recoded.taqadam.models.db.JobDbHandler;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

/**
 * Created by Wisam Naji on 12/26/2017.
 */

public class BoundingBoxFragment extends TaskFragment {
    private static final String TAG = BoundingBoxFragment.class.getSimpleName();

    private final static int TOOLBOX_HIDE_DURATION = 600; //Animate in ms
    private final static int TOOLBOX_SHOW_DURATION = 10; //Animate in ms
    private final static int TOOLBOX_HIDE_TIMEOUT = 2000; //Hide after ms
    //private final static int TOOLBOX_HIDE_Y = 100; //Offset out of screen
    //private static final int SWIPE_THRESHOLD_VELOCITY = 1500;

    private FragBoundingBoxBinding binding;
    private Runnable mToolboxHider;
    private Runnable mToolboxShower;
    private ViewPropertyAnimator mToolboxAnimator;
    private boolean mAnimationCancelled;
    private AttributesFragment.LabelChangeListener listener;
    private int mSelectedId = -1; //Selected tool id
    private boolean mToolboxVisible = true;
    //private GestureDetectorCompat mDetector;
    private int selectedRegion;
    private List<Region> mRegions = new ArrayList<>();

    protected boolean imageFrozen = false;
    private AttributesFragment attributesFragment;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.frag_bounding_box, container, false);
        binding = DataBindingUtil.bind(rootView);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("regions")) {
                mRegions = savedInstanceState.getParcelableArrayList("regions");
            }
            if (mImage == null && savedInstanceState.containsKey("image")) {
                mImage = savedInstanceState.getParcelable("image");
            }
            if (jobId == null && savedInstanceState.containsKey("job_id")) {
                jobId = savedInstanceState.getString("job_id");
            }
        }
        answer = new Answer(jobId, mImage.id);

        initTaskImg();

        initToolbox();

        binding.tvInstruction.setVisibility(View.GONE);

        /*//This library has an issue that it intercepts all touch events so I changed it's touch listener to dispatch other gestures as well
        binding.ivTaskImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!mDetector.onTouchEvent(event))
                    v.onTouchEvent(event); //if the toolbox swipe was unsuccessful then dispatch the image touch events
                return true;
            }
        });*/

        listener = new AttributesFragment.LabelChangeListener() {
            @Override
            public void onLabelSelected(String label) {
                binding.tvInstruction.setText(label);
            }
        };

        toggleToolbox(); //hide or show the toolbox

        return rootView;
    }

    private void initTaskImg() {
        taskImageView = binding.ivTaskImage;
        taskImageView.setDisplayType(ImageViewTouchBase.DisplayType.NONE);
        binding.bboxView.setEnabled(false);
        binding.bboxView.setVisibility(View.GONE);
        if (mImage.path.getScheme().equalsIgnoreCase("http") || mImage.path.getScheme().equalsIgnoreCase("https")) {
            loadTaskImage(mImage.path);
        } else if (mImage.path.getScheme().equalsIgnoreCase("gs")) {
            int indexOfSlash = mImage.path.toString().indexOf('/', 5);
            String bucket = mImage.path.toString().substring(0, indexOfSlash);
            String ref = mImage.path.toString().substring(indexOfSlash + 1);
            FirebaseStorage.getInstance(bucket).getReference(ref).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    loadTaskImage(uri);
                }
            });
        }
    }

    private void loadTaskImage(Uri uri) {
        Picasso.with(getContext()).load(uri).into(taskImageView, new Callback() {
            @Override
            public void onSuccess() {
                binding.imageProgressBar.setVisibility(View.GONE);
                RectF boundingRect = taskImageView.getBitmapRect();
                if (boundingRect.width() == 0 || boundingRect.height() == 0) {
                    Rect localRect = new Rect();
                    taskImageView.getLocalVisibleRect(localRect);
                    boundingRect.set(localRect);
                }
                binding.bboxView.setBoundingRect(boundingRect);
                binding.bboxView.setImageRect(boundingRect);
                if (mRegions.size() != 0) binding.bboxView.addRegions(mRegions);
                binding.bboxView.setEnabled(true);
                binding.bboxView.setVisibility(View.VISIBLE);
                imageLoaded = true;
                taskImageView.setOnMatrixChangeListener(new ImageViewTouchBase.OnMatrixChangeListener() {
                    @Override
                    public void onMatrixChanged(Matrix m) {
                        binding.bboxView.transformRegions(taskImageView.getBitmapRect());
                    }
                });
                taskImageView.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
            }

            @Override
            public void onError() {
                Log.d(TAG, "Error while loading image " + mImage.path.toString());
                binding.imageProgressBar.setVisibility(View.GONE);
                binding.tvError.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        //if (binding.bboxView != null)
        //outState.putParcelableArrayList("regions", (ArrayList<Region>) binding.bboxView.getDrawnRegions());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        if (attributesFragment != null && attributesFragment.isVisible()) {
            attributesFragment.dismiss();
        }
        super.onDestroy();
    }

    @Override
    public Answer getAnswer() {
        if (mRegions.size() == 0) {
            return null;
        }

        JSONObject rawAnswer = new JSONObject();
        JSONArray regions = new JSONArray();
        float scaleTo = binding.bboxView.getImageRect().width() / binding.bboxView.getBoundingRect().width();
        for (Region r : binding.bboxView.getDrawnRegions()) {
            Region newRegion = Region.copyRegion(r);
            newRegion.transform(scaleTo);
            regions.put(newRegion.toJSONObject());
        }
        try {
            rawAnswer.put("image_width", binding.bboxView.getImageRect().width());
            rawAnswer.put("image_height", binding.bboxView.getImageRect().height());
            rawAnswer.put("image_name", mImage.path.getLastPathSegment());
            rawAnswer.put("regions", regions);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        answer.setRawAnswerData(rawAnswer.toString());
        return answer;
    }

    private void initToolbox() {
        mToolboxAnimator = binding.toolbox.animate();

        mToolboxHider = new Runnable() {
            @Override
            public void run() {
                mToolboxAnimator.setDuration(TOOLBOX_HIDE_DURATION)
                        .setStartDelay(TOOLBOX_HIDE_TIMEOUT)
                        .alpha(0.4f)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                if (!mAnimationCancelled) {
                                    mToolboxVisible = false;
                                } else {
                                    binding.toolbox.setAlpha(1);
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
                    mToolboxAnimator.setDuration(TOOLBOX_SHOW_DURATION)
                            .setStartDelay(0)
                            .alpha(1)
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

        View.OnClickListener mToolsClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mToolboxAnimator != null) {
                    mToolboxAnimator.cancel();
                    toggleToolbox();
                }

                if (!mToolboxVisible) {
                    toggleToolbox();
                }

                if (v.getId() == R.id.button_lock_image) {
                    toggleImageFreeze();
                    if (imageFrozen) {
                        ((ImageButton) v).setImageResource(R.drawable.ic_coord_lock);
                        v.setAlpha(0.8f);
                    } else {
                        ((ImageButton) v).setImageResource(R.drawable.ic_coord);
                        v.setAlpha(1);
                    }
                } else if (v.getId() == mSelectedId) {
                    mSelectedId = -1;
                    binding.bboxView.setTool(null);
                    v.setAlpha(1);
                } else {
                    if (mSelectedId != -1) {
                        View selected = binding.toolbox.findViewById(mSelectedId);
                        selected.setAlpha(1);
                    }
                    mSelectedId = v.getId();
                    binding.bboxView.setTool((Region.Shape) v.getTag());
                    v.setAlpha(0.8f);
                }
            }
        };

        binding.buttonRect.setOnClickListener(mToolsClickListener);
        binding.buttonCircle.setOnClickListener(mToolsClickListener);
        binding.buttonPolygon.setOnClickListener(mToolsClickListener);
        binding.buttonEllipse.setOnClickListener(mToolsClickListener);
        binding.buttonLockImage.setOnClickListener(mToolsClickListener);

        binding.buttonAttributes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchAttributesDialog(binding.bboxView.getRegion(selectedRegion));
            }
        });

        binding.buttonRect.setTag(Region.Shape.RECTANGLE);
        binding.buttonCircle.setTag(Region.Shape.CIRCLE);
        binding.buttonEllipse.setTag(Region.Shape.ELLIPSE);
        binding.buttonPolygon.setTag(Region.Shape.POLYGON);

        //Drawing finished
        binding.bboxView.setOnDrawingFinishedListener(new BoundingBoxView.OnDrawingFinished() {
            @Override
            public void onDrawingFinished(Region region, int regionIndex) {
                if (mSelectedId != -1) {
                    binding.getRoot().findViewById(mSelectedId).setAlpha(1);
                }
                mSelectedId = -1;
                mRegions.add(region);
                dispatchAttributesDialog(region);
            }
        });

        //Region selection
        binding.bboxView.setOnRegionSelectedListener(new BoundingBoxView.OnRegionSelected() {
            @Override
            public void onRegionSelected(int regionId) {
                selectedRegion = regionId;
                if (regionId == -1) {
                    binding.buttonAttributes.setVisibility(View.GONE);
                    binding.tvInstruction.setVisibility(View.GONE);
                } else {
                    if (binding.bboxView.getRegion(selectedRegion).getRegionAttributes().containsKey("label")) {
                        String label = binding.bboxView.getRegion(selectedRegion).getRegionAttributes().get("label");
                        binding.tvInstruction.setText(label);
                        binding.tvInstruction.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvInstruction.setText(R.string.no_label);
                        binding.tvInstruction.setVisibility(View.VISIBLE);
                    }
                    binding.buttonAttributes.setVisibility(View.VISIBLE);
                }
            }
        });

        //Region Delete
        binding.bboxView.setOnRegionDeleteListener(new BoundingBoxView.OnRegionDelete() {
            @Override
            public void onRegionDelete(final int regionId) {
                if (mRegions.get(regionId).getRegionAttributes().containsKey("label")) {
                    String label = mRegions.get(regionId).getRegionAttributes().get("label");
                    AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                    dialog.setCancelable(true);
                    dialog.setTitle(R.string.region_delete_title);
                    dialog.setMessage(String.format(getString(R.string.region_delete_msg), label));
                    dialog.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mRegions.remove(regionId);
                            binding.bboxView.deleteRegion(regionId);
                            dialog.dismiss();
                        }
                    });
                    dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    dialog.create().show();
                } else {
                    mRegions.remove(regionId);
                    binding.bboxView.deleteRegion(regionId);
                }
            }
        });
    }

    private void dispatchAttributesDialog(Region region) {
        attributesFragment = AttributesFragment.getInstance(region, JobDbHandler.getInstance().getJob(jobId).getOptions());
        attributesFragment.setCancelable(false);
        attributesFragment.setLabelChangeListener(listener);
        attributesFragment.show(getFragmentManager(), "AttributesFrag");
    }

    private void toggleToolbox() {
        if (mToolboxVisible) {
            mToolboxHider.run();
        } else {
            mToolboxShower.run();
        }
    }

    private void toggleImageFreeze() {
        taskImageView.setScaleEnabled(imageFrozen);
        taskImageView.setScrollEnabled(imageFrozen);
        taskImageView.setDoubleTapEnabled(imageFrozen);
        imageFrozen = !imageFrozen;
    }

    /*class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (mToolboxVisible) return false; //Toolbox is already visible
            if (e1.getY() < (displayDims.y * 2 / 3))
                return false; //Swipe didn't start at the bottom of screen
            if (e1.getY() < e2.getY()) return false; //Swipe was downward
            if (Math.abs(velocityX) > Math.abs(velocityY)) return false; //Swipe in the x axis
            if (Math.abs(velocityY) < SWIPE_THRESHOLD_VELOCITY)
                return false; //Swipe velocity is low (Not for toolbox)
            toggleToolbox();
            return true;
        }
    }*/
}