package com.recoded.taqadam.activities.workActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.crashlytics.android.Crashlytics;
import com.github.chrisbanes.photoview.OnMatrixChangedListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.navigation.NavigationView;
import com.recoded.taqadam.fragments.DetailsFragmentBottomSheet;
import com.recoded.taqadam.dialogs.FlagImageDialog;
import com.recoded.taqadam.R;
import com.recoded.taqadam.models.Responses.SuccessResponse;
import com.recoded.taqadam.utils.Preference;
import com.recoded.taqadam.utils.Utils;
import com.recoded.taqadam.adapters.FlagsAdapter;
import com.recoded.taqadam.adapters.LabelsAdapter;
import com.recoded.taqadam.fragments.RegionAttributesFragment;
import com.recoded.taqadam.models.Answer;
import com.recoded.taqadam.models.Api.Api;
import com.recoded.taqadam.objects.Assignment;
import com.recoded.taqadam.models.ImageFlag;
import com.recoded.taqadam.models.Label;
import com.recoded.taqadam.models.Link;
import com.recoded.taqadam.models.ProgressDetails;
import com.recoded.taqadam.models.Region;
import com.recoded.taqadam.objects.Task;
import com.recoded.taqadam.views.DrawingView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkActivity extends AppCompatActivity implements DrawingView.OnDrawingFinished, DrawingView.OnRegionSelected {
    private String TAG = WorkActivity.class.getSimpleName();

    static final float MAX_SCALE = 16f;
    static final float MIN_SCALE = 0.95f;
    static final String ZOOM_ENABLED = "zoom_enabled";
    static final String REGIONS = "regions";
    static final String ASSIGNMENT = "assignment";
    private static final String REGIONS_LINKS = "links";
    private static final String IMAGE_FLAGS = "flags";
    private static final String TOOL = "drawing_tool";
    private static final String TOTAL_TIME = "total_time";
    private static final String ENDLESS_DRAWING = "endless_drawing";

    //Per assignment
    private Assignment assignment;

    //per task
    private List<Region> toBeAddedRegions;
    private int currentImageWidth, currentImageHeight;
    private Answer mCurrentAnswer;
    private Task mCurrentTask;
    private List<String> currentImageFlags = new ArrayList<>();


    //General
    private LabelsAdapter labelsAdapter;
    private DrawerLayout mDrawer;
    private NavigationView mNavigation;
    private Toolbar mToolbar;
    private PhotoView mPhotoView;
    private View mLoadingIndicator;
    private DrawingView mDrawingView;
    private SeekBar mZoomSeeker;
    private SeekBar mPanXSeeker;
    private boolean mZoomEnabled = true;
    private SwitchCompat mZoomToggle;
    private AlertDialog clearAllAlert;
    private AlertDialog deleteAnswerAlert;
    private AlertDialog completedDialog;
    private AlertDialog instructionsDialog;
    private DetailsFragmentBottomSheet detailsDialog;
    private FlagImageDialog flagsDialog;
    private RegionAttributesFragment attributesFragment;
    private boolean continuesDrawing = true;
    private long mTotalSessionTime;
    private boolean isLoadingImage = false;

    private Button validate;
    private Button reject;

    //region buttons
    private ToggleButton regionLock;
    private Button regionDelete;
    private Button regionAttribute;
    private ToggleButton regionLink;

    private Context mContext;
    Preference mPreference;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (clearAllAlert != null && clearAllAlert.isShowing()) clearAllAlert.dismiss();
        if (deleteAnswerAlert != null && deleteAnswerAlert.isShowing()) deleteAnswerAlert.dismiss();
        if (completedDialog != null && completedDialog.isShowing()) completedDialog.dismiss();
        if (instructionsDialog != null && instructionsDialog.isShowing())
            instructionsDialog.dismiss();
        if (detailsDialog != null && detailsDialog.isResumed()) detailsDialog.dismiss();
        if (attributesFragment != null && attributesFragment.isResumed())
            attributesFragment.dismiss();
        if (flagsDialog != null && flagsDialog.isResumed()) flagsDialog.dismiss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work);

        Intent i = getIntent();
        if (i.hasExtra(ASSIGNMENT)) {
            assignment = (Assignment) i.getSerializableExtra(ASSIGNMENT);
        } else {
            Toast.makeText(this, "An error occured!", Toast.LENGTH_SHORT).show();
            Crashlytics.log("Work Activity Called Without an assignment");
            finish();
        }

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white);

        mDrawer = findViewById(R.id.drawer_layout);

        mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        mNavigation = findViewById(R.id.tools_drawer);
        NavigationView mRegionsDrawer = findViewById(R.id.regions_drawer);

        mPhotoView = findViewById(R.id.task_photo_view);
        mPhotoView.setScaleLevels(MIN_SCALE, (MAX_SCALE - MIN_SCALE) / 2, MAX_SCALE);

        mPhotoView.setOnMatrixChangeListener(new OnMatrixChangedListener() {
            @Override
            public void onMatrixChanged(RectF rect) {
                mDrawingView.setBoundingRect(rect);
                float range = MAX_SCALE - MIN_SCALE;
                int progress = Math.round((mPhotoView.getScale() - MIN_SCALE) * 100 / range);
                mZoomSeeker.setProgress(progress);
            }
        });
        mPhotoView.setTag(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                currentImageHeight = bitmap.getHeight();
                currentImageWidth = bitmap.getWidth();
                mDrawingView.setImageRect(new RectF(0, 0, currentImageWidth, currentImageHeight));
                mPhotoView.setImageBitmap(bitmap);
                toggleLoader(false);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                mPhotoView.setImageResource(R.drawable.ic_error_image);
                toggleLoader(false);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                toggleLoader(true);
            }
        });

        mLoadingIndicator = findViewById(R.id.progress_view);
        mDrawingView = findViewById(R.id.drawing_canvas);

        mDrawingView.setActive(false);

        mDrawingView.setOnRegionSelectedListener(this);

        mDrawingView.setOnDrawingFinishedListener(this);


        mNavigation.setNavigationItemSelectedListener(getToolsDrawerListener());
        mRegionsDrawer.setNavigationItemSelectedListener(getRegionsDrawerListener());

        mZoomSeeker = findViewById(R.id.zoom_seeker);
        mPanXSeeker = findViewById(R.id.pan_x_seeker);
        mPanXSeeker.setVisibility(View.GONE); //todo later

        validate = findViewById(R.id.validate);
        reject = findViewById(R.id.reject);

        regionLink = findViewById(R.id.switch_link_region);
        regionAttribute = findViewById(R.id.btn_add_region_attr);
        regionDelete = findViewById(R.id.btn_delete_region);
        regionLock = findViewById(R.id.switch_region_lock);
        if (assignment.forValidator()) {
            removeViewWhenMemberIsValidator();
        } else {
            removeViewWhenMemberIsAnnotator();
        }

        setRegionButtonsClickListeners();

        mZoomSeeker.setOnSeekBarChangeListener(getZoomSeekBarListener());

        //disable continues drawing
        continuesDrawing = false;
        SwitchCompat s = (SwitchCompat) mNavigation.getMenu().findItem(R.id.tools_cont_drawing).getActionView();
        s.setChecked(continuesDrawing);
        mDrawingView.setContinuesDrawing(continuesDrawing);

        restoreState(savedInstanceState);

        mContext = this;
        mPreference = new Preference(this);

        setListener();

        loadTask();
    }

    private void removeViewWhenMemberIsValidator(){
        regionLink.setVisibility(View.GONE);
        regionAttribute.setVisibility(View.GONE);
        regionDelete.setVisibility(View.GONE);
        regionLock.setVisibility(View.GONE);
    }

    private void removeViewWhenMemberIsAnnotator() {
        validate.setVisibility(View.GONE);
        reject.setVisibility(View.GONE);
    }

    private void setListener() {
        validate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Call<SuccessResponse> call = Api.getInstance().endpoints.validateTask(assignment.getId(), mCurrentTask.getId());
                call.enqueue(new Callback<SuccessResponse>() {
                    @Override
                    public void onResponse(Call<SuccessResponse> call, Response<SuccessResponse> response) {
                        SuccessResponse successResponse = response.body();
                        Toast.makeText(mContext, successResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        deleteTaskUrl(assignment.getId());
                        loadTask();
                    }

                    @Override
                    public void onFailure(Call<SuccessResponse> call, Throwable t) {

                    }
                });
            }
        });

        reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Call<SuccessResponse> call = Api.getInstance().endpoints.rejectTask(assignment.getId(), mCurrentTask.getId());
                call.enqueue(new Callback<SuccessResponse>() {
                    @Override
                    public void onResponse(Call<SuccessResponse> call, Response<SuccessResponse> response) {
                        SuccessResponse successResponse = response.body();
                        Toast.makeText(mContext, successResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        deleteTaskUrl(assignment.getId());
                        loadTask();
                    }

                    @Override
                    public void onFailure(Call<SuccessResponse> call, Throwable t) {

                    }
                });
            }
        });
    }

    private void restoreState(Bundle state) {
        if (state != null) {
            if (state.containsKey(ASSIGNMENT) && assignment == null) {
                assignment = (Assignment) state.getSerializable(ASSIGNMENT);
            }
            if (state.containsKey(REGIONS)) {
                toBeAddedRegions = state.getParcelableArrayList(REGIONS);
            }
            if (state.containsKey(ZOOM_ENABLED)) {
                mZoomEnabled = state.getBoolean(ZOOM_ENABLED);
            }
            if (state.containsKey(REGIONS_LINKS)) {
                mDrawingView.setLinks((Link[]) state.getParcelableArray(REGIONS_LINKS));
            }
            if (state.containsKey(TOOL)) {
                mDrawingView.setTool(Region.Shape.valueOf(state.getString(TOOL)));
            }
            if (state.containsKey(TOTAL_TIME)) {
                mTotalSessionTime = state.getLong(TOTAL_TIME);
            }
            if (state.containsKey(ENDLESS_DRAWING)) {
                continuesDrawing = state.getBoolean(ENDLESS_DRAWING);
                SwitchCompat s = (SwitchCompat) mNavigation.getMenu().findItem(R.id.tools_cont_drawing).getActionView();
                s.setChecked(continuesDrawing);
                mDrawingView.setContinuesDrawing(continuesDrawing);
            }
            if (state.containsKey(IMAGE_FLAGS)) {
                currentImageFlags = state.getStringArrayList(IMAGE_FLAGS);
            }
        }
    }

    private void setRegionButtonsClickListeners() {
        regionLock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Region r = mDrawingView.getSelectedRegion();
                if (r != null) {
                    r.setLocked(isChecked);
                }
            }
        });

        regionAttribute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeRegions();
                attributesFragment = RegionAttributesFragment.getInstance(mDrawingView.getSelectedRegion());
                attributesFragment.setCancelable(false);
                attributesFragment.show(getSupportFragmentManager(), "AttributesFrag");
            }
        });

        regionDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeRegions();
                Region r = mDrawingView.getSelectedRegion();
                if (r == null) return;
                if (r.getLabel().equals("NO LABEL") && r.linkedBy() == null) {
                    mDrawingView.deleteSelected();
                } else {
                    AlertDialog.Builder b = new AlertDialog.Builder(WorkActivity.this);
                    b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    b.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mDrawingView.deleteSelected();
                        }
                    });
                    b.setTitle("Delete Region");
                    b.setMessage("Are you sure you to delete region: " + r.getId() + " labeled: " + r.getLabel());
                    b.create().show();
                }
            }
        });

        regionLink.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mDrawingView.setLinking(isChecked);
                closeRegions();
            }
        });
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mDrawingView.getRegionsCount() != 0)
            outState.putParcelableArrayList(REGIONS, (ArrayList<Region>) mDrawingView.getNormalizedRegions());
        outState.putSerializable(ASSIGNMENT, assignment);
        outState.putBoolean(ZOOM_ENABLED, mZoomEnabled);
        outState.putParcelableArray(REGIONS_LINKS, mDrawingView.getLinks());
        if (mDrawingView.getTool() != null)
            outState.putString(TOOL, mDrawingView.getTool().toString());

        outState.putLong(TOTAL_TIME, mTotalSessionTime);
        outState.putBoolean(ENDLESS_DRAWING, continuesDrawing);
        if (currentImageFlags.size() > 0)
            outState.putStringArrayList(IMAGE_FLAGS, (ArrayList<String>) currentImageFlags);
    }

    private void toggleLoader(boolean show) {
        isLoadingImage = show;
        if (show) {
            mLoadingIndicator.setVisibility(View.VISIBLE);
            mDrawingView.setActive(false);
        } else {
            mLoadingIndicator.setVisibility(View.GONE);
            mDrawingView.setActive(true);
        }
    }

    private void loadTask() {
        if (tryToGetLastTask()) {
            return;
        }

        Call<Task> call = Api.getInstance().endpoints.getTask(assignment.getId());
        call.enqueue(new Callback<Task>() {
            @Override
            public void onFailure(Call<Task> call, Throwable t) {
                Log.d(TAG, "onFail get task");
            }

            @Override
            public void onResponse(Call<Task> call, Response<Task> response) {
                Task task = response.body();
                if (task != null) {
                    mCurrentTask = task;
                    loadImageToView(task.getUrl());
                    mCurrentAnswer = new Answer(assignment.getId(), task.getId());
                    if (assignment.forValidator()) {
                        String dataOfAnswer = task.getAnswer().getData();
                        parseAnswer(dataOfAnswer);
                        saveAnswer(dataOfAnswer);
                    }
                    saveTaskUrl(assignment.getId(), task.getRealUrl(), task.getId());
                } else {
                    noMoreTasks();
                }

            }
        });
    }

    private boolean tryToGetLastTask() {
        String keyGetLastUrl;
        String keyGetLastTaskId;
        if (assignment.forAnnotator()) {
            keyGetLastUrl = Preference.LAST_URL_OF_ANNOTATOR;
            keyGetLastTaskId = Preference.LAST_TASK_ID_OF_ANNOTATOR;
        } else {
            keyGetLastUrl = Preference.LAST_URL_OF_VALIDATOR;
            keyGetLastTaskId = Preference.LAST_TASK_ID_OF_VALIDATOR;
        }
        String lastUrl = mPreference.getString(keyGetLastUrl + assignment.getId());
        if (lastUrl != null) {
            Task task = new Task();
            task.setUrl(lastUrl);
            task.setId(mPreference.getLong(keyGetLastTaskId + assignment.getId()));
            mCurrentTask = task;
            mCurrentAnswer = new Answer(assignment.getId(), task.getId());
            loadImageToView(task.getUrl());
            if (assignment.forValidator()) {
                String dataOfAnswer = mPreference.getString(Preference.LAST_ANSWER_FOR_VALIDATOR + assignment.getId());
                parseAnswer(dataOfAnswer);
            }
            return true;
        }
        return false;
    }

    private void saveAnswer(String dataOfAnswer){
        mPreference.putString(Preference.LAST_ANSWER_FOR_VALIDATOR + assignment.getId(), dataOfAnswer);
    }

    private void saveTaskUrl(long projectId, String url, long taskId) {
        if (assignment.forAnnotator()) {
            mPreference.putString(Preference.LAST_URL_OF_ANNOTATOR + projectId, url);
            mPreference.putLong(Preference.LAST_TASK_ID_OF_ANNOTATOR + projectId, taskId);
        } else {
            mPreference.putString(Preference.LAST_URL_OF_VALIDATOR + projectId, url);
            mPreference.putLong(Preference.LAST_TASK_ID_OF_VALIDATOR + projectId, taskId);
        }

    }

    private void deleteTaskUrl(long projectId) {
        if (assignment.forAnnotator()) {
            mPreference.putString(Preference.LAST_URL_OF_ANNOTATOR + projectId, null);
        } else {
            mPreference.putString(Preference.LAST_URL_OF_VALIDATOR + projectId, null);
        }

    }

    private void loadImageToView(Uri url) {
        Picasso.with(this).load(url).into((Target) mPhotoView.getTag());
        if (assignment.forAnnotator()) {
            mDrawingView.changeIdOfRegions();
        }
    }

    private void noMoreTasks() {
        if (completedDialog == null) {
            AlertDialog.Builder d = new AlertDialog.Builder(this);
            d.setCancelable(false);
            d.setTitle(R.string.no_more_tasks);
            d.setMessage(R.string.you_completed_tasks);
            d.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            });
            completedDialog = d.create();
        }
        completedDialog.show();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        prepareLabelsList();
    }

    private void prepareLabelsList() {
        List<Label> list = convertJsonToLabelsMap();
        labelsAdapter = new LabelsAdapter(this, R.layout.label_list_item, list);
        ListView labelsListView = findViewById(R.id.labels_list);
        labelsListView.setAdapter(labelsAdapter);
        labelsAdapter.setOnLabelClickLitener(new LabelsAdapter.OnLabelClickLister() {
            @Override
            public void onLabelClick(Label label, int position) {
                Region selectedRegion = mDrawingView.getSelectedRegion();
                if (selectedRegion != null)
                    selectedRegion.addRegionAttribute(
                            assignment.getJob().getAttributeName(),
                            label.getLabel()
                    );
            }
        });
    }

    private List<Label> convertJsonToLabelsMap() {
        List<Label> labels = new ArrayList<>();
        for (String label : assignment.getJob().getOptions()) {
            Label l = new Label();
            l.setLabel(label);
            labels.add(l);
        }

        return labels;
    }

    private void sendAnswer() {
        final Answer a = getAnswer();
        if (a == null) {
            return;
        }
        Call<SuccessResponse> call = Api.getInstance().endpoints.postAnswer(assignment.getId(), a);
        call.enqueue(new Callback<SuccessResponse>() {
            @Override
            public void onResponse(Call<SuccessResponse> call, Response<SuccessResponse> response) {
                SuccessResponse s = response.body();
                String mesg;
                if (s != null) {
                    mesg = s.getMessage();
                } else {
                    mesg = "Unknown error from server";
                }
                Toast.makeText(mContext, mesg, Toast.LENGTH_SHORT).show();
                deleteTaskUrl(assignment.getId());
                loadTask();
            }

            @Override
            public void onFailure(Call<SuccessResponse> call, Throwable t) {

            }
        });
    }

    private void parseAnswer(String dataString) {
        mCurrentAnswer = new Answer(assignment.getId(), mCurrentTask.getId());
        try {
            JSONObject data = new JSONObject(dataString);

            JSONArray regionsJson = data.optJSONArray("regions");
//            JSONArray flagsJson = data.optJSONArray("image_flags");
//            JSONObject linksJson = data.optJSONObject("links");

//            if (linksJson != null) {
//                Iterator<String> iterator = linksJson.keys();
//                List<Link> linksArray = new ArrayList<>();
//                while (iterator.hasNext()) {
//                    Link link = new Link();
//                    String linkId = iterator.next();
//                    JSONArray regionIds = linksJson.getJSONArray(linkId);
//                    for (int i = 0; i < regionIds.length(); i++) {
//                        String regionId = regionIds.getString(i);
//                        link.regionIds.add(regionId);
//                    }
//                    link.id = linkId;
//                    linksArray.add(link);
//                }
//                mDrawingView.setLinks(linksArray.toArray(new Link[0]));
//            }
            if (regionsJson != null) {
                List<Region> regionsArray = new ArrayList<>();
                for (int i = 0; i < regionsJson.length(); i++) {
                    Region r = Region.fromJSONObject(regionsJson.getJSONObject(i));
                    r.setLocked(true);
                    regionsArray.add(r);
                }

                if (mDrawingView.getRegionsCount() == 0 || assignment.forValidator())
                    mDrawingView.addRegions(regionsArray);
            }

//            if (flagsJson != null) {
//                for (int i = 0; i < flagsJson.length(); i++) {
//                    String flag = flagsJson.getString(i);
//                    currentImageFlags.add(flag);
//                }
//            }
        } catch (JSONException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    private Answer getAnswer() {
        List<Region> regions = mDrawingView.getNormalizedRegions();
        Link[] links = mDrawingView.getLinks();

        JSONArray annotations = new JSONArray();
        JSONArray flagsJson = new JSONArray(currentImageFlags);
        for (Region r : regions) {
            annotations.put(r.toJSONObject());
        }
        if (regions.size() == 0 && currentImageFlags.size() == 0) {
            Toast.makeText(this, "Empty Answer", Toast.LENGTH_SHORT).show();
            return null;
        }

        JSONObject jsonLinks = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            for (Link l : links) {
                jsonLinks.put(l.id, l.toJsonArray());
            }
            data.put("image_width", currentImageWidth);
            data.put("image_height", currentImageHeight);
            data.put("image_name", "Test name");
            if (currentImageFlags.size() > 0)
                data.put("image_flags", flagsJson);
            if (regions.size() > 0 || assignment.forValidator()) {
                data.put("regions", annotations);
                data.put("links", jsonLinks);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        mCurrentAnswer.setData(data.toString());
        return mCurrentAnswer;
    }

    private void showInstructionsDialog() {
        if (!assignment.getJob().getInstructions().isEmpty()) {
            if (instructionsDialog == null) {
                LinearLayout root = new LinearLayout(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                root.setLayoutParams(lp);
                WebView wv = new WebView(this);
                wv.loadData(assignment.getJob().getInstructions(), "text/html", "utf-8");
                root.addView(wv);

                AlertDialog.Builder b = new AlertDialog.Builder(this);
                b.setTitle("Instructions");
                b.setView(root);
                b.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                instructionsDialog = b.create();
            }
            instructionsDialog.show();
        }
    }

    private void showDetailsDialog() {
        ProgressDetails pd = new ProgressDetails();
        pd.imageDims = currentImageWidth + "x" + currentImageHeight;
        pd.imageName = mCurrentTask.getFileName();
        pd.totalTime = Utils.getFormattedDuration(mTotalSessionTime);
        pd.noOfRegions = mDrawingView.getRegionsCount() + "";

        if (detailsDialog == null) {
            detailsDialog =
                    DetailsFragmentBottomSheet.newInstance();
        } else if (detailsDialog.isResumed()) {
            detailsDialog.dismiss();
            return;
        }
        detailsDialog.setProgressDetails(pd);
        detailsDialog.show(getSupportFragmentManager(),
                "WORK_DETAILS_FRAG");
    }

    @Override
    public void onDrawingFinished(Region drawnRegion, int index) {
        if (!continuesDrawing) openRegions();
    }

    @Override
    public void onRegionSelected(Region r, int regionId) {
        if (r != null) {
            regionLock.setChecked(r.isLocked());
            regionLock.setEnabled(true);
            regionDelete.setEnabled(true);
            regionAttribute.setEnabled(true);
            regionLink.setEnabled(true);

            labelsAdapter.setEnabled(true);
            if (!r.getLabel().equalsIgnoreCase("no label"))
                labelsAdapter.setSelectedLabel(r.getLabel());
            else
                labelsAdapter.setSelectedLabel("");
        } else {
            labelsAdapter.setSelectedLabel("");
            labelsAdapter.setEnabled(false);
            regionLock.setEnabled(false);
            regionDelete.setEnabled(false);
            regionAttribute.setEnabled(false);
            regionLink.setEnabled(false);
        }
    }

    private void clearRegions() {
        if (mDrawingView.getRegionsCount() == 0) return;
        if (clearAllAlert == null) {
            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setTitle("Delete ALL");
            b.setMessage("Are you sure you want to delete all drawings?");
            b.setPositiveButton("Delete ALL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mDrawingView.clearAll();
                    dialog.dismiss();
                }
            });
            b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            clearAllAlert = b.create();
        }
        clearAllAlert.show();
    }

    private void displayFlaggingDialog() {
        String[] flags = {"Bad Image", "Empty Image"};
        List<ImageFlag> list = new ArrayList<>();
        for (String flag : flags) {
            list.add(new ImageFlag(flag, currentImageFlags.contains(flag)));
        }

        if (flagsDialog == null) {
            FlagsAdapter adapter = new FlagsAdapter(this, R.layout.flag_item, list);
            adapter.setOnFlagClickListener(new FlagsAdapter.OnFlagClickListener() {
                @Override
                public void onFlagClick(String flag, boolean isChecked) {
                    if (isChecked) {
                        if (!currentImageFlags.contains(flag))
                            currentImageFlags.add(flag);
                    } else {
                        currentImageFlags.remove(flag);
                    }
                }
            });

            flagsDialog =
                    FlagImageDialog.newInstance(adapter);
        } else if (flagsDialog.isResumed()) {
            flagsDialog.dismiss();
            return;
        } else {
            flagsDialog.getAdapter().setFlags(list);
        }
        flagsDialog.show(getSupportFragmentManager(),
                "FLAG_IMG_FRAG");
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            closeTools();
        } else if (mDrawer.isDrawerOpen(GravityCompat.END)) {
            closeRegions();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.work_activity_menu, menu);
        if (!mZoomEnabled) {
            MenuItem zoom = menu.findItem(R.id.action_zoom);
            if (zoom != null)
                zoom.setIcon(R.drawable.ic_coord_lock);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_zoom) {
            mZoomToggle.toggle();
        } else if (id == android.R.id.home) {
            openTools();
        } else if (id == R.id.action_regions) {
            openRegions();
        } else if (id == R.id.action_details) {
            showDetailsDialog();
        } else if (id == R.id.action_instructions) {
            showInstructionsDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    private NavigationView.OnNavigationItemSelectedListener getToolsDrawerListener() {
        mNavigation.getMenu().findItem(R.id.tools_cont_drawing).getActionView().setClickable(false);
        final MenuItem m = mNavigation.getMenu().findItem(R.id.tools_zoom);
        mZoomToggle = (SwitchCompat) m.getActionView();
        mZoomToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    toggleZoom(false);
                    m.setTitle("Zoom Disabled");
                    m.setIcon(R.drawable.ic_coord_lock);
                    MenuItem zoom = mToolbar.getMenu().findItem(R.id.action_zoom);
                    if (zoom != null)
                        zoom.setIcon(R.drawable.ic_coord_lock);
                } else {
                    toggleZoom(true);
                    m.setTitle("Zoom Enabled");
                    m.setIcon(R.drawable.ic_coord);
                    MenuItem zoom = mToolbar.getMenu().findItem(R.id.action_zoom);
                    if (zoom != null)
                        zoom.setIcon(R.drawable.ic_coord);
                }
            }
        });
        return new NavigationView.OnNavigationItemSelectedListener() {
            private void checkTools(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();

                if (id == R.id.tools_rect) {
                    closeTools();
                    if (menuItem.isChecked()) {
                        menuItem.setChecked(false);
                        mDrawingView.setTool(null);
                    } else {
                        mDrawingView.setTool(Region.Shape.RECTANGLE);
                        menuItem.setChecked(continuesDrawing);
                    }
                } else if (id == R.id.tools_ellipse) {
                    closeTools();
                    if (menuItem.isChecked()) {
                        menuItem.setChecked(false);
                        mDrawingView.setTool(null);
                    } else {
                        mDrawingView.setTool(Region.Shape.ELLIPSE);
                        menuItem.setChecked(continuesDrawing);
                    }
                } else if (id == R.id.tools_polygon) {
                    closeTools();
                    if (menuItem.isChecked()) {
                        menuItem.setChecked(false);
                        mDrawingView.setTool(null);
                    } else {
                        mDrawingView.setTool(Region.Shape.POLYGON);
                        menuItem.setChecked(continuesDrawing);
                    }
                } else if (id == R.id.tools_point) {
                    closeTools();
                    if (menuItem.isChecked()) {
                        menuItem.setChecked(false);
                        mDrawingView.setTool(null);
                    } else {
                        mDrawingView.setTool(Region.Shape.POINT);
                        menuItem.setChecked(continuesDrawing);
                    }
                } else if (id == R.id.tools_line) {
                    closeTools();
                    if (menuItem.isChecked()) {
                        menuItem.setChecked(false);
                        mDrawingView.setTool(null);
                    } else {
                        mDrawingView.setTool(Region.Shape.LINE);
                        menuItem.setChecked(continuesDrawing);
                    }
                } else if (id == R.id.tools_arc) {
                    if (menuItem.isChecked()) {
                        menuItem.setChecked(false);
                        mDrawingView.setTool(null);
                    } else {
                        mDrawingView.setTool(Region.Shape.ARC);
                        menuItem.setChecked(continuesDrawing);
                    }
                    closeTools();
                } else if (id == R.id.tools_spline) {
                    if (menuItem.isChecked()) {
                        menuItem.setChecked(false);
                        mDrawingView.setTool(null);
                    } else {
                        mDrawingView.setTool(Region.Shape.SPLINE);
                        menuItem.setChecked(true);
                        menuItem.setChecked(continuesDrawing);
                    }
                    closeTools();
                }

            }

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                // Handle navigation view item clicks here.
                int id = menuItem.getItemId();

                checkTools(menuItem);

                //todo fix this
                if (id == R.id.tools_hide) {
                    Menu m = mNavigation.getMenu();
                    boolean isVisible = m.hasVisibleItems();
                    m.setGroupVisible(R.id.tools_group, !isVisible);
                    menuItem.setIcon(isVisible ? R.drawable.ic_collapse_white : R.drawable.ic_expand_white);
                } else if (id == R.id.tools_zoom) {
                    SwitchCompat s = (SwitchCompat) menuItem.getActionView();
                    s.toggle();
                } else if (id == R.id.tools_skip) {
                    skipTask();
                    closeTools();
                } else if (id == R.id.tools_send) {
                    sendAnswer();
                    closeTools();
                } else if (id == R.id.tools_clear) {
                    clearRegions();
                    closeTools();
                } else if (id == R.id.tools_delete_from_db) {
                    closeTools();
                } else if (id == R.id.tools_flag) {
                    displayFlaggingDialog();
                    closeTools();
                } else if (id == R.id.tools_back) {
                    finish();
                } else if (id == R.id.tools_cont_drawing) {
                    SwitchCompat s = (SwitchCompat) menuItem.getActionView();
                    s.setChecked(!continuesDrawing);
                    mDrawingView.setContinuesDrawing(!continuesDrawing);
                    continuesDrawing = !continuesDrawing;
                }

                return true;
            }
        };
    }

    private void toggleZoom(boolean enabled) {
        mZoomEnabled = enabled;
        mPhotoView.setEnabled(enabled);
        mDrawingView.setActive(!enabled);
    }

    private void skipTask() {
        Call<SuccessResponse> call = Api.getInstance().endpoints.skipTask(mCurrentTask.getId());
        call.enqueue(new Callback<SuccessResponse>() {
            @Override
            public void onResponse(Call<SuccessResponse> call, Response<SuccessResponse> response) {
                SuccessResponse successResponse = response.body();
                if (successResponse != null) {
                    Toast.makeText(mContext, successResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    if (successResponse.getSuccess()) {
                        deleteTaskUrl(assignment.getId());
                        loadTask();
                    }
                }
            }

            @Override
            public void onFailure(Call<SuccessResponse> call, Throwable t) {

            }
        });

    }

    private NavigationView.OnNavigationItemSelectedListener getRegionsDrawerListener() {
        return new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                return false;
            }
        };
    }

    private SeekBar.OnSeekBarChangeListener getZoomSeekBarListener() {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && !isLoadingImage) {
                    // todo fix this
                    float range = MAX_SCALE - MIN_SCALE;
                    float scale = MIN_SCALE + (progress * range / 100);
                    PointF p = new PointF(); //Center of screen
                    p.x = mPhotoView.getRight() / 2;
                    p.y = mPhotoView.getBottom() / 2;
                    RectF r = new RectF();
                    r.set(mDrawingView.getBoundingRect());
                    /*if(true) {
                        p.x -= (r.left);
                    }
                    if(true) {
                        p.y -= (r.top);
                    }*/
                    float s = mPhotoView.getScale();
                    p.x /= scale;
                    p.y /= scale;
                    //mPhotoView.getImageMatrix().mapRect(r);
                    mPhotoView.setScale(scale, false);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };
    }

    private void openTools() {
        mDrawer.openDrawer(GravityCompat.START);
    }

    private void closeTools() {
        mDrawer.closeDrawer(GravityCompat.START);
    }

    private void openRegions() {
        mDrawer.openDrawer(GravityCompat.END);
    }
    private void closeRegions() {
        mDrawer.closeDrawer(GravityCompat.END);
    }
}

