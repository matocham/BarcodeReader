package pl.edu.pb.wi.projekt.barcodereader.database;

/**
 * Created by matocham on 03.01.2017.
 */

public class TableNotFoundException extends DatabaseSchemaException {

    public TableNotFoundException(String tableName) {
        super(tableName, null);
    }
}
