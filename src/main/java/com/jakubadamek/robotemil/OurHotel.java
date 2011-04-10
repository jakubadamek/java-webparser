package com.jakubadamek.robotemil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Info about our hotel (if the app is used in several hotels with different
 * settings)
 * 
 * @author Jakub
 */
public class OurHotel implements Serializable {
    private static final long serialVersionUID = 5132068969703535736L;
    /** Web structs */
    private List<WebStruct> webStructs = new ArrayList<WebStruct>();
    /** Our hotel name */
    private String ourHotelName;

    private final int ourHotelIndex;

    /**
     * @param app
     */
    public OurHotel(int ourHotelIndex) {
        this.ourHotelIndex = ourHotelIndex;
    }

    /**
     * @param web
     * @return File name for the web
     */
    public String fileName(WebParams web) {
        if (ourHotelIndex == 0) {
            return web.getFileName();
        }
        return ourHotelIndex + "_" + web.getFileName();
    }

    /**
     * @param webStructs
     *            the webStructs to set
     */
    public void setWebStructs(List<WebStruct> webStructs) {
        this.webStructs = webStructs;
    }

    /**
     * @return the webStructs
     */
    public List<WebStruct> getWebStructs() {
        return webStructs;
    }
    
    public List<WebStruct> getEnabledWebStructs() {
        List<WebStruct> retval = new ArrayList<WebStruct>(webStructs.size());
        for(WebStruct webStruct : webStructs) {
            if(webStruct.getParams().getEnabled()) {
                retval.add(webStruct);
            }
        }
        return retval;
    }

    /**
     * @param ourHotelName
     *            the ourHotelName to set
     */
    public void setOurHotelName(String ourHotelName) {
        this.ourHotelName = ourHotelName;
    }

    /**
     * @return the ourHotelName
     */
    public String getOurHotelName() {
        return ourHotelName;
    }
}