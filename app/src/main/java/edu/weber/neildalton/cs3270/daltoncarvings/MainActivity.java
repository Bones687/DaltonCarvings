package edu.weber.neildalton.cs3270.daltoncarvings;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;

public class MainActivity extends Activity
        implements ItemListFragment.ItemListFragmentListener,
        DetailsFragment.DetailsFragmentListener,
        AddEditFragment.AddEditFragmentListener,
        FilterFragment.FilterFragmentListener
{
    private FilterFragment filterFragment = new FilterFragment();
    // keys for storing row ID in Bundle passed to a fragment
    public static final String ROW_ID = "row_id";

    ItemListFragment itemListFragment; // displays item list

    // display itemListFragment when MainActivity first loads
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // return if Activity is being restored, no need to recreate GUI
        if (savedInstanceState != null)
            return;

        // check whether layout contains fragmentContainer (phone layout);
        // itemListFragment is always displayed
        if (findViewById(R.id.fragmentContainer) != null)
        {
            // create itemListFragment
            itemListFragment = new ItemListFragment();

            // add the fragment to the FrameLayout
            FragmentTransaction transaction =
                    getFragmentManager().beginTransaction();
            transaction.add(R.id.fragmentContainer, itemListFragment);
            transaction.commit(); // causes itemListFragment to display
        }
    }

    // called when MainActivity resumes
    @Override
    protected void onResume()
    {
        super.onResume();

    }

    // display DetailsFragment for selected item
    @Override
    public void onItemSelected(long rowID)
    {
        displayItem(rowID, R.id.fragmentContainer);
    }

    // display a item
    private void displayItem(long rowID, int viewID)
    {
        DetailsFragment detailsFragment = new DetailsFragment();

        // specify rowID as an argument to the DetailsFragment
        Bundle arguments = new Bundle();
        arguments.putLong(ROW_ID, rowID);
        detailsFragment.setArguments(arguments);

        // use a FragmentTransaction to display the DetailsFragment
        FragmentTransaction transaction =
                getFragmentManager().beginTransaction();
        transaction.replace(viewID, detailsFragment);
        transaction.addToBackStack(null);
        transaction.commit(); // causes DetailsFragment to display
    }

    // display the AddEditFragment to add a new item
    @Override
    public void onAddItem()
    {
        displayAddEditFragment(R.id.fragmentContainer, null);
    }

    @Override
    public void onFilterItem() { displayFilterOptions(R.id.fragmentContainer, null); }

    // display fragment for adding a new or editing an existing item
    private void displayAddEditFragment(int viewID, Bundle arguments)
    {
        AddEditFragment addEditFragment = new AddEditFragment();

        if (arguments != null) // editing existing item
            addEditFragment.setArguments(arguments);

        // use a FragmentTransaction to display the AddEditFragment
        FragmentTransaction transaction =
                getFragmentManager().beginTransaction();
        transaction.replace(viewID, addEditFragment);
        transaction.addToBackStack(null);
        transaction.commit(); // causes AddEditFragment to display
    }

    private void displayFilterOptions(int viewID, Bundle arguments)
    {

        if (arguments != null) // editing existing item
            filterFragment.setArguments(arguments);

        FragmentTransaction transaction =
                getFragmentManager().beginTransaction();
        transaction.replace(viewID, filterFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // return to item list when displayed item deleted
    @Override
    public void onItemDelete()
    {
        getFragmentManager().popBackStack(); // removes top of back stack

        itemListFragment.updateItemList();
    }

    // display the AddEditFragment to edit an existing item
    @Override
    public void onEditItem(Bundle arguments)
    {
        displayAddEditFragment(R.id.fragmentContainer, arguments);
    }

    // update GUI after new item or updated item saved
    @Override
    public void onAddEditCompleted(long rowID)
    {
        getFragmentManager().popBackStack(); // removes top of back stack
        getFragmentManager().popBackStack(); // removes top of back stack
        itemListFragment.updateItemList(); // refresh items
    }

    @Override
    public void onFilter(String main, String type, double low, double high)
    {

        itemListFragment.updateFilteredItemList(main, type, low, high);
    }
}

