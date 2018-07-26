package com.recoded.taqadam.models.Api;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.recoded.taqadam.BuildConfig;
import com.recoded.taqadam.models.Auth;
import com.recoded.taqadam.models.Comment;
import com.recoded.taqadam.models.Post;
import com.recoded.taqadam.models.Profile;
import com.recoded.taqadam.models.Responses.AvatarResponse;
import com.recoded.taqadam.models.User;
import com.recoded.taqadam.models.auth.UserAuthHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Api {
    public static final String TAG = Api.class.getSimpleName();
    public static final String BASE = "https://taqadam.io/api/";
    public static final String LOGIN = "login";
    public static final String REGISTER = "register";
    public static final String REFRESH = "refresh";
    public static final String AVATARS = "avatars";
    public static final String LOGOUT = "logout";
    public static final String USERS = "users";
    public static final String ME = "me";
    public static final String ASSIGNMENTS = "assignments";
    public static final String TASKS = "tasks";
    public static final String ANSWERS = "answers";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String POSTS = "posts";
    public static final String COMMENTS = "comments";
    public static final String VERSIONS = "app_versions";

    private static Api instance = null;

    public static Api getInstance() {
        if(instance == null){
            initiate(UserAuthHandler.getInstance().getAuth());
        }
        return instance;
    }

    public static void initiate(Auth auth) {
        if (auth != null)
            instance = new Api(auth.getToken(), auth.getType());
        else instance = new Api(null, null);
    }

    private Retrofit api = null;
    public ApiEndpoints endpoints;

    public Api(final String token, final String token_type) {
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                Request.Builder b = request.newBuilder()
                        .addHeader("Accept", "application/json")
                        .addHeader("Content-Type", "application/json");
                if (token != null) {
                    b.addHeader("Authorization", token_type + " " + token);
                }

                Response response = chain.proceed(b.build());


                int responceCode = response.code();
                if (responceCode >= 400) {
                    String responseBody = response.body().string();
                    ApiError ex = new ApiError(responceCode, responseBody);
                    try {
                        JSONObject res = new JSONObject(responseBody);
                        String msg = res.getString("message");
                        ex.setMessage(msg);
                        if (msg.toLowerCase().contains("invalid parameters")) {
                            JSONObject errors = (JSONObject) res.get("errors");
                            ex = new InvalidException(responceCode, msg, errors.toString());
                        } else if (msg.toLowerCase().contains("unauthenticated")) {
                            UserAuthHandler.getInstance().refresh();
                            ex = new UnauthenticatedException(responceCode, msg);
                        } else if (msg.toLowerCase().contains("unauthorized")) {
                            ex = new UnauthenticatedException(responceCode, msg);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    throw ex;
                } else if (responceCode >= 200) {
                    //We want to modify the response object and bring the resource to main level for GSON to deserialize properly
                    MediaType contentType = response.body().contentType();
                    String responseBody = response.body().string();
                    String newRes = "";
                    List<String> requestPath = request.url().pathSegments();
                    try {
                        JSONObject res = new JSONObject(responseBody);
                        List<String> keys = new ArrayList<>();
                        if (res.has("success")) { //typical responses
                            res.remove("success");
                            Iterator<String> iterator = res.keys();
                            while (iterator.hasNext()) {
                                keys.add(iterator.next());
                            }
                            if (keys.size() == 1 && !keys.get(0).equalsIgnoreCase("message")) {
                                //get the resource object and put it on top
                                newRes = res.getJSONObject(keys.get(0)).toString();
                            } else {
                                //rare case would occur only on login and registration calls
                                //like login response contains token next to user object
                                newRes = res.toString();
                            }
                        } else if (res.has("data")) { //collection response no success
                            if (res.has("meta") || res.has("links")) { //check if paginated
                                //try to get resource name from request
                                //String resourceName = requestPath.get(requestPath.size() - 1);
                                //JSONObject paginatedResponse = new JSONObject();
                                //paginatedResponse.put("meta", res.optJSONObject("meta"));
                                //paginatedResponse.put("links", res.optJSONObject("links"));
                                //paginatedResponse.put(resourceName, res.getJSONArray("data"));

                                //newRes = new JSONObject().put("paginated_response", paginatedResponse).toString();
                                newRes = res.toString();
                            } else { //only data
                                newRes = res.getJSONArray("data").toString();
                            }
                        } else { //no success and no data and not response code < 400 means fault in the api we would log it.
                            newRes = res.toString();
                            Log.d(TAG, "encountered invalid success response: " + newRes);
                            Crashlytics.log(6, TAG, "encountered invalid success response: " + newRes);
                        }

                        ResponseBody body = ResponseBody.create(contentType, newRes);
                        return response.newBuilder().body(body).build();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                return response;
            }
        });


        api = new Retrofit.Builder()
                .baseUrl(BASE)
                .addConverterFactory(GsonConverterFactory.create(
                        new GsonBuilder()
                                .setDateFormat("yyyy-MM-dd")
                                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                                .create()))
                .client(client.build())
                .build();

        endpoints = api.create(ApiEndpoints.class);
    }

    public static Task<Uri> uploadImage(File img) {
        final TaskCompletionSource<Uri> task = new TaskCompletionSource<>();

        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), img);
        MultipartBody.Part avatar = MultipartBody.Part.createFormData("avatar", img.getName(), reqFile);
        Call<AvatarResponse> call = Api.getInstance().endpoints.uploadAvatar(avatar);
        call.enqueue(new Callback<AvatarResponse>() {
            @Override
            public void onResponse(Call<AvatarResponse> call, retrofit2.Response<AvatarResponse> response) {
                AvatarResponse res = response.body();
                task.setResult(Uri.parse(res.getUrl()));
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                if (t instanceof ApiError) {
                    task.setException((ApiError) t);
                } else {
                    Crashlytics.logException(t);
                    task.setException(new ApiError(500, "Unknown error occurred!"));
                }
            }
        });

        return task.getTask();
    }

    public static void submitFeedback(String feedback, String comment) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("feedback", feedback);
        payload.put("comment", comment);
        payload.put("app_version", BuildConfig.VERSION_CODE);
    }

    public static Task<User> updateProfile(Profile p, Boolean update) {
        final TaskCompletionSource<User> task = new TaskCompletionSource<>();

        Call<User> call;
        if (update) {
            call = Api.getInstance().endpoints.putProfile(p);
        } else {
            call = Api.getInstance().endpoints.postProfile(p);
        }
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull retrofit2.Response<User> response) {
                task.setResult(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                if (t instanceof ApiError) {
                    task.setException((ApiError) t);
                } else {
                    Crashlytics.logException(t);
                    task.setException(new ApiError(500, "Unknown error occurred!"));
                }
            }
        });

        return task.getTask();
    }


    public static void postPost(Post post) {
        //todo check
        Call<Post> call = Api.getInstance().endpoints.postPost(post);
        call.enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, retrofit2.Response<Post> response) {

            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {

            }
        });
    }

    public static void updatePost(Post post) {
        Call<Post> call = Api.getInstance().endpoints.updatePost(post.getId(), post);
        call.enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, retrofit2.Response<Post> response) {

            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {

            }
        });
    }

    public static void deletePost(Post post) {
        Call<Post> call = Api.getInstance().endpoints.deletePost(post.getId());
        call.enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, retrofit2.Response<Post> response) {

            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {

            }
        });
    }

    public static void postComment(Comment comment) {
        //todo check
        Call<Comment> call = Api.getInstance().endpoints.postComment(comment.getPostId(), comment);
        call.enqueue(new Callback<Comment>() {
            @Override
            public void onResponse(Call<Comment> call, retrofit2.Response<Comment> response) {

            }

            @Override
            public void onFailure(Call<Comment> call, Throwable t) {

            }
        });
    }

    public static void updateComment(Comment comment) {
        Call<Comment> call = Api.getInstance().endpoints.updateComment(comment.getPostId(), comment.getId(), comment);
        call.enqueue(new Callback<Comment>() {
            @Override
            public void onResponse(Call<Comment> call, retrofit2.Response<Comment> response) {

            }

            @Override
            public void onFailure(Call<Comment> call, Throwable t) {

            }
        });
    }

    public static void deleteComment(Comment comment) {
        Call<Comment> call = Api.getInstance().endpoints.deleteComment(comment.getPostId(), comment.getId());
        call.enqueue(new Callback<Comment>() {
            @Override
            public void onResponse(Call<Comment> call, retrofit2.Response<Comment> response) {

            }

            @Override
            public void onFailure(Call<Comment> call, Throwable t) {

            }
        });
    }


}
