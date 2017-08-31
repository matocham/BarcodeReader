package pl.edu.pb.wi.projekt.barcodereader;

/**
 * Created by Mateusz on 04.10.2016.
 * Represents result of barcode search
 */
public class SearchResultRow {
    private String id;
    private String name;
    private String description;

    public SearchResultRow(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
