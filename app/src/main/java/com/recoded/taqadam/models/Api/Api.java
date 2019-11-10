package com.recoded.taqadam.models.Api;

import android.net.Uri;
import androidx.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.recoded.taqadam.BuildConfig;
import com.recoded.taqadam.objects.Auth;
import com.recoded.taqadam.models.Comment;
import com.recoded.taqadam.models.Post;
import com.recoded.taqadam.models.Profile;
import com.recoded.taqadam.models.Responses.AvatarResponse;
import com.recoded.taqadam.objects.User;
import com.recoded.taqadam.models.auth.UserAuthHandler;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Api {
    public static final String TAG = Api.class.getSimpleName();

    public static final String ROOT = "http://192.168.0.101:8000/";
//    public static final String ROOT = "http://104.248.207.70:8000/";

    public static final String MEDIA_ROOT = ROOT + "media/";

    public static final String BASE =  ROOT + "api/";
    public static final String LOGIN = "user/login";
    public static final String REGISTER = "user/mobile/register";
    public static final String REFRESH = "user/refresh";
    public static final String ASSIGNMENTS = "user/assignments";

    public static final String AVATARS = "avatars";
    public static final String LOGOUT = "user/logout";
    public static final String USERS = "users";
    public static final String ME = "me";
    public static final String TASKS = "tasks";
    public static final String ANSWERS = "answers";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String POSTS = "posts";
    public static final String COMMENTS = "comments";
    public static final String VERSIONS = "app_versions";

    private static Api instance = null;

    public static Api getInstance() {
        Auth auth = UserAuthHandler.getAuthOfClass();
        if(instance == null || auth == null){
            initiate(auth);
        }
        return instance;
    }

    public static void initiate(Auth auth) {
        if (auth != null)
            instance = new Api(auth.getToken());
        else instance = new Api(null);
    }

    private Retrofit api = null;
    public ApiEndpoints endpoints;

    public Api(final String token) {
        OkHttpClient.Builder client = new OkHttpClient.Builder();

        client.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                Request.Builder b = request.newBuilder()
                        .addHeader("Accept", "application/json")
                        .addHeader("Content-Type", "application/json");
                if (token != null) {
                    b.addHeader("Authorization", "Token " + token);
                }
                Response response = chain.proceed(b.build());
                return response;
            }
        });


        api = new Retrofit.Builder()
                .baseUrl(BASE)
                .addConverterFactory(GsonConverterFactory.create(
                        new GsonBuilder()
                                .setDateFormat("yyyy-MM-dd")
                                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                                .excludeFieldsWithoutExposeAnnotation()
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
