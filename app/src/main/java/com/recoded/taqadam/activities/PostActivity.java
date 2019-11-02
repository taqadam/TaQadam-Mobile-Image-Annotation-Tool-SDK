package com.recoded.taqadam.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.recoded.taqadam.R;
import com.recoded.taqadam.models.Api.Api;
import com.recoded.taqadam.models.Post;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostActivity extends BaseActivity {

    private EditText editName;
    private EditText editDescription;
    Button postButton;

    private boolean editMode = false;
    private Post editPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (getIntent().getLongExtra("post_id", -1) != -1) {
            editMode = true;
            loadPostData();
        }

        setTitle(editMode ? getString(R.string.edit_post) : getString(R.string.write_post));

        editName = findViewById(R.id.Edit_Name);
        editDescription = findViewById(R.id.Edit_Description);
        postButton = findViewById(R.id.Button_clicked);


        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = editName.getText().toString().trim();
                String body = editDescription.getText().toString().trim();
                if (title.length() <= 5 || body.length() <= 10) {
                    Toast.makeText(PostActivity.this, R.string.too_short_post, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (editMode && editPost != null) {
                    editPost.setTitle(title);
                    editPost.setBody(body);
                    Api.updatePost(editPost);
                    Intent i = new Intent(PostActivity.this, PostViewerActivity.class);
                    i.putExtra("post_id", editPost.getId());
                    startActivity(i);
                    finish();
                } else {
                    Post post = new Post();
                    post.setTitle(title);
                    post.setBody(body);
                    Api.postPost(post);
                    finish();
                }
            }
        });
    }

    private void loadPostData() {
        final View v = findViewById(R.id.progress_bar);
        v.setVisibility(View.VISIBLE);
        Long post_id = getIntent().getLongExtra("post_id", -1);
        Call<Post> call = Api.getInstance().endpoints.getPost(post_id);
        call.enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                editPost = response.body();
                editName.setText(editPost.getTitle());
                editDescription.setText(editPost.getBody());
                v.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                Toast.makeText(PostActivity.this, getString(R.string.error), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
