package com.jakubadamek.robotemil.services;

import com.jakubadamek.robotemil.OurHotel;

public interface SettingsService {
    void storeSetting(String key, String value);

    String readSetting(String key, String defaultValue);

    void readEnabledWebs(OurHotel ourHotel);
    
    void storeEnabledWebs(OurHotel ourHotel);
    
    void createTables();
}
