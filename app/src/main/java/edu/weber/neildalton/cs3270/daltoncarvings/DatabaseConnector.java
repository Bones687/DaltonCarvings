package edu.weber.neildalton.cs3270.daltoncarvings;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class DatabaseConnector
{
    // database name
    private static final String DATABASE_NAME = "Items";

    private SQLiteDatabase database; // for interacting with the database
    private DatabaseOpenHelper databaseOpenHelper; // creates the database

    // public constructor for DatabaseConnector
    public DatabaseConnector(Context context)
    {
        // create a new DatabaseOpenHelper
        databaseOpenHelper =
                new DatabaseOpenHelper(context, DATABASE_NAME, null, 1);
    }

    // open the database connection
    public void open() throws SQLException
    {
        // create or open a database for reading/writing
        database = databaseOpenHelper.getWritableDatabase();
    }

    // close the database connection
    public void close()
    {
        if (database != null)
            database.close(); // close the database connection
    }

    // inserts a new item in the database
    public long insertItem( String name,
                            String item_type, String item_main_type, double item_price, int item_qty, String item_url)
    {
        ContentValues newItem = new ContentValues();
        newItem.put("name", name);
        newItem.put("item_type", item_type);
        newItem.put("item_main_type", item_main_type);
        newItem.put("item_price", item_price);
        newItem.put("item_qty", item_qty);
        newItem.put("item_url", item_url);

        open(); // open the database
        long _id = database.insert("items", null, newItem);
        close(); // close the database
        return _id;
    }

    // updates an existing item in the database
    public void updateItem(long _id, String name,
                             String item_type, String item_main_type, double item_price, int item_qty, String item_url)
    {
        ContentValues editItem = new ContentValues();
        editItem.put("name", name);
        editItem.put("item_type", item_type);
        editItem.put("item_main_type", item_main_type);
        editItem.put("item_price", item_price);
        editItem.put("item_qty", item_qty);
        editItem.put("item_url", item_url);

        open(); // open the database
        database.update("items", editItem, "_id=" + _id, null);
        close(); // close the database
    } // end method updateitem

    // return a Cursor with all item names in the database
    public Cursor getAllItems()
    {
        return database.query("items", new String[] {"_id", "name"},null,null,null,null,"name");
    }

    // return a Cursor containing specified item's information
    public Cursor getOneItem(long id)
    {
        return database.query(
                "items", null, "_id=" + id, null, null, null, null);
    }

    public Cursor getFilteredItems(String main, String type, double low, double high)
    {
        String where = "";
        if (main != "")
            where = "item_type_main = " + main;
        if (type != "")
            where = where + " and item_type = " + type;
        if (where != "" && low != 0)
            where = where + " and item_price >= " + low;
        else if (low != 0)
            where = "item_price >= " + low;
        if (where != "" && high != 0)
            where = where + " and item_price <= " + high;
        else if (high != 0)
            where = "item_price >= " + high;
        if (where == "")
            return getAllItems();

        return database.query(
                "items",null,where,null,null,null,null
        );
    }

    // delete the item specified by the given String name
    public void deleteItem(long id)
    {
        open(); // open the database
        database.delete("items", "_id=" + id, null);
        close(); // close the database
    }

    private class DatabaseOpenHelper extends SQLiteOpenHelper
    {
        // constructor
        public DatabaseOpenHelper(Context context, String name,
                                  CursorFactory factory, int version)
        {
            super(context, name, factory, version);
        }

        // creates the items table when the database is created
        @Override
        public void onCreate(SQLiteDatabase db)
        {
            // query to create a new table named items
            String createQuery = "CREATE TABLE items" +
                    "(_id integer primary key autoincrement," +
                    "name TEXT, item_type TEXT, item_main_type," +
                    "item_price DOUBLE, item_qty INTEGER, item_url TEXT);";

            db.execSQL(createQuery); // execute query to create the database
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int newVersion)
        {
        }
    } // end class DatabaseOpenHelper
} // end class DatabaseConnector
