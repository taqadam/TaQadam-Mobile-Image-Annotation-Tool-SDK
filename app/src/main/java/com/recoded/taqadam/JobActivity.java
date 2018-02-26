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

import com.google.android.gms.tasks.OnSuccessListener;
import com.recoded.taqadam.databinding.ActivityJobBinding;
import com.recoded.taqadam.models.Answer;
import com.recoded.taqadam.models.Job;
import com.recoded.taqadam.models.Task;
import com.recoded.taqadam.models.db.JobDbHandler;
import com.recoded.taqadam.models.db.TaskDbHandler;

import java.util.ArrayList;
import java.util.List;

public class JobActivity extends BaseActivity {
    private static final String TAG = JobActivity.class.getSimpleName();
    private TasksPagerAdapter mTasksPagerAdapter;
    private ActivityJobBinding binding;

    private Job job;
    private int totalTasksCount, loadedTasksCount;

    private AlertDialog instructions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_job);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent i = getIntent();
        String jobId = i.getStringExtra("job_id");
        if (jobId == null) {
            finish();
        } else {
            job = JobDbHandler.getInstance().getJob(jobId);
            if (job != null && !job.getTasks().isEmpty()) {
                totalTasksCount = job.getTasks().size();
                //setTitle(String.format(getString(R.string.job_activity_title), 1, totalTasksCount));
                loadedTasksCount = 0;
                toggleProgressFrame(true);
                if (savedInstanceState == null) {
                    showInstructionsDialog();
                }
                loadTasks(3);
            } else {
                finish();
            }
        }

        mTasksPagerAdapter = new TasksPagerAdapter(getSupportFragmentManager());
        /* Set up the ViewPager with the sections adapter. */
        binding.viewPager.setAdapter(mTasksPagerAdapter);
        binding.viewPager.setLocked(true);
        binding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position + 3 > loadedTasksCount) {
                    loadTasks(1);
                }

                setTitle(String.format(getString(R.string.job_activity_title), position + 1, job.getTasks().size()));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
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

    private void submitAnswer() {
        String tag = "android:switcher:" + binding.viewPager.getId() + ":" + binding.viewPager.getCurrentItem();
        TaskFragment frag = (TaskFragment) getSupportFragmentManager().findFragmentByTag(tag);
        Answer answer = frag.getAnswer();
        if (answer != null && !answer.isCompleted()) {
            answer.setCompleted(true);
            TaskDbHandler.getInstance().completeTask(answer.getTaskId());
            frag.notifyFragmentForAnswer();
        }

        if (answer != null && answer.isCompleted()) {
            if (mTasksPagerAdapter.getCount() == 0) {
                showCompletedJobDialog();
            } else {
                binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem() + 1);
            }
            mTasksPagerAdapter.removeTask(binding.viewPager.getCurrentItem());
        }
    }

    private void toggleProgressFrame(boolean show) {
        if (show) {
            binding.progressBarFrame.setVisibility(View.VISIBLE);
        } else {
            binding.progressBarFrame.setVisibility(View.GONE);
        }
    }

    private void loadTasks(int countToLoad) {
        final List<String> ids = new ArrayList<>();
        if (totalTasksCount >= loadedTasksCount + countToLoad) {
            //We can load more
            for (int i = loadedTasksCount; i < loadedTasksCount + countToLoad; i++) {
                ids.add(job.getTasks().get(i));
            }
            loadedTasksCount += countToLoad;
        } else if (loadedTasksCount == totalTasksCount) {
            return;
        } else {
            //in case we have a remainder which is less that count to load
            int remainder = totalTasksCount - loadedTasksCount;
            for (int i = loadedTasksCount; i < loadedTasksCount + remainder; i++) {
                ids.add(job.getTasks().get(i));
            }
            loadedTasksCount += remainder;
        }
        //if no ids return
        if (ids.isEmpty()) return;

        //Let's load the ids
        TaskDbHandler.getInstance().getTasks(ids.toArray(new String[0]))
                .addOnSuccessListener(this, new OnSuccessListener<List<Task>>() {
                    @Override
                    public void onSuccess(List<Task> tasksList) {
                        if (tasksList.size() != 0) {
                            mTasksPagerAdapter.addNewTasks(tasksList.toArray(new Task[0]));
                            toggleProgressFrame(false);
                        }
                        if (tasksList.size() < ids.size()) {
                            if (totalTasksCount != loadedTasksCount) {
                                loadTasks(ids.size() - tasksList.size());
                            } else {
                                showCompletedJobDialog();
                            }
                        }
                    }
                });
    }

    private void showCompletedJobDialog() {
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
        d.create().show();
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
        super.onDestroy();
    }
}