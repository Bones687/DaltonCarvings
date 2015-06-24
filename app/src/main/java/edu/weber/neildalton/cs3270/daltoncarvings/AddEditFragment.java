package edu.weber.neildalton.cs3270.daltoncarvings;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class AddEditFragment extends Fragment
{
    // callback method implemented by MainActivity
    public interface AddEditFragmentListener
    {
        // called after edit completed so item can be redisplayed
        public void onAddEditCompleted(long rowID);
    }

    private AddEditFragmentListener listener;

    private long rowID; // database row ID of the item
    private Bundle itemInfoBundle; // arguments for editing a item

    // EditTexts for item information
    private EditText nameEditText;
    private Spinner itemTypeEditText;
    private Spinner itemMainTypeEditText;
    private EditText itemPriceEditText;
    private EditText itemQuantityEditText;
    private EditText itemUrlEditText;

    // set AddEditFragmentListener when Fragment attached
    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        listener = (AddEditFragmentListener) activity;
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
                inflater.inflate(R.layout.fragment_add_edit, container, false);
        nameEditText = (EditText) view.findViewById(R.id.nameEditText);
        itemTypeEditText = (Spinner) view.findViewById(R.id.itemTypeEditText);
        itemMainTypeEditText = (Spinner) view.findViewById(R.id.itemMainTypeEditText);
        itemPriceEditText = (EditText) view.findViewById(R.id.itemPriceEditText);
        itemQuantityEditText = (EditText) view.findViewById(R.id.itemQuantityEditText);
        itemUrlEditText = (EditText) view.findViewById(R.id.itemUrlEditText);


        String [] values =
                {"Wood", "Metal", "Organic", "Plastic", "Knife Blank", "Cufflink Blank", "Ring Blank", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, values);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        itemTypeEditText.setAdapter(adapter);

        String [] mainValues = {"Material", "Item"};
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, mainValues);
        adapter2.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        itemMainTypeEditText.setAdapter(adapter2);

        itemInfoBundle = getArguments(); // null if creating new item

        if (itemInfoBundle != null)
        {
            rowID = itemInfoBundle.getLong(MainActivity.ROW_ID);
            nameEditText.setText(itemInfoBundle.getString("name"));
            itemPriceEditText.setText(itemInfoBundle.getString("price"));
            itemQuantityEditText.setText(itemInfoBundle.getString("qty"));
            itemUrlEditText.setText(itemInfoBundle.getString("url"));
        }

        // set Save item Button's event listener
        Button saveitemButton =
                (Button) view.findViewById(R.id.saveitemButton);
        saveitemButton.setOnClickListener(saveitemButtonClicked);

        itemMainTypeEditText.setOnItemSelectedListener(getNewListForSpinner);
        return view;
    }

    AdapterView.OnItemSelectedListener getNewListForSpinner = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (itemMainTypeEditText.getSelectedItem().toString() == "Material"){
                String [] values = {"Wood", "Metal", "Organic", "Plastic", "Knife Blank", "Cufflink Blank", "Ring Blank", "Other"};
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, values);
                adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
                itemTypeEditText.setAdapter(adapter);
            }
            else {
                String [] values = {"Knife", "Cufflinks", "Pendants", "Ring", "Custom Order"};
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, values);
                adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
                itemTypeEditText.setAdapter(adapter);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };
    // responds to event generated when user saves a item
    OnClickListener saveitemButtonClicked = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if (nameEditText.getText().toString().trim().length() != 0)
            {
                // AsyncTask to save item, then notify listener
                AsyncTask<Object, Object, Object> saveitemTask =
                        new AsyncTask<Object, Object, Object>()
                        {
                            @Override
                            protected Object doInBackground(Object... params)
                            {
                                saveitem(); // save item to the database
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

                                listener.onAddEditCompleted(rowID);
                            }
                        }; // end AsyncTask

                // save the item to the database using a separate thread
                saveitemTask.execute((Object[]) null);
            }
            else // required item name is blank, so display error dialog
            {
                DialogFragment errorSaving =
                        new DialogFragment()
                        {
                            @Override
                            public Dialog onCreateDialog(Bundle savedInstanceState)
                            {
                                AlertDialog.Builder builder =
                                        new AlertDialog.Builder(getActivity());
                                builder.setMessage(R.string.error_message);
                                builder.setPositiveButton(R.string.ok, null);
                                return builder.create();
                            }
                        };

                errorSaving.show(getFragmentManager(), "error saving item");
            }
        } // end method onClick
    }; // end OnClickListener saveitemButtonClicked

    // saves item information to the database
    private void saveitem()
    {
        // get DatabaseConnector to interact with the SQLite database
        DatabaseConnector databaseConnector =
                new DatabaseConnector(getActivity());
        if (itemPriceEditText.getText().toString() == "")
            itemPriceEditText.setText("0");
        if (itemQuantityEditText.getText().toString() == "")
            itemQuantityEditText.setText("0");

        if (itemInfoBundle == null)
        {
            rowID = databaseConnector.insertItem(
                    nameEditText.getText().toString(),
                    itemTypeEditText.getSelectedItem().toString(),
                    itemMainTypeEditText.getSelectedItem().toString(),
                    Double.parseDouble(itemPriceEditText.getText().toString()),
                    Integer.parseInt(itemQuantityEditText.getText().toString()),
                    itemUrlEditText.getText().toString());
        }
        else
        {
            databaseConnector.updateItem(rowID,
                    nameEditText.getText().toString(),
                    itemTypeEditText.getSelectedItem().toString(),
                    itemMainTypeEditText.getSelectedItem().toString(),
                    Double.parseDouble(itemPriceEditText.getText().toString()),
                    Integer.parseInt(itemQuantityEditText.getText().toString()),
                    itemUrlEditText.getText().toString());

        }
    } // end method saveitem
} // end class AddEditFragment

