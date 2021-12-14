package simulation;

import java.io.*;
import java.util.ArrayList;

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

            String[] data = null;
            String line;
            while ((line = br.readLine()) != null) {
                if (line.length() <= 0) break;
                data = line.split(";");
                if (!lastAppearingName.equals(data[0])) {   // First of a station
                    if (lineNumber != 0) roadData.chargingStations.add(lastStation);
                    lastStation = new ChargingStation(
                            data[0],
                            Double.parseDouble(data[1]),
                            Double.parseDouble(data[2]),
                            new boolean[]{data[6].equals("1"), data[7].equals("1"), data[8].equals("1")}
                    );
                }
                ChargingStation.ChargerType type = null;
                switch (data[5]) {
                    case ("Type2"):
                        type = ChargingStation.ChargerType.Type2;
                        break;
                    case ("CCS"):
                        type = ChargingStation.ChargerType.CCS;
                        break;
                    case ("CCS (HPC)"):
                        type = ChargingStation.ChargerType.CCS;
                    case ("SuperCharger"):
                        type = ChargingStation.ChargerType.Tesla;
                        break;
                    case ("CHAdeMO"):
                        type = ChargingStation.ChargerType.CHAdeMO;
                        break;
                    case ("Työmaapistoke"):
                        type = ChargingStation.ChargerType.Tyomaapistoke;
                        break;
                    default:
                        throw new IllegalArgumentException(String.format("Illegal charger type at line %s in file %s: \"%s\"", lineNumber, path, data[5]));
                }

                assert lastStation != null;

                for (int i = 0; i < Integer.parseInt(data[4]); i++) {
                    lastStation.addCharger(Integer.parseInt(data[3]), type);
                }

                lastStation.sortChargers();
                lastAppearingName = data[0];
                lineNumber++;
            }
            roadData.chargingStations.add(lastStation);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Charger file is in wrong format");
        }

        return roadData;
    }
}
