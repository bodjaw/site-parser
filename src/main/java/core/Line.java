package core;

public class Line implements Comparable<Line> {
    private String number;
    private String name;

    public Line(String number, String name) {
        this.number = number;
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    @Override
    public int compareTo(Line line) {
        return number.compareTo(line.getNumber());
    }

    @Override
    public String toString() {
        return name + " (" + number + ")";
    }
}