package pl.edu.pb.wi.projekt.barcodereader.database;

/**
 * Created by matocham on 03.01.2017.
 */

public class ColumnNotFoundException extends DatabaseSchemaException {
    public ColumnNotFoundException(String tableName, String columnName) {
        super(tableName, columnName);
    }
}
