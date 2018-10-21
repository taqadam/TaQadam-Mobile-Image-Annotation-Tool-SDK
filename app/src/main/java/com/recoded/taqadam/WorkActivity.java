package com.recoded.taqadam;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.material.navigation.NavigationView;
import com.recoded.taqadam.models.Answer;
import com.recoded.taqadam.models.Api.Api;
import com.recoded.taqadam.models.Api.ApiError;
import com.recoded.taqadam.models.Assignment;
import com.recoded.taqadam.models.ImageFlag;
import com.recoded.taqadam.models.Label;
import com.recoded.taqadam.models.Link;
import com.recoded.taqadam.models.ProgressDetails;
import com.recoded.taqadam.models.Region;
import com.recoded.taqadam.models.Responses.PaginatedResponse;
import com.recoded.taqadam.models.Task;
import com.recoded.taqadam.models.db.AnswersDatabase;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkActivity extends AppCompatActivity implements DrawingView.OnDrawingFinished, DrawingView.OnRegionSelected {

    static final float MAX_SCALE = 16f;
    static final float MIN_SCALE = 0.95f;
    static final String ZOOM_ENABLED = "zoom_enabled";
    static final String REGIONS = "regions";
    static final String ASSIGNMENT = "assignment";
    private static final String REGIONS_LINKS = "links";
    private static final String IMAGE_FLAGS = "flags";
    private static final String TOOL = "drawing_tool";
    private static final String TIME_TAKEN = "time_taken";
    private static final String TOTAL_TIME = "total_time";
    private static final String STARTED_AT = "started_at";
    private static final String ENDLESS_DRAWING = "endless_drawing";
    private static final String SKIPPED_TASKS = "skipped_tasks";

    //Per assignment
    private Assignment assignment;
    private PaginatedResponse<Task> mTasks;
    private PaginatedResponse<Task> mMoreTasks;
    private long mTaskCounter = 1;
    private Long mCurrentPage = 1L;
    private List<Long> skippedTasks = new ArrayList<>();

    //per task
    private int mTaskIndex = 0; //currentTaskIndex
    private List<Region> toBeAddedRegions;
    private int currentImageWidth, currentImageHeight;
    private long mTimeTaken = 0;
    private long startedAt = new Date().getTime();
    private final Timer mTimer = new Timer();
    private TimerTask mTimerTask;
    private Answer mCurrentAnswer;
    private Task mCurrentTask;
    private List<String> currentImageFlags = new ArrayList<>();


    //General
    private LabelsAdapter labelsAdapter;
    private DrawerLayout mDrawer;
    private NavigationView mToolsDrawer;
    private Toolbar mToolbar;
    private PhotoView mPhotoView;
    private View mLoadingIndicator;
    private DrawingView mDrawingView;
    private SeekBar mZoomSeeker;
    private SeekBar mPanYSeeker;
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
    private AnswersDatabase db;
    private boolean isRefreshingImage = false;
    private boolean continuesDrawing = true;
    private long mTotalSessionTime;

    //region buttons
    private ToggleButton regionLock;
    private Button regionDelete;
    private Button regionAttribute;
    private ToggleButton regionLink;


    //private RegionsAdapter mRegionsPickerAdapter;
    //private DSListView<Region> mRegionsPicker;
    private void prepareForNewTask() {
        //reset all task related fields
        mDrawingView.clearAll();
        currentImageFlags.clear();
        mCurrentAnswer = new Answer(assignment.getId(), mCurrentTask.getId());
        loadAnswer();
        startedAt = new Date().getTime();
        resetTimer();
        if (regionLink.isChecked()) regionLink.setChecked(false);
        if (regionLock.isChecked()) regionLock.setChecked(false);
    }

    private void loadAnswer() {
        if (mCurrentTask.getAnswer() != null) {
            this.mCurrentAnswer = mCurrentTask.getAnswer();
            parseAnswer();
        } else {
            loadAnswerFromDb(mCurrentTask).observe(this, new Observer<List<Answer>>() {
                @Override
                public void onChanged(List<Answer> answers) {
                    if (answers.size() == 0) {
                        mCurrentAnswer = new Answer(assignment.getId(), mCurrentTask.getId());
                    } else if (answers.size() == 1) {
                        mCurrentAnswer = answers.get(0);
                    } else {
                        mCurrentAnswer = answers.get(answers.size() - 1);
                    }
                    parseAnswer();
                }
            });
        }
    }

    private void parseAnswer() {
        //Answer Data;
        if (mCurrentAnswer.getData() != null && !mCurrentAnswer.getData().isEmpty()) {
            try {
                JSONObject data = new JSONObject(mCurrentAnswer.getData());

                JSONArray regionsJson = data.optJSONArray("regions");
                JSONArray flagsJson = data.optJSONArray("image_flags");
                JSONObject linksJson = data.optJSONObject("links");

                if (linksJson != null) {
                    Iterator<String> iterator = linksJson.keys();
                    List<Link> linksArray = new ArrayList<>();
                    while (iterator.hasNext()) {
                        Link link = new Link();
                        String linkId = iterator.next();
                        JSONArray regionIds = linksJson.getJSONArray(linkId);
                        for (int i = 0; i < regionIds.length(); i++) {
                            String regionId = regionIds.getString(i);
                            link.regionIds.add(regionId);
                        }
                        link.id = linkId;
                        linksArray.add(link);
                    }
                    mDrawingView.setLinks(linksArray.toArray(new Link[0]));
                }
                if (regionsJson != null) {
                    List<Region> regionsArray = new ArrayList<>();
                    for (int i = 0; i < regionsJson.length(); i++) {
                        Region r = Region.fromJSONObject(regionsJson.getJSONObject(i));
                        regionsArray.add(r);
                    }

                    if (mDrawingView.getRegionsCount() == 0)
                        mDrawingView.addRegions(regionsArray);
                }

                if (flagsJson != null) {
                    for (int i = 0; i < flagsJson.length(); i++) {
                        String flag = flagsJson.getString(i);
                        currentImageFlags.add(flag);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
        }

        //Answer Time
        if (mCurrentAnswer.getTimeTaken() != null)
            this.mTimeTaken = mCurrentAnswer.getTimeTaken();
        if (mCurrentAnswer.getStartedAt() != null)
            this.startedAt = mCurrentAnswer.getStartedAt();

    }

    private Answer getAnswer() {
        if (mCurrentAnswer == null) return null;
        mCurrentAnswer.setStartedAt(startedAt);
        mCurrentAnswer.setTimeTaken(mTimeTaken);
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
            data.put("image_name", mTasks.data.get(mTaskIndex).getFileName());
            if (currentImageFlags.size() > 0)
                data.put("image_flags", flagsJson);
            if (regions.size() > 0) {
                data.put("regions", annotations);
                data.put("links", jsonLinks);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        mCurrentAnswer.setData(data.toString());
        return mCurrentAnswer;
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeTimer();
    }

    private void resetTimer() {
        pauseTimer();
        mTimeTaken = 0;
        resumeTimer();
    }

    private void resumeTimer() {
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                mTimeTaken++;
                mTotalSessionTime++;
            }
        };
        mTimer.scheduleAtFixedRate(mTimerTask, 0, 1000);
    }

    private void pauseTimer() {
        mTimerTask.cancel();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pauseTimer();
        mTimer.cancel();

        saveAnswer(getAnswer());

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

        initDatabase();

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white);

        mDrawer = findViewById(R.id.drawer_layout);

        mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        mToolsDrawer = findViewById(R.id.tools_drawer);
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
                /*RectF rS = new RectF();
                RectF rD = new RectF();
                Matrix d = new Matrix();
                Matrix s = new Matrix();
                mPhotoView.getSuppMatrix(s);
                s.mapRect(rS);
                Log.d("ORIG", rect.toString());
                Log.d("SUP", rS.toString());
                mPhotoView.getDisplayMatrix(d);
                d.mapRect(rD);
                Log.d("DIS", rD.toString());*/
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
                setTitle(String.format(getString(R.string.job_activity_title), mTaskCounter, mTasks.meta.total));
                if (!isRefreshingImage) {
                    prepareForNewTask();
                    if (toBeAddedRegions != null)
                        mDrawingView.addRegions(toBeAddedRegions);
                    toBeAddedRegions = null;
                } else {
                    isRefreshingImage = false;
                }
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


        mToolsDrawer.setNavigationItemSelectedListener(getToolsDrawerListener());
        mRegionsDrawer.setNavigationItemSelectedListener(getRegionsDrawerListener());

        mZoomSeeker = findViewById(R.id.zoom_seeker);
        mPanXSeeker = findViewById(R.id.pan_x_seeker);
        mPanXSeeker.setVisibility(View.GONE); //todo later

        regionLink = findViewById(R.id.switch_link_region);
        regionAttribute = findViewById(R.id.btn_add_region_attr);
        regionDelete = findViewById(R.id.btn_delete_region);
        regionLock = findViewById(R.id.switch_region_lock);

        setRegionButtonsClickListeners();

        mZoomSeeker.setOnSeekBarChangeListener(getZoomSeekBarListener());

        //disable continues drawing
        continuesDrawing = false;
        SwitchCompat s = (SwitchCompat) mToolsDrawer.getMenu().findItem(R.id.tools_cont_drawing).getActionView();
        s.setChecked(continuesDrawing);
        mDrawingView.setContinuesDrawing(continuesDrawing);

        restoreState(savedInstanceState);

        loadTasks();

    }

    private void initDatabase() {
        db = AnswersDatabase.getInstance(this);
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

        outState.putLong(TIME_TAKEN, mTimeTaken);
        outState.putLong(STARTED_AT, startedAt);
        outState.putLong(TOTAL_TIME, mTotalSessionTime);
        outState.putBoolean(ENDLESS_DRAWING, continuesDrawing);
        if (skippedTasks.size() > 0) {
            long[] list = new long[skippedTasks.size()];
            for (int i = 0; i < skippedTasks.size(); i++) {
                list[i] = skippedTasks.get(i);
            }
            outState.putLongArray(SKIPPED_TASKS, list);
        }
        if (currentImageFlags.size() > 0)
            outState.putStringArrayList(IMAGE_FLAGS, (ArrayList<String>) currentImageFlags);
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
            if (state.containsKey(TIME_TAKEN)) {
                mTimeTaken = state.getLong(TIME_TAKEN);
            }
            if (state.containsKey(STARTED_AT)) {
                startedAt = state.getLong(STARTED_AT);
            }
            if (state.containsKey(TOTAL_TIME)) {
                mTotalSessionTime = state.getLong(TOTAL_TIME);
            }
            if (state.containsKey(ENDLESS_DRAWING)) {
                continuesDrawing = state.getBoolean(ENDLESS_DRAWING);
                SwitchCompat s = (SwitchCompat) mToolsDrawer.getMenu().findItem(R.id.tools_cont_drawing).getActionView();
                s.setChecked(continuesDrawing);
                mDrawingView.setContinuesDrawing(continuesDrawing);
            }
            if (state.containsKey(SKIPPED_TASKS)) {
                long list[] = state.getLongArray(SKIPPED_TASKS);
                for (long id : list) {
                    skippedTasks.add(id);
                }
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

    private void toggleLoader(boolean show) {
        if (show) {
            mLoadingIndicator.setVisibility(View.VISIBLE);
            mDrawingView.setActive(false);
        } else {
            mLoadingIndicator.setVisibility(View.GONE);
            mDrawingView.setActive(true);
        }
    }

    private void loadTasks() {
        toggleLoader(true);
        getTasks(assignment.getId(), 1L).addOnSuccessListener(this, new OnSuccessListener<PaginatedResponse<Task>>() {
            @Override
            public void onSuccess(PaginatedResponse<Task> taskPaginatedResponse) {
                mTasks = taskPaginatedResponse;
                if (mTasks.data.size() > 0) {
                    loadTask(0);
                }
            }
        });
    }

    private void loadMoreTasks() {
        Long total = mTasks.meta.lastPage;
        if (mCurrentPage.equals(total)) return; //No need to load more

        getTasks(assignment.getId(), mCurrentPage + 1).addOnSuccessListener(this, new OnSuccessListener<PaginatedResponse<Task>>() {
            @Override
            public void onSuccess(PaginatedResponse<Task> taskPaginatedResponse) {
                mMoreTasks = taskPaginatedResponse;
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadMoreTasks();
            }
        });
    }

    private void nextImage() {
        //Check if the current task index == tasks size then load from more tasks
        //Check if there is no more tasks
        if (mTaskIndex == mTasks.data.size() - 3) {
            loadMoreTasks();
        }

        if (mTaskIndex == mTasks.data.size() - 1) {
            if (mMoreTasks != null) {
                mTasks = mMoreTasks;
                mMoreTasks = null;
                mCurrentPage++;
                mTaskIndex = 0;
                mTaskCounter++;
                loadTask(mTaskIndex);
            } else {
                noMoreTasks();
            }
        } else {
            loadTask(mTaskIndex + 1);
            mTaskCounter++;
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

    private void loadTask(int taskIndex) {
        toggleLoader(true);

        if (taskIndex < mTasks.data.size()) {
            Task t = mTasks.data.get(taskIndex);
            if (skippedTasks.contains(t.getId())) {
                mTaskIndex = taskIndex;
                mCurrentTask = t;
                nextImage();
                return;
            }
            Picasso.with(this).load(t.getUrl()).into((Target) mPhotoView.getTag());
            mTaskIndex = taskIndex;
            mCurrentTask = t;
        }
    }


    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //prepareRegionsSelector();
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
                    selectedRegion.setLabel(label.getLabel());
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
        //Test String
/*
        String s = "{\"Felafel\":[],\"Cheese\":{\"Herbs\":[\"Paneer\",\"Yoghurt\"],\"Halloom\":{},\"Cake\":[\"Choco\",\"Vanilla\"]},\"Mastaw\":[],\"Sedan\":{\"Cruze\":[],\"Malibu\":[\"2015\",\"2016\"]}}";
        s = "[\"Falefel\", \"Cheese\", \"Dolama\", \"Doner\", \"Kabab\", \"Burger\", \"Pizza\", \"Cake\", \"Honey\", \"Milk Shake\", \"Top Occluded Iris\", \"Bottom Occluded Iris\"]";
        List<Label> ret = new ArrayList<>();
        if (s.trim().charAt(0) == '[') {
            try {
                JSONArray a = new JSONArray(s);
                for (int i = 0; i < a.length(); i++) {
                    Label l = new Label();
                    l.setLabel(a.getString(i));
                    ret.add(l);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
            return ret;
        }
        //todo fix nested taxonomies
        return ret;

        JSONTokener tokener = new JSONTokener(s);
        while (tokener.more()) {
            try {
                Object o = tokener.nextValue();
                if(o instanceof JSONObject) { //nested

                }
            } catch (JSONException e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
        }
*/

    }

    //@todo NOT USED NOW
    /*private void prepareRegionsSelector() {
        mRegionsPickerAdapter = new RegionsAdapter(this, R.layout.regions_list_item, mDrawingView.getDrawnRegions());
        mRegionsPicker = findViewById(R.id.regions_list);
        mRegionsPicker.setAdapter(mRegionsPickerAdapter);
    }*/

    private void sendAnswer() {
        final Answer a = getAnswer();
        if (a == null) {
            nextImage();
            return;
        }
        a.setSubmittedAt(new Date().getTime());
        saveAnswer(a);
        Call call = Api.getInstance().endpoints.postAnswer(assignment.getId(), a);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                deleteAnswer(a);
                //Snackbar.make(findViewById(android.R.id.content),"Submitted",Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                //Toast.makeText(WorkActivity.this, "Failed to submit answer", Toast.LENGTH_SHORT).show();
                Crashlytics.logException(t);
            }
        });

        nextImage();
    }

    private void deleteAnswer(final Answer a) {
        if (a == null) return;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                db.answersDao().deleteAnswer(a);
            }
        });

        t.start();
    }

    private void saveAnswer(final Answer a) {
        if (a == null) return;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                if (a.getDbId() == null) {
                    db.answersDao().saveAnswer(a);
                } else {
                    db.answersDao().updateAnswer(a);
                }
            }
        });

        t.start();
    }

    private LiveData<List<Answer>> loadAnswerFromDb(Task t) {
        final Long taskId = t.getId();
        final Long assignmentId = assignment.getId();
        return db.answersDao().getAnswer(assignmentId, taskId);
    }

    private void skipAnswer() {
        final Answer a = getAnswer();
        saveAnswer(a);
        skippedTasks.add(mCurrentTask.getId());
        nextImage();
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
        pd.timeTaken = Utils.getFormattedDuration(mTimeTaken);
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

    private void refreshImage() {
        isRefreshingImage = true;
        loadTask(mTaskIndex);
    }

    @Override
    public void onDrawingFinished(Region drawnRegion, int index) {

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

    private void deleteAnswerFromDb() {
        if (mCurrentAnswer == null) return;
        if (mCurrentAnswer.getDbId() == null) return;
        if (deleteAnswerAlert == null) {
            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setTitle("Delete Answer");
            b.setIcon(R.drawable.ic_delete_forever);
            b.setMessage("Are you sure you want to delete your answer from local database?");
            b.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mDrawingView.clearAll();
                    deleteAnswer(mCurrentAnswer);
                    dialog.dismiss();
                }
            });
            b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            deleteAnswerAlert = b.create();
        }
        deleteAnswerAlert.show();
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
        } else if (id == R.id.action_refresh) {
            refreshImage();
        } else if (id == R.id.action_details) {
            showDetailsDialog();
        } else if (id == R.id.action_instructions) {
            showInstructionsDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    private NavigationView.OnNavigationItemSelectedListener getToolsDrawerListener() {
        mToolsDrawer.getMenu().findItem(R.id.tools_cont_drawing).getActionView().setClickable(false);
        final MenuItem m = mToolsDrawer.getMenu().findItem(R.id.tools_zoom);
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
                    Menu m = mToolsDrawer.getMenu();
                    //menuItem.getSubMenu().setHeaderTitle("TITLE");
                    boolean isVisible = m.hasVisibleItems();
                    m.setGroupVisible(R.id.tools_group, !isVisible);
                    menuItem.setIcon(isVisible ? R.drawable.ic_collapse_white : R.drawable.ic_expand_white);
                } else if (id == R.id.tools_zoom) {
                    SwitchCompat s = (SwitchCompat) menuItem.getActionView();
                    s.toggle();
                    //s.setChecked(!s.isChecked());
                } else if (id == R.id.tools_send) {
                    sendAnswer();
                    closeTools();
                } else if (id == R.id.tools_skip) {
                    skipAnswer();
                    closeTools();
                } else if (id == R.id.tools_clear) {
                    clearRegions();
                    closeTools();
                } else if (id == R.id.tools_delete_from_db) {
                    deleteAnswerFromDb();
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
        }

                ;
    }

    private void toggleZoom(boolean enabled) {
        mZoomEnabled = enabled;
        mPhotoView.setEnabled(enabled);
        mDrawingView.setActive(!enabled);
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
                if (fromUser) {
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

    private void closeTools() {
        mDrawer.closeDrawer(GravityCompat.START);
    }

    private void closeRegions() {
        mDrawer.closeDrawer(GravityCompat.END);
    }

    private void openTools() {
        mDrawer.openDrawer(GravityCompat.START);
    }

    private void openRegions() {
        mDrawer.openDrawer(GravityCompat.END);
    }

    public com.google.android.gms.tasks.Task<PaginatedResponse<Task>> getTasks(Long assignmentId, Long page) {
        final TaskCompletionSource<PaginatedResponse<Task>> task = new TaskCompletionSource<>();

        Call<PaginatedResponse<Task>> call = Api.getInstance().endpoints.getTasksPaginated(assignmentId, page);
        call.enqueue(new Callback<PaginatedResponse<Task>>() {
            @Override
            public void onResponse(Call<PaginatedResponse<Task>> call, Response<PaginatedResponse<Task>> response) {
                task.setResult(response.body());
            }

            @Override
            public void onFailure(Call<PaginatedResponse<Task>> call, Throwable t) {
                if (t instanceof ApiError) {
                    task.setException((ApiError) t);
                } else {
                    Crashlytics.logException(t);
                    task.setException(new ApiError(500, "Unknown error occurred!"));
                }
            }
        });

        return task.getTask();
    }
}

