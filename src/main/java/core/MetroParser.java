package core;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetroParser {

    private static String lineNumberAndNamePattern = "data-line=\"([A-Z]?\\d{1,2}[A-Z]?)\">(.+)<";
    private static String stationNamePattern = "name\">(.+)<";
    private static String lineNumberPattern = "ln-([A-Z]?\\d{1,2}[A-Z]?).+";
    private static String leftQuotationMark = "«";
    private static String rightQuotationMark = "»";

    private static String selectorForStations = "span.name, span[data-line]";
    private static String selectorForLines = "span.js-metro-line";
    private static String selectorForConnections = "span.num, span.name, span.t-icon-metroln";


    private Map<String, ArrayList<String>> stations;
    private List<Line> lines;
    private List<ArrayList<Station>> connections;

    public MetroParser(Document document) {
        stations = parseStations(document);
        lines = parseLines(document);
        connections = parseConnections(document);
    }

    public Map<String, ArrayList<String>> getStations() {
        return stations;
    }

    public List<Line> getLines() {
        return lines;
    }

    public List<ArrayList<Station>> getConnections() {
        return connections;
    }

    public Map<String, ArrayList<String>> parseStations(Document document) {
        Map<String, ArrayList<String>> parseStations = new LinkedHashMap<>();
        Elements elementsWithStations = document.select(selectorForStations);
        String lineNumber = null;
        for (Element elementsWithStation : elementsWithStations) {
            String str = elementsWithStation.toString();
            Matcher lineMatcher = Pattern.compile(lineNumberAndNamePattern).matcher(str);
            Matcher stationMatcher = Pattern.compile(stationNamePattern).matcher(str);
            if (lineMatcher.find()) {
                lineNumber = lineMatcher.group(1);
                parseStations.put(lineNumber, new ArrayList<>());
            }
            if (stationMatcher.find()) {
                String station = stationMatcher.group(1);
                ArrayList<String> lineStations = parseStations.get(lineNumber);
                lineStations.add(station);
            }
        }
        return parseStations;
    }

    public List<Line> parseLines(Document document) {
        List<Line> parseLines = new ArrayList<>();
        Elements elementsWithLines = document.select(selectorForLines);
        for (Element element : elementsWithLines) {
            Matcher lineMatcher = Pattern.compile(lineNumberAndNamePattern).matcher(element.toString());
            if (lineMatcher.find()) {
                String lineNumber = lineMatcher.group(1);
                String lineName = lineMatcher.group(2);
                parseLines.add(new Line(lineNumber, lineName));
            }
        }
        return parseLines;
    }

    public List<ArrayList<Station>> parseConnections(Document document) {
        Map<String, ArrayList<Station>> parseConnections = new LinkedHashMap<>();
        Elements elementsWithConnections = document.select(selectorForConnections);
        String fromLineNumber = null;
        String fromStationName = null;
        String toLineNumber = null;
        String toStationName = null;

        for (Element elementsWithConnection : elementsWithConnections) {
            String str = elementsWithConnection.toString();
            Matcher fromLineNumberAndName = Pattern.compile(lineNumberAndNamePattern).matcher(str);
            Matcher fromStation = Pattern.compile(stationNamePattern).matcher(str);
            Matcher toLineToStation = Pattern.compile(lineNumberPattern + leftQuotationMark + "(.+)" + rightQuotationMark).matcher(str);
            if (fromLineNumberAndName.find()) {
                fromLineNumber = fromLineNumberAndName.group(1);
            }
            if (fromStation.find()) {
                fromStationName = fromStation.group(1);
                parseConnections.put(fromStationName, new ArrayList<>());
            }
            if (toLineToStation.find()) {
                toLineNumber = toLineToStation.group(1);
                toStationName = toLineToStation.group(2);
                ArrayList<Station> connect = parseConnections.get(fromStationName);
                connect.add(new Station(toStationName, toLineNumber));
                connect.add(new Station(fromStationName, fromLineNumber));
            }
        }
        List<ArrayList<Station>> connects = new ArrayList<>();
        for (Map.Entry<String, ArrayList<Station>> map : parseConnections.entrySet()) {
            if (!map.getValue().isEmpty()) {
                connects.add(map.getValue());
            }
        }
        return connects;
    }
}
