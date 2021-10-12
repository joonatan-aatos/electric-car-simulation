package simulation;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class RoadData {
    private double length;
    private ArrayList<ChargingStation> chargingStations;
    private RoadData() {
        length = 0;
        chargingStations = new ArrayList<>();
    }

    public double getLength() {
        return length;
    }

    public ArrayList<ChargingStation> getChargingStations() {
        return chargingStations;
    }

    public static RoadData readChargingStations(String path) {
        RoadData roadData = new RoadData();

        try {
            InputStream in = RoadData.class.getResourceAsStream(path);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            int lineNumber = 0;
            String lastAppearingName = "";
            ChargingStation lastStation = null;

            String[] distanceData = br.readLine().split(";");
            roadData.length = Double.parseDouble(distanceData[1]);

            br.readLine();
            br.readLine();

            String line;
            while ((line = br.readLine()) != null) {
                if (line.length() <= 0) break;
                String[] data = line.split(";");
                if (!lastAppearingName.equals(data[0])) {   // First of a station
                    if (lineNumber != 0) roadData.chargingStations.add(lastStation);
                    lastStation = new ChargingStation(
                            Double.parseDouble(data[1]),
                            Double.parseDouble(data[2]),
                            new ArrayList<>(),
                            new ArrayList<>(),
                            new boolean[]{"1".equals(data[6]), "1".equals(data[7]), "1".equals(data[8])}
                    );

                }
                ChargerType type = null;
                switch (data[5]) {
                    case ("Type2"):
                        type = ChargerType.Type2;
                        break;
                    case ("CCS"):
                        type = ChargerType.CCS;
                        break;
                    case ("Tesla"):
                        type = ChargerType.Tesla;
                        break;
                    case ("CHAdeMO"):
                        type = ChargerType.CHAdeMO;
                        break;
                    case ("Työmaapistoke"):
                        type = ChargerType.Tyomaapistoke;
                        break;

                }

                for (int i = 0; i < Integer.parseInt(data[4]); i++) {
                    lastStation.addCharger(Integer.parseInt(data[3]), type);
                }

                lastAppearingName = data[0];
                lineNumber++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return roadData;
    }
}
