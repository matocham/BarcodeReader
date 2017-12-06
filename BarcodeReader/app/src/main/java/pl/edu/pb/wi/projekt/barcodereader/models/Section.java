package pl.edu.pb.wi.projekt.barcodereader.models;

/**
 * Created by Mateusz on 13.11.2017.
 */

public class Section {
    private String name;
    private int id;

    public Section(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return name;
    }
}
