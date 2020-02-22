/*
 *  Copyright 2017 Rozdoum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.discoveregypttourism.SocialComponents.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Toast;

import com.discoveregypttourism.R;
import com.discoveregypttourism.SocialComponents.activities.MainActivity;
import com.discoveregypttourism.SocialComponents.adapters.holders.LoadViewHolder;
import com.discoveregypttourism.SocialComponents.adapters.holders.PostViewHolder;
import com.discoveregypttourism.SocialComponents.controllers.LikeController;
import com.discoveregypttourism.SocialComponents.enums.ItemType;
import com.discoveregypttourism.SocialComponents.managers.PostManager;
import com.discoveregypttourism.SocialComponents.managers.listeners.OnPostListChangedListener;
import com.discoveregypttourism.SocialComponents.model.Post;
import com.discoveregypttourism.SocialComponents.model.PostListResult;
import com.discoveregypttourism.SocialComponents.utils.PreferencesUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Kristina on 10/31/16.
 */

public class PostsAdapter extends BasePostsAdapter {
    public static final String TAG = PostsAdapter.class.getSimpleName();

    private Callback callback;
    private boolean isLoading = false;
    private boolean isMoreDataAvailable = true;
    private long lastLoadedItemCreatedDate;
    private SwipeRefreshLayout swipeContainer;
    private MainActivity mainActivity;
    private Context context ;
    private String search;

    public PostsAdapter(final MainActivity activity, SwipeRefreshLayout swipeContainer) {
        super(activity);
        this.mainActivity = activity;
        this.swipeContainer = swipeContainer;
        initRefreshLayout();
        setHasStableIds(true);
    }

    public PostsAdapter(final Context homeFragment, SwipeRefreshLayout swipeContainer) {
        super(homeFragment);
        this.context = homeFragment;
        this.swipeContainer = swipeContainer;
        initRefreshLayout();
        setHasStableIds(true);
    }

