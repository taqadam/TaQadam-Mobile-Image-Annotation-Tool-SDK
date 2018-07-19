
package com.recoded.taqadam.models;

import com.google.gson.annotations.Expose;

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
        BBOX("detection"),
        CATEGORIZATION("validation,categorization"),
        CLASSIFICATION("classification"),
        SEGMENTATION("semantic,segmentation");

        private final String[] wildcards;

        Services(String wildcards) {
            this.wildcards = wildcards.split(",");
        }

        public String[] getWildCards() {
            return wildcards;
        }
    }
}
