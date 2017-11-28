package pl.edu.pb.wi.projekt.barcodereader.activities;

import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import pl.edu.pb.wi.projekt.barcodereader.Person;
import pl.edu.pb.wi.projekt.barcodereader.R;
import pl.edu.pb.wi.projekt.barcodereader.Section;
import pl.edu.pb.wi.projekt.barcodereader.Utils;
import pl.edu.pb.wi.projekt.barcodereader.database.Contract;
import pl.edu.pb.wi.projekt.barcodereader.fragments.SearchItemFragment;

public class AddItemActivity extends AppCompatActivity {
    private String barcode;

    private EditText inventoryName;
    private EditText serialNumber;
    private EditText partName;
    private EditText addDate;
    private EditText price;
    private EditText amount;
    private EditText room;
    private EditText comments;
    private TextView inventoryNumber;
    private Spinner person;
    private Spinner section;
    private Button save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        barcode = getIntent().getStringExtra(SearchItemFragment.BARCODE_KEY);
        inventoryNumber = (TextView) findViewById(R.id.inventoryNumber);
        inventoryName = (EditText) findViewById(R.id.inventoryName);
        serialNumber = (EditText) findViewById(R.id.serialNumber);
        partName = (EditText) findViewById(R.id.partName);
        addDate = (EditText) findViewById(R.id.addedDate);
        price = (EditText) findViewById(R.id.price);
        amount = (EditText) findViewById(R.id.amount);
        room = (EditText) findViewById(R.id.room);
        comments = (EditText) findViewById(R.id.comments);
        person = (Spinner) findViewById(R.id.person);
        section = (Spinner) findViewById(R.id.section);
        save = (Button) findViewById(R.id.addButton);

        person.setAdapter(getPersonsContent());
        section.setAdapter(getSectionContent());
        inventoryNumber.setText(barcode);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveItem();
            }
        });
    }

    private ArrayAdapter<Person> getPersonsContent() {
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(Contract.Persons.CONTENT_URI,
                Contract.Persons.PROJECTION_ALL, null, null, Contract.Persons.SORT_ORDER_DEFAULT);
        List<Person> searchResults = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String surname = cursor.getString(2);
                searchResults.add(new Person(name, surname, id));
            } while (cursor.moveToNext());
        }
        cursor.close();
        ArrayAdapter<Person> persons = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, searchResults.toArray(new Person[0]));
        return persons;
    }

    private ArrayAdapter<Section> getSectionContent() {
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(Contract.Sections.CONTENT_URI,
                Contract.Sections.PROJECTION_ALL, null, null, Contract.Sections.SORT_ORDER_DEFAULT);
        List<Section> searchResults = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                searchResults.add(new Section(name, id));
            } while (cursor.moveToNext());
        }
        cursor.close();
        ArrayAdapter<Section> sections = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, searchResults.toArray(new Section[0]));
        return sections;
    }

    private void saveItem() {
        if (validate()) {
            ContentValues values = new ContentValues();
            values.put(Contract.Devices.COLUMN_INVENTARY_NR, barcode);
            values.put(Contract.Devices.COLUMN_INVENTARY_NAME, inventoryName.getText().toString());
            values.put(Contract.Devices.COLUMN_SERIAL_NR, serialNumber.getText().toString());
            values.put(Contract.Devices.COLUMN_PART_NAME, partName.getText().toString());
            values.put(Contract.Devices.COLUMN_RECIVE_DATE, addDate.getText().toString());
            values.put(Contract.Devices.COLUMN_PART_PRICE, price.getText().toString());
            values.put(Contract.Devices.COLUMN_ACOUNTANCY_AMOUNT, amount.getText().toString());
            values.put(Contract.Devices.COLUMN_ROOM, room.getText().toString());
            values.put(Contract.Devices.COLUMN_COMMENTS, comments.getText().toString());
            values.put(Contract.Devices.COLUMN_PERSON_ID, ((Person) person.getSelectedItem()).getId());
            values.put(Contract.Devices.COLUMN_SECTION_ID, ((Section) section.getSelectedItem()).getId());

            getContentResolver().insert(Contract.Devices.Search.CONTENT_URI, values);
            Utils.showInfoDialog(this, "", getString(R.string.item_added_message), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    Intent intent = new Intent(AddItemActivity.this, SearchActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.setAction(SearchActivity.SCAN_RESULT_ACTION);
                    intent.putExtra(SearchActivity.BARCODE_KEY, barcode);
                    startActivity(intent);
                }
            });
        } else {
            Utils.showInfoDialog(this, getString(R.string.validation_dialog_title), "[set message]");
        }
    }

    private boolean validate() {
        return true;
    }
}
