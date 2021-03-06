package com.recoded.taqadam.models.Api;

import com.recoded.taqadam.models.Answer;
import com.recoded.taqadam.models.AppVersion;
import com.recoded.taqadam.objects.Assignment;
import com.recoded.taqadam.objects.Auth;
import com.recoded.taqadam.models.Comment;
import com.recoded.taqadam.models.Post;
import com.recoded.taqadam.models.Profile;
import com.recoded.taqadam.models.Responses.AvatarResponse;
import com.recoded.taqadam.models.Responses.PaginatedResponse;
import com.recoded.taqadam.models.Responses.SuccessResponse;
import com.recoded.taqadam.objects.Task;
import com.recoded.taqadam.objects.User;
import com.recoded.taqadam.models.auth.Login;
import com.recoded.taqadam.models.auth.Register;
import com.recoded.taqadam.objects.WorkResult;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiEndpoints {

    //Login
    @POST(Api.LOGIN)
    Call<Auth> login(@Body Login login);

    //Register
    @POST(Api.REGISTER)
    Call<Auth> register(@Body Register register);

    //Refresh Token
    @POST(Api.REFRESH)
    Call<Auth> refresh();

    //Logout
    @POST(Api.LOGOUT)
    Call<SuccessResponse> logout();

    //assignments
    @GET(Api.ASSIGNMENTS)
    Call<List<Assignment>> getAssignments();

    @GET(Api.ASSIGNMENTS + "/{assignmentId}/task")
    Call<Task> getTask(@Path("assignmentId") Long assignmentId);

    //post answer
    @POST(Api.ASSIGNMENTS + "/{assignmentId}/answer")
    Call<SuccessResponse> postAnswer(@Path("assignmentId") Long assignmentId, @Body Answer answer);

    @GET("user/skip/{taskId}")
    Call<SuccessResponse> skipTask(@Path("taskId") Long taskId);

    @GET("user/validate/{projectId}/{taskId}")
    Call<SuccessResponse> validateTask(@Path("projectId") Long projectId, @Path("taskId") Long taskId);

    @GET("user/reject/{projectId}/{taskId}")
    Call<SuccessResponse> rejectTask(@Path("projectId") Long projectId, @Path("taskId") Long taskId);

    @GET("user/result")
    Call<List<WorkResult>> getWorkResult();

    //Post photo
    @Multipart
    @POST(Api.AVATARS)
    Call<AvatarResponse> uploadAvatar(@Part MultipartBody.Part avatar);

    //Post profile
    @POST(Api.ME)
    Call<User> postProfile(@Body Profile profile);

    //update profile
    @PUT(Api.ME)
    Call<User> putProfile(@Body Profile profile);



    //one assignment
    @GET(Api.ASSIGNMENTS + "/{assignmentId}")
    Call<Assignment> getAssignment(@Path("assignmentId") Long assignmentId);

    @GET(Api.ASSIGNMENTS + "/{assignmentId}/" + Api.TASKS)
    Call<List<Task>> getTasks(@Path("assignmentId") Long assignmentId);

    @GET(Api.ASSIGNMENTS + "/{assignmentId}/" + Api.TASKS)
    Call<PaginatedResponse<Task>> getTasksPaginated(@Path("assignmentId") Long assignmentId, @Query("page") Long page);



    //get all posts
    @GET(Api.POSTS)
    Call<List<Post>> getPosts();

    //get one post
    @GET(Api.POSTS + "/{postId}")
    Call<Post> getPost(@Path("postId") Long postId);

    //post new post
    @POST(Api.POSTS)
    Call<Post> postPost(@Body Post post);

    //delete post
    @DELETE(Api.POSTS + "/{postId}")
    Call<Post> deletePost(@Path("postId") Long postId);

    //update existing post
    @PUT(Api.POSTS + "/{postId}")
    Call<Post> updatePost(@Path("postId") Long postId, @Body Post post);

    //get post comments
    @GET(Api.POSTS + "/{postId}/" + Api.COMMENTS)
    Call<List<Comment>> getComments(@Path("postId") Long postId);

    //post new comment
    @POST(Api.POSTS + "/{postId}/" + Api.COMMENTS)
    Call<Comment> postComment(@Path("postId") Long postId, @Body Comment comment);

    //delete comment
    @DELETE(Api.POSTS + "/{postId}" + Api.COMMENTS + "/{commentId}")
    Call<Comment> deleteComment(@Path("postId") Long postId, @Path("commentId") Long commentId);

    //update comment
    @PUT(Api.POSTS + "/{postId}/" + Api.COMMENTS + "/{commentId}")
    Call<Comment> updateComment(@Path("postId") Long postId, @Path("commentId") Long commentId, @Body Comment comment);

    //get app versions
    @GET(Api.VERSIONS)
    Call<List<AppVersion>> getLatestVersions(@Query("code") Long code);
}
