package com.discoveregypttourism.SocialComponents.fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.discoveregypttourism.DB.TinyDB;
import com.discoveregypttourism.Models.Interest;
import com.discoveregypttourism.R;

import net.igenius.customcheckbox.CustomCheckBox;

import java.util.ArrayList;
import java.util.List;

public class InterestsFragment extends BaseFragment {

    private View view;
    private RecyclerView recyclerView;
    private List<Interest> itemsList;
    private InterestsFragment.InterestsFragmentAdapter mAdapter;
    private Button next ;
    private TinyDB db ;
    private List<String> interestslist;
    private TextView header , header2;

    public static InterestsFragment newInstance() {
        InterestsFragment interestsFragment = new InterestsFragment();
        return interestsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(com.discoveregypttourism.R.layout.activity_interests, container, false);

        recyclerView = (RecyclerView) view.findViewById(com.discoveregypttourism.R.id.recyclerview);
        next = view.findViewById(com.discoveregypttourism.R.id.next);
        header = view.findViewById(com.discoveregypttourism.R.id.header1);
        header2 = view.findViewById(com.discoveregypttourism.R.id.header2);

        next.setEnabled(false);
        next.setText(getResources().getString(R.string.button_title_save));

        header.setVisibility(View.GONE);
        header2.setVisibility(View.GONE);

        itemsList = new ArrayList<>();
        db = new TinyDB(getContext());
        if (db.getListString("interests") == null){
            interestslist = new ArrayList<>();
        }
        else {
            interestslist = db.getListString("interests");
        }
        mAdapter = new InterestsFragment.InterestsFragmentAdapter(getActivity(), itemsList);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getContext() , 3);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setNestedScrollingEnabled(false);
        itemsList.clear();

        fill_interests();

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.putListString("interests", (ArrayList<String>) interestslist);
                next.setEnabled(false);
            }
        });
       return view ;
    }

    private void fill_interests (){
        itemsList.add(new Interest(getResources().getString(R.string.cairo) , R.drawable.cairo));
        itemsList.add(new Interest(getResources().getString(R.string.giza) , R.drawable.giza));
        itemsList.add(new Interest(getResources().getString(R.string.alex) , R.drawable.alex));
        itemsList.add(new Interest(getResources().getString(R.string.luxor) , R.drawable.luxor));
        itemsList.add(new Interest(getResources().getString(R.string.aswan) , R.drawable.aswan));
        itemsList.add(new Interest(getResources().getString(R.string.sharm) , R.drawable.sharm));
        itemsList.add(new Interest(getResources().getString(R.string.rasheed) , R.drawable.rasheed));
        itemsList.add(new Interest(getResources().getString(R.string.hargada) , R.drawable.hardagha));
        itemsList.add(new Interest(getResources().getString(R.string.sahl_hashesh) , R.drawable.sahl_hashesh));
        itemsList.add(new Interest(getResources().getString(R.string.gona) , R.drawable.gona));
        itemsList.add(new Interest(getResources().getString(R.string.saphaga) , R.drawable.saphaga));
        itemsList.add(new Interest(getResources().getString(R.string.soma_bay) , R.drawable.soma_bay));
        itemsList.add(new Interest(getResources().getString(R.string.marsa_alam) , R.drawable.marsa_alam));
        itemsList.add(new Interest(getResources().getString(R.string.marsa_matroh) , R.drawable.marsa_matroh));
        itemsList.add(new Interest(getResources().getString(R.string.alameen) , R.drawable.alameen));
        itemsList.add(new Interest(getResources().getString(R.string.ain_shams) , R.drawable.ain_sona));
        itemsList.add(new Interest(getResources().getString(R.string.taba) , R.drawable.taba));
        itemsList.add(new Interest(getResources().getString(R.string.dahab) , R.drawable.dahab));
        itemsList.add(new Interest(getResources().getString(R.string.siwa) , R.drawable.siwa));
        mAdapter.notifyDataSetChanged();
    }

    private class InterestsFragmentAdapter extends RecyclerView.Adapter<InterestsFragment.InterestsFragmentAdapter.MyViewHolder> {
        private Context context;
        private List<Interest> citiesList;


        public class MyViewHolder extends RecyclerView.ViewHolder {
            Context context;
            public ImageView image ;
            public CustomCheckBox scb ;
            public TextView name ;

            public MyViewHolder(View view) {
                super(view);
                image = view.findViewById(com.discoveregypttourism.R.id.image);
                scb = (CustomCheckBox) view.findViewById(com.discoveregypttourism.R.id.cb);
                name = view.findViewById(com.discoveregypttourism.R.id.name);
                context = itemView.getContext();


            }
        }

        public InterestsFragmentAdapter(Context context, List<Interest> citiesList) {
            this.context = context;
            this.citiesList = citiesList;
        }

        @Override
        public InterestsFragment.InterestsFragmentAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(com.discoveregypttourism.R.layout.interest_item, parent, false);

            return new InterestsFragment.InterestsFragmentAdapter.MyViewHolder(itemView);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onBindViewHolder(final InterestsFragment.InterestsFragmentAdapter.MyViewHolder holder, final int position) {
            final Interest interst = citiesList.get(position);

            if (interestslist.contains(interst.getName())){
                holder.scb.setChecked(true);
            }
            else {
                holder.scb.setChecked(false);
            }


            holder.scb.setCheckedColor(getResources().getColor(com.discoveregypttourism.R.color.white));
            holder.scb.setUnCheckedColor(getResources().getColor(com.discoveregypttourism.R.color.white));

             holder.name.setText(interst.getName());

             holder.image.setImageDrawable(getResources().getDrawable(interst.getImage()));

            holder.scb.setOnCheckedChangeListener(new CustomCheckBox.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CustomCheckBox checkBox, boolean isChecked) {
                    next.setEnabled(true);
                    if (isChecked){
                        if (!interestslist.contains(interst.getName())) {
                            interestslist.add(interst.getName());
                        }
                    }
                    else {
                        interestslist.remove(interst.getName());
                    }
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean checked = holder.scb.isChecked() ? false:  true;
                    holder.scb.setChecked(checked);
                }
            });


        }

        @Override
        public int getItemCount() {
            return citiesList.size();
        }
    }

}



