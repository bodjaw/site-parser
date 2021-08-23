package core;

import com.google.gson.annotations.SerializedName;

public class Station implements Comparable<Station>{
    @SerializedName("line")
    private String lineNumber;
    private String name;

    public Station(String name, String lineNumber) {
        this.name = name;
        this.lineNumber = lineNumber;
    }

    public String getLineNumber() {
        return lineNumber;
    }

    public String getName() {
        return name;
    }

    @Override
    public int compareTo(Station station) {
        int lineComparison = lineNumber.compareTo(station.getLineNumber());
        if (lineComparison != 0) {
            return lineComparison;
        }
        return name.compareToIgnoreCase(station.getName());
    }
}