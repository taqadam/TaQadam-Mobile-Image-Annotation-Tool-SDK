package com.recoded.taqadam;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.recoded.taqadam.models.Post;
import com.recoded.taqadam.models.db.PostDbHandler;
import com.recoded.taqadam.models.db.UserDbHandler;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FragmentDiscuss extends Fragment {
    private RecyclerView mRecyclerView;

    public FragmentDiscuss() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_discuss, container, false);

        mRecyclerView = view.findViewById(R.id.posts_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setHasFixedSize(true);

        PostDbHandler.getInstance().getRecentPostsTask().addOnSuccessListener(new OnSuccessListener<List<Post>>() {
            @Override
            public void onSuccess(List<Post> posts) {
                PostListAdapter postListAdapter = new PostListAdapter(posts);
                mRecyclerView.setAdapter(postListAdapter);
            }
        });

        view.findViewById(R.id.fab_add_post).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), PostActivity.class));
            }
        });

        return view;
    }

    public class PostListAdapter extends RecyclerView.Adapter<PostListAdapter.ViewHolder> {


        private List<Post> listItems;

        public PostListAdapter(final List<Post> listItems) {
            this.listItems = listItems;

            PostDbHandler.getInstance().setPostsListener(new PostDbHandler.OnPostsChangedListener() {
                @Override
                public void onPostsChanged(List<Post> posts) {
                    listItems.clear();
                    listItems.addAll(posts);
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
        public void onBindViewHolder(final ViewHolder holder, int position) {
            Post listItem = listItems.get(position);

            holder.tvTitle.setText(listItem.getTitle());
            holder.tvDesc.setText(listItem.getBody());
            holder.tvUser.setText(listItem.getAuthor());

            UserDbHandler.getInstance().fetchUserPicture(listItem.getUid()).addOnSuccessListener(new OnSuccessListener<String>() {
                @Override
                public void onSuccess(String s) {
                    Picasso.with(getContext()).load(s).into(holder.ivUser);
                }
            });
        }

        @Override
        public int getItemCount() {
            return listItems.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView tvTitle;
            public TextView tvDesc;
            public TextView tvUser;
            public ImageView ivUser;

            public ViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tv_title);
                tvDesc = itemView.findViewById(R.id.tv_desc);
                tvUser = itemView.findViewById(R.id.tv_full_name);
                ivUser = itemView.findViewById(R.id.iv_user);
            }
        }
    }
}
