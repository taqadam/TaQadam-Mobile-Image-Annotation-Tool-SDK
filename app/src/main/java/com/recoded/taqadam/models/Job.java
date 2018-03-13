package com.recoded.taqadam.models;

import android.net.Uri;

import com.recoded.taqadam.models.auth.UserAuthHandler;
import com.recoded.taqadam.models.db.ImageDbHandler;
import com.recoded.taqadam.models.db.JobDbHandler;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by hp on 1/10/2018.
 */

public class Job {
    public static final String CATEGORIZATION = "categorization";
    public static final String BBOX = "bbox";

    private String jobId;
    private Date dateCreated;
    private Date dateExpires;
    private String type;
    private int noOfImpressions;
    private int imagesCount;
    private String tasksType;
    private List<String> options;
    private String instructions;
    private String description;
    private float taskReward;
    private String jobName;
    private String company;

    private List<Image> imagesList;

    public Job(String jobId) {
        this.jobId = jobId;
        options = new ArrayList<>();
        imagesList = new ArrayList<>();
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

    public List<String> getOptions() {
        return options;
    }

    public String getJobName() {
        return jobName;
    }

    public String getCompany() {
        return company;
    }

    public int getImagesCount() {
        return imagesCount;
    }

    public int getNoOfImpressions() {
        return noOfImpressions;
    }

    public String getTasksType() {
        return tasksType;
    }

    public String getInstructions() {
        return instructions;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public float getTaskReward() {
        return taskReward;
    }

    public List<Image> getImagesList() {
        return imagesList;
    }

    public void removeeImage(String imageId) {
        Iterator<Image> i = imagesList.iterator();
        while (i.hasNext()) {
            Image image = i.next();
            if (image.id.equals(imageId))
                i.remove();
        }
    }

    public void setImages(Map<String, Map<String, Object>> val) {
        imagesList.clear();
        for (String k : val.keySet()) {
            boolean done = false, skipped = false;
            int skipCount = 0;
            if (val.get(k).containsKey(ImageDbHandler.COMPLETED_BY)) {
                Map<String, Boolean> uids = (Map<String, Boolean>) val.get(k).get(ImageDbHandler.COMPLETED_BY);
                if (uids.containsKey(UserAuthHandler.getInstance().getUid())) {
                    done = true;
                } else if (uids.size() >= noOfImpressions) {
                    done = true;
                }
            }
            if (val.get(k).containsKey(ImageDbHandler.SKIPPED_BY)) {
                Map<String, Boolean> skippedBy = (Map<String, Boolean>) val.get(k).get(ImageDbHandler.SKIPPED_BY);
                skipCount = skippedBy.size();
                if (skippedBy.containsKey(UserAuthHandler.getInstance().getUid())) {
                    skipped = true;
                }
            }
            if (!done) {
                Image im = new Image();
                im.id = k;
                im.path = Uri.parse((String) val.get(k).get(ImageDbHandler.TASK_IMAGE));
                im.skipCount = skipCount;
                im.skipped = skipped;
                imagesList.add(im);
            }
        }

        Collections.sort(imagesList, new Comparator<Image>() {
            @Override
            public int compare(Image o1, Image o2) {
                int b1 = o1.skipped ? 1 : 0;
                int b2 = o2.skipped ? 1 : 0;
                return b1 - b2;
            }
        });
    }

    public Job fromMap(Map<String, Object> map) {
        for (String k : map.keySet()) {
            switch (k) {
                case JobDbHandler.DATE_CREATED:
                    if (dateCreated == null) {
                        dateCreated = new Date((long) map.get(k) * 1000);
                    } else {
                        dateCreated.setTime((long) map.get(k) * 1000);
                    }
                    break;
                case JobDbHandler.DATE_EXPIRES:
                    if (dateExpires == null) {
                        dateExpires = new Date((long) map.get(k) * 1000);
                    } else {
                        dateExpires.setTime((long) map.get(k) * 1000);
                    }
                    break;
                case JobDbHandler.TYPE:
                    type = (String) map.get(k);
                    break;
                case JobDbHandler.TASKS_TYPE:
                    tasksType = (String) map.get(k);
                    break;
                case JobDbHandler.IMPRESSIONS:
                    noOfImpressions = ((Long) map.get(k)).intValue();
                    break;
                case JobDbHandler.COUNT:
                    imagesCount = ((Long) map.get(k)).intValue();
                    break;
                case JobDbHandler.INSTRUCTIONS:
                    instructions = (String) map.get(k);
                    break;
                case JobDbHandler.JOB_NAME:
                    jobName = (String) map.get(k);
                    break;
                case JobDbHandler.DESC:
                    description = (String) map.get(k);
                    break;
                case JobDbHandler.COMPANY:
                    company = (String) map.get(k);
                    break;

                case JobDbHandler.OPTIONS:
                    options.clear();
                    options.addAll((List<String>) map.get(k));
                    break;

                case JobDbHandler.TASK_REWARD:
                    if (map.get(k) instanceof Double) {
                        double tReward = (double) map.get(k);
                        BigDecimal bd = new BigDecimal(tReward);
                        bd = bd.setScale(2, RoundingMode.DOWN);
                        taskReward = bd.floatValue();
                    } else {
                        taskReward = ((long) map.get(k)) * 1f;
                    }
                    break;
            }
        }
        return this;
    }

    public void release() {
        imagesList.clear();
    }
}
