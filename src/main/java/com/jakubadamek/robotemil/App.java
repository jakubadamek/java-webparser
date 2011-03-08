package com.jakubadamek.robotemil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.jakubadamek.robotemil.services.PriceService;
import com.jakubadamek.robotemil.services.SettingsService;

/**
 */
public class App implements InitializingBean 
{
    private static final Logger logger = Logger.getLogger(App.class);
    /** restart the same work unit when no response for as long */
    static final int RESTART_AFTER_SECONDS = 900;
    /** concurrent thread count */
    private int threadCount;
    //private static final boolean TEST = false;
    private ResourceBundle bundle = ResourceBundle.getBundle("robotemil");
    private final String CUSTOMER = System.getProperty("customer", Customer.JALTA.toString());
    enum Customer { JALTA, PERLA, TESTHOTEL }
    private AppFrame appFrame;
    private boolean useCache;
    public WorkUnitsManager workUnitsManager = new WorkUnitsManager(this);
    Customers customers;    
    public PriceService priceService;
    public SettingsService settingsService;

    /**
     * Index of currently chosen tab in our hotels
     */
    int ourHotelIndex = 0;
    /** stop execution */
    public static boolean stop;

    Date startDate;
    int dayCount;

    /**
     * Main
     * @param args
     * @throws IOException
     * @throws InterruptedException 
     */
    public static void main( String[] args ) throws IOException, InterruptedException
    {
        if(args.length == 0) {
            runWithGui();
        } else if(args.length == 1 && args[0].equals("runPeriodically")) {
            runPeriodically();
        } else if(args.length == 2) {
            runWithoutGui(args);
        } else {
            throw new IllegalArgumentException("Error: " + args.length + " args: " + Arrays.toString(args));
        }
    }

    private App() {
    	// do nothing
    }

    private static App getApp() {
        ClassPathXmlApplicationContext spring = new ClassPathXmlApplicationContext(new String[]{"spring-config.xml"});
        //settingsService = spring.getBean(SettingsService.class);
        return spring.getBean(App.class);
    }
    
    private static void runWithGui() {
        App app = getApp();
        app.appFrame = new AppFrame(app);
        app.startWork();
    }

    private static void runWithoutGui(String ... args) throws InterruptedException {
        /* Args: - count of days since today, when to start
         *       - count of days including the start date
         */
        //if(new Scheduler().isScheduled("automatic " + args[0] + " " + args[1])) {
            App app = getApp();
            app.threadCount = 1;
            app.startDate = new Date(new Date().getTime() + Long.valueOf(args[0]) * 24*60*60*1000);
            app.dayCount = Integer.valueOf(args[1]);
            app.useCache = true;
            logger.info("startDate " + args[0] + " dayCount " + app.dayCount);
            app.startWork();
            app.workBody();
        /*} else {
            logger.info(new Date() + " task is not scheduled now");
        }*/
    }

