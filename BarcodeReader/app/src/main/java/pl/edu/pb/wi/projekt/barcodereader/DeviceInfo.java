package pl.edu.pb.wi.projekt.barcodereader;

import android.content.ContentValues;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import pl.edu.pb.wi.projekt.barcodereader.database.Contract;

/**
 * Created by Mateusz on 29.11.2017.
 */

public class DeviceInfo {
    private String barcode;
    private String setName;
    private String serialNr;
    private String partName;
    private String reciveDate;
    private String price;
    private String amount;
    private String room;
    private String comments;
    private String personId;
    private String sectionId;

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public void setSetName(String setName) {
        this.setName = setName;
    }

    public void setSerialNr(String serialNr) {
        this.serialNr = serialNr;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    public void setReciveDate(String reciveDate) {
        this.reciveDate = reciveDate;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(Contract.Devices.COLUMN_INVENTARY_NR, barcode);
        values.put(Contract.Devices.COLUMN_INVENTARY_NAME, setName);
        values.put(Contract.Devices.COLUMN_SERIAL_NR, serialNr);
        values.put(Contract.Devices.COLUMN_PART_NAME, partName);
        values.put(Contract.Devices.COLUMN_RECIVE_DATE, reciveDate);
        values.put(Contract.Devices.COLUMN_PART_PRICE, price);
        values.put(Contract.Devices.COLUMN_ACOUNTANCY_AMOUNT, amount);
        values.put(Contract.Devices.COLUMN_ROOM, room);
        values.put(Contract.Devices.COLUMN_COMMENTS, comments);
        values.put(Contract.Devices.COLUMN_PERSON_ID, personId);
        values.put(Contract.Devices.COLUMN_SECTION_ID, sectionId);
        return values;
    }

    public void validate() throws ValidateException {
        validateIsEmpty();
        validateNumbers();
    }

    private void validateIsEmpty() throws ValidateException {
        if (barcode.isEmpty()) {
            throw new ValidateException(R.string.barcode_is_empty);
        }
        if (setName.isEmpty()) {
            throw new ValidateException(R.string.set_name_empty);
        }
        if (serialNr.isEmpty()) {
            throw new ValidateException(R.string.serial_nr_empty);
        }
        if (partName.isEmpty()) {
            throw new ValidateException(R.string.part_name_empty);
        }
        if (price.isEmpty()) {
            throw new ValidateException(R.string.price_empty);
        }
        if (amount.isEmpty()) {
            throw new ValidateException(R.string.amount_is_empty);
        }
        if (room.isEmpty()) {
            throw new ValidateException(R.string.room_is_empty);
        }
        if (personId.isEmpty()) {
            throw new ValidateException(R.string.person_not_set);
        }
        if (sectionId.isEmpty()) {
            throw new ValidateException(R.string.section_not_set);
        }
        if (reciveDate.isEmpty()) {
            throw new ValidateException(R.string.date_is_empty);
        }
    }

    private void validateNumbers() throws ValidateException {
        try {
            double priceValue = Double.parseDouble(price);
            if (priceValue <= 0) {
                throw new ValidateException(R.string.price_below_zero);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new ValidateException(R.string.price_not_number);
        }

        try {
            int amountValue = Integer.parseInt(amount);
            if (amountValue <= 0) {
                throw new ValidateException(R.string.amount_not_positive);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new ValidateException(R.string.amount_not_number);
        }
    }

    private void validateDate() throws ValidateException {
        try {
            Date d = SimpleDateFormat.getDateInstance().parse(reciveDate);
            if (d.after(Calendar.getInstance().getTime())) {
                throw new ValidateException(R.string.date_in_future);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            throw new ValidateException(R.string.date_fromat_wrong);
        }
    }
}
