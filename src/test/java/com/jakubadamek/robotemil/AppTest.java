package com.jakubadamek.robotemil;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AppTest {
    private static App app;
    
    @BeforeClass
    public static void setUp() {
        System.setProperty("customer", "TESTHOTEL");
        ClassPathXmlApplicationContext spring = new ClassPathXmlApplicationContext(new String[]{"testhotel/test-config.xml"});
        //settingsService = spring.getBean(SettingsService.class);
        app = (App) spring.getBean("app");
    }
    
    //@Test
    public void testLoadSaveHotels() throws IOException {
        app.loadHotels();
        WebStruct webStruct = app.getSettingsModel().getOurHotels().get(0).getWebStructs().get(0); 
        Assert.assertEquals("Esplanade Prague", webStruct.getHotelList().get(1));
        
        webStruct.getHotelList().remove(1);
        webStruct.getHotelList().add(1, "Esplanad");        
        app.loadHotels();
        Assert.assertEquals("Esplanade Prague", webStruct.getHotelList().get(1));
        
        webStruct.getHotelList().remove(1);
        webStruct.getHotelList().add(1, "Esplanad");       
        app.saveHotels();
        app.loadHotels();
        Assert.assertEquals("Esplanad", webStruct.getHotelList().get(1));

        webStruct.getHotelList().remove(1);
        webStruct.getHotelList().add(1, "Esplanade Prague");       
        app.saveHotels();
    }
}
