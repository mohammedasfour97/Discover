package com.discoveregypttourism.SocialComponents.fragments;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.discoveregypttourism.Activities.ChooseLanguageActivity;
import com.discoveregypttourism.Activities.LoginActivity;
import com.discoveregypttourism.Activities.SignupActivity;
import com.discoveregypttourism.DB.TinyDB;
import com.discoveregypttourism.MainActivity;
import com.discoveregypttourism.Messenger.ui.FriendsList;
import com.discoveregypttourism.R;
import com.discoveregypttourism.SocialComponents.activities.CreatePostActivity;
import com.discoveregypttourism.SocialComponents.activities.ProfileActivity;
import com.discoveregypttourism.SocialComponents.enums.ProfileStatus;
import com.discoveregypttourism.SocialComponents.managers.ProfileManager;
import com.discoveregypttourism.SocialComponents.managers.listeners.OnObjectChangedListener;
import com.discoveregypttourism.SocialComponents.model.Profile;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends BaseFragment {

    private Toolbar toolbar;
    private View view;
    private static final String TAG = ProfileActivity.class.getSimpleName();
    public static final int CREATE_POST_FROM_PROFILE_REQUEST = 22;
    public static final String USER_ID_EXTRA_KEY = "ProfileActivity.USER_ID_EXTRA_KEY";
    public TextView nameEditText;
    public ImageView imageView;
    public ProgressBar progressBar;
    public  TextView postsCounterTextView;
    private TextView postsLabelTextView;
    private ProgressBar postsProgressBar;
    private FloatingActionButton floatingActionButton;

    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    private String currentUserId;
    public String userID;

    private SwipeRefreshLayout swipeContainer;
    public TextView likesCountersTextView;
    private ProfileManager profileManager;
    private Button message ;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    public static ProfileFragment newInstance() {
        ProfileFragment profileFragment = new ProfileFragment();
        return profileFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.activity_profile, container, false);

        toolbar = view.findViewById(R.id.toolbar);

        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
        }

        // Set up the login form.
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        imageView = (ImageView) view.findViewById(R.id.imageView);
        nameEditText = (TextView) view.findViewById(R.id.nameEditText);
        postsCounterTextView = (TextView) view.findViewById(R.id.postsCounterTextView);
        likesCountersTextView = (TextView) view.findViewById(R.id.likesCountersTextView);
        postsLabelTextView = (TextView) view.findViewById(R.id.postsLabelTextView);
        postsProgressBar = (ProgressBar) view.findViewById(R.id.postsProgressBar);
        message = view.findViewById(R.id.send_message);
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        floatingActionButton = (FloatingActionButton) view.findViewById(R.id.addNewPostFab);

        if (floatingActionButton != null) {
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (hasInternetConnection()) {
                        addPostClickAction();
                    } else {
                        showFloatButtonRelatedSnackBar(R.string.internet_connection_failed);
                    }
                }
            });
        }

        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        getActivity().supportPostponeEnterTransition();

        if (currentUserId.equals( userID)){
            message.setVisibility(View.GONE);
        }

        postsLabelTextView.setVisibility(View.GONE);

        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase.getInstance().getReference().child("user").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String email = dataSnapshot.child("email").getValue().toString();
                        String name = dataSnapshot.child("name").getValue().toString();
                        Intent intent = (new Intent(getContext() , FriendsList.class));
                        intent.putExtra("email_message" , email);
                        intent.putExtra("detectm" , "messengerr");
                        intent.putExtra("name" , name);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });

        return view ;

    }
    @Override
    public void onStart() {
        super.onStart();
        loadProfile();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    private void addPostClickAction() {
        ProfileStatus profileStatus = profileManager.checkProfile();

        openCreatePostActivity();
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
                Glide.with(getContext())
                        .load(profile.getPhotoUrl())
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .crossFade()
                        .error(R.drawable.ic_stub)
                        .listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                scheduleStartPostponedTransition(imageView);
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                scheduleStartPostponedTransition(imageView);
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(imageView);
            } else {
                progressBar.setVisibility(View.GONE);
                imageView.setImageResource(R.drawable.ic_stub);
            }

            int likesCount = (int) profile.getLikesCount();
            String likesLabel = getResources().getQuantityString(R.plurals.likes_counter_format, likesCount, likesCount);
            likesCountersTextView.setText(likesCount + "\n" + likesLabel);
        }
    }

    private void hideLoadingPostsProgressBar() {
        if (postsProgressBar.getVisibility() != View.GONE) {
            postsProgressBar.setVisibility(View.GONE);
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

    private void startMainActivity() {
        Intent intent = new Intent(getContext(), LoginActivity.class);
        startActivity(intent);
    }


    private void openCreatePostActivity() {
        Intent intent = new Intent(getContext(), CreatePostActivity.class);
        startActivityForResult(intent, CreatePostActivity.CREATE_NEW_POST_REQUEST);
    }

    public void showFloatButtonRelatedSnackBar(int messageId) {
        showSnackBar(floatingActionButton, messageId);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.profile_menu, menu);
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
            case R.id.editProfile:
                startEditProfileActivity();
                return true;
            case R.id.my_profile:
                openProfileActivity(FirebaseAuth.getInstance().getCurrentUser().getUid());
                return true;
            case R.id.change_language:
                new TinyDB(getContext()).putString("selectlan" , "no");
                getActivity().startActivity(new Intent(getActivity() , ChooseLanguageActivity.class));

                return true;
            case R.id.signOut:
                FirebaseAuth.getInstance().signOut();
                ((MainActivity)getActivity()).goToLogin();
                return true;
            case R.id.createPost:
                if (hasInternetConnection()) {
                    openCreatePostActivity();
                } else {
                    showToast(getContext(),getResources().getString(R.string.internet_connection_failed));
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void setupViewPager(ViewPager viewPager) {
        ProfileFragment.ViewPagerAdapter adapter = new ProfileFragment.ViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(PostsByAuthoerFragment.newInstance(), getResources().getString(R.string.postss));
        if (userID .equals( currentUserId)){
            adapter.addFragment(InterestsFragment.newInstance(), getResources().getString(R.string.interests));
        }

        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}

