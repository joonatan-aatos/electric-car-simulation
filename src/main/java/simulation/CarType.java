package simulation;

public enum CarType {
    AUDI_E(301, -1, -1, -1),
    BMWI_I3S(29, -1, -1, -1),
    BMWI_I3(215, -1, -1, -1),
    BMW_I3S(5, -1, -1, -1),
    BMW_I3(38, -1, -1, -1),
    BMW_Maarittelematon(1, -1, -1, -1),
    CHEVROLET_BOLT(3, -1, -1, -1),
    CITROEN_2CV(1, -1, -1, -1),
    CITROEN_AX(1, -1, -1, -1),
    CITROEN_BERLINGO(2, -1, -1, -1),
    CITROEN_C_ZERO(13, -1, -1, -1),
    CITROEN_SAXO(2, -1, -1, -1),
    CITROEN_XSARA(1, -1, -1, -1),
    DS_3CROSSBACK(7, -1, -1, -1),
    FIAT_500E(47, -1, -1, -1),
    FIAT_500(24, -1, -1, -1),
    FIAT_DOBLO(1, -1, -1, -1),
    FIAT_Maarittelematon(1, -1, -1, -1),
    FORD_CNG_TECHNIK_FOCUS(3, -1, -1, -1),
    FORD_FOCUS(2, -1, -1, -1),
    FORD_MUSTANG(2, -1, -1, -1),
    HONDA_E(18, -1, -1, -1),
    HYUNDAI_IONIQ(465, -1, -1, -1),
    HYUNDAI_KONA(565, -1, -1, -1),
    JAGUAR_I_PACE(164, -1, -1, -1),
    KIA_NIRO(183, -1, -1, -1),
    KIA_SOUL(62, -1, -1, -1),
    LandRover_I_PACE(2, -1, -1, -1),
    MAZDA_MX_3(26, -1, -1, -1),
    MERCEDES_BENZ_240GD(1, -1, -1, -1),
    MERCEDES_BENZ_250E(1, -1, -1, -1),
    MERCEDES_BENZ_B200(2, -1, -1, -1),
    MERCEDES_BENZ_B250E(3, -1, -1, -1),
    MERCEDES_BENZ_B250(45, -1, -1, -1),
    MERCEDES_BENZ_ELECTRIC(40, -1, -1, -1),
    MERCEDES_BENZ_EQC400(127, -1, -1, -1),
    MERCEDES_BENZ_V_KLASSE(16, -1, -1, -1),
    MERCEDES_BENZ_VITO(5, -1, -1, -1),
    MERCEDES_BENZ_Maarittelematon(1, -1, -1, -1),
    MICRO_VETT___(1, -1, -1, -1),
    MICRO_VETT_Maarittelematon(1, -1, -1, -1),
    MINI_COOPERSE(62, -1, -1, -1),
    MITSUBISHI_I_MIEV(14, -1, -1, -1),
    MITSUBISHI_MINICAB(1, -1, -1, -1),
    NISSAN_E_NV200(37, -1, -1, -1),
    NISSAN_LEAF(1213, -1, -1, -1),
    OMAVALMISTE_ELECTRIC(1, -1, -1, -1),
    OPEL_AMPERA(5, -1, -1, -1),
    OPEL_CORSA(43, -1, -1, -1),
    PEUGEOT_106(3, -1, -1, -1),
    PEUGEOT_2008(39, -1, -1, -1),
    PEUGEOT_208(31, -1, -1, -1),
    PEUGEOT_ION(11, -1, -1, -1),
    POLESTAR_2(2, -1, -1, -1),
    PORSCHE_TAYCAN(124, -1, -1, -1),
    RENAULT_FLUENCE(2, -1, -1, -1),
    RENAULT_TWINGO(1, -1, -1, -1),
    RENAULT_ZOE(292, -1, -1, -1),
    SAAB_99(1, -1, -1, -1),
    SEAT_MII(271, -1, -1, -1),
    SKODA_CITIGO(149, -1, -1, -1),
    SMART_EQ(15, -1, -1, -1),
    SMART_FORFOUR(1, -1, -1, -1),
    SMART_FORTWO(3, -1, -1, -1),
    TESLAMOTORS_85D(2, -1, -1, -1),
    TESLAMOTORS_MODEL3(1541, -1, -1, -1),
    TESLAMOTORS_MODELS(1440, -1, -1, -1),
    TESLAMOTORS_MODELX(423, -1, -1, -1),
    TESLAMOTORS_P85D(2, -1, -1, -1),
    TESLAMOTORS_S85(6, -1, -1, -1),
    TESLAMOTORS_TESLA(1, -1, -1, -1),
    TESLAMOTORS_Maarittelematon(6, -1, -1, -1),
    THINK_CITY(2, -1, -1, -1),
    THINK_TH_NK(9, -1, -1, -1),
    TOYOTA_COROLLA(2, -1, -1, -1),
    TOYOTA_PREVIA(1, -1, -1, -1),
    TUNTEMATON_THINK(2, -1, -1, -1),
    VOLKSWAGEN_2000(1, -1, -1, -1),
    VOLKSWAGEN_E_GOLF(1, -1, -1, -1),
    VOLKSWAGEN_EGOLF(2, -1, -1, -1),
    VOLKSWAGEN_GOLF(487, -1, -1, -1),
    VOLKSWAGEN_ID_3(496, -1, -1, -1),
    VOLKSWAGEN_UP(371, -1, -1, -1),
    VOLVO_XC40(154, -1, -1, -1),
    GAS(-1, -1, -1, -1);

    private final int DEFAULT_CAPACITY = 50; // kWh
    private final int DEFAULT_CHARGING_EFFICIENCY = Integer.MAX_VALUE; // kW
    private final double DEFAULT_DRIVING_EFFICIENCY = 14; // kWh / 100km
    public final int amount;
    public final int capacity;
    public final int chargingEfficiency;
    public final double drivingEfficiency;

    private CarType(int amount_, int capacity_, int averageChargingEfficiency_, double drivingEfficiency_) {
        amount = amount_;
        capacity = capacity_ == -1 ? DEFAULT_CAPACITY : capacity_;
        chargingEfficiency = averageChargingEfficiency_ == -1 ? DEFAULT_CHARGING_EFFICIENCY : averageChargingEfficiency_;
        drivingEfficiency = drivingEfficiency_ == -1 ? DEFAULT_DRIVING_EFFICIENCY : drivingEfficiency_;
    }
}
