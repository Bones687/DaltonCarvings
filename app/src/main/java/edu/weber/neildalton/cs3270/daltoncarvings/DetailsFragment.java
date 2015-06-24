package edu.weber.neildalton.cs3270.daltoncarvings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DetailsFragment extends Fragment
{
    // callback methods implemented by MainActivity
    public interface DetailsFragmentListener
    {
        // called when a item is deleted
        public void onItemDelete();

        // called to pass Bundle of item's info for editing
        public void onEditItem(Bundle arguments);
    }

    private DetailsFragmentListener listener;

    private long rowID = -1; // selected item's rowID
    private TextView nameTextView; // displays item's name
    private TextView itemTypeTextView; // displays item's item code
    private TextView itemMainTypeTextView;
    private TextView itemPriceTextView; // displays item's start at
    private TextView itemQtyTextView; // displays item's end at
    private TextView itemUrlTextView; // displays item's end at

    // set DetailsFragmentListener when fragment attached
    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        listener = (DetailsFragmentListener) activity;
    }

    // remove DetailsFragmentListener when fragment detached
    @Override
    public void onDetach()
    {
        super.onDetach();
        listener = null;
    }

    // called when DetailsFragmentListener's view needs to be created
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        setRetainInstance(true); // save fragment across config changes

        // if DetailsFragment is being restored, get saved row ID
        if (savedInstanceState != null)
            rowID = savedInstanceState.getLong(MainActivity.ROW_ID);
        else
        {
            // get Bundle of arguments then extract the item's row ID
            Bundle arguments = getArguments();

            if (arguments != null)
                rowID = arguments.getLong(MainActivity.ROW_ID);
        }

        // inflate DetailsFragment's layout
        View view =
                inflater.inflate(R.layout.fragment_details, container, false);
        setHasOptionsMenu(true); // this fragment has menu items to display

        // get the EditTexts
        nameTextView = (TextView) view.findViewById(R.id.nameTextView);
        itemTypeTextView = (TextView) view.findViewById(R.id.itemTypeTextView);
        itemMainTypeTextView = (TextView) view.findViewById(R.id.itemMainTypeTextView);
        itemPriceTextView = (TextView) view.findViewById(R.id.itemPriceTextView);
        itemQtyTextView = (TextView) view.findViewById(R.id.itemQtyTextView);
        itemUrlTextView = (TextView) view.findViewById(R.id.itemUrlTextView);
        return view;
    }

    // called when the DetailsFragment resumes
    @Override
    public void onResume()
    {
        super.onResume();
        new LoadItemTask().execute(rowID); // load item at rowID
    }

    // save currently displayed item's row ID
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putLong(MainActivity.ROW_ID, rowID);
    }

    // display this fragment's menu items
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_details_menu, menu);
    }

    // handle menu item selections
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_edit:
                // create Bundle containing item data to edit
                Bundle arguments = new Bundle();
                arguments.putLong(MainActivity.ROW_ID, rowID);
                arguments.putCharSequence("name", nameTextView.getText());
                arguments.putCharSequence("type", itemTypeTextView.getText());
                arguments.putCharSequence("main", itemMainTypeTextView.getText());
                arguments.putCharSequence("price", itemPriceTextView.getText());
                arguments.putCharSequence("qty", itemQtyTextView.getText());
                arguments.putCharSequence("url", itemUrlTextView.getText());
                listener.onEditItem(arguments); // pass Bundle to listener
                return true;
            case R.id.action_delete:
                deleteItem();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // performs database query outside GUI thread
    private class LoadItemTask extends AsyncTask<Long, Object, Cursor>
    {
        DatabaseConnector databaseConnector =
                new DatabaseConnector(getActivity());

        // open database & get Cursor representing specified item's data
        @Override
        protected Cursor doInBackground(Long... params)
        {
            databaseConnector.open();
            return databaseConnector.getOneItem(params[0]);
        }

        // use the Cursor returned from the doInBackground method
        @Override
        protected void onPostExecute(Cursor result)
        {
            super.onPostExecute(result);
            result.moveToFirst(); // move to the first item

            // get the column index for each data item
            int nameIndex = result.getColumnIndex("name");
            int type = result.getColumnIndex("item_type");
            int mainType = result.getColumnIndex("item_main_type");
            int price = result.getColumnIndex("item_price");
            int qty = result.getColumnIndex("item_qty");
            int url = result.getColumnIndex("item_url");

            // fill TextViews with the retrieved data
            nameTextView.setText(result.getString(nameIndex));
            itemTypeTextView.setText(result.getString(type));
            itemMainTypeTextView.setText(result.getString(mainType));
            itemPriceTextView.setText(result.getString(price));
            itemQtyTextView.setText(result.getString(qty));
            itemUrlTextView.setText(result.getString(url));

            result.close(); // close the result cursor
            databaseConnector.close(); // close database connection
        } // end method onPostExecute
    } // end class LoaditemTask

    // delete a item
    private void deleteItem()
    {
        // use FragmentManager to display the confirmDelete DialogFragment
        confirmDelete.show(getFragmentManager(), "confirm delete");
    }

    // DialogFragment to confirm deletion of item
    private DialogFragment confirmDelete =
            new DialogFragment()
            {
                // create an AlertDialog and return it
                @Override
                public Dialog onCreateDialog(Bundle bundle)
                {
                    // create a new AlertDialog Builder
                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(getActivity());

                    builder.setTitle(R.string.confirm_title);
                    builder.setMessage(R.string.confirm_message);

                    // provide an OK button that simply dismisses the dialog
                    builder.setPositiveButton(R.string.button_delete,
                            new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(
                                        DialogInterface dialog, int button)
                                {
                                    final DatabaseConnector databaseConnector =
                                            new DatabaseConnector(getActivity());

                                    // AsyncTask deletes item and notifies listener
                                    AsyncTask<Long, Object, Object> deleteTask =
                                            new AsyncTask<Long, Object, Object>()
                                            {
                                                @Override
                                                protected Object doInBackground(Long... params)
                                                {
                                                    databaseConnector.deleteItem(params[0]);
                                                    return null;
                                                }

                                                @Override
                                                protected void onPostExecute(Object result)
                                                {
                                                    listener.onItemDelete();
                                                }
                                            }; // end new AsyncTask

                                    // execute the AsyncTask to delete item at rowID
                                    deleteTask.execute(new Long[] { rowID });
                                } // end method onClick
                            } // end anonymous inner class
                    ); // end call to method setPositiveButton

                    builder.setNegativeButton(R.string.button_cancel, null);
                    return builder.create(); // return the AlertDialog
                }
            }; // end DialogFragment anonymous inner class
} // end class DetailsFragment