    private static void runPeriodically() {
        Date lastRun = new Date();
        int waitMinutes = 5;
        while(true) {
            try {
                runWithoutGui("0", "0");
                Calendar lastRunCalendar = Calendar.getInstance();
                lastRunCalendar.setTime(lastRun);
                Calendar nowCalendar = Calendar.getInstance();
                nowCalendar.setTime(new Date());
                if(lastRunCalendar.get(Calendar.DATE) == nowCalendar.get(Calendar.DATE)) {
                    waitMinutes *= 2;
                } else {
                    waitMinutes = 10;
                }
                lastRun = new Date();
                logger.info("waitMinutes: " + waitMinutes);
                Thread.sleep(waitMinutes * 60 * 1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    Customer getCustomer() {
        return Customer.valueOf(CUSTOMER);
    }

    /**
     * @return dates for which prices are searched
     */
    List<Date> getDates() {
        final List<Date> retval = new ArrayList<Date>();
        Calendar calendar = Calendar.getInstance();
        //calendar.set(App.this.dateTime.getYear(), App.this.dateTime.getMonth(), App.this.dateTime.getDay(), 0, 0, 0);
        calendar.setTime(startDate);
        calendar.set(Calendar.MILLISECOND, 0);
        //for(int iduration = 0; iduration < App.this.spinnerDays.getSelection(); iduration ++) {
        for(int iduration = 0; iduration < dayCount; iduration ++) {
            retval.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return retval;
    }

    /** sleep
     * @param milliSeconds */
    static void sleep(int milliSeconds) {
        try {
            Thread.sleep(milliSeconds);
        } catch(InterruptedException e) {
            logger.info(e.toString());
        }
    }

    private void startWork() {
        try {
            loadHotels();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException(e);
        }
        if(this.appFrame != null) {
            this.appFrame.runGui();
        }
    }

    void workBody() throws InterruptedException {
        workUnitsManager.prepare();
        if(appFrame != null) {
            appFrame.runPrepare();
            appFrame.runProgress();
        }
        workUnitsManager.downloadAll(threadCount);
        /*if(TEST) {
            try {
                deserialize();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {*/
        //}
    }

    String getBundleString(String key) {
        return this.bundle.getString(key);
    }
    
    public SettingsModel getSettingsModel() {
    	return customers.getSettingsModel();
    }

    void saveHotels() {
        try {
            if(! new File(hotelsDir()).exists()) {
                new File(hotelsDir()).mkdirs();
            }
			for (OurHotel ourHotel : getSettingsModel().getOurHotels()) {
                for(WebStruct webStruct : ourHotel.getWebStructs()) {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(new File(hotelsDir(), ourHotel.fileName(webStruct.getParams()))));
                    for(String hotel : webStruct.getHotelList()) {
                        writer.append(hotel);
                        writer.newLine();
                    }
                    writer.close();
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
            if(appFrame != null) {
                appFrame.displayException(getBundleString("Chyba pri ukladani nazvu hotelu"), e);
            }
        }
    }

    private String hotelsDir() {
        return new File(System.getProperty("user.home"), getCustomer().toString()).getPath();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        int index = 0;
        if(getSettingsModel().getOurHotels().size() > 0) {
            return;
        }
        for (String ourHotelName : getSettingsModel().getOurHotelNames()) {
            OurHotel ourHotel = new OurHotel(index++);
            ourHotel.setOurHotelName(ourHotelName);
            for(WebParams webParams : getSettingsModel().getWebParams()) {
                WebStruct webStruct = new WebStruct();
                webStruct.setParams(webParams);
                ourHotel.getWebStructs().add(webStruct);
            }
            getSettingsModel().getOurHotels().add(ourHotel);
        }        
    }
    
    void loadHotels() throws IOException {
        for(OurHotel ourHotel : getSettingsModel().getOurHotels()) {
            for (WebStruct web : ourHotel.getWebStructs()) {
	            String[] hotels = readFileToStrings(hotelsDir(), ourHotel.fileName(web.getParams()));
	            if (hotels.length == 0) {
	                hotels = readFileToStrings(hotelsDir(), getSettingsModel().getOurHotels().get(0).fileName(web.getParams()));
	            }
	            web.getHotelList().clear();
	            for (String hotel : hotels) {
	                web.getHotelList().add(DiacriticsRemover.removeDiacritics(hotel).trim());
	            }
            }
        }
    }

    @Required
    public void setCustomers(Customers customers) {
		this.customers = customers;
	}

    @Required
	public void setPriceService(PriceService priceService) {
		this.priceService = priceService;
	}

    @Required
	public void setSettingsService(SettingsService settingsService) {
		this.settingsService = settingsService;
	}

	private String[] readFileToStrings(String dir, String file) throws IOException {
        Reader reader;
        if(new File(dir, file).exists()) {
            reader = new FileReader(new File(dir, file));
        } else {
            String filename = getCustomer().name().toLowerCase() + "/" + file;
            try {
                reader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(filename));
            } catch(NullPointerException e) {
                logger.info(filename + " not found");
                return new String[] {};
            }
        }
        char[] retval = new char[10000];
        reader.read(retval);
        List<String> strings = new ArrayList<String>();
        for(String row : new String(retval).trim().split("\n")) {
            strings.add(row.trim());
        }
        return strings.toArray(new String[] {});
    }

    OurHotel getOurHotel() {
        return getSettingsModel().getOurHotels().get(this.ourHotelIndex);
    }

    private DateFormat timeFormat = DateFormat.getTimeInstance();
    private ExecutorService threadPool;

    public void showLog(String row) {
        String logRow = timeFormat.format(new Date()) + " " + row;
        logger.info(logRow);
        if(this.appFrame != null) {
            this.appFrame.showLog(logRow);
        }
    }

    /**
     * @return the useCache
     */
    public boolean isUseCache() {
        return useCache;
    }

    /**
     * @param useCache the useCache to set
     */
    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }
    
    /**
    private void serialize() throws FileNotFoundException, IOException {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("webStructs.ser"));
        out.writeObject(getOurHotel().webStructs);
        out.close();
    }

    @SuppressWarnings("unchecked")
    private void deserialize() throws FileNotFoundException, IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream("webStructs.ser"));
        getOurHotel().webStructs = (List<WebStruct>) in.readObject();
        in.close();
    }
    */

    /**
     * @return the threadPool
     */
    public ExecutorService getThreadPool() {
        return threadPool;
    }

    /**
     * @return the threadCount
     */
    public int getThreadCount() {
        return threadCount;
    }

    /**
     * @param threadCount the threadCount to set
     */
    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }
}
