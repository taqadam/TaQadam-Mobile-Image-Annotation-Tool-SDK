package com.recoded.taqadam;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.recoded.taqadam.models.Post;
import com.recoded.taqadam.models.auth.UserAuthHandler;
import com.recoded.taqadam.models.db.PostDbHandler;

public class PostActivity extends BaseActivity {

    private EditText editName;
    private EditText editDescription;
    Button postButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        editName = findViewById(R.id.Edit_Name);
        editDescription = findViewById(R.id.Edit_Description);
        postButton = findViewById(R.id.Button_clicked);


        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Post post = new Post();
                post.setTitle(editName.getText().toString());
                post.setBody(editDescription.getText().toString());
                post.setUid(UserAuthHandler.getInstance().getUid());
                PostDbHandler.getInstance().writePost(post);
                finish();
            }
        });

    }

}
