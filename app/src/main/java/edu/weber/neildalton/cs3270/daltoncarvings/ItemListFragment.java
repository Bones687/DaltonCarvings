package edu.weber.neildalton.cs3270.daltoncarvings;

import android.app.Activity;
import android.app.ListFragment;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class ItemListFragment extends ListFragment
{
    private String main = "";
    private String type = "";
    private double low = 0;
    private double high = 0;
    // callback methods implemented by MainActivity
    public interface ItemListFragmentListener
    {
        // called when user selects a item
        public void onItemSelected(long rowID);

        // called when user decides to add a item
        public void onAddItem();

        public void onFilterItem();

    }

    private ItemListFragmentListener listener;

    private ListView itemListView; // the ListActivity's ListView
    private CursorAdapter itemAdapter; // adapter for ListView

    // set itemListFragmentListener when fragment attached
    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        listener = (ItemListFragmentListener) activity;
    }


    @Override
    public void onDetach()
    {
        super.onDetach();
        listener = null;
    }

    // called after View is created
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        setRetainInstance(true); // save fragment across config changes
        setHasOptionsMenu(true); // this fragment has menu items to display

        // set text to display when there are no items
        setEmptyText(getResources().getString(R.string.no_items));

        // get ListView reference and configure ListView
        itemListView = getListView();
        itemListView.setOnItemClickListener(viewItemListener);
        itemListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // map each item's name to a TextView in the ListView layout
        String[] from = new String[] { "name" };
        int[] to = new int[] { android.R.id.text1 };
        itemAdapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_1, null, from, to, 0);
        setListAdapter(itemAdapter); // set adapter that supplies data
    }

    // responds to the user touching a item's name in the ListView
    OnItemClickListener viewItemListener = new OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view,
                                int position, long id)
        {
            listener.onItemSelected(id); // pass selection to MainActivity
        }
    }; // end viewitemListener

    // when fragment resumes, use a GetitemsTask to load items
    @Override
    public void onResume()
    {
        super.onResume();
        new GetItemTask().execute((Object[]) null);
    }

    // performs database query outside GUI thread
    private class GetItemTask extends AsyncTask<Object, Object, Cursor>
    {
        DatabaseConnector databaseConnector =
                new DatabaseConnector(getActivity());

        // open database and return Cursor for all items
        @Override
        protected Cursor doInBackground(Object... params)
        {
            databaseConnector.open();
            return databaseConnector.getFilteredItems(main, type, low, high);
        }

        // use the Cursor returned from the doInBackground method
        @Override
        protected void onPostExecute(Cursor result)
        {
            itemAdapter.changeCursor(result); // set the adapter's Cursor
            databaseConnector.close();
        }
    } // end class GetitemsTask

    // performs database query outside GUI thread
    private class GetItemFilterTask extends AsyncTask<Object, Object, Cursor>
    {
        DatabaseConnector databaseConnector =
                new DatabaseConnector(getActivity());

        // open database and return Cursor for all items
        @Override
        protected Cursor doInBackground(Object... params)
        {
            databaseConnector.open();
            return databaseConnector.getFilteredItems(main, type, low, high);
        }

        // use the Cursor returned from the doInBackground method
        @Override
        protected void onPostExecute(Cursor result)
        {
            itemAdapter.changeCursor(result); // set the adapter's Cursor
            databaseConnector.close();
        }
    } // end class GetitemsTask

    // when fragment stops, close Cursor and remove from itemAdapter
    @Override
    public void onStop()
    {
        Cursor cursor = itemAdapter.getCursor(); // get current Cursor
        itemAdapter.changeCursor(null); // adapter now has no Cursor

        if (cursor != null)
            cursor.close(); // release the Cursor's resources

        super.onStop();
    }

    // display this fragment's menu items
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_item_list_menu, menu);
    }

    // handle choice from options menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_add:
                listener.onAddItem();
                return true;
            case R.id.action_filter:
                listener.onFilterItem();
                return true;
        }

        return super.onOptionsItemSelected(item); // call super's method
    }

    // update data set
    public void updateItemList()
    {
        new GetItemTask().execute((Object[]) null);
    }

    public void updateFilteredItemList(String iMain, String iType, double iLow, double iHigh) {
        main = iMain;
        type = iType;
        low = iLow;
        high = iHigh;
        new GetItemFilterTask().execute((Object[]) null);
    }
} // end class itemListFragment


