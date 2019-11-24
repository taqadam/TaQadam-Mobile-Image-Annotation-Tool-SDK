
package com.recoded.taqadam.objects;

import android.net.Uri;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.recoded.taqadam.models.Answer;
import com.recoded.taqadam.models.Api.Api;
import com.recoded.taqadam.models.Model;

import javax.annotation.Generated;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class Task extends Model {
    @Expose
    private String fileName;
    @Expose
    private String url;
    @Expose
    private Answer answer;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Uri getUrl() {
        return Uri.parse(url);
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setAnswer(Answer answer) {
        this.answer = answer;
    }

    public Answer getAnswer() {
        return answer;
    }

    public String getRealUrl() {
        return url;
    }
}
