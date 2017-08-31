package pl.edu.pb.wi.projekt.barcodereader.database;

/**
 * Created by matocham on 03.01.2017.
 */

public class DatabaseSchemaException extends Exception {
    protected String tableName;
    protected String columnName;

    public DatabaseSchemaException(String tableName, String columnName) {
        this.tableName = tableName;
        this.columnName = columnName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }
}
