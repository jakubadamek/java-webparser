package com.jakubadamek.robotemil.services;

import org.junit.Assert;

import com.jakubadamek.robotemil.OurHotel;

public abstract class JdbcSettingTest extends SpringTransactionalTest {
    private SettingsService settingsService;
    private OurHotel ourHotel;
    
    public void setSettingsService(SettingsService settingsService) {
		this.settingsService = settingsService;
	}

	public void setOurHotel(OurHotel ourHotel) {
		this.ourHotel = ourHotel;
	}
	
    @SuppressWarnings("unused")
	private void testStoreEnabledWebs() {
        settingsService.storeEnabledWebs(ourHotel);
        ourHotel.getWebStructs().get(0).getParams().setEnabled(false);
        settingsService.storeEnabledWebs(ourHotel);
        Assert.assertFalse(ourHotel.getWebStructs().get(0).getParams().getEnabled());
        ourHotel.getWebStructs().get(0).getParams().setEnabled(true);
        Assert.assertTrue(ourHotel.getWebStructs().get(0).getParams().getEnabled());        
        settingsService.readEnabledWebs(ourHotel);
        Assert.assertFalse(ourHotel.getWebStructs().get(0).getParams().getEnabled());        
    }
}
