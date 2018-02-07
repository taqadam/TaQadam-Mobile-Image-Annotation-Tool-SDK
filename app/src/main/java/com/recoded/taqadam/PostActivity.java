package com.recoded.taqadam;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.recoded.taqadam.models.Post;
import com.recoded.taqadam.models.db.PostDbHandler;

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
        if (getIntent().getStringExtra("post_id") != null) {
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
                    PostDbHandler.getInstance().updatePost(editPost);
                    Intent i = new Intent(PostActivity.this, PostViewerActivity.class);
                    i.putExtra("post_id", editPost.getId());
                    startActivity(i);
                    finish();
                } else {
                    Post post = new Post();
                    post.setTitle(title);
                    post.setBody(body);
                    PostDbHandler.getInstance().writePost(post);
                    finish();
                }
            }
        });
    }

    private void loadPostData() {
        final View v = findViewById(R.id.progress_bar);
        v.setVisibility(View.VISIBLE);
        PostDbHandler.getInstance().getPost(getIntent().getStringExtra("post_id"))
                .addOnSuccessListener(this, new OnSuccessListener<Post>() {
                    @Override
                    public void onSuccess(Post post) {
                        editPost = post;
                        editName.setText(post.getTitle());
                        editDescription.setText(post.getBody());
                        v.setVisibility(View.GONE);
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
