package simulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum CarType {
    AUDI_E(301, 71.0, 14.7, 11.0, 120.0, new ArrayList<String>(Arrays.asList("Type2", "CCS"))),
    BMW_I3S(29, 42.2, 15.8, 11.0, 50.0, new ArrayList<String>(Arrays.asList("Type2", "CCS"))),
    BMW_I3(253, 42.2, 15.2, 11.0, 50.0, new ArrayList<String>(Arrays.asList("Type2", "CCS"))),
    CITROEN_C_ZERO(13, 16.0, 12.4, 3.7, 46.0, new ArrayList<String>(Arrays.asList("Type2", "CHAdeMO"))),
    FIAT_500E(71, 42.0, 13.9, 11.0, 85.0, new ArrayList<String>(Arrays.asList("Type2", "CCS"))),
    HONDA_E(18, 35.5, 17.1, 6.6, 56.0, new ArrayList<String>(Arrays.asList("Type2", "CCS"))),
    HYUNDAI_IONIQ(465, 58.0, 16.7, 11.0, 220.0, new ArrayList<String>(Arrays.asList("Type2", "CCS"))),
    HYUNDAI_KONA(565, 42.0, 13.7, 7.2, 50.0, new ArrayList<String>(Arrays.asList("Type2", "CCS"))),
    JAGUAR_I_PACE(164, 90.0, 22.0, 11.0, 100.0, new ArrayList<String>(Arrays.asList("Type2", "CCS"))),
    KIA_NIRO(183, 39.0, 15.2, 7.2, 50.0, new ArrayList<String>(List.of("Type2", "CCS"))),
    KIA_SOUL(62, 33.0, 14.3, 6.6, 100.0, new ArrayList<String>(Arrays.asList("Type2", "CHAdeMO"))),
    MAZDA_MX_3(26, 35.5, 19.0, 6.6, 40.0, new ArrayList<String>(Arrays.asList("Type2", "CCS"))),
    MERCEDES_B250(45, 31.0, 21.5, 9.6, 0.0, new ArrayList<String>(List.of("Type2"))),
    MERCEDES_EQC(127, 85.0, 22.1, 7.4, 110.0, new ArrayList<String>(Arrays.asList("Type2", "CCS"))),
    MERCEDES_EQV(16, 90.0, 28.9, 11.0, 110.0, new ArrayList<String>(Arrays.asList("Type2", "CCS"))),
    MINI_COOPER_SE(62, 32.6, 14.9, 11.0, 49.0, new ArrayList<String>(Arrays.asList("Type2", "CCS"))),
    MITSUBISHI_I_MIEV(14, 16.0, 10.0, 3.6, 50.0, new ArrayList<String>(Arrays.asList("Type2", "CHAdeMO"))), // https://evcompare.io/cars/mitsubishi/mitsubishi_i-miev/
    NISSAN_E_NV200(37, 40.0, 25.8, 6.6, 46.0, new ArrayList<String>(Arrays.asList("Type2", "CHAdeMO"))),
    NISSAN_LEAF(1213, 40.0, 20.5, 3.6, 50.0, new ArrayList<String>(List.of("Type2"))),
    OPEL_CORSA(43, 50.0, 15.0, 11.0, 100.0, new ArrayList<String>(Arrays.asList("Type2", "CCS"))), // https://evcompare.io/cars/opel/opel-corsa-e/
    PEUGEOT_E_2008(39, 50.0, 16.0, 7.4, 100.0, new ArrayList<String>(Arrays.asList("Type2", "CCS"))),
    PEUGEOT_E_208(31, 50.0, 16.2, 7.4, 100.0, new ArrayList<String>(Arrays.asList("Type2", "CCS"))),
    PEUGEOT_ION(11, 16.0, 12.4, 3.7, 40.0, new ArrayList<String>(Arrays.asList("Type2", "CHAdeMO"))),
    PORSCHE_TAYCAN(124, 79.2, 20.8, 11.0, 225.0, new ArrayList<String>(Arrays.asList("Type2", "CCS"))),
    RENAULT_ZOE(292, 41.0, 15.5, 22.0, 0.0, new ArrayList<String>(List.of("Type2"))),
    SEAT_MII(271, 36.8, 14.3, 7.2, 40.0, new ArrayList<String>(Arrays.asList("Type2", "CCS"))),
    SKODA_CITIGO(149, 36.8, 14.3, 7.2, 40.0, new ArrayList<String>(Arrays.asList("Type2", "CCS"))),
    SMART_EQ(15, 17.6, 16.5, 22.0, 0.0, new ArrayList<String>(List.of("Type2"))),
    TESLA_MODEL_3(1541, 68.5, 15.2, 11.0, 210.0, new ArrayList<String>(Arrays.asList("Type2", "Tesla"))),
    TESLA_MODEL_S(1440, 87.5, 19.1, 16.5, 150.0, new ArrayList<String>(Arrays.asList("Type2", "Tesla"))),
    TESLA_MODEL_X(423, 87.5, 22.5, 16.5, 150.0, new ArrayList<String>(Arrays.asList("Type2", "Tesla"))),
    VOLKSWAGEN_E_GOLF(487, 35.8, 15.2, 7.2, 40.0, new ArrayList<String>(Arrays.asList("Type2", "CCS"))),
    VOLKSWAGEN_ID_3(496, 65.0, 14.6, 11.0, 87.5, new ArrayList<String>(Arrays.asList("Type2", "CCS"))),
    VOLKSWAGEN_E_UP1(371, 36.8, 14.3, 7.2, 50.0, new ArrayList<String>(Arrays.asList("Type2", "CCS"))),
    VOLVO_XC40(154, 78.0, 23.6, 11.0, 150.0, new ArrayList<String>(List.of("Type2","CCS")));

    private int amount; // count
    private double capacity; // kWh
    private double maxChargingPowerAC; // kW
    private double maxChargingPowerDC; // kW
    private double drivingEfficiency; // kWh / 100km
    private ArrayList<ChargingStation.ChargerType> supportedChargers;
    private double winterChargingCoefficient = 0.5;
    private double winterDrivingCoefficient = 0.5;

    private CarType(int amount_, double capacity_, double drivingEfficiency_, double maxChargingPowerAC_, double maxChargingPowerDC_, ArrayList<String> supportedChargers_) {
        amount = amount_;
        capacity = capacity_;
        maxChargingPowerAC = maxChargingPowerAC_;
        maxChargingPowerDC = maxChargingPowerDC_;
        drivingEfficiency = drivingEfficiency_;
        supportedChargers = new ArrayList<>();
        for (String charger : supportedChargers_) {
            switch (charger) {
                case "Type2":
                    supportedChargers.add(ChargingStation.ChargerType.Type2);
                    break;
                case "CCS":
                    supportedChargers.add(ChargingStation.ChargerType.CCS);
                    break;
                case "CHAdeMO":
                    supportedChargers.add(ChargingStation.ChargerType.CHAdeMO);
                    break;
                case "Tesla":
                    supportedChargers.add(ChargingStation.ChargerType.Tesla);
                    break;
            }
        }
    }

    public void itIsWinter() {
        maxChargingPowerAC *= winterChargingCoefficient;
        maxChargingPowerDC *= winterChargingCoefficient;
        drivingEfficiency /= winterDrivingCoefficient;      // Because the greater the value of "drivingEfficiency", the worse the efficiency. What a stupid metric.
    }

    public int getAmount() {
        return amount;
    }

    public double getCapacity() {
        return capacity;
    }

    public double getMaxChargingPowerAC() {
        return maxChargingPowerAC;
    }

    public double getMaxChargingPowerDC() {
        return maxChargingPowerDC;
    }

    public double getDrivingEfficiency() {
        return drivingEfficiency;
    }

    public ArrayList<ChargingStation.ChargerType> getSupportedChargers() {
        return supportedChargers;
    }
}
