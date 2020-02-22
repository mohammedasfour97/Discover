package com.discoveregypttourism.SocialComponents.fragments;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.discoveregypttourism.SocialComponents.activities.CreatePostActivity;
import com.discoveregypttourism.SocialComponents.activities.PostDetailsActivity;
import com.discoveregypttourism.SocialComponents.activities.ProfileActivity;
import com.discoveregypttourism.SocialComponents.adapters.PostsByUserAdapter;
import com.discoveregypttourism.SocialComponents.enums.PostStatus;
import com.discoveregypttourism.SocialComponents.managers.PostManager;
import com.discoveregypttourism.SocialComponents.managers.ProfileManager;
import com.discoveregypttourism.SocialComponents.managers.listeners.OnObjectChangedListener;
import com.discoveregypttourism.SocialComponents.managers.listeners.OnObjectExistListener;
import com.discoveregypttourism.SocialComponents.model.Post;
import com.discoveregypttourism.SocialComponents.model.Profile;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static android.support.v7.app.AppCompatActivity.RESULT_OK;

public class PostsByAuthoerFragment extends BaseFragment {

    private static final String TAG = ProfileActivity.class.getSimpleName();
    public static final int CREATE_POST_FROM_PROFILE_REQUEST = 22;
    public static final String USER_ID_EXTRA_KEY = "ProfileActivity.USER_ID_EXTRA_KEY";
    private View view;
    private TextView nameEditText;
    private ImageView imageView;
    private RecyclerView recyclerView;
    private TextView postsCounterTextView;
    private TextView postsLabelTextView;

    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    private String currentUserId;
    private String userID;
    private TextView likesCountersTextView;

    private PostsByUserAdapter postsAdapter;
    private ProfileManager profileManager;



    public static PostsByAuthoerFragment newInstance() {
        PostsByAuthoerFragment postsByAuthoerFragment = new PostsByAuthoerFragment();
        return postsByAuthoerFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(com.discoveregypttourism.R.layout.recycler, container, false);

        ProfileFragment profileFragment = ((ProfileFragment)PostsByAuthoerFragment.this.getParentFragment());

        if (profileFragment==null) {
            userID = ((ProfileActivity)getActivity()).userID;
            likesCountersTextView = ((ProfileActivity)getActivity()).likesCountersTextView;
            postsCounterTextView = ((ProfileActivity)getActivity()).postsCounterTextView;
            nameEditText = ((ProfileActivity)getActivity()).nameEditText;
            imageView = ((ProfileActivity)getActivity()).imageView;

        }
        else {
            userID = profileFragment.userID;
            likesCountersTextView = profileFragment.likesCountersTextView;
            postsCounterTextView = profileFragment.postsCounterTextView;
            nameEditText = profileFragment.nameEditText;
            imageView = profileFragment.imageView;
        }

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
        }

        loadPostsList();
        loadProfile();
        getActivity().supportPostponeEnterTransition();
        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        profileManager.closeListeners(getContext());

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.stopAutoManage(getActivity());
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CreatePostActivity.CREATE_NEW_POST_REQUEST:
                    Toast.makeText(getContext(), "CREATE_NEW_POST_REQUEST", Toast.LENGTH_SHORT).show();
                    postsAdapter.loadPosts();
                    showToast(getContext() , getResources().getString(com.discoveregypttourism.R.string.message_new_post_was_created));
                    getActivity().setResult(RESULT_OK);
                    break;

