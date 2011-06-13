package com.jakubadamek.robotemil.services;

import java.util.List;

import com.jakubadamek.robotemil.OurHotel;

public interface SettingsService {
    void storeSetting(String key, String value);

    String readSetting(String key, String defaultValue);

    void readEnabledWebs(OurHotel ourHotel);
    
    void storeEnabledWebs(OurHotel ourHotel);
    
    List<Integer> readLengthsOfStay();
    
    void storeLengthsOfStay(List<Integer> lengthsOfStay);
    
    void createTables();
}
