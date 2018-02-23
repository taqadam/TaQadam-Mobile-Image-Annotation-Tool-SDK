package com.recoded.taqadam;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;

import com.google.android.gms.tasks.OnSuccessListener;
import com.recoded.taqadam.models.Answer;
import com.recoded.taqadam.models.Job;
import com.recoded.taqadam.models.Task;
import com.recoded.taqadam.models.auth.UserAuthHandler;
import com.recoded.taqadam.models.db.JobDbHandler;
import com.recoded.taqadam.models.db.TaskDbHandler;

import java.util.Date;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

/**
 * Created by Ahmad Siafaddin on 12/26/2017.
 */

public class TaskFragment extends Fragment {
    private static final String TAG = TaskFragment.class.getSimpleName();

    protected Task mTask;
    protected ImageViewTouch taskImageView;
    protected Answer taskAnswer;

    public static TaskFragment newTask(String taskId) {
        Task t = TaskDbHandler.getInstance().getTask(taskId);
        Job j = JobDbHandler.getInstance().getJob(t.getJobId());
        if (j.getTasksType().equals(Task.BOUNDING_BOX) || j.getTasksType().equals(Task.BBOX)) { //We have two different values as of now
            TaskFragment frag = new BoundingBoxFragment();
            frag.setTask(t);
            return frag;
        } else {
            TaskFragment frag = new CategorizationFragment();
            frag.setTask(t);
            return frag;
        }
    }

    public TaskFragment() {
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("task_id", mTask.getTaskId());
        super.onSaveInstanceState(outState);
    }

    public void setTask(Task t) {
        mTask = t;
        if (t.answer == null) {
            //check if the task was attempted
            if (mTask.getAttemptedBy().containsKey(UserAuthHandler.getInstance().getUid())) {
                String answerId = mTask.getAttemptedBy().get(UserAuthHandler.getInstance().getUid());

                mTask.answer = new Answer(mTask.getTaskId(), answerId); //to not make new ids all the time

                TaskDbHandler.getInstance().getAnswer(mTask.getTaskId(), answerId).addOnSuccessListener(new OnSuccessListener<Answer>() {
                    @Override
                    public void onSuccess(Answer answer) {
                        mTask.answer = answer;
                        notifyFragmentForAnswer();
                    }
                });
            } else {
                taskAnswer = new Answer(mTask.getTaskId());
                taskAnswer.setAnswerStartTime(new Date());
                t.answer = taskAnswer;
            }
        } else {
            taskAnswer = t.answer;
            this.notifyFragmentForAnswer();
        }
    }

    protected int pxToDp(int px) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    protected int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public Answer getAnswer() {
        return null;
    }

    protected void notifyFragmentForAnswer() {
    }
}