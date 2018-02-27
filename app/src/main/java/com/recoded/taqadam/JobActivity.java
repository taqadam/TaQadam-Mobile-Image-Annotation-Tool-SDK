package com.recoded.taqadam;

import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.recoded.taqadam.databinding.ActivityJobBinding;
import com.recoded.taqadam.models.Answer;
import com.recoded.taqadam.models.Image;
import com.recoded.taqadam.models.Job;
import com.recoded.taqadam.models.db.ImageDbHandler;
import com.recoded.taqadam.models.db.JobDbHandler;

import java.util.ArrayList;
import java.util.List;

public class JobActivity extends BaseActivity {
    private static final String TAG = JobActivity.class.getSimpleName();
    private TasksPagerAdapter mTasksPagerAdapter;
    private ActivityJobBinding binding;
    private Job job;
    private AlertDialog instructions, completedDialog;
    private TaskFragment currentFragment;
    private List<String> completedImgs = new ArrayList<>(); //to input impressions
    private boolean instructionsSeen = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_job);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent i = getIntent();
        final String jobId = i.getStringExtra("job_id");
        if (jobId == null) {
            finish();
        } else {
            job = JobDbHandler.getInstance().getJob(jobId);
            if (job != null) {
                //setTitle(String.format(getString(R.string.job_activity_title), 1, totalTasksCount));
                toggleProgressFrame(true);
                if (savedInstanceState != null) {
                    instructionsSeen = true;
                }
                ImageDbHandler.getInstance().getTasks(jobId).addOnSuccessListener(this, new OnSuccessListener<List<Image>>() {
                    @Override
                    public void onSuccess(List<Image> images) {
                        if (images.size() == 0) {
                            showCompletedJobDialog();
                            return;
                        }
                        if (!instructionsSeen) showInstructionsDialog();
                        mTasksPagerAdapter = new TasksPagerAdapter(getSupportFragmentManager(), images, job.getTasksType(), jobId);
                        binding.viewPager.setAdapter(mTasksPagerAdapter);
                        binding.viewPager.setLocked(true);
                        toggleProgressFrame(false);
                        setCurrentFragment();
                        ImageDbHandler.getInstance().setImpressionsReachedListener(new ImageDbHandler.OnImpressionsReachedListener() {
                            @Override
                            public void onImpressionsReached(String imgId) {
                                completedImgs.add(imgId);
                                if (currentFragment.mImage.id.equals(imgId)) {
                                    Toast.makeText(JobActivity.this, "This image has reached the required impressions", Toast.LENGTH_LONG).show();
                                    gotoNextImage();
                                }
                            }
                        });
                    }
                });
            } else {
                finish();
            }
        }

        binding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //setTitle(String.format(getString(R.string.job_activity_title), position + 1, job.getImagesList().size()));
                setCurrentFragment();
                if (completedImgs.contains(currentFragment.mImage.id)) {
                    Toast.makeText(JobActivity.this, "This image has reached the required impressions", Toast.LENGTH_LONG).show();
                    gotoNextImage();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void gotoNextImage() {
        if (binding.viewPager.getCurrentItem() == mTasksPagerAdapter.getCount() - 1) {
            showCompletedJobDialog();
        } else {
            binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem() + 1);
        }
    }

    private void showInstructionsDialog() {
        if (!job.getInstructions().isEmpty()) {
            if (instructions == null) {
                LinearLayout root = new LinearLayout(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                root.setLayoutParams(lp);
                WebView wv = new WebView(this);
                wv.loadData(job.getInstructions(), "text/html", "utf-8");
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
                instructions = b.create();
            }
            instructions.show();
        }
    }

    public void setCurrentFragment() {
        String tag = "android:switcher:" + binding.viewPager.getId() + ":" + binding.viewPager.getCurrentItem();
        this.currentFragment = (TaskFragment) getSupportFragmentManager().findFragmentByTag(tag);
        setTitle(String.format(getString(R.string.job_activity_title), binding.viewPager.getCurrentItem() + 1, job.getImagesList().size()));
    }

    private void submitAnswer() {
        if (!currentFragment.imageLoaded) {
            gotoNextImage();
        } else {
            Answer answer = currentFragment.getAnswer();
            if (answer != null) {
                if (ImageDbHandler.getInstance().submitAnswer(answer)) {
                    gotoNextImage();
                }
            } else {
                Toast.makeText(this, "No answer was submitted", Toast.LENGTH_SHORT).show();
                gotoNextImage();
            }
        }
    }

    private void toggleProgressFrame(boolean show) {
        if (show) {
            binding.progressBarFrame.setVisibility(View.VISIBLE);
        } else {
            binding.progressBarFrame.setVisibility(View.GONE);
        }
    }

    private void showCompletedJobDialog() {
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.tasks_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(JobActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_lock_slider) {
            /*if (binding.viewPager.isLocked()) {
                item.setIcon(R.drawable.ic_unlock);
                binding.viewPager.setLocked(false);
            } else {
                item.setIcon(R.drawable.ic_lock);
                binding.viewPager.setLocked(true);
            }*/
            showInstructionsDialog();
        } else if (id == R.id.action_submit_answer) {
            submitAnswer();
        } else if (id == android.R.id.home) {
            onBackPressed();
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (instructions != null && instructions.isShowing())
            instructions.dismiss();
        if (completedDialog != null && completedDialog.isShowing())
            completedDialog.dismiss();

        if (job != null) job.release();
        ImageDbHandler.getInstance().release();
        super.onDestroy();
    }
}