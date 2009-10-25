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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.swt.widgets.Text;

import com.jakubadamek.robotemil.htmlparser.BookingCom;
import com.jakubadamek.robotemil.htmlparser.HrsCom;
import com.jakubadamek.robotemil.htmlparser.LastminuteEs;

/**
 */
public class App
{
	private static final String DEFAULT_CACHE_LENGTH = "0";
	private static final String CACHE_LENGTH = "cacheLength";
	/** restart the same work unit when no response for as long */
	static final int RESTART_AFTER_SECONDS = 60;
	/** concurrent thread count */
	static int threadCount;
	/** default for the GUI version */
	private static final int GUI_THREAD_COUNT = 4;
	//private static final boolean TEST = false;
	private ResourceBundle bundle = ResourceBundle.getBundle("robotemil");
	private final String CUSTOMER = System.getProperty("customer", Customer.JALTA.toString());
	enum Customer { JALTA, PERLA }
	private AppFrame appFrame;
	private boolean useCache;

	/**
	 * List of all our hotels
	 */
	List<OurHotel> ourHotels = new ArrayList<OurHotel>();
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
	 */
    public static void main( String[] args ) throws IOException
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
    	JavaServiceWrapper.setApp(this);
    }

	private static void runWithGui() {
		App app = new App();
		app.appFrame = new AppFrame(app);
		threadCount = GUI_THREAD_COUNT;
		app.startWork();
	}

	private static void runWithoutGui(String ... args) {
		/* Args: - count of days since today, when to start
		 *       - count of days including the start date
		 */
		//if(new Scheduler().isScheduled("automatic " + args[0] + " " + args[1])) {
	    	App app = new App();
	    	threadCount = 1;
			app.startDate = new Date(new Date().getTime() + Long.valueOf(args[0]) * 24*60*60*1000);
			app.dayCount = Integer.valueOf(args[1]);
			app.useCache = true;
			System.out.println("startDate " + args[0] + " dayCount " + app.dayCount);
			app.startWork();
			app.workBody();
		/*} else {
			System.out.println(new Date() + " task is not scheduled now");
		}*/
	}

	private static void runPeriodically() {
		Date lastRun = new Date();
		int waitMinutes = 5;
		while(true) {
			try {
				runWithoutGui("0", "" + getCacheLength());
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
				System.out.println("waitMinutes: " + waitMinutes);
				Thread.sleep(waitMinutes * 60 * 1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

    private List<WebStruct> initWebStructs() {
    	List<WebStruct> webStructs = new ArrayList<WebStruct>();

    	WebStruct webStruct = new WebStruct();
    	webStruct.label = "booking.com";
    	webStruct.fileName = "booking_com_1.1.txt";
    	webStruct.excelName = "Booking";
    	webStruct.parserClass = BookingCom.class;
    	webStruct.iconName = "bookingcom.gif";
    	webStructs.add(webStruct);

    	webStruct = new WebStruct();
    	webStruct.label = "lastminute.es";
    	webStruct.fileName = "lastminute_1.1.txt";
    	webStruct.excelName = "Lastminute";
    	webStruct.parserClass = LastminuteEs.class;
    	webStruct.iconName = "lastminutees.gif";
    	webStructs.add(webStruct);

    	webStruct = new WebStruct();
    	webStruct.label = "hrs.com";
    	webStruct.fileName = "hrs_com_1.1.txt";
    	webStruct.excelName = "Hrs";
    	webStruct.parserClass = HrsCom.class;
    	webStruct.iconName = "hrscom.gif";
    	webStructs.add(webStruct);

   		return webStructs;
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

    /** thread work queue */
    List<WorkUnit> workQueue = Collections.synchronizedList(new ArrayList<WorkUnit>());

    /** progress - count of pages processed */
    int progress = 0;

    /** sleep
     * @param milliSeconds */
    static void sleep(int milliSeconds) {
    	try {
    		Thread.sleep(milliSeconds);
    	} catch(InterruptedException e) {
    		System.out.println(e.toString());
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

    void workBody() {
        OurHotel ourHotel = getOurHotel();
        for(Date date : getDates()) {
        	for(WebStruct webStruct : ourHotel.webStructs) {
        		WorkUnit workUnit = new WorkUnit();
        		workUnit.web = webStruct;
        		workUnit.date = date;
        		this.workQueue.add(workUnit);
        	}
        }
        if(appFrame != null) {
        	appFrame.runPrepare();
        }
		/*if(TEST) {
			try {
				deserialize();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else {*/
			List<DownloadThread> downloadThreads = new ArrayList<DownloadThread>();
	        for(int i=0; i < threadCount; i ++) {
	        	DownloadThread thread = new DownloadThread(this);
	        	thread.start();
	        	downloadThreads.add(thread);
	        }
	        if(appFrame != null) {
	        	appFrame.runProgress();
	        }
			while(this.workQueue.size() > 0) {
				App.sleep(1000);
			}
		//}
    }

	String getBundleString(String key) {
		return this.bundle.getString(key);
	}

	void saveHotels() {
		try {
			if(! new File(hotelsDir()).exists()) {
				new File(hotelsDir()).mkdirs();
			}
			for(OurHotel ourHotel : this.ourHotels) {
				for(WebStruct webStruct : ourHotel.webStructs) {
					BufferedWriter writer = new BufferedWriter(new FileWriter(new File(hotelsDir(), ourHotel.fileName(webStruct))));
					for(Text hotel : webStruct.hotelTexts) {
						writer.append(hotel.getText());
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

	void loadHotels() throws IOException {
		String[] ourHotelNames;
		switch(getCustomer()) {
		case JALTA: ourHotelNames = new String[] { "Jalta", "Alta" }; break;
		case PERLA: ourHotelNames = new String[] { "Perla" }; break;
		default: throw new IllegalArgumentException(getCustomer().toString());
		}
		for(String ourHotelName : ourHotelNames) {
			OurHotel ourHotel = new OurHotel(this);
			ourHotel.ourHotelName = ourHotelName;
			ourHotel.webStructs = initWebStructs();
			this.ourHotels.add(ourHotel);
	    	for(WebStruct web : ourHotel.webStructs) {
				web.hotelList.clear();
				String[] hotels = readFileToStrings(hotelsDir(), ourHotel.fileName(web));
				if(hotels.length == 0) {
					hotels = readFileToStrings(hotelsDir(), this.ourHotels.get(0).fileName(web));
				}
				for(String hotel : hotels) {
					web.hotelList.add(DiacriticsRemover.removeDiacritics(hotel).trim());
				}
	    	}
		}
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
				System.out.println(filename + " not found");
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
		return this.ourHotels.get(this.ourHotelIndex);
	}

	private DateFormat timeFormat = DateFormat.getTimeInstance();

	public void showLog(String row) {
		String logRow = timeFormat.format(new Date()) + " " + row;
		System.out.println(logRow);
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

	public void setCacheLength(int cacheLength) {
		Database.storeSetting(CACHE_LENGTH, "" + cacheLength);
		if(cacheLength == 0) {
			JavaServiceWrapper.stop();
		} else {
			JavaServiceWrapper.start();
		}
	}

	public static int getCacheLength() {
		return Integer.valueOf(Database.readSetting(CACHE_LENGTH, DEFAULT_CACHE_LENGTH));
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

	public static File netxDir() {
		File retval = new File(System.getenv("ALLUSERSPROFILE"),
				"netx\\cache\\http\\jakubadamek.me.cz\\trickbenchmark");
		if(! retval.exists()) {
			retval.mkdirs();
		}
		return retval;
	}
}
