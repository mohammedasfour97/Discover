package com.discoveregypttourism.Messenger.fragments;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.discoveregypttourism.Messenger.data.FriendDB;
import com.discoveregypttourism.Messenger.data.StaticConfig;
import com.discoveregypttourism.Messenger.model.Friend;
import com.discoveregypttourism.Messenger.model.ListFriend;
import com.discoveregypttourism.Messenger.service.ServiceUtils;
import com.discoveregypttourism.Messenger.ui.ChatActivity;
import com.discoveregypttourism.Messenger.ui.FriendsList;
import com.discoveregypttourism.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView recyclerListFrends;
    private com.discoveregypttourism.Messenger.fragments.ListFriendsAdapter adapter;
    private ListFriend dataListFriend = null;
    private ArrayList<String> listFriendID = null;
    private CountDownTimer detectFriendOnline;
    public static int ACTION_START_CHAT = 1;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser userr;
    public FragFriendClickFloatButton onClickFloatButton;
    public static final String ACTION_DELETE_FRIEND = "com.android.rivchat.DELETE_FRIEND";
    private ProgressDialog progress_spinner;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private BroadcastReceiver deleteFriendReceiver;

    public FriendsFragment() {
        onClickFloatButton = new FragFriendClickFloatButton();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        detectFriendOnline = new CountDownTimer(System.currentTimeMillis(), StaticConfig.TIME_TO_REFRESH) {
            @Override
            public void onTick(long l) {
                ServiceUtils.updateFriendStatus(getContext(), dataListFriend);
                ServiceUtils.updateUserStatus(getContext());
            }

            @Override
            public void onFinish() {

            }
        };

        userr = FirebaseAuth.getInstance().getCurrentUser();
        if (dataListFriend == null) {
            dataListFriend = FriendDB.getInstance(getContext()).getListFriend();
            if (dataListFriend.getListFriend().size() > 0) {
                listFriendID = new ArrayList<>();
                for (Friend friend : dataListFriend.getListFriend()) {
                    listFriendID.add(friend.id);
                }
                detectFriendOnline.start();
            }
        }
        View layout = inflater.inflate(R.layout.fragment_people, container, false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerListFrends = (RecyclerView) layout.findViewById(R.id.recycleListFriend);
        recyclerListFrends.setLayoutManager(linearLayoutManager);
        mSwipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        adapter = new ListFriendsAdapter(getContext(), dataListFriend, this);
        recyclerListFrends.setAdapter(adapter);
        progress_spinner = new ProgressDialog(getContext());
        if (listFriendID == null) {
            listFriendID = new ArrayList<>();
            progress_spinner.setMessage(getResources().getString(R.string.get_all_friends));
            progress_spinner.show();
            getListFriendUId();
        }

        deleteFriendReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String idDeleted = intent.getExtras().getString("idFriend");
                for (Friend friend : dataListFriend.getListFriend()) {
                    if(idDeleted.equals(friend.id)){
                        ArrayList<Friend> friends = dataListFriend.getListFriend();
                        friends.remove(friend);
                        break;
                    }
                }
                adapter.notifyDataSetChanged();
            }
        };

        IntentFilter intentFilter = new IntentFilter(ACTION_DELETE_FRIEND);
        getContext().registerReceiver(deleteFriendReceiver, intentFilter);

        return layout;
    }

    @Override
    public void onDestroyView (){
        super.onDestroyView();

        getContext().unregisterReceiver(deleteFriendReceiver);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (ACTION_START_CHAT == requestCode && data != null && ListFriendsAdapter.mapMark != null) {
            ListFriendsAdapter.mapMark.put(data.getStringExtra("idFriend"), false);
        }
    }

    @Override
    public void onRefresh() {
        listFriendID.clear();
        dataListFriend.getListFriend().clear();
        adapter.notifyDataSetChanged();
        FriendDB.getInstance(getContext()).dropDB();
        detectFriendOnline.cancel();
        getListFriendUId();
    }

    public class FragFriendClickFloatButton implements View.OnClickListener {
        Context context;
        AlertDialog.Builder dialogWait;

        public FragFriendClickFloatButton() {
        }

        public FragFriendClickFloatButton getInstance(Context context) {
            this.context = context;
            dialogWait = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
            return this;
        }

        @Override
        public void onClick(final View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(getResources().getString(R.string.add_friend));

            final EditText input = new EditText(getContext());
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            input.setHint(getResources().getString(R.string.enter_friend_email));
            builder.setView(input);

            builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(input.getText().toString());
                    if (matcher.find())
                        findIDEmail(input.getText().toString());
                    else
                        Toast.makeText(context, getResources().getString(R.string.not_valid_email), Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();

        }

        /**
         * TIm id cua email tren server
         *
         * @param email
         */
        private void findIDEmail(String email) {

            FirebaseDatabase.getInstance().getReference().child("user").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null) {
                        AlertDialog.Builder builder;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
                        } else {
                            builder = new AlertDialog.Builder(getContext());
                        }
                        builder.setTitle(getResources().getString(R.string.error))
                                .setMessage(getResources().getString(R.string.email_not_found))
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    } else {
                        String id = ((HashMap) dataSnapshot.getValue()).keySet().iterator().next().toString();
                        if (id.equals(userr.getUid())) {
                            AlertDialog.Builder builder;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
                            } else {
                                builder = new AlertDialog.Builder(getContext());
                            }
                            builder.setTitle(getResources().getString(R.string.error))
                                    .setMessage(getResources().getString(R.string.error))
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            getActivity().finish();
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        } else {
                            HashMap userMap = (HashMap) ((HashMap) dataSnapshot.getValue()).get(id);
                            Friend user = new Friend();
                            user.name = (String) userMap.get("name");
                            user.email = (String) userMap.get("email");
                            user.avata = (String) userMap.get("avata");
                            user.id = id;
                            user.idRoom = id.compareTo(userr.getUid()) > 0 ? (userr.getUid() + id).hashCode() + "" : "" + (id + com.discoveregypttourism.Messenger.StaticConfig.UID).hashCode();
                            checkBeforAddFriend(id, user);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


        /**
         * Lay danh sach friend cua một UID
         */
        private void checkBeforAddFriend(final String idFriend, Friend userInfo) {

            //Check xem da ton tai id trong danh sach id chua
            if (listFriendID.contains(idFriend)) {
                Toast.makeText(context, getResources().getString(R.string.user) + " " + userInfo.email + " " + getResources().getString(R.string.has_been_friend), Toast.LENGTH_SHORT).show();
            } else {
                addFriend(idFriend, true);
                listFriendID.add(idFriend);
                dataListFriend.getListFriend().add(userInfo);
                FriendDB.getInstance(getContext()).addFriend(userInfo);
                adapter.notifyDataSetChanged();
            }
        }

        /**
         * Add friend
         *
         * @param idFriend
         */
        private void addFriend(final String idFriend, boolean isIdFriend) {
            if (idFriend != null) {
                if (isIdFriend) {
                    FirebaseDatabase.getInstance().getReference().child("friend/" +userr.getUid()).push().setValue(idFriend)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        addFriend(idFriend, false);
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getContext(), getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    FirebaseDatabase.getInstance().getReference().child("friend/" + idFriend).push().setValue(userr.getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                addFriend(null, false);
                            }
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getContext(), getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            } else {
                Toast.makeText(context, getResources().getString(R.string.successfully_added_friend), Toast.LENGTH_SHORT).show();
            }
        }
    }


        private void getListFriendUId() {
            FirebaseDatabase.getInstance().getReference().child("friend/" + userr.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        HashMap mapRecord = (HashMap) dataSnapshot.getValue();
                        Iterator listKey = mapRecord.keySet().iterator();
                        while (listKey.hasNext()) {
                            String key = listKey.next().toString();
                            listFriendID.add(mapRecord.get(key).toString());
                        }
                        getAllFriendInfo(0);
                    } else {
                        progress_spinner.hide();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

        /**
         * Truy cap bang user lay thong tin id nguoi dung
         */
        private void getAllFriendInfo(final int index) {
            if (index == listFriendID.size()) {
                //save list friend
                adapter.notifyDataSetChanged();
                progress_spinner.dismiss();
                detectFriendOnline.start();
            } else {
                final String id = listFriendID.get(index);
                FirebaseDatabase.getInstance().getReference().child("user/" + id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            Friend user = new Friend();
                            HashMap mapUserInfo = (HashMap) dataSnapshot.getValue();
                            user.name = (String) mapUserInfo.get("name");
                            user.email = (String) mapUserInfo.get("email");
                            user.avata = (String) mapUserInfo.get("avata");
                            user.id = id;
                            user.idRoom = id.compareTo(userr.getUid()) > 0 ? (userr.getUid() + id).hashCode() + "" : "" + (id + com.discoveregypttourism.Messenger.StaticConfig.UID).hashCode();
                            dataListFriend.getListFriend().add(user);
                            FriendDB.getInstance(getContext()).addFriend(user);
                        }
                        getAllFriendInfo(index + 1);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
    }

    class ListFriendsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private ListFriend listFriend;
        private Context context;
        public static Map<String, Query> mapQuery;
        public static Map<String, DatabaseReference> mapQueryOnline;
        public static Map<String, ChildEventListener> mapChildListener;
        public static Map<String, ChildEventListener> mapChildListenerOnline;
        public static Map<String, Boolean> mapMark;
        private FriendsFragment friendsList;
        ProgressDialog dialogWaitDeleting;

        public ListFriendsAdapter(Context context, ListFriend listFriend, FriendsFragment friendsList) {
            this.listFriend = listFriend;
            this.context = context;
            mapQuery = new HashMap<>();
            mapChildListener = new HashMap<>();
            mapMark = new HashMap<>();
            mapChildListenerOnline = new HashMap<>();
            mapQueryOnline = new HashMap<>();
            this.friendsList = friendsList;
            dialogWaitDeleting = new ProgressDialog(context);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.rc_item_friend, parent, false);
            return new com.discoveregypttourism.Messenger.fragments.ItemFriendViewHolder(context, view);
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            final String name = listFriend.getListFriend().get(position).name;
            final String id = listFriend.getListFriend().get(position).id;
            final String idRoom = listFriend.getListFriend().get(position).idRoom;
            final String avata = listFriend.getListFriend().get(position).avata;
            ((com.discoveregypttourism.Messenger.fragments.ItemFriendViewHolder) holder).txtName.setText(name);

            holder.itemView
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ((com.discoveregypttourism.Messenger.fragments.ItemFriendViewHolder) holder).txtMessage.setTypeface(Typeface.DEFAULT);
                            ((com.discoveregypttourism.Messenger.fragments.ItemFriendViewHolder) holder).txtName.setTypeface(Typeface.DEFAULT);
                            Intent intent = new Intent(context, ChatActivity.class);
                            intent.putExtra(com.discoveregypttourism.Messenger.StaticConfig.INTENT_KEY_CHAT_FRIEND, name);
                            ArrayList<CharSequence> idFriend = new ArrayList<CharSequence>();
                            idFriend.add(id);
                            intent.putCharSequenceArrayListExtra(com.discoveregypttourism.Messenger.StaticConfig.INTENT_KEY_CHAT_ID, idFriend);
                            intent.putExtra(com.discoveregypttourism.Messenger.StaticConfig.INTENT_KEY_CHAT_ROOM_ID, idRoom);
                            ChatActivity.bitmapAvataFriend = new HashMap<>();
                            if (!avata.equals(com.discoveregypttourism.Messenger.StaticConfig.STR_DEFAULT_BASE64)) {
                                byte[] decodedString = Base64.decode(avata, Base64.DEFAULT);
                                ChatActivity.bitmapAvataFriend.put(id, BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
                            } else {
                                ChatActivity.bitmapAvataFriend.put(id, BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_person_black_24dp));
                            }

                            mapMark.put(id, null);
                            friendsList.startActivityForResult(intent, FriendsList.ACTION_START_CHAT);
                        }
                    });

            ((View) ((com.discoveregypttourism.Messenger.fragments.ItemFriendViewHolder) holder).txtName.getParent().getParent().getParent())
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ((com.discoveregypttourism.Messenger.fragments.ItemFriendViewHolder) holder).txtMessage.setTypeface(Typeface.DEFAULT);
                            ((com.discoveregypttourism.Messenger.fragments.ItemFriendViewHolder) holder).txtName.setTypeface(Typeface.DEFAULT);
                            Intent intent = new Intent(context, ChatActivity.class);
                            intent.putExtra(com.discoveregypttourism.Messenger.StaticConfig.INTENT_KEY_CHAT_FRIEND, name);
                            ArrayList<CharSequence> idFriend = new ArrayList<CharSequence>();
                            idFriend.add(id);
                            intent.putCharSequenceArrayListExtra(com.discoveregypttourism.Messenger.StaticConfig.INTENT_KEY_CHAT_ID, idFriend);
                            intent.putExtra(com.discoveregypttourism.Messenger.StaticConfig.INTENT_KEY_CHAT_ROOM_ID, idRoom);
                            ChatActivity.bitmapAvataFriend = new HashMap<>();
                            if (!avata.equals(com.discoveregypttourism.Messenger.StaticConfig.STR_DEFAULT_BASE64)) {
                                byte[] decodedString = Base64.decode(avata, Base64.DEFAULT);
                                ChatActivity.bitmapAvataFriend.put(id, BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
                            } else {
                                ChatActivity.bitmapAvataFriend.put(id, BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_person_black_24dp));
                            }

                            mapMark.put(id, null);
                            friendsList.startActivityForResult(intent, FriendsList.ACTION_START_CHAT);
                        }
                    });

            //nhấn giữ để xóa bạn
            ((View) ((com.discoveregypttourism.Messenger.fragments.ItemFriendViewHolder) holder).txtName.getParent().getParent().getParent())
                    .setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            String friendName = (String)((com.discoveregypttourism.Messenger.fragments.ItemFriendViewHolder) holder).txtName.getText();

                            new AlertDialog.Builder(context)
                                    .setTitle("Delete Friend")
                                    .setMessage("Are you sure want to delete "+friendName+ "?")
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                            final String idFriendRemoval = listFriend.getListFriend().get(position).id;
                                            dialogWaitDeleting.setMessage(context.getResources().getString(R.string.deleting));
                                            dialogWaitDeleting.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                            deleteFriend(idFriendRemoval);
                                        }
                                    })
                                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    }).show();

                            return true;
                        }
                    });


            if (listFriend.getListFriend().get(position).message.text.length() > 0) {
                ((com.discoveregypttourism.Messenger.fragments.ItemFriendViewHolder) holder).txtMessage.setVisibility(View.VISIBLE);
                ((com.discoveregypttourism.Messenger.fragments.ItemFriendViewHolder) holder).txtTime.setVisibility(View.VISIBLE);
                if (!listFriend.getListFriend().get(position).message.text.startsWith(id)) {
                    ((com.discoveregypttourism.Messenger.fragments.ItemFriendViewHolder) holder).txtMessage.setText(listFriend.getListFriend().get(position).message.text);
                    ((com.discoveregypttourism.Messenger.fragments.ItemFriendViewHolder) holder).txtMessage.setTypeface(Typeface.DEFAULT);
                    ((com.discoveregypttourism.Messenger.fragments.ItemFriendViewHolder) holder).txtName.setTypeface(Typeface.DEFAULT);
                } else {
                    ((com.discoveregypttourism.Messenger.fragments.ItemFriendViewHolder) holder).txtMessage.setText(listFriend.getListFriend().get(position).message.text.substring((id + "").length()));
                    ((com.discoveregypttourism.Messenger.fragments.ItemFriendViewHolder) holder).txtMessage.setTypeface(Typeface.DEFAULT_BOLD);
                    ((com.discoveregypttourism.Messenger.fragments.ItemFriendViewHolder) holder).txtName.setTypeface(Typeface.DEFAULT_BOLD);
                }
                String time = new SimpleDateFormat("EEE, d MMM yyyy").format(new Date(listFriend.getListFriend().get(position).message.timestamp));
                String today = new SimpleDateFormat("EEE, d MMM yyyy").format(new Date(System.currentTimeMillis()));
                if (today.equals(time)) {
                    ((com.discoveregypttourism.Messenger.fragments.ItemFriendViewHolder) holder).txtTime.setText(new SimpleDateFormat("HH:mm").format(new Date(listFriend.getListFriend().get(position).message.timestamp)));
                } else {
                    ((com.discoveregypttourism.Messenger.fragments.ItemFriendViewHolder) holder).txtTime.setText(new SimpleDateFormat("MMM d").format(new Date(listFriend.getListFriend().get(position).message.timestamp)));
                }
            } else {
                ((com.discoveregypttourism.Messenger.fragments.ItemFriendViewHolder) holder).txtMessage.setVisibility(View.GONE);
                ((com.discoveregypttourism.Messenger.fragments.ItemFriendViewHolder) holder).txtTime.setVisibility(View.GONE);
                if (mapQuery.get(id) == null && mapChildListener.get(id) == null) {
                    mapQuery.put(id, FirebaseDatabase.getInstance().getReference().child("message/" + idRoom).limitToLast(1));
                    mapChildListener.put(id, new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            HashMap mapMessage = (HashMap) dataSnapshot.getValue();
                            if (mapMark.get(id) != null) {
                                if (!mapMark.get(id)) {
                                    listFriend.getListFriend().get(position).message.text = id + mapMessage.get("text");
                                } else {
                                    listFriend.getListFriend().get(position).message.text = (String) mapMessage.get("text");
                                }
                                notifyDataSetChanged();
                                mapMark.put(id, false);
                            } else {
                                listFriend.getListFriend().get(position).message.text = (String) mapMessage.get("text");
                                notifyDataSetChanged();
                            }
                            listFriend.getListFriend().get(position).message.timestamp = (long) mapMessage.get("timestamp");
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
                    mapQuery.get(id).addChildEventListener(mapChildListener.get(id));
                    mapMark.put(id, true);
                } else {
                    mapQuery.get(id).removeEventListener(mapChildListener.get(id));
                    mapQuery.get(id).addChildEventListener(mapChildListener.get(id));
                    mapMark.put(id, true);
                }
            }
            if (listFriend.getListFriend().get(position).avata.equals(com.discoveregypttourism.Messenger.StaticConfig.STR_DEFAULT_BASE64)) {
                ((com.discoveregypttourism.Messenger.fragments.ItemFriendViewHolder) holder).avata.setImageResource(R.drawable.ic_person_black_24dp);
            } else {
                byte[] decodedString = Base64.decode(listFriend.getListFriend().get(position).avata, Base64.DEFAULT);
                Bitmap src = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                ((com.discoveregypttourism.Messenger.fragments.ItemFriendViewHolder) holder).avata.setImageBitmap(src);
            }


            if (mapQueryOnline.get(id) == null && mapChildListenerOnline.get(id) == null) {
                mapQueryOnline.put(id, FirebaseDatabase.getInstance().getReference().child("user/" + id+"/status"));
                mapChildListenerOnline.put(id, new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if(dataSnapshot.getValue() != null && dataSnapshot.getKey().equals("isOnline")) {
                            Log.d("FriendsFragment add " + id,  (boolean)dataSnapshot.getValue() +"");
                            listFriend.getListFriend().get(position).status.isOnline = (boolean)dataSnapshot.getValue();
                            notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        if(dataSnapshot.getValue() != null&& dataSnapshot.getKey().equals("isOnline")) {
                            Log.d("FriendsFragment change " + id,  (boolean)dataSnapshot.getValue() +"");
                            listFriend.getListFriend().get(position).status.isOnline = (boolean)dataSnapshot.getValue();
                            notifyDataSetChanged();
                        }
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
                mapQueryOnline.get(id).addChildEventListener(mapChildListenerOnline.get(id));
            }

            if (listFriend.getListFriend().get(position).status.isOnline) {
                ((com.discoveregypttourism.Messenger.fragments.ItemFriendViewHolder) holder).avata.setBorderWidth(10);
            } else {
                ((com.discoveregypttourism.Messenger.fragments.ItemFriendViewHolder) holder).avata.setBorderWidth(0);
            }
        }

        @Override
        public int getItemCount() {
            return listFriend.getListFriend() != null ? listFriend.getListFriend().size() : 0;
        }

        public int getPosition (String name) {
            int position = 0 ;
            int wantedpos = 0;
            for (Friend friend : listFriend.getListFriend()){
                if (friend.name.equals(name)){
                    wantedpos = position ;
                    break;
                }
                position++;
            }
            return wantedpos ;
        }

        /**
         * Delete friend
         *
         * @param idFriend
         */
        private void deleteFriend(final String idFriend) {
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(context);
            if (idFriend != null) {
                FirebaseDatabase.getInstance().getReference().child("friend").child(com.discoveregypttourism.Messenger.StaticConfig.UID)
                        .orderByValue().equalTo(idFriend).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.getValue() == null) {
                            //email not found
                            dialogWaitDeleting.hide();

                            builder.setMessage(context.getResources().getString(R.string.error_occured))
                                    .setPositiveButton(android.R.string.yes, null)
                                    .show();
                        } else {
                            String idRemoval = ((HashMap) dataSnapshot.getValue()).keySet().iterator().next().toString();
                            FirebaseDatabase.getInstance().getReference().child("friend")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(idRemoval).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            dialogWaitDeleting.dismiss();

                                            builder.setMessage(context.getResources().getString(R.string.friend_deleted_successfully))
                                                    .setPositiveButton(android.R.string.yes, null)
                                                    .show();

                                            Intent intentDeleted = new Intent(FriendsList.ACTION_DELETE_FRIEND);
                                            intentDeleted.putExtra("idFriend", idFriend);
                                            context.sendBroadcast(intentDeleted);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            dialogWaitDeleting.dismiss();
                                            builder.setMessage(context.getResources().getString(R.string.error_occured))
                                                    .setPositiveButton(android.R.string.yes, null)
                                                    .show();
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            } else {
                dialogWaitDeleting.dismiss();
                builder.setMessage(context.getResources().getString(R.string.error_occured))
                        .setPositiveButton(android.R.string.yes, null)
                        .show();
            }
        }
    }

    class ItemFriendViewHolder extends RecyclerView.ViewHolder{
        public CircleImageView avata;
        public TextView txtName, txtTime, txtMessage;
        private Context context;

        ItemFriendViewHolder(Context context, View itemView) {
            super(itemView);
            avata = (CircleImageView) itemView.findViewById(R.id.icon_avata);
            txtName = (TextView) itemView.findViewById(R.id.txtName);
            txtTime = (TextView) itemView.findViewById(R.id.txtTime);
            txtMessage = (TextView) itemView.findViewById(R.id.txtMessage);
            this.context = context;
        }
    }



