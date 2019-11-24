
package com.recoded.taqadam.objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.recoded.taqadam.models.Model;

import javax.annotation.Generated;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class Type extends Model {
    @Expose
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
