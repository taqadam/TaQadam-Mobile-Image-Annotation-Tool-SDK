package com.recoded.taqadam.models.auth;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.facebook.AccessToken;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.recoded.taqadam.models.Api.Api;
import com.recoded.taqadam.models.Api.ApiError;
import com.recoded.taqadam.objects.Auth;
import com.recoded.taqadam.models.Responses.SuccessResponse;
import com.recoded.taqadam.objects.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by wisam on Dec 21 17.
 */

public class UserAuthHandler {
    private static final String TAG = UserAuthHandler.class.getSimpleName();
    private static UserAuthHandler instance;

    private Auth auth;
    private Task<User> initTask;
    private SharedPreferences sharedPreferences;

    public static UserAuthHandler getInstance() {
        return instance;
    }

    public static void init(Context ctx) {
        instance = new UserAuthHandler(ctx);
    }

    private UserAuthHandler(Context ctx) {
        final TaskCompletionSource<User> initTask = new TaskCompletionSource<>();
        //try to get token from shared preference
        this.sharedPreferences = ctx.getSharedPreferences("auth", Context.MODE_PRIVATE);
        this.getToken();
        if (this.auth != null) {
            Api.initiate(auth);
            refresh().addOnSuccessListener(new OnSuccessListener<Auth>() {
                @Override
                public void onSuccess(Auth auth) {
                    UserAuthHandler.this.auth = auth;
                    Api.initiate(auth);
                    UserAuthHandler.this.saveToken(auth);
                    initTask.setResult(auth.getUser());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (((ApiError) e).getStatusCode() == 401) {
                        destroyToken();
                        auth = null;
                        Api.initiate(null);
                        initTask.setResult(null);
                    } else if (((ApiError) e).getStatusCode() == 503) {
                        initTask.setException(e);
                    } else {
                        Crashlytics.log(4, "ApiError", e.getMessage());
                        initTask.setException(e);
                    }
                }
            });
        } else {
            Api.initiate(null);
            initTask.setResult(null);
        }
        this.initTask = initTask.getTask();
    }

    public Task<User> login(String email, String password) {
        final TaskCompletionSource<User> task = new TaskCompletionSource<>();
        Call<Auth> call = Api.getInstance().endpoints.login(new Login(email, password));
        call.enqueue(new Callback<Auth>() {
            @Override
            public void onResponse(@NonNull Call<Auth> call, @NonNull retrofit2.Response<Auth> response) {
                Auth auth = response.body();
                if (auth != null) {
                    UserAuthHandler.this.auth = auth;
                    Api.initiate(auth);
                    UserAuthHandler.this.saveToken(auth);
                    task.setResult(auth.getUser());
                } else {
                    task.setException(new ApiError(response.code(), "Unable to log in with provided credentials"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Auth> call, @NonNull Throwable t) {
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

    public Task<User> login(AccessToken token) {
        return null;
    }

    public Task<Auth> refresh() {
        final TaskCompletionSource<Auth> task = new TaskCompletionSource<>();

        Call<Auth> call = Api.getInstance().endpoints.refresh();
        call.enqueue(new Callback<Auth>() {
            @Override
            public void onResponse(@NonNull Call<Auth> call, @NonNull Response<Auth> response) {
                Auth auth = response.body();
                if (auth != null) {
                    UserAuthHandler.this.auth = auth;
                    task.setResult(auth);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Auth> call, @NonNull Throwable t) {
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

    public Task<User> register(String name, String email, String password) {
        final TaskCompletionSource<User> task = new TaskCompletionSource<>();

        Call<Auth> call = Api.getInstance().endpoints.register(new Register(name, email, password));
        call.enqueue(new Callback<Auth>() {
            @Override
            public void onResponse(@NonNull Call<Auth> call, @NonNull retrofit2.Response<Auth> response) {
                int code = response.code();
                if (code == 201) {
                    Auth auth = response.body();
                    UserAuthHandler.this.auth = auth;
                    Api.initiate(auth);
                    UserAuthHandler.this.saveToken(auth);
                    task.setResult(auth.getUser());
                } else {
                    task.setException(new ApiError(code, "Email or username exist"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Auth> call, @NonNull Throwable t) {
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

    public Task<SuccessResponse> logout() {
        final TaskCompletionSource<SuccessResponse> task = new TaskCompletionSource<>();

        Call<SuccessResponse> call = Api.getInstance().endpoints.logout();
        call.enqueue(new Callback<SuccessResponse>() {
            @Override
            public void onResponse(Call<SuccessResponse> call, Response<SuccessResponse> response) {
                auth = null;
                destroyToken();
                task.setResult(response.body());
            }

            @Override
            public void onFailure(Call<SuccessResponse> call, Throwable t) {
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

    public User getCurrentUser() {
        return this.auth == null
                ? null
                : this.auth.getUser();
    }

    private void getToken() {
        if (this.sharedPreferences != null && this.sharedPreferences.contains("token")) {
            this.auth = new Auth();
            auth.setToken(this.sharedPreferences.getString("token", null));
        }
    }

    private void saveToken(Auth auth) {
        if (this.sharedPreferences != null) {
            SharedPreferences.Editor editor = this.sharedPreferences.edit();
            editor.putString("token", auth.getToken());
            editor.commit();
        }
    }

    private void destroyToken() {
        if (this.sharedPreferences != null) {
            SharedPreferences.Editor editor = this.sharedPreferences.edit();
            editor.remove("token");
            editor.commit();
        }
    }

    public Task<User> getInitTask() {
        return initTask;
    }

    public boolean shouldLogin() {
        return auth == null;
    }

    public void updateUser(User user) {
        this.auth.setUser(user);
    }

    public Auth getAuth() {
        if (auth == null) {
            getToken();
        }
        return auth;
    }
}