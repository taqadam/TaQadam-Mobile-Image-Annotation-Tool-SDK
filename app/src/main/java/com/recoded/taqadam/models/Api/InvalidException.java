package com.recoded.taqadam.models.Api;

import com.recoded.taqadam.models.Error;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class InvalidException extends ApiError {

    private Map<String, List<Error>> errors;

    public Map<String, List<Error>> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, List<Error>> errors) {
        this.errors = errors;
    }

    public InvalidException(int statusCode, String message) {
        super(statusCode, message);
    }

    public InvalidException(int statusCode, String message, String errors) {
        super(statusCode, message);

        try {
            Map<String, List<Error>> errorsMap = new HashMap<>();
            JSONObject e = new JSONObject(errors.trim());
            Iterator<String> keys = e.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (e.get(key) instanceof JSONArray) {
                    List<Error> keyErrors = new ArrayList<>();
                    JSONArray array = (JSONArray) e.get(key);
                    for (int i = 0; i < array.length(); i++) {
                        Error keyError = new Error(key, array.getString(i));
                        keyErrors.add(keyError);
                    }
                    errorsMap.put(key, keyErrors);
                }
            }

            this.setErrors(errorsMap);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }
}
