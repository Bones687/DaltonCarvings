package edu.weber.neildalton.cs3270.daltoncarvings;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;


/**
 * A simple {@link Fragment} subclass.
 */
public class FilterFragment extends Fragment {
    // callback method implemented by MainActivity
    public interface FilterFragmentListener
    {
        // called after edit completed so item can be redisplayed
        public void onFilter(String main, String type, double low, double high);
    }

    private FilterFragmentListener listener;

    private Bundle itemInfoBundle; // arguments for editing a item
    // EditTexts for item information
    private Spinner type;
    private Spinner main;
    private EditText low;
    private EditText high;

    // set AddEditFragmentListener when Fragment attached
    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        listener = (FilterFragmentListener) activity;
    }

    // remove AddEditFragmentListener when Fragment detached
    @Override
    public void onDetach()
    {
        super.onDetach();
        listener = null;
    }

    // called when Fragment's view needs to be created
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        setRetainInstance(true); // save fragment across config changes
        setHasOptionsMenu(true); // fragment has menu items to display

        // inflate GUI and get references to EditTexts
        View view =
                inflater.inflate(R.layout.fragment_filter, container, false);

        main = (Spinner) view.findViewById(R.id.mainSpin);
        type = (Spinner) view.findViewById(R.id.typeSpin);
        low = (EditText) view.findViewById(R.id.lowPrice);
        high = (EditText) view.findViewById(R.id.highPrice);
        String [] values ={""};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, values);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        type.setAdapter(adapter);

        String [] mainValues = {""};
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, mainValues);
        adapter2.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        main.setAdapter(adapter2);


        main.setOnItemSelectedListener(getNewListForSpinner);
        main.setOnTouchListener(setMainSpinner);

        itemInfoBundle = getArguments(); // null if creating new item

        if (itemInfoBundle != null)
        {
            low.setText(itemInfoBundle.getString("low"));
            high.setText(itemInfoBundle.getString("high"));
        }

        // set Save item Button's event listener
        Button filter =
                (Button) view.findViewById(R.id.filter);
        filter.setOnClickListener(filterClicked);
        return view;
    }

    View.OnTouchListener setMainSpinner = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            String [] mainValues = {"Material", "Item"};
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, mainValues);
            adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
            main.setAdapter(adapter);
            return true;
        }
    };

    AdapterView.OnItemSelectedListener getNewListForSpinner = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (main.getSelectedItem().toString() == "Material"){
                String [] values = {"Wood", "Metal", "Organic", "Plastic", "Knife Blank", "Cufflink Blank", "Ring Blank", "Other"};
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, values);
                adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
                type.setAdapter(adapter);
            }
            else if (main.getSelectedItem().toString() == "Item") {
                String [] values = {"Knife", "Cufflinks", "Pendants", "Ring", "Custom Order"};
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, values);
                adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
                type.setAdapter(adapter);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };
    // responds to event generated when user saves a item
    View.OnClickListener filterClicked = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            // AsyncTask to save item, then notify listener
            AsyncTask<Object, Object, Object> filterItemTask =
                    new AsyncTask<Object, Object, Object>()
                    {
                        @Override
                        protected Object doInBackground(Object... params)
                        {
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Object result)
                        {
                            // hide soft keyboard
                            InputMethodManager imm = (InputMethodManager)
                                    getActivity().getSystemService(
                                            Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(
                                    getView().getWindowToken(), 0);


                            double tempLow = !low.getText().toString().equals("") ? Double.parseDouble(low.getText().toString()) : 0;
                            double tempHigh = !high.getText().toString().equals("") ? Double.parseDouble(high.getText().toString()) : 0;
                            String tempMain = !main.getSelectedItem().toString().equals("") ? main.getSelectedItem().toString() : "";
                            String tempType = !type.getSelectedItem().toString().equals("") ? type.getSelectedItem().toString() : "";
                            if (tempLow > tempHigh)
                            {
                                double temp = tempLow;
                                tempLow = tempHigh;
                                tempHigh = temp;
                            }

                            listener.onFilter(
                                              tempMain,
                                              tempType,
                                              tempLow,
                                              tempHigh
                                              );
                        }
                    }; // end AsyncTask

            filterItemTask.execute((Object[]) null);
        } // end method onClick
    }; // end OnClickListener saveitemButtonClicked
} // end class AddEditFragment

