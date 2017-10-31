package pl.edu.pb.wi.projekt.barcodereader.database;

import android.content.ContentResolver;
import android.net.Uri;

/**
 * Created by Mateusz on 28.09.2016.
 * Contract class used to provide information about database to user
 * Every tableName has it's own class with constants and helper methods
 * Modification of databaser requires changes in this file and optionally in searchable.xml in resources
 * in case columnName or tables names changed
 */
public final class Contract {
    public static final String BASE_LIMIT = "50";

    public static final String AUTHORITY =
            "pl.edu.pb.wi.projekt.barcodereader";

    public static final Uri CONTENT_URI =
            Uri.parse("content://" + AUTHORITY);

    public static final class Devices {
        public static final String TABLE_NAME = "DEVICES";
        public static final String COLUMN_ID = "DEVICE_ID";
        public static final String COLUMN_INVENTARY_NR = "INVENTORY_NUMBER";
        public static final String COLUMN_INVENTARY_NAME = "INVENTORY_NAME";
        public static final String COLUMN_SERIAL_NR = "SERIAL_NUMBER";
        public static final String COLUMN_PART_NAME = "PART_NAME";
        public static final String COLUMN_PART_PRICE = "PART_VALUE";
        public static final String COLUMN_RECIVE_DATE = "DATE_OF_ACCEPTANCE";
        public static final String COLUMN_ACOUNTANCY_AMOUNT = "NUMBER_OF_BOOK";
        public static final String COLUMN_PERSON_ID = "PERSON_ID";
        public static final String COLUMN_ROOM = "ROOM";
        public static final String COLUMN_COMMENTS = "COMMENTS";
        public static final String COLUMN_SECTION_ID = "SECTION_ID";

        public static class Search {
            public static final String LAST_PATH_SEGMENT = "search";
            /**
             * The content URI for this tableName.
             */
            public static final Uri CONTENT_URI = Uri.withAppendedPath(Contract.Devices.CONTENT_URI, LAST_PATH_SEGMENT);
        }

        /**
         * The content URI for this tableName.
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(Contract.CONTENT_URI, TABLE_NAME);
        /**
         * The mime type of a directory of items.
         */
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + AUTHORITY + "_" + TABLE_NAME;
        /**
         * The mime type of a single item.
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + AUTHORITY + "_" + TABLE_NAME;
        /**
         * A projection of all columns
         * in the items tableName.
         */
        public static final String[] PROJECTION_ALL =
                {COLUMN_ID, COLUMN_INVENTARY_NR, COLUMN_INVENTARY_NAME,
                        COLUMN_SERIAL_NR,
                        COLUMN_PART_NAME, COLUMN_PART_PRICE, COLUMN_RECIVE_DATE,
                        COLUMN_ACOUNTANCY_AMOUNT, COLUMN_PERSON_ID,
                        COLUMN_COMMENTS, COLUMN_SECTION_ID, COLUMN_ROOM};

        public static final String SORT_ORDER_DEFAULT =
                COLUMN_INVENTARY_NR + " ASC";

        public static String getNameWithDB(String column) {
            return TABLE_NAME + "." + column;
        }
    }

    public static final class Persons {
        public static final String TABLE_NAME = "PERSONS";
        public static final String COLUMN_ID = "PERSON_ID";
        public static final String COLUMN_FIRST_NAME = "NAME";
        public static final String COLUMN_LAST_NAME = "SURNAME";
        public static final String COLUMN_PASSWORD = "PASSWORD";

        public static final String LAST_PATH_SEGMENT = "persons";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(Contract.CONTENT_URI, LAST_PATH_SEGMENT);

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + AUTHORITY + "_" + TABLE_NAME;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + AUTHORITY + "_" + TABLE_NAME;

        public static final String[] PROJECTION_ALL = {COLUMN_ID, COLUMN_FIRST_NAME, COLUMN_LAST_NAME, COLUMN_PASSWORD};

        public static final String SORT_ORDER_DEFAULT =
                COLUMN_FIRST_NAME + " ASC";

