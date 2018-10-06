
package com.recoded.taqadam.models;

import com.google.gson.annotations.Expose;
import com.recoded.taqadam.R;

import javax.annotation.Generated;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class Service extends Model {
    @Expose
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Services getTypeOfService() {
        for (Services service : Services.values()) {
            for (String wildcard : service.getWildCards()) {
                if (name.toLowerCase().contains(wildcard))
                    return service;
            }
        }

        return null;
    }

    public enum Services {
        BBOX(R.drawable.ic_object_detection + "-detection"),
        CATEGORIZATION(R.drawable.ic_classification + "-validation,categorization"),
        CLASSIFICATION(R.drawable.ic_tagging + "-classification"),
        SEGMENTATION(R.drawable.ic_segmentation + "-semantic,segmentation");

        private final String[] wildcards;
        private final int icon;

        Services(String args) {
            String[] splitted = args.split("-");
            this.icon = Integer.parseInt(splitted[0]);
            this.wildcards = splitted[1].split(",");
        }

        public String[] getWildCards() {
            return wildcards;
        }
        public int getDrawable(){
            return icon;
        }
    }
}
