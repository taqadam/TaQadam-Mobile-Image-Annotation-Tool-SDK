package com.recoded.taqadam;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.recoded.taqadam.models.Post;
import com.recoded.taqadam.models.db.PostDbHandler;

import java.util.List;

public class FragmentDiscuss extends Fragment {
    private RecyclerView mRecyclerView;

    public FragmentDiscuss() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fragment_discuss, container, false);

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

        public PostListAdapter(List<Post> listItems) {
            this.listItems = listItems;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Post listItem = listItems.get(position);

            holder.textViewTitle.setText(listItem.getTitle());
            holder.textViewDecsription.setText(listItem.getBody());
        }

        @Override
        public int getItemCount() {
            return listItems.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView textViewTitle;
            public TextView textViewDecsription;

            public ViewHolder(View itemView) {
                super(itemView);

                textViewTitle = itemView.findViewById(R.id.text_Title);
                textViewDecsription = itemView.findViewById(R.id.text_Descrption);
            }
        }
    }
}
