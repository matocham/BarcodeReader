package pl.edu.pb.wi.projekt.barcodereader.activities;

import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import pl.edu.pb.wi.projekt.barcodereader.DeviceInfo;
import pl.edu.pb.wi.projekt.barcodereader.Person;
import pl.edu.pb.wi.projekt.barcodereader.R;
import pl.edu.pb.wi.projekt.barcodereader.Section;
import pl.edu.pb.wi.projekt.barcodereader.Utils;
import pl.edu.pb.wi.projekt.barcodereader.ValidateException;
import pl.edu.pb.wi.projekt.barcodereader.database.Contract;
import pl.edu.pb.wi.projekt.barcodereader.fragments.SearchItemFragment;

public class AddItemActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, View.OnClickListener {
    private String barcode;

    private EditText inventoryName;
    private EditText serialNumber;
    private EditText partName;
    private Button addDate;
    private EditText price;
    private EditText amount;
    private EditText room;
    private EditText comments;
    private TextView inventoryNumber;
    private Spinner person;
    private Spinner section;
    private Button save;
    private DateFormat dateFormat = SimpleDateFormat.getDateInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        barcode = getIntent().getStringExtra(SearchItemFragment.BARCODE_KEY);
        inventoryNumber = (TextView) findViewById(R.id.inventoryNumber);
        inventoryName = (EditText) findViewById(R.id.inventoryName);
        serialNumber = (EditText) findViewById(R.id.serialNumber);
        partName = (EditText) findViewById(R.id.partName);
        addDate = (Button) findViewById(R.id.addedDate);
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
        addDate.setOnClickListener(this);
        addDate.setText(dateFormat.format(Calendar.getInstance().getTime()));
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
        try {
            DeviceInfo info = new DeviceInfo();
            info.setBarcode(barcode);
            info.setSetName(inventoryName.getText().toString());
            info.setSerialNr(serialNumber.getText().toString());
            info.setPartName(partName.getText().toString());
            info.setReciveDate(addDate.getText().toString());
            info.setPrice(price.getText().toString());
            info.setAmount(amount.getText().toString());
            info.setRoom(room.getText().toString());
            info.setComments(comments.getText().toString());
            info.setPersonId(((Person) person.getSelectedItem()).getId() + "");
            info.setSectionId(((Section) section.getSelectedItem()).getId() + "");
            info.validate();
            getContentResolver().insert(Contract.Devices.Search.CONTENT_URI, info.toContentValues());
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
        } catch (ValidateException e) {
            e.printStackTrace();
            Utils.showInfoDialog(this, getString(R.string.validation_dialog_title), getString(e.getId()));
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, dayOfMonth);
        addDate.setText(dateFormat.format(c.getTime()));
    }

    @Override
    public void onClick(View view) {
        Calendar c = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, this, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMaxDate(c.getTimeInMillis());
        datePickerDialog.show();
    }
}