        public static String getNameWithDB(String column) {
            return TABLE_NAME + "." + column;
        }
    }

    public static final class Sections {
        public static final String TABLE_NAME = "SECTIONS";
        public static final String COLUMN_ID = "SECTION_ID";
        public static final String COLUMN_NAME = "NAME";

        public static final String LAST_PATH_SEGMENT = "sections";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(Contract.CONTENT_URI, LAST_PATH_SEGMENT);

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + AUTHORITY + "_" + TABLE_NAME;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + AUTHORITY + "_" + TABLE_NAME;

        public static final String[] PROJECTION_ALL = {COLUMN_ID, COLUMN_NAME};

        public static final String SORT_ORDER_DEFAULT = COLUMN_NAME + " ASC";

        public static String getNameWithDB(String column) {
            return TABLE_NAME + "." + column;
        }
    }

    public static class SummaryData {
        public static final String TABLE_NAME = Devices.TABLE_NAME +
                " JOIN " + Persons.TABLE_NAME + " ON " +
                Devices.getNameWithDB(Devices.COLUMN_PERSON_ID) + " = " + Persons.getNameWithDB(Persons.COLUMN_ID) +
                " JOIN " + Sections.TABLE_NAME + " ON " + Devices.getNameWithDB(Devices.COLUMN_SECTION_ID) + " = " +
                Sections.getNameWithDB(Sections.COLUMN_ID);

        public static final String COLUMN_ID = Devices.getNameWithDB(Devices.COLUMN_ID);
        public static final String COLUMN_CODE = Devices.getNameWithDB(Devices.COLUMN_SERIAL_NR);

        public static final String[] PROJECTION_ALL =
                {
                        //Devices.getNameWithDB(Devices.COLUMN_ID),
                        //Devices.getNameWithDB(Devices.COLUMN_TYPE_RESOURCES_ID),
                        Devices.getNameWithDB(Devices.COLUMN_INVENTARY_NR),
                        Devices.getNameWithDB(Devices.COLUMN_SERIAL_NR),
                        Devices.getNameWithDB(Devices.COLUMN_INVENTARY_NAME),
                        Devices.getNameWithDB(Devices.COLUMN_PART_NAME),
                        Devices.getNameWithDB(Devices.COLUMN_PART_PRICE),
                        Devices.getNameWithDB(Devices.COLUMN_RECIVE_DATE),
                        Devices.getNameWithDB(Devices.COLUMN_ROOM),
                        Devices.getNameWithDB(Devices.COLUMN_ACOUNTANCY_AMOUNT),
                        Devices.getNameWithDB(Devices.COLUMN_COMMENTS),

                        Persons.getNameWithDB(Persons.COLUMN_FIRST_NAME) + " || ' ' || " +
                                Persons.getNameWithDB(Persons.COLUMN_LAST_NAME),

                        Sections.getNameWithDB(Sections.COLUMN_NAME)
                };

        public static final String[] ALIASES =
                {
                        "'Numer inwentarzowy'",
                        "'Numer seryjny'",
                        "'Numer zestawu'",
                        "'Nazwa części'",
                        "'Cena'",
                        "'Data przyjęcia'",
                        "'Pokój'",
                        "'Ilość księgowa'",
                        "'Komentarz'",
                        "'Osoba odpowiedzialna'",
                        "Sekcja"
                };

        public static final String LAST_PATH_SEGMENT = "summary";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(Contract.CONTENT_URI, LAST_PATH_SEGMENT);

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + AUTHORITY + "_" + LAST_PATH_SEGMENT;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + AUTHORITY + "_" + LAST_PATH_SEGMENT;

        public static final String[] getAliasedProjection() {
            String[] proj = new String[PROJECTION_ALL.length];

            for (int i = 0; i < PROJECTION_ALL.length; i++) {
                proj[i] = PROJECTION_ALL[i] + " AS " + ALIASES[i];
            }
            return proj;
        }
    }
}

