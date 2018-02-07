package com.recoded.taqadam;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewSwitcher;

import com.google.android.gms.tasks.OnSuccessListener;
import com.recoded.taqadam.databinding.ActivityPostViewerBinding;
import com.recoded.taqadam.databinding.CommentItemBinding;
import com.recoded.taqadam.models.Comment;
import com.recoded.taqadam.models.Post;
import com.recoded.taqadam.models.auth.UserAuthHandler;
import com.recoded.taqadam.models.db.PostDbHandler;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PostViewerActivity extends BaseActivity {

    ActivityPostViewerBinding binding;
    CommentsAdapter adapter;
    Post post;

    PopupMenu postPopup;
    private PopupMenu.OnMenuItemClickListener menuClickListener;
    private PopupMenu.OnMenuItemClickListener commentMenuClickListener;

    CommentsAdapter.ViewHolder clickedViewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_post_viewer);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String postId = getIntent().getStringExtra("post_id");
        if (postId == null) finish();

        PostDbHandler.getInstance().getPost(postId).addOnSuccessListener(this, new OnSuccessListener<Post>() {
            @Override
            public void onSuccess(Post postItem) {
                if (postItem == null) {
                    finish();
                    return;
                }
                post = postItem;
                setupViews();
                setupCommentsViewer();
            }
        });

        binding.btSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (post != null && binding.etComment.getText().toString().trim().length() > 0) {
                    Comment comm = new Comment();
                    comm.setPostId(post.getId());
                    comm.setBody(binding.etComment.getText().toString().trim());
                    adapter.addComment(PostDbHandler.getInstance().writeComment(comm));
                    binding.etComment.setText("");
                }
            }
        });
    }

    private void setupViews() {
        binding.setPost(post);
        setTitle(post.getTitle());
        Picasso.with(this)
                .load(post.getAuthorImage())
                .resizeDimen(R.dimen.discuss_img_dim, R.dimen.discuss_img_dim)
                .centerCrop()
                .placeholder(R.drawable.no_image)
                .into(binding.ivUser);

        binding.tvTimestamp.setText(getTimestamp(post.getPostTime()));
        binding.progressBar.setVisibility(View.GONE);

        menuClickListener = new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_edit:
                        Intent i = new Intent(PostViewerActivity.this, PostActivity.class);
                        i.putExtra("post_id", post.getId());
                        startActivity(i);
                        finish();
                        return true;
                    case R.id.action_delete:
                        AlertDialog.Builder ab = new AlertDialog.Builder(PostViewerActivity.this);
                        ab.setTitle(R.string.delete_post);
                        ab.setMessage(R.string.delete_post_confirmation);
                        ab.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        ab.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                PostDbHandler.getInstance().deletePost(post);
                                dialog.dismiss();
                                finish();
                            }
                        });
                        ab.create().show();
                        return true;
                    default:
                        return false;
                }
            }
        };

        commentMenuClickListener = new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_edit:
                        clickedViewHolder.binding.vsCommentEditor.showNext();
                        //clickedViewHolder.binding.commentTools.setVisibility(View.VISIBLE);
                        adapter.disableEdits = true;
                        return true;
                    case R.id.action_delete:
                        AlertDialog.Builder ab = new AlertDialog.Builder(PostViewerActivity.this);
                        ab.setTitle(R.string.delete_comment);
                        ab.setMessage(R.string.delete_comment_confirmation);
                        ab.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        ab.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int pos = clickedViewHolder.getAdapterPosition();
                                PostDbHandler.getInstance().deleteComment(adapter.comments.get(pos));
                                adapter.comments.remove(pos);
                                adapter.notifyDataSetChanged();
                                dialog.dismiss();
                                clickedViewHolder = null;
                            }
                        });
                        ab.create().show();
                        return true;
                    default:
                        clickedViewHolder = null;
                        return false;
                }
            }
        };
    }

    private void setupCommentsViewer() {
        binding.rvComments.setLayoutManager(new LinearLayoutManager(PostViewerActivity.this));
        adapter = new CommentsAdapter(new ArrayList<Comment>());
        adapter.setHasStableIds(true);
        binding.rvComments.setAdapter(adapter);
        PostDbHandler.getInstance().getComments(post.getId()).addOnSuccessListener(this, new OnSuccessListener<List<Comment>>() {
            @Override
            public void onSuccess(List<Comment> comments) {
                adapter.comments.addAll(comments);
                Collections.sort(adapter.comments);
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (adapter.disableEdits) {
            clickedViewHolder.binding.btDiscardComment.performClick();
            return;
        }
        super.onBackPressed();
        finish();
    }

    public void showPopUp(View view) {
        if (view.getId() == R.id.bt_post_editor) {
            if (postPopup == null) {
                postPopup = new PopupMenu(this, view);
                postPopup.inflate(R.menu.posts_actions);
                if (!post.getUid().equals(UserAuthHandler.getInstance().getUid())) {
                    postPopup.getMenu().findItem(R.id.action_delete).setVisible(false);
                    postPopup.getMenu().findItem(R.id.action_edit).setVisible(false);
                }
                postPopup.setOnMenuItemClickListener(menuClickListener);
            }
            postPopup.show();
        } else {
            Comment c = adapter.comments.get(clickedViewHolder.getAdapterPosition());
            PopupMenu commentPopup = new PopupMenu(this, view);
            commentPopup.inflate(R.menu.posts_actions);
            if (!c.getUid().equals(UserAuthHandler.getInstance().getUid())) {
                commentPopup.getMenu().findItem(R.id.action_delete).setVisible(false);
                commentPopup.getMenu().findItem(R.id.action_edit).setVisible(false);
            }
            commentPopup.setOnMenuItemClickListener(commentMenuClickListener);

            commentPopup.show();
        }
    }

    public class CommentsAdapter extends RecyclerView.Adapter<PostViewerActivity.CommentsAdapter.ViewHolder> {


        private List<Comment> comments;
        public boolean disableEdits = false;

        public CommentsAdapter(final List<Comment> listItems) {
            this.comments = listItems;
            Collections.sort(listItems);
            setHasStableIds(true);
        }

        @Override
        public PostViewerActivity.CommentsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
            return new PostViewerActivity.CommentsAdapter.ViewHolder(view);
        }

        @Override
        public long getItemId(int position) {
            return comments.get(position).getCommentTime();
        }

        @Override
        public void onBindViewHolder(final PostViewerActivity.CommentsAdapter.ViewHolder holder, int position) {
            final Comment comm = comments.get(position);

            holder.binding.setComment(comm);
            Picasso.with(PostViewerActivity.this)
                    .load(comm.getAuthorImage())
                    .resizeDimen(R.dimen.discuss_img_dim, R.dimen.discuss_img_dim)
                    .centerCrop()
                    .placeholder(R.drawable.no_image)
                    .into(holder.binding.ivUser);

            holder.binding.tvTimestamp.setText(getTimestamp(comm.getCommentTime()));

            holder.binding.commentArea.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!disableEdits) {
                        clickedViewHolder = holder;
                        showPopUp(v);
                        return true;
                    }
                    return false;
                }
            });

            holder.binding.btUpdateComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = clickedViewHolder.getAdapterPosition();
                    String body = clickedViewHolder.binding.etDesc.getText().toString().trim();
                    ViewSwitcher vs = clickedViewHolder.binding.vsCommentEditor;
                    if (body.length() > 0) {
                        comments.get(pos).setBody(body);
                        PostDbHandler.getInstance().updateComment(comments.get(pos));
                        vs.showPrevious();
                        //clickedViewHolder.binding.commentTools.setVisibility(View.INVISIBLE);
                        disableEdits = false;
                        notifyDataSetChanged();
                    } else {
                        vs.showPrevious();
                        //clickedViewHolder.binding.commentTools.setVisibility(View.INVISIBLE);
                        disableEdits = false;
                    }
                    clickedViewHolder = null;
                }
            });

            holder.binding.btDiscardComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickedViewHolder.binding.vsCommentEditor.showPrevious();
                    //clickedViewHolder.binding.commentTools.setVisibility(View.INVISIBLE);
                    disableEdits = false;
                    clickedViewHolder = null;
                }
            });

            holder.binding.etDesc.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!s.toString().equals(comm.getBody()) && s.toString().trim().length() != 0) {
                        holder.binding.btUpdateComment.setVisibility(View.VISIBLE);
                        holder.binding.btDiscardComment.setVisibility(View.GONE);
                    } else {
                        holder.binding.btDiscardComment.setVisibility(View.VISIBLE);
                        holder.binding.btUpdateComment.setVisibility(View.GONE);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        public void addComment(Comment c) {
            comments.add(c);
            Collections.sort(comments);
            notifyDataSetChanged();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            CommentItemBinding binding;

            public ViewHolder(View itemView) {
                super(itemView);
                binding = DataBindingUtil.bind(itemView);
            }
        }
    }
}
