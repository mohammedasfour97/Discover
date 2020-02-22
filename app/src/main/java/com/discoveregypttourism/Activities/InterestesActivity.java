package com.discoveregypttourism.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.discoveregypttourism.DB.TinyDB;
import com.discoveregypttourism.MainActivity;
import com.discoveregypttourism.Models.Interest;
import com.discoveregypttourism.R;

import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import net.igenius.customcheckbox.CustomCheckBox;

import java.util.ArrayList;
import java.util.List;

public class InterestesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<Interest> itemsList;
    private InterestesActivity.InterestsActivityAdapter mAdapter;
    private Button next ;
    private TinyDB db ;
    private List<String> interestslist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interests);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        next = findViewById(R.id.next);

        itemsList = new ArrayList<>();
        db = new TinyDB(this);
        if (db.getListString("interests") == null){
            interestslist = new ArrayList<>();
        }
        else {
            interestslist = db.getListString("interests");
        }
        mAdapter = new InterestesActivity.InterestsActivityAdapter(this, itemsList);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this , 3);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setNestedScrollingEnabled(false);
        itemsList.clear();

        fill_interests();

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.putListString("interests", (ArrayList<String>) interestslist);
                startActivity(new Intent(InterestesActivity.this , MainActivity.class));
            }
        });

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

    private class InterestsActivityAdapter extends RecyclerView.Adapter<InterestesActivity.InterestsActivityAdapter.MyViewHolder> {
        private Context context;
        private List<Interest> citiesList;


        public class MyViewHolder extends RecyclerView.ViewHolder {
            Context context;
            public ImageView image ;
            public CustomCheckBox scb ;
            public TextView name ;

            public MyViewHolder(View view) {
                super(view);
                image = view.findViewById(R.id.image);
                scb = (CustomCheckBox) view.findViewById(R.id.cb);
                name = view.findViewById(R.id.name);
                context = itemView.getContext();


            }
        }

        public InterestsActivityAdapter(Context context, List<Interest> citiesList) {
            this.context = context;
            this.citiesList = citiesList;
        }

        @Override
        public InterestesActivity.InterestsActivityAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.interest_item, parent, false);

            return new InterestesActivity.InterestsActivityAdapter.MyViewHolder(itemView);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onBindViewHolder(final InterestesActivity.InterestsActivityAdapter.MyViewHolder holder, final int position) {
            final Interest interst = citiesList.get(position);

            if (interestslist.contains(interst.getName())){
                holder.scb.setChecked(true);
            }
            else {
                holder.scb.setChecked(false);
            }


            holder.scb.setCheckedColor(getResources().getColor(R.color.white));
            holder.scb.setUnCheckedColor(getResources().getColor(R.color.white));

            holder.name.setText(interst.getName());

            holder.image.setImageDrawable(getResources().getDrawable(interst.getImage()));

            holder.scb.setOnCheckedChangeListener(new CustomCheckBox.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CustomCheckBox checkBox, boolean isChecked) {
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



