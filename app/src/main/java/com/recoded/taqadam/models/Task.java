package com.recoded.taqadam.models;

import android.support.annotation.NonNull;

import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by HP PC on 12/17/2017.
 */

public class Task {
    private String taskId;
    private String jobId;
    private Date dateCreated;
    private Date dateExpires;
    private URL taskImage;
    private List<String>options;
    private String attemptedBy;
    private String completedBy;
    private String Type;
    private String title;
    private String description;

    public Task(String type, String title, String description) {
        Type = type;
        this.title = title;
        this.description = description;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTaskId() {
        return taskId;
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

    public URL getTaskImage() {
        return taskImage;
    }

    public List<String> getOptions() {
        return options;
    }

    public String getAttemptedBy() {
        return attemptedBy;
    }

    public String getCompletedBy() {
        return completedBy;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
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

    public void setTaskImage(URL taskImage) {
        this.taskImage = taskImage;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public void setAttemptedBy(String attemptedBy) {
        this.attemptedBy = attemptedBy;
    }

    public void setCompletedBy(String completedBy) {
        this.completedBy = completedBy;
    }
    public static Task fromMap(HashMap map){
        String id="";
        List<String> options=new List<String>() {
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
        Task task=new Task("","","");
        task.jobId = (String) map.get("");
        task.dateCreated = (Date) map.get("");
        task.dateExpires = (Date) map.get("");
        task.description = (String) map.get("");
        task.attemptedBy=(String) map.get("");
        task.completedBy=(String) map.get("");
        task.options=(List<String>) map.get("");
        task.taskImage=(URL ) map.get("");
        task.taskId=(String) map.get("");
        task.title=(String)map.get("");
        task.Type=(String)map.get("");



        return task;
    }
}

