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
import com.google.android.gms.tasks.TaskCompletionSource;
import com.recoded.taqadam.databinding.ActivityAssignmentBinding;
import com.recoded.taqadam.models.Answer;
import com.recoded.taqadam.models.Api.Api;
import com.recoded.taqadam.models.Assignment;
import com.recoded.taqadam.models.Responses.PaginatedResponse;
import com.recoded.taqadam.models.Task;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssignmentActivity extends BaseActivity {
    private static final String TAG = AssignmentActivity.class.getSimpleName();
    private TasksPagerAdapter mTasksPagerAdapter;
    private ActivityAssignmentBinding binding;
    private Assignment assignment;
    private AlertDialog instructions, completedDialog;
    private TaskFragment currentFragment;
    private boolean instructionsSeen = false;

    private PaginatedResponse<Task> tasks;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_assignment);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent i = getIntent();
        assignment = (Assignment) i.getSerializableExtra("assignment");
        if (assignment == null) {
            finish();
        } else {
            if (savedInstanceState != null) {
                instructionsSeen = true;
            }
            toggleProgressFrame(true);
            if (!instructionsSeen) showInstructionsDialog();
            getTasks(assignment.getId(), 1L).addOnSuccessListener(this, new OnSuccessListener<PaginatedResponse<Task>>() {
                @Override
                public void onSuccess(PaginatedResponse<Task> tasks) {
                    toggleProgressFrame(false);
                    AssignmentActivity.this.tasks = tasks;
                    if (tasks.data != null && tasks.data.size() == 0) {
                        showCompletedJobDialog();
                        return;
                    }
                    mTasksPagerAdapter = new TasksPagerAdapter(
                            getSupportFragmentManager(),
                            tasks.data,
                            assignment.getJob().getService().getTypeOfService(),
                            assignment);
                    binding.viewPager.setAdapter(mTasksPagerAdapter);
                    binding.viewPager.setLocked(true);
                    setCurrentFragment(0);
                }
            });
        }

        binding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //setTitle(String.format(getString(R.string.job_activity_title), position + 1, job.getImagesList().size()));
                setCurrentFragment(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void gotoNextImage() {
        if (binding.viewPager.getCurrentItem() == mTasksPagerAdapter.getCount() - 1) {
            //set completed check for next page
            if (tasks.meta.lastPage > tasks.meta.currentPage) {
                //Load more tasks
                toggleProgressFrame(true);
                getTasks(assignment.getId(), tasks.meta.currentPage + 1).addOnSuccessListener(new OnSuccessListener<PaginatedResponse<Task>>() {
                    @Override
                    public void onSuccess(PaginatedResponse<Task> tasks) {
                        toggleProgressFrame(false);
                        AssignmentActivity.this.tasks = tasks;
                        mTasksPagerAdapter.addNewTasks(tasks.data);
                        binding.viewPager.setCurrentItem(0);
                    }
                });
            } else
                showCompletedJobDialog();
        } else {
            binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem() + 1);
        }
    }

    private void showInstructionsDialog() {
        if (!assignment.getJob().getInstructions().isEmpty()) {
            if (instructions == null) {
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
                instructions = b.create();
            }
            instructions.show();
        }
    }

    public void setCurrentFragment(int pos) {
        //String tag = "android:switcher:" + binding.viewPager.getId() + ":" + binding.viewPager.getCurrentItem();
        //this.currentFragment = (TaskFragment) getSupportFragmentManager().findFragmentByTag(tag);
        if (pos == mTasksPagerAdapter.currentPosition)
            currentFragment = mTasksPagerAdapter.mCurrentFrag;
        setTitle(String.format(getString(R.string.job_activity_title), binding.viewPager.getCurrentItem() + 1, tasks.data.size()));
    }

    private void submitAnswer() {
        if (!currentFragment.imageLoaded) {
            gotoNextImage();
        } else {
            Answer answer = currentFragment.getAnswer();
            if (answer != null) {
                if (postAnswer(answer)) {
                    gotoNextImage();
                }
            } else {
                Toast.makeText(this, R.string.skipping_image, Toast.LENGTH_SHORT).show();
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
            Intent intent = new Intent(AssignmentActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_lock_slider) {
            if (binding.viewPager.isLocked()) {
                item.setIcon(R.drawable.ic_unlock);
                binding.viewPager.setLocked(false);
            } else {
                item.setIcon(R.drawable.ic_lock);
                binding.viewPager.setLocked(true);
            }
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

        //if (job != null) job.release();
        //ImageDbHandler.getInstance().release();
        super.onDestroy();
    }

    public boolean postAnswer(Answer answer) {
        if (answer.getData() != null && !answer.getData().isEmpty()) {
            final Long assignmentId = answer.getAssignmentId();

            answer.setSubmittedAt();
            Call<Answer> call = Api.getInstance().endpoints.postAnswer(assignmentId, answer);
            call.enqueue(new Callback<Answer>() {
                @Override
                public void onResponse(Call<Answer> call, Response<Answer> response) {

                }

                @Override
                public void onFailure(Call<Answer> call, Throwable t) {

                }
            });
            return true;
        }
        return false;
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
                task.setException((IOException) t);
            }
        });

        return task.getTask();
    }
}