                case PostDetailsActivity.UPDATE_POST_REQUEST:
                    if (data != null) {
                        PostStatus postStatus = (PostStatus) data.getSerializableExtra(PostDetailsActivity.POST_STATUS_EXTRA_KEY);
                        if (postStatus.equals(PostStatus.REMOVED)) {
                            postsAdapter.removeSelectedPost();

                        } else if (postStatus.equals(PostStatus.UPDATED)) {
                            postsAdapter.updateSelectedPost();
                        }
                    }
                    break;
            }
        }
    }

    private void loadPostsList() {
        if (recyclerView == null) {
            recyclerView = view.findViewById(com.discoveregypttourism.R.id.recyclerview);
            postsAdapter = new PostsByUserAdapter(getActivity(), userID);
            postsAdapter.setCallBack(new PostsByUserAdapter.CallBack() {
                @Override
                public void onItemClick(final Post post, final View view) {
                    PostManager.getInstance(getActivity()).isPostExistSingleValue(post.getId(), new OnObjectExistListener<Post>() {
                        @Override
                        public void onDataChanged(boolean exist) {
                            if (exist) {
                                openPostDetailsActivity(post, view);
                            } else {
                                showToast(getContext() , getResources().getString(com.discoveregypttourism.R.string.error_post_was_removed));
                            }
                        }
                    });
                }

                @Override
                public void onPostsListChanged(int postsCount) {
                    String postsLabel = getResources().getQuantityString(com.discoveregypttourism.R.plurals.posts_counter_format, postsCount, postsCount);
                    postsCounterTextView.setText(  postsCount+ "\n" +postsLabel);

                    likesCountersTextView.setVisibility(View.VISIBLE);
                    postsCounterTextView.setVisibility(View.VISIBLE);

                    if (postsCount > 0) {
                    }

                }

                @Override
                public void onPostLoadingCanceled() {
                }
            });

            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
            recyclerView.setAdapter(postsAdapter);
            postsAdapter.loadPosts();
        }
    }

    private Spannable buildCounterSpannable(String label, int value) {
        SpannableStringBuilder contentString = new SpannableStringBuilder();
        contentString.append(String.valueOf(value));
        contentString.append("\n");
        int start = contentString.length();
        contentString.append(label);
        contentString.setSpan(new TextAppearanceSpan(getContext(), com.discoveregypttourism.R.style.TextAppearance_Second_Light), start, contentString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return contentString;
    }

    private void openPostDetailsActivity(Post post, View v) {
        Intent intent = new Intent(getContext(), PostDetailsActivity.class);
        intent.putExtra(PostDetailsActivity.POST_ID_EXTRA_KEY, post.getId());
        intent.putExtra(PostDetailsActivity.AUTHOR_ANIMATION_NEEDED_EXTRA_KEY, true);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            View imageView = v.findViewById(com.discoveregypttourism.R.id.postImageView);

            ActivityOptions options = ActivityOptions.
                    makeSceneTransitionAnimation(getActivity(),
                            new android.util.Pair<>(imageView, getString(com.discoveregypttourism.R.string.post_image_transition_name))
                    );
            startActivityForResult(intent, PostDetailsActivity.UPDATE_POST_REQUEST, options.toBundle());
        } else {
            startActivityForResult(intent, PostDetailsActivity.UPDATE_POST_REQUEST);
        }
    }

    private void loadProfile() {
        profileManager = ProfileManager.getInstance(getContext());
        profileManager.getProfileValue(getContext(), userID, createOnProfileChangedListener());
    }

    private OnObjectChangedListener<Profile> createOnProfileChangedListener() {
        return new OnObjectChangedListener<Profile>() {
            @Override
            public void onObjectChanged(Profile obj) {
                fillUIFields(obj);
            }
        };
    }

    private void fillUIFields(Profile profile) {
        if (profile != null) {
            nameEditText.setText(profile.getUsername());

            if (profile.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(profile.getPhotoUrl())
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .crossFade()
                        .error(com.discoveregypttourism.R.drawable.ic_stub)
                        .listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                scheduleStartPostponedTransition(imageView);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                scheduleStartPostponedTransition(imageView);
                                return false;
                            }
                        })
                        .into(imageView);
            } else {
                imageView.setImageResource(com.discoveregypttourism.R.drawable.ic_stub);
            }

            int likesCount = (int) profile.getLikesCount();
            String likesLabel = getResources().getQuantityString(com.discoveregypttourism.R.plurals.likes_counter_format, likesCount, likesCount);
            likesCountersTextView.setText(likesCount + "\n" + likesLabel);
        }
    }



    private void scheduleStartPostponedTransition(final ImageView imageView) {
        imageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                imageView.getViewTreeObserver().removeOnPreDrawListener(this);
                getActivity().supportStartPostponedEnterTransition();
                return true;
            }
        });
    }

  }
