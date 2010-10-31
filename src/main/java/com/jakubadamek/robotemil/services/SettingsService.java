package com.jakubadamek.robotemil.services;

public interface SettingsService {
    void storeSetting(String key, String value);

    String readSetting(String key, String defaultValue);

}
