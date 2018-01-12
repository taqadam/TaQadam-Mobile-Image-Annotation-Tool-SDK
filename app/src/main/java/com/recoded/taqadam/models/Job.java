package com.recoded.taqadam.models;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.recoded.taqadam.models.db.UserDbHandler;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by hp on 1/10/2018.
 */

public class Job {
    private String jobId;
    private Date dateCreated;
    private Date dateExpires;
    private int numberOfAttempts;
    private int successfulAttempts;
    private String description;
    private float taskReward;
    private List<String> tasks;

    public Job(String jobId,List<String>  tasks) {
        this.jobId = jobId;
        this.tasks = tasks;
    }

    public String getJobId() {
        return jobId;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public Date getDateExpires() {
        return dateExpires;
    }

    public int getNumberOfAttempts() {
        return numberOfAttempts;
    }

    public int getSuccessfulAttempts() {
        return successfulAttempts;
    }

    public String getDescription() {
        return description;
    }

    public float getTaskReward() {
        return taskReward;
    }

    public List<String> getTasks() {
        return tasks;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public void setDateExpires(Date dateExpires) {
        this.dateExpires = dateExpires;
    }

    public void setNumberOfAttempts(int numberOfAttempts) {
        this.numberOfAttempts = numberOfAttempts;
    }

    public void setSuccessfulAttempts(int successfulAttempts) {
        this.successfulAttempts = successfulAttempts;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTaskReward(float taskReward) {
        this.taskReward = taskReward;
    }

    public void setTasks(List<String> tasks) {
        this.tasks = tasks;
    }

    public static Job fromMap(HashMap map){
        String id="";
        List<String> tasks=new List<String>() {
            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean contains(Object o) {
                return false;
            }

            @NonNull
            @Override
            public Iterator<String> iterator() {
                return null;
            }

            @NonNull
            @Override
            public Object[] toArray() {
                return new Object[0];
            }

            @NonNull
            @Override
            public <T> T[] toArray(@NonNull T[] ts) {
                return null;
            }

            @Override
            public boolean add(String s) {
                return false;
            }

            @Override
            public boolean remove(Object o) {
                return false;
            }

            @Override
            public boolean containsAll(@NonNull Collection<?> collection) {
                return false;
            }

            @Override
            public boolean addAll(@NonNull Collection<? extends String> collection) {
                return false;
            }

            @Override
            public boolean addAll(int i, @NonNull Collection<? extends String> collection) {
                return false;
            }

            @Override
            public boolean removeAll(@NonNull Collection<?> collection) {
                return false;
            }

            @Override
            public boolean retainAll(@NonNull Collection<?> collection) {
                return false;
            }

            @Override
            public void clear() {

            }

            @Override
            public String get(int i) {
                return null;
            }

            @Override
            public String set(int i, String s) {
                return null;
            }

            @Override
            public void add(int i, String s) {

            }

            @Override
            public String remove(int i) {
                return null;
            }

            @Override
            public int indexOf(Object o) {
                return 0;
            }

            @Override
            public int lastIndexOf(Object o) {
                return 0;
            }

            @NonNull
            @Override
            public ListIterator<String> listIterator() {
                return null;
            }

            @NonNull
            @Override
            public ListIterator<String> listIterator(int i) {
                return null;
            }

            @NonNull
            @Override
            public List<String> subList(int i, int i1) {
                return null;
            }
        };
        Job job=new Job(id,tasks);
        job.jobId = (String) map.get("");
        job.dateCreated = (Date) map.get(1996);
        job.dateExpires = (Date) map.get(1997);
        job.description = (String) map.get("");
       job.numberOfAttempts=(int)map.get(2);
        job.successfulAttempts=(int)map.get(3);
        job.taskReward=(float)map.get(1.2);
        job.tasks=(List )map.get("");

        return job;
    }
}