    private void initRefreshLayout() {
        if (swipeContainer != null) {
            this.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    onRefreshAction();
                }
            });
        }
    }

    private void onRefreshAction() {
        if (activity != null) {
            if (activity.hasInternetConnection()) {
                loadFirstPage();
                cleanSelectedPostInformation();
            } else {
                swipeContainer.setRefreshing(false);
                mainActivity.showFloatButtonRelatedSnackBar(R.string.internet_connection_failed);
            }
        }
        else {
            if (hasInternetConnection(context)) {
                loadFirstPage();
                cleanSelectedPostInformation();
            } else {
                swipeContainer.setRefreshing(false);
                Toast.makeText(context, context.getResources().getString(R.string.internet_connection_failed), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == ItemType.ITEM.getTypeCode()) {
            return new PostViewHolder(inflater.inflate(R.layout.post_item_list_view, parent, false),
                    createOnClickListener());
        } else {
            return new LoadViewHolder(inflater.inflate(R.layout.loading_view, parent, false));
        }
    }

    private PostViewHolder.OnClickListener createOnClickListener() {
        return new PostViewHolder.OnClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                if (callback != null) {
                    selectedPostPosition = position;
                    callback.onItemClick(getItemByPosition(position), view);
                }
            }

            @Override
            public void onLikeClick(LikeController likeController, int position) {
                Post post = getItemByPosition(position);
                if (activity != null) {
                    likeController.handleLikeClickAction(activity, post);
                }
                else {
                    likeController.handleLikeClickAction(homeFragment, post);
                }
            }

            @Override
            public void onAuthorClick(int position, View view) {
                if (callback != null) {
                    callback.onAuthorClick(getItemByPosition(position).getAuthorId(), view);
                }
            }
        };
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading) {
            android.os.Handler mHandler = activity.getWindow().getDecorView().getHandler();
            mHandler.post(new Runnable() {
                public void run() {



                    if (activity != null){
                        if (activity.hasInternetConnection()) {
                            isLoading = true;
                            filtered.add(new Post(ItemType.LOAD));
                            notifyItemInserted(filtered.size());
                            loadNext(lastLoadedItemCreatedDate - 1);
                        } else {
                            mainActivity.showFloatButtonRelatedSnackBar(R.string.internet_connection_failed);
                        }
                    }//ch

                    else {
                        if (hasInternetConnection(context)) {
                            isLoading = true;
                            filtered.add(new Post(ItemType.LOAD));
                            notifyItemInserted(filtered.size());
                            loadNext(lastLoadedItemCreatedDate - 1);
                        } else {
                            Toast.makeText(context, context.getResources().getString(R.string.internet_connection_failed), Toast.LENGTH_SHORT).show();

                        }
                    }// ange adapter contents

                }
            });


        }

        if (getItemViewType(position) != ItemType.LOAD.getTypeCode()) {
            ((PostViewHolder)holder).bindData(filtered.get(position));
        }
    }

    private void addList(List<Post> list) {
        this.postList.addAll(list);
        this.filtered = postList;
        notifyDataSetChanged();
        isLoading = false;
    }

    private void addListTrend(List<Post> list) {
        Collections.sort(list);
        Collections.reverse(list);
        this.postList.addAll(list);
        this.filtered=postList;
        notifyDataSetChanged();
        isLoading = false;
    }

    public void loadFirstPageTrend() {
        loadNextTrend(0);
        if (activity != null){
            PostManager.getInstance(mainActivity.getApplicationContext()).clearNewPostsCounter();
        }
        else {
            PostManager.getInstance(context).clearNewPostsCounter();
        }
    }

    public void loadFirstPageTrend(String search) {
        search = search ;
        loadNextTrend(0);
        if (activity != null){
            PostManager.getInstance(mainActivity.getApplicationContext()).clearNewPostsCounter();
        }
        else {
            PostManager.getInstance(context).clearNewPostsCounter();
        }
    }

    public void loadFirstPage() {
        loadNext(0);
        if (activity != null){
            PostManager.getInstance(mainActivity.getApplicationContext()).clearNewPostsCounter();
        }
        else {
            PostManager.getInstance(context).clearNewPostsCounter();
        }
    }

    public void loadFirstPage(String search) {
        search = search;
        loadNext(0);
        if (activity != null){
            PostManager.getInstance(mainActivity.getApplicationContext()).clearNewPostsCounter();
        }
        else {
            PostManager.getInstance(context).clearNewPostsCounter();
        }
    }

    private void loadNext(final long nextItemCreatedDate) {

        if (activity!=null){
            if (!PreferencesUtil.isPostWasLoadedAtLeastOnce(mainActivity) && !activity.hasInternetConnection()) {
                mainActivity.showFloatButtonRelatedSnackBar(R.string.internet_connection_failed);
                hideProgress();
                callback.onListLoadingFinished();
                return;
            }
        }
        else {
            if (!PreferencesUtil.isPostWasLoadedAtLeastOnce(context) && !hasInternetConnection(context)) {
                Toast.makeText(context, context.getResources().getString(R.string.internet_connection_failed), Toast.LENGTH_SHORT).show();

                hideProgress();
                callback.onListLoadingFinished();
                return;
            }
        }



        OnPostListChangedListener<Post> onPostsDataChangedListener = new OnPostListChangedListener<Post>() {
            @Override
            public void onListChanged(PostListResult result) {
                lastLoadedItemCreatedDate = result.getLastItemCreatedDate();
                isMoreDataAvailable = result.isMoreDataAvailable();
                List<Post> list = result.getPosts();

                if (nextItemCreatedDate == 0) {
                    filtered.clear();
                    notifyDataSetChanged();
                    swipeContainer.setRefreshing(false);
                }

                hideProgress();

                if (!list.isEmpty()) {
                    addList(list);

                    if (activity!= null){
                        if (!PreferencesUtil.isPostWasLoadedAtLeastOnce(mainActivity)) {
                            PreferencesUtil.setPostWasLoadedAtLeastOnce(mainActivity, true);
                        }
                        else {
                            if (!PreferencesUtil.isPostWasLoadedAtLeastOnce(context)) {
                                PreferencesUtil.setPostWasLoadedAtLeastOnce(context, true);
                            }
                        }
                    }

                } else {
                    isLoading = false;
                }

                callback.onListLoadingFinished();
            }

            @Override
            public void onCanceled(String message) {
                callback.onCanceled(message);
            }
        };

        PostManager.getInstance(activity).getPostsList(onPostsDataChangedListener, nextItemCreatedDate);
    }

    private void loadNextTrend(final long nextItemCreatedDate) {

        if (activity!=null){
            if (!PreferencesUtil.isPostWasLoadedAtLeastOnce(mainActivity) && !activity.hasInternetConnection()) {
                mainActivity.showFloatButtonRelatedSnackBar(R.string.internet_connection_failed);
                hideProgress();
                callback.onListLoadingFinished();
                return;
            }
        }
        else {
            if (!PreferencesUtil.isPostWasLoadedAtLeastOnce(context) && !hasInternetConnection(context)) {
                Toast.makeText(context, context.getResources().getString(R.string.internet_connection_failed), Toast.LENGTH_SHORT).show();

                hideProgress();
                callback.onListLoadingFinished();
                return;
            }
        }



        OnPostListChangedListener<Post> onPostsDataChangedListener = new OnPostListChangedListener<Post>() {
            @Override
            public void onListChanged(PostListResult result) {
                lastLoadedItemCreatedDate = result.getLastItemCreatedDate();
                isMoreDataAvailable = result.isMoreDataAvailable();
                List<Post> list = result.getPosts();

                if (nextItemCreatedDate == 0) {
                    filtered.clear();
                    notifyDataSetChanged();
                    swipeContainer.setRefreshing(false);
                }

                hideProgress();

                if (!list.isEmpty()) {
                    addListTrend(list);

                    if (activity!= null){
                        if (!PreferencesUtil.isPostWasLoadedAtLeastOnce(mainActivity)) {
                            PreferencesUtil.setPostWasLoadedAtLeastOnce(mainActivity, true);
                        }
                        else {
                            if (!PreferencesUtil.isPostWasLoadedAtLeastOnce(context)) {
                                PreferencesUtil.setPostWasLoadedAtLeastOnce(context, true);
                            }
                        }
                    }

                } else {
                    isLoading = false;
                }

                callback.onListLoadingFinished();
            }

            @Override
            public void onCanceled(String message) {
                callback.onCanceled(message);
            }
        };

        PostManager.getInstance(activity).getPostsList(onPostsDataChangedListener, nextItemCreatedDate);
    }

    private void hideProgress() {
        if (!filtered.isEmpty() && getItemViewType(filtered.size() - 1) == ItemType.LOAD.getTypeCode()) {
            filtered.remove(filtered.size() - 1);
            notifyItemRemoved(filtered.size() - 1);
        }
    }

    public void removeSelectedPost() {
        filtered.remove(selectedPostPosition);
        notifyItemRemoved(selectedPostPosition);
    }

    @Override
    public long getItemId(int position) {
        return getItemByPosition(position).getId().hashCode();
    }

    public interface Callback {
        void onItemClick(Post post, View view);
        void onListLoadingFinished();
        void onAuthorClick(String authorId, View view);
        void onCanceled(String message);
    }

    public boolean hasInternetConnection(Context contextt) {
        ConnectivityManager cm = (ConnectivityManager) contextt.getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission") NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (search.equals("")){
                    filtered = postList ;
                }
                else
                {
                    List<Post> filteredList = new ArrayList<>();
                    for (Post ad : filteredList) {

                        if (ad.getTitle().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(ad);
                        }
                    }

                    filtered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filtered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filtered = (ArrayList<Post>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

}
