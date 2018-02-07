package com.recoded.taqadam;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnSuccessListener;
import com.recoded.taqadam.databinding.ActivityPostsFeedBinding;
import com.recoded.taqadam.databinding.PostItemBinding;
import com.recoded.taqadam.models.Post;
import com.recoded.taqadam.models.db.PostDbHandler;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

public class PostsFeedActivity extends BaseActivity {
    private ActivityPostsFeedBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_posts_feed);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.discussions);

        binding.postsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        PostDbHandler.getInstance().getRecentPostsTask().addOnSuccessListener(new OnSuccessListener<List<Post>>() {
            @Override
            public void onSuccess(List<Post> posts) {
                PostListAdapter postListAdapter = new PostListAdapter(posts);
                binding.postsRecyclerView.setAdapter(postListAdapter);
            }
        });

        binding.fabAddPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PostsFeedActivity.this, PostActivity.class));
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

    public class PostListAdapter extends RecyclerView.Adapter<PostListAdapter.ViewHolder> {


        private List<Post> listItems;

        public PostListAdapter(final List<Post> listItems) {
            this.listItems = listItems;
            Collections.sort(listItems);

            PostDbHandler.getInstance().setPostsListener(new PostDbHandler.OnPostsChangedListener() {
                @Override
                public void onPostsChanged(List<Post> posts) {
                    listItems.clear();
                    listItems.addAll(posts);
                    Collections.sort(listItems);
                    notifyDataSetChanged();
                }
            });
            setHasStableIds(true);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public long getItemId(int position) {
            return listItems.get(position).getPostTime();
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final Post post = listItems.get(position);

            holder.binding.setPost(post);
            Picasso.with(PostsFeedActivity.this)
                    .load(post.getAuthorImage())
                    .resizeDimen(R.dimen.discuss_img_dim, R.dimen.discuss_img_dim)
                    .centerCrop()
                    .placeholder(R.drawable.no_image)
                    .into(holder.binding.ivUser);
            holder.binding.tvComments.setText(String.format(getString(R.string.comments_holder), post.getNoOfComments()));
            holder.binding.tvComments.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(PostsFeedActivity.this, PostViewerActivity.class);
                    i.putExtra("post_id", post.getId());
                    startActivity(i);
                }
            });

            holder.binding.tvTimestamp.setText(getTimestamp(post.getPostTime()));

            if (post.getBody().length() > 150) {
                holder.binding.bReadMore.setVisibility(View.VISIBLE);
                holder.binding.bReadMore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(PostsFeedActivity.this, PostViewerActivity.class);
                        i.putExtra("post_id", post.getId());
                        startActivity(i);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return listItems.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            PostItemBinding binding;

            public ViewHolder(View itemView) {
                super(itemView);
                binding = DataBindingUtil.bind(itemView);
            }
        }
    }
}
