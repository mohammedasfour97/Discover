package com.discoveregypttourism.SocialComponents.fragments;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.discoveregypttourism.Activities.ChooseLanguageActivity;
import com.discoveregypttourism.Activities.SignupActivity;
import com.discoveregypttourism.DB.TinyDB;
import com.discoveregypttourism.MainActivity;
import com.discoveregypttourism.R;
import com.discoveregypttourism.SocialComponents.activities.CreatePostActivity;
import com.discoveregypttourism.SocialComponents.activities.PostDetailsActivity;
import com.discoveregypttourism.SocialComponents.activities.ProfileActivity;
import com.discoveregypttourism.SocialComponents.adapters.PostsAdapter;
import com.discoveregypttourism.SocialComponents.enums.PostStatus;
import com.discoveregypttourism.SocialComponents.enums.ProfileStatus;
import com.discoveregypttourism.SocialComponents.managers.DatabaseHelper;
import com.discoveregypttourism.SocialComponents.managers.PostManager;
import com.discoveregypttourism.SocialComponents.managers.ProfileManager;
import com.discoveregypttourism.SocialComponents.managers.listeners.OnObjectExistListener;
import com.discoveregypttourism.SocialComponents.model.Post;
import com.discoveregypttourism.SocialComponents.utils.AnimationUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import static android.support.v7.app.AppCompatActivity.RESULT_OK;

public class TrendFragment extends BaseFragment {

    private PostsAdapter postsAdapter;
    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;

    private ProfileManager profileManager;
    private PostManager postManager;
    private int counter;
    private TextView newPostsCounterTextView;
    private PostManager.PostCounterWatcher postCounterWatcher;
    private boolean counterAnimationInProgress = false;
    private View view ;
    private android.support.v7.widget.Toolbar toolbar;


    public static TrendFragment newInstance() {
        TrendFragment trendFragment = new TrendFragment();
        return trendFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view =  inflater.inflate(com.discoveregypttourism.R.layout.activity_main, container, false);

        toolbar = view.findViewById(com.discoveregypttourism.R.id.toolbar);

        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        profileManager = ProfileManager.getInstance(getContext());
        postManager = PostManager.getInstance(getContext());
        initContentView();

        postCounterWatcher = new PostManager.PostCounterWatcher() {
            @Override
            public void onPostCounterChanged(int newValue) {
                updateNewPostCounter();
            }
        };

        postManager.setPostCounterWatcher(postCounterWatcher);

        return view ;

//        setOnLikeAddedListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateNewPostCounter();
    }

