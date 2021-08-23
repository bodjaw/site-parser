import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import core.Line;
import core.MetroParser;
import core.Station;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    public static final String METRO = "https://www.moscowmap.ru/metro.html#lines";
    private static String dataFile = "map" + File.separator + "mskMetro.json";

    public static void main(String[] args) {
        Document document = null;
        try {
            document = Jsoup.connect(METRO).get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        MetroParser metro = new MetroParser(document);

        writeJson(metro);
        getStationsInfo();
        getLinesInfo();
        getConnectionsInfo();
    }

    private static void getConnectionsInfo() {
        List<ArrayList<Station>> connections = new ArrayList<>();
        String json = readJson();
        JSONParser parser = new JSONParser();
        JSONObject jsonData = null;
        try {
            jsonData = (JSONObject) parser.parse(json);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (jsonData == null) throw new RuntimeException("JSON doesn't contains connections.");
        JSONArray connectionsArray = (JSONArray) jsonData.get("connections");
        connectionsArray.forEach(connectionsObj -> {
            JSONArray connection = (JSONArray) connectionsObj;
            List<Station> connectStations = new ArrayList<>();
            connection.forEach(i -> {
                JSONObject itemObject = (JSONObject) i;
                String lineNumber = (String) itemObject.get("line");
                String stationName = (String) itemObject.get("station");
                Station station = new Station(stationName, lineNumber);
                connectStations.add(station);
            });
            connections.add(connection);
        });
        System.out.println("Количество переходов: " + connections.size());
    }


    private static List<Line> getLinesInfo() {
        List<Line> lines = new ArrayList<>();
        String json = readJson();
        JSONParser parser = new JSONParser();
        JSONObject jsonData = null;
        try {
            jsonData = (JSONObject) parser.parse(json);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (jsonData == null) throw new RuntimeException("JSON doesn't contains lines.");
        JSONArray linesArray = (JSONArray) jsonData.get("lines");

        linesArray.forEach((Object lineObject) -> {
            JSONObject lineJsonObj = (JSONObject) lineObject;
            Line line = new Line((String) lineJsonObj.get("number"), (String) lineJsonObj.get("name"));
            lines.add(line);
        });
        return lines;
    }

    private static void getStationsInfo() {
        Map<String, ArrayList<String>> stations = new TreeMap<>();
        String json = readJson();
        JSONParser parser = new JSONParser();
        JSONObject jsonData = null;
        try {
            jsonData = (JSONObject) parser.parse(json);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (jsonData == null) throw new RuntimeException("JSON doesn't contains stations.");
        JSONObject stationsObject = (JSONObject) jsonData.get("stations");
        stationsObject.keySet().forEach(line -> {
            String lineNumber = (String) line;
            stations.put(lineNumber, new ArrayList<>());
            JSONArray stationsArray = (JSONArray) stationsObject.get(line);
            stationsArray.forEach(station -> {
                String name = (String) station;
                ArrayList<String> lineStations = stations.get(line);
                lineStations.add(name);
            });
        });
        for (Map.Entry<String, ArrayList<String>> map : stations.entrySet()) {
            System.out.println(map.getKey() + ": " + map.getValue().size() + " ст.");
        }
    }

    private static String readJson() {
        StringBuilder builder = new StringBuilder();
        List<String> lines = null;
        try {
            lines = Files.readAllLines(Paths.get(dataFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        lines.forEach(builder::append);
        return builder.toString();
    }

    private static void writeJson(MetroParser metro) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(metro);
        FileWriter writer = null;
        try {
            writer = new FileWriter(dataFile);
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
