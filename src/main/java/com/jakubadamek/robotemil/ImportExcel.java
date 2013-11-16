package com.jakubadamek.robotemil;

import java.io.File;
import java.io.IOException;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportExcel {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private OurHotel ourHotel;
    private App app;

    /**
     * Constructor
     * 
     * @param ourHotel
     */
    public ImportExcel(OurHotel ourHotel, App app) {
        this.ourHotel = ourHotel;
        this.app = app;
    }

    public void readXlsSettings(String filename) throws BiffException, IOException {
    	Workbook workbook = Workbook.getWorkbook(new File(filename));
    	Sheet sheet = workbook.getSheet("nastaveni");
    	if(sheet == null) {
    		throw new RuntimeException("V souboru chybi list 'nastaveni'");
    	}
        for (WebStruct webStruct : this.ourHotel.getWebStructs()) {
        	for(int icol = 0; icol < sheet.getColumns(); icol ++) {
        		String webName = sheet.getCell(icol, 0).getContents(); 
    			logger.info("webName " + webName);
        		if(webName.equals(webStruct.getParams().getLabel())) {
        			logger.info("Found web " + sheet.getCell(icol, 0).getContents());
                	webStruct.getHotelList().clear();
                	for(int irow = 1; irow < sheet.getRows(); irow ++) {
                		String hotel = sheet.getCell(icol, irow).getContents();
                		webStruct.getHotelList().add(hotel);
            			logger.info("Found hotel " + hotel + " for web " + webName);
                	}
        		}
        	}
        }
    	app.saveHotels();
        app.loadHotels();    	
    }
}