    private void setOnLikeAddedListener() {
        DatabaseHelper.getInstance(getContext()).onNewLikeAddedListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                counter++;
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ProfileActivity.CREATE_POST_FROM_PROFILE_REQUEST:
                    refreshPostList();
                    break;
                case CreatePostActivity.CREATE_NEW_POST_REQUEST:
                    refreshPostList();
                    showFloatButtonRelatedSnackBar(com.discoveregypttourism.R.string.message_post_was_created);
                    break;

                case PostDetailsActivity.UPDATE_POST_REQUEST:
                    if (data != null) {
                        PostStatus postStatus = (PostStatus) data.getSerializableExtra(PostDetailsActivity.POST_STATUS_EXTRA_KEY);
                        if (postStatus.equals(PostStatus.REMOVED)) {
                            postsAdapter.removeSelectedPost();
                            showFloatButtonRelatedSnackBar(com.discoveregypttourism.R.string.message_post_was_removed);
                        } else if (postStatus.equals(PostStatus.UPDATED)) {
                            postsAdapter.updateSelectedPostFragment();
                        }
                    }
                    break;
            }
        }
    }

    private void refreshPostList() {
        postsAdapter.loadFirstPageTrend();
        if (postsAdapter.getItemCount() > 0) {
            recyclerView.scrollToPosition(0);
        }
    }

    private void initContentView() {
        if (recyclerView == null) {
            floatingActionButton = (FloatingActionButton) view.findViewById(com.discoveregypttourism.R.id.addNewPostFab);

            if (floatingActionButton != null) {
                floatingActionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (hasInternetConnection()) {
                            addPostClickAction();
                        } else {
                            showFloatButtonRelatedSnackBar(com.discoveregypttourism.R.string.internet_connection_failed);
                        }
                    }
                });
            }

            newPostsCounterTextView = (TextView) view.findViewById(com.discoveregypttourism.R.id.newPostsCounterTextView);
            newPostsCounterTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refreshPostList();
                }
            });

            final ProgressBar progressBar = (ProgressBar) view.findViewById(com.discoveregypttourism.R.id.progressBar);
            SwipeRefreshLayout swipeContainer = (SwipeRefreshLayout) view.findViewById(com.discoveregypttourism.R.id.swipeContainer);
            recyclerView = (RecyclerView) view.findViewById(com.discoveregypttourism.R.id.recycler_view);
            postsAdapter = new PostsAdapter(getContext(), swipeContainer);
            postsAdapter.setCallback(new PostsAdapter.Callback() {
                @Override
                public void onItemClick(final Post post, final View view) {
                    PostManager.getInstance(getContext()).isPostExistSingleValue(post.getId(), new OnObjectExistListener<Post>() {
                        @Override
                        public void onDataChanged(boolean exist) {
                            if (exist) {
                                openPostDetailsActivity(post, view);
                            } else {
                                showFloatButtonRelatedSnackBar(com.discoveregypttourism.R.string.error_post_was_removed);
                            }
                        }
                    });
                }

                @Override
                public void onListLoadingFinished() {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onAuthorClick(String authorId, View view) {
                    openProfileActivity(authorId, view);
                }

                @Override
                public void onCanceled(String message) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                }
            });

            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
            recyclerView.setAdapter(postsAdapter);
            postsAdapter.loadFirstPageTrend();
            updateNewPostCounter();

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    hideCounterView();
                    super.onScrolled(recyclerView, dx, dy);
                }
            });
        }
    }

    private void hideCounterView() {
        if (!counterAnimationInProgress && newPostsCounterTextView.getVisibility() == View.VISIBLE) {
            counterAnimationInProgress = true;
            AlphaAnimation alphaAnimation = AnimationUtils.hideViewByAlpha(newPostsCounterTextView);
            alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    counterAnimationInProgress = false;
                    newPostsCounterTextView.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            alphaAnimation.start();
        }
    }

    private void showCounterView() {
        AnimationUtils.showViewByScaleAndVisibility(newPostsCounterTextView);
    }

    private void openPostDetailsActivity(Post post, View v) {
        Intent intent = new Intent(getContext(), PostDetailsActivity.class);
        intent.putExtra(PostDetailsActivity.POST_ID_EXTRA_KEY, post.getId());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            if (post.getImagePath()!=null){
                View imageView = v.findViewById(R.id.postImageView);
                View authorImageView = v.findViewById(R.id.authorImageView);

                ActivityOptions options = ActivityOptions.
                        makeSceneTransitionAnimation(getActivity(),
                                new android.util.Pair<>(imageView, getString(R.string.post_image_transition_name)),
                                new android.util.Pair<>(authorImageView, getString(R.string.post_author_image_transition_name))
                        );
                startActivityForResult(intent, PostDetailsActivity.UPDATE_POST_REQUEST, options.toBundle());
            }
            else {
                startActivityForResult(intent, PostDetailsActivity.UPDATE_POST_REQUEST);
            }

        } else {
            startActivityForResult(intent, PostDetailsActivity.UPDATE_POST_REQUEST);
        }
    }

    public void showFloatButtonRelatedSnackBar(int messageId) {
        showSnackBar(floatingActionButton, messageId);
    }

    private void addPostClickAction() {
        ProfileStatus profileStatus = profileManager.checkProfile();

        openCreatePostActivity();
    }

    private void openCreatePostActivity() {
        Intent intent = new Intent(getContext(), CreatePostActivity.class);
        startActivityForResult(intent, CreatePostActivity.CREATE_NEW_POST_REQUEST);
    }

    private void openProfileActivity(String userId) {
        openProfileActivity(userId, null);
    }

    private void openProfileActivity(String userId, View view) {
        Intent intent = new Intent(getContext(), ProfileActivity.class);
        intent.putExtra(ProfileActivity.USER_ID_EXTRA_KEY, userId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && view != null) {

            View authorImageView = view.findViewById(com.discoveregypttourism.R.id.authorImageView);

            ActivityOptions options = ActivityOptions.
                    makeSceneTransitionAnimation(getActivity(),
                            new android.util.Pair<>(authorImageView, getString(com.discoveregypttourism.R.string.post_author_image_transition_name)));
            startActivityForResult(intent, ProfileActivity.CREATE_POST_FROM_PROFILE_REQUEST, options.toBundle());
        } else {
            startActivityForResult(intent, ProfileActivity.CREATE_POST_FROM_PROFILE_REQUEST);
        }
    }

    private void updateNewPostCounter() {

        int newPostsQuantity = postManager.getNewPostsCounter();

        if (newPostsCounterTextView != null) {
            if (newPostsQuantity > 0) {
                showCounterView();

                String counterFormat = getResources().getQuantityString(com.discoveregypttourism.R.plurals.new_posts_counter_format, newPostsQuantity, newPostsQuantity);
                newPostsCounterTextView.setText(String.format(counterFormat, newPostsQuantity));
            } else {
                hideCounterView();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(com.discoveregypttourism.R.menu.profile_menu, menu);
      /*  SearchManager searchManager = (SearchManager)getContext().getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setQueryHint(getResources().getString(R.string.search));
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getActivity().getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                postsAdapter.getFilter().filter(query);
                search = query;
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                postsAdapter.getFilter().filter(query);
                search = query;
                return false;
            }
        });
        */

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void startEditProfileActivity() {
        if (hasInternetConnection()) {
            Intent intent = new Intent(getContext(), SignupActivity.class);
            intent.putExtra("edit" , "yes");
            startActivity(intent);
        } else {
            showToast(getContext(),getResources().getString(R.string.internet_connection_failed));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case com.discoveregypttourism.R.id.editProfile:
                startEditProfileActivity();
                return true;
            case com.discoveregypttourism.R.id.signOut:
                FirebaseAuth.getInstance().signOut();
                ((MainActivity)getActivity()).goToLogin();
                return true;
            case R.id.my_profile:
                openProfileActivity(FirebaseAuth.getInstance().getCurrentUser().getUid());
                return true;
            case R.id.change_language:
                new TinyDB(getContext()).putString("selectlan" , "no");
                startActivity(new Intent(getActivity() , ChooseLanguageActivity.class));
                return true;
            case com.discoveregypttourism.R.id.createPost:
                if (hasInternetConnection()) {
                    openCreatePostActivity();
                } else {
                    showToast(getContext(),getResources().getString(com.discoveregypttourism.R.string.internet_connection_failed));
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}