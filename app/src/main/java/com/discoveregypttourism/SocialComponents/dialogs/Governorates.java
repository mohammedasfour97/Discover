package com.discoveregypttourism.SocialComponents.dialogs;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import com.discoveregypttourism.R;


public class Governorates extends DialogFragment {


   // Button btn;
    ListView lv;
    SearchView sv;
    ArrayAdapter<String> adapter;
    String gover;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View rootView=inflater.inflate(R.layout.governorates_selection_fragment, null);
        //SET TITLE DIALOG TITLE
        getDialog().setTitle(getResources().getString(R.string.select_country));
        //BUTTON,LISTVIEW,SEARCHVIEW INITIALIZATIONS
        lv=(ListView) rootView.findViewById(R.id.listView1);
        sv=(SearchView) rootView.findViewById(R.id.searchView1);
       // btn=(Button) rootView.findViewById(R.id.next);
        //CREATE AND SET ADAPTER TO LISTVIEW
        String[] players={getResources().getString(R.string.cairo),
                getResources().getString(R.string.giza),
                getResources().getString(R.string.alex),
                getResources().getString(R.string.luxor),
                getResources().getString(R.string.aswan),
                getResources().getString(R.string.sharm),
                getResources().getString(R.string.rasheed),
                getResources().getString(R.string.hargada),
                getResources().getString(R.string.sahl_hashesh),
                getResources().getString(R.string.gona),
                getResources().getString(R.string.saphaga),
                getResources().getString(R.string.soma_bay),
                getResources().getString(R.string.marsa_matroh),
                getResources().getString(R.string.marsa_alam),
                getResources().getString(R.string.alameen),
                getResources().getString(R.string.ain_shams),
                getResources().getString(R.string.taba),
                getResources().getString(R.string.dahab),
                getResources().getString(R.string.siwa)};
        adapter=new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1,players);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String text = String.valueOf(lv.getAdapter().getItemId(i));
            }
        });
        //SEARCH
        sv.setQueryHint("Search..");
        sv.setOnQueryTextListener(new OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String txt) {
                // TODO Auto-generated method stub
                return false;
            }
            @Override
            public boolean onQueryTextChange(String txt) {
                // TODO Auto-generated method stub
                adapter.getFilter().filter(txt);
                return false;
            }
        });
        //BUTTON
      /*  btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {

                dismiss();
            }
        });
        */
        return rootView;
    }
}
