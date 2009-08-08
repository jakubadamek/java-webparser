package com.jakubadamek.robotemil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TreeSet;

import jxl.Workbook;
import jxl.biff.EmptyCell;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.NumberFormat;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import com.jakubadamek.robotemil.htmlparser.BookingCom;
import com.jakubadamek.robotemil.htmlparser.HrsCom;
import com.jakubadamek.robotemil.htmlparser.LastminuteEs;

/**
 * TODO: seznam hotelu se aktualizuje az pri pristim spusteni
 * 
 * hrs.com parseCSS 35 s
 *         getByXPath 26 s
 *         HTML parse 24 s
 *              loadExternalJavaScriptFile 10 s
 *         setSelectedAttrib 69 s
 * booking.com extractAllNodesThatMatch 189 s
 *                 Lexer.nextNode       151 s
 * lastminute.es extractAllNodesThatMatch 82 s	                         
 *          
 */
public class App 
{
	private static final int MIN_HOTEL_ROWS = 15;
	/** restart the same work unit when no response for as long */
	static final int RESTART_AFTER_SECONDS = 30;
	/** concurrent thread count */
	static final int THREAD_COUNT = 4;
	/** days per row in the Excel result */
	private static final int DAYS_PER_ROW = 3;
	private static final boolean TEST = false;
	private static final int EMPTY_ROWS_EXCEL = 3;
	private ResourceBundle bundle = ResourceBundle.getBundle("robotemil");
	private final String CUSTOMER = System.getProperty("customer", Customer.JALTA.toString());
	private enum Customer { JALTA, PERLA };	
	
	/**
	 * List of all our hotels
	 */
	List<OurHotel> ourHotels = new ArrayList<OurHotel>();
	/**
	 * Index of currently chosen tab in our hotels
	 */
	int ourHotelIndex = 0;
	private DateTime dateTime;
	private Spinner spinnerDays;
	/** shell */
	public Shell shell;
	/** progress bar */
	ProgressBar progressBar;
	private Font searchFont;
	/** stop execution */
	public static boolean stop;

	/**
	 * Main
	 * @param args
	 * @throws IOException 
	 */
    public static void main( String[] args ) throws IOException
    {
    	new App().run();
    }
    
    private List<WebStruct> initWebStructs() {
    	List<WebStruct> webStructs = new ArrayList<WebStruct>();
    	
    	WebStruct webStruct = new WebStruct();
    	webStruct.label = "booking.com";
    	webStruct.fileName = "booking_com_1.1.txt";
    	webStruct.excelName = "Booking";
    	webStruct.parserClass = BookingCom.class;
    	webStruct.iconName = "bookingcom.gif";
    	webStructs.add(webStruct); // */

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
    	webStructs.add(webStruct);// */
    	
   		return webStructs;
    }
    
    private Customer getCustomer() {
    	return Customer.valueOf(CUSTOMER);
    }
    
    private String title() {
        switch(getCustomer()) {
        case JALTA:
            return "Robot Emil";
        default:
            return "Trick Benchmark";        	
        }    	
    }
    
    private class Authorizer implements Runnable {
    	// is the usage authorized?
    	boolean authorized = false;
    	
		@Override
		public void run() {
			authorized = true;
	    	for(OurHotel ourHotel : ourHotels) {
	    		for(WebStruct webStruct : ourHotel.webStructs) {
	    			if(webStruct.hotelTexts.size() > 0) {
	    				String hotel = DiacriticsRemover.removeDiacritics(webStruct.hotelTexts.get(0).getText()).toUpperCase();
	    				if(! hotel.contains(ourHotel.ourHotelName.toUpperCase())) {
	    					String msg = MessageFormat.format(getBundleString("Neopravneny pristup"), webStruct.label, ourHotel.ourHotelName);
	    					displayException(msg, new IllegalStateException());
	    					authorized = false;
	    					return;
	    				}
	    			}
	    		}
	    	}
		}    	
    }
    
    private boolean authorize() {
    	Authorizer authorizer = new Authorizer();
    	this.shell.getDisplay().syncExec(authorizer);
    	return authorizer.authorized;
    }
    
    private void run() throws IOException {
    	loadHotels();
        Display display = new Display ();
        this.shell = new Shell (display);
        this.shell.setText(title());
        this.shell.setImage(new Image(display, this.getClass().getClassLoader().getResourceAsStream("images.jpg")));
		initWidgets();        
        display.timerExec(500, new Runnable() {
        	public void run() {
        	    App.this.shell.forceActive();
        	    App.this.shell.setActive();
        	}
        });
        this.shell.open ();
        while (!this.shell.isDisposed ()) {
           if (!display.readAndDispatch ()) display.sleep ();
        }
        display.dispose ();    	
    }
    
    private void initWidgets() {
    	this.shell.setLayout(new GridLayout());
        Composite cmp = new Composite(this.shell, SWT.NONE);
        GridLayout cmpLayout = new GridLayout();
        cmp.setLayout(cmpLayout);
        GridData cmpGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        cmpGridData.minimumWidth = 640;
        cmp.setLayoutData(cmpGridData);

        // new row
        /*Label help = new Label(cmp, SWT.WRAP);
        help.setText(title() + " " + getBundleString("Help"));
        GridData gdHelp = new GridData(SWT.LEFT, SWT.TOP, false, false);
        help.setLayoutData(gdHelp);
        */
        
        // new row
        Composite row3 = new Composite(cmp, SWT.NONE);
        GridLayout row3Layout = new GridLayout();
        row3Layout.numColumns = 7;
        row3.setLayout(row3Layout);
        row3.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
        
        biggerFont = new Font(this.shell.getDisplay(), new FontData("Arial", 11, SWT.BOLD));
        Label datum = new Label (row3, SWT.NONE);
        datum.setText ("Datum:");
        datum.setFont(biggerFont);
        this.dateTime = new DateTime(row3, SWT.LONG | SWT.DATE);
        this.dateTime.setFont(biggerFont);
        this.dateTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        new Label(row3, SWT.NONE).setText("   ");
        
        Label dayCount = new Label (row3, SWT.NONE);
        dayCount.setText (getBundleString("Pocet dnu:"));
        dayCount.setFont(biggerFont);
        this.spinnerDays = new Spinner(row3, SWT.BORDER);
		this.spinnerDays.setMinimum(1);
		this.spinnerDays.setMaximum(99);
		this.spinnerDays.setFont(biggerFont);
		GridData gdDays = new GridData();
		gdDays.widthHint = 30;
		this.spinnerDays.setLayoutData(gdDays);
        this.spinnerDays.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        new Label(row3, SWT.NONE).setText("   ");
		
        searchFont = new Font(this.shell.getDisplay(), new FontData("Arial", 12, SWT.BOLD));
        this.btnRun = new Button(row3, SWT.CENTER);
		this.btnRun.setText("Hledat ");
		this.btnRun.setFont(searchFont);
        this.btnRun.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        // new row
        initTabFolder(cmp);
                
        // new row
        Composite rowLog = new Composite(cmp, SWT.NONE);
        GridLayout rowLogLayout = new GridLayout();
        rowLogLayout.numColumns = 3;
        rowLog.setLayout(rowLogLayout);
        rowLog.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
        this.txtDuration = new Text(rowLog, SWT.BORDER | SWT.READ_ONLY);
        this.txtDuration.setTextLimit(5);
        this.progressBar = new ProgressBar(rowLog, SWT.BORDER);
        this.progressBar.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
        //this.txtDuration.setFont(biggerFont);

        this.txtLog = new Text(rowLog, SWT.BORDER | SWT.READ_ONLY);
        this.txtLog.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        initListeners();
        
        this.shell.pack();
    }

	private void initListeners() {
		this.btnRun.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings({ "synthetic-access" })
			@Override
			public void widgetSelected(SelectionEvent arg0) {
		    	if(! authorize()) {
		    		return;
		    	}
				App.this.btnRun.setEnabled(false);
				new Thread() {
					@Override
					public void run() {
						onBtnRun();
					}
				}.start();
			}
        });
        
        this.shell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
	             System.exit(0);
	             biggerFont.dispose();
	             searchFont.dispose();
			}        	
        });
	}

	private void initTabFolder(Composite cmp) {
		if(this.ourHotels.size() > 1) {
			this.tabFolder = new TabFolder(cmp, SWT.NONE);
	        this.tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
	        this.tabFolder.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent arg0) {
					App.this.ourHotelIndex = App.this.tabFolder.getSelectionIndex();
				}
				public void widgetSelected(SelectionEvent arg0) {
					widgetDefaultSelected(arg0);
				}        	
	        });
	        this.tabFolder.setFont(biggerFont);
		}
        for(OurHotel ourHotel : this.ourHotels) {
        	Composite cmpWebs;
        	if(this.ourHotels.size() > 1) {
	        	TabItem tabItem = new TabItem(this.tabFolder, SWT.NONE);
	        	tabItem.setText(ourHotel.ourHotelName);
		        cmpWebs = new Composite(this.tabFolder, SWT.NONE);
		        tabItem.setControl(cmpWebs);
        	} else {
        		cmpWebs = new Composite(cmp, SWT.NONE);
        	}
	        GridLayout glWebs = new GridLayout();
	        glWebs.numColumns = ourHotel.webStructs.size() * 2;
	        //glWebs.marginWidth = glWebs.marginHeight = 0;
	        cmpWebs.setLayout(glWebs);
	        GridData gdWebs = new GridData(SWT.FILL, SWT.TOP, true, false);
	        cmpWebs.setLayoutData(gdWebs);
	        
	        // row 2
	        for(WebStruct webStruct : ourHotel.webStructs) {
	        	Image icon = new Image(this.shell.getDisplay(), getClass().getClassLoader().getResourceAsStream(webStruct.iconName));
	        	new Button(cmpWebs, SWT.FLAT).setImage(icon);
	            new Label(cmpWebs, SWT.NONE).setText(webStruct.label);        	
	        }
	
	        KeyListener hotelsListener =  new KeyAdapter() {
				@SuppressWarnings("synthetic-access")
				@Override
				public void keyReleased(KeyEvent arg0) {
					saveHotels();				
				}
	        };
	        
	        // row 3 
	        int nhotels = ourHotel.webStructs.get(0).hotelList.size() + 3;
	        if(nhotels < MIN_HOTEL_ROWS) {
	        	nhotels = MIN_HOTEL_ROWS;
	        }
	        for(int i=0; i < nhotels; i ++) {
	        	for(WebStruct webStruct : ourHotel.webStructs) {
		        	Text text = new Text(cmpWebs, SWT.BORDER);
		        	GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, false);
		        	gridData.horizontalSpan = 2;
		        	text.setLayoutData(gridData);
		        	text.addKeyListener(hotelsListener);
		        	if(i < webStruct.hotelList.size()) {
		        		text.setText(webStruct.hotelList.get(i));
		        	}
		        	webStruct.hotelTexts.add(text);
	        	}
	        }
        }
	}
    
    /**
     * @return dates for which prices are searched
     */
    List<Date> getDates() {    	
        final List<Date> retval = new ArrayList<Date>();
        this.shell.getDisplay().syncExec(new Runnable() {
        	@SuppressWarnings({ "synthetic-access", "boxing" })
			public void run() {
				Calendar calendar = Calendar.getInstance(); 
		        calendar.set(App.this.dateTime.getYear(), App.this.dateTime.getMonth(), App.this.dateTime.getDay(), 0, 0, 0);
		        calendar.set(Calendar.MILLISECOND, 0);
		        for(int iduration = 0; iduration < App.this.spinnerDays.getSelection(); iduration ++) {
		        	retval.add(calendar.getTime());
		        	calendar.add(Calendar.DAY_OF_MONTH, 1);
		        }
        	}
        });
        return retval;
    }
    
    /** show the progress 
     * @param finishedCount */
    void progress(final int finishedCount) {
		this.shell.getDisplay().syncExec(new Runnable() {
			@SuppressWarnings("synthetic-access")
			public void run() {
				App.this.progressBar.setSelection(finishedCount + 1);							
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(new Date().getTime() - App.this.start.getTime()));
				final Formatter formatter = new Formatter(new StringBuilder(), Locale.getDefault());
				App.this.txtDuration.setText(formatter.format("%1$tM:%1$tS", cal).toString());
			}
		});    	
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
    
    private void onBtnRun() {
		this.shellDisposed = false;
		try {
	        this.start = new Date();
	        App.stop = false;
	        OurHotel ourHotel = getOurHotel();
	        for(Date date : getDates()) {
	        	for(WebStruct webStruct : ourHotel.webStructs) {
	        		WorkUnit workUnit = new WorkUnit();
	        		workUnit.web = webStruct;
	        		workUnit.date = date;
	        		this.workQueue.add(workUnit);
	        	}
	        }
	        this.progress = 0;
			this.shell.getDisplay().syncExec(new Runnable() {
				public void run() {
					App.this.progressBar.setMaximum(App.this.workQueue.size() + 1);
					App.this.progressBar.setSelection(1);
				}
			});
			if(TEST) {
				try {
					deserialize();
				} catch (Exception e) {
					throw new RuntimeException(e);
				} 
			} else {
				List<DownloadThread> downloadThreads = new ArrayList<DownloadThread>();
		        for(int i=0; i < THREAD_COUNT; i ++) {
		        	DownloadThread thread = new DownloadThread(this);
		        	thread.start();
		        	downloadThreads.add(thread);	        	
		        }
		        Runnable runnableProgress = new Runnable() {
		        	public void run() {
		        		if(! App.this.progressBar.isDisposed()) {
		        			progress(App.this.progress);
		        			App.this.shell.getDisplay().timerExec(1, this);
		        		}
		        	}
				};
				this.shell.getDisplay().asyncExec(runnableProgress);
				while(this.workQueue.size() > 0) {
					App.sleep(1000);
				}
			}
	        if(App.stop) {
	        	return;
	        }
        	// dispose shell
			this.shell.getDisplay().syncExec(new Runnable() {
				@SuppressWarnings("synthetic-access")
				public void run() {
					boolean retval = false;
			        while(true) {
			    		try {
			    			retval = createXls();
			    			break;
			    		} catch(Exception e) {
			    			e.printStackTrace();
			    			displayException(getBundleString("Doslo k chybe Excel"), e);
			    		}
			        }
					if(retval) {
						App.this.shell.dispose();
						App.this.shellDisposed = true;
					}
				}
			});
		} finally {
			if(! this.shellDisposed) {
				this.shell.getDisplay().syncExec(new Runnable() {
					@SuppressWarnings("synthetic-access")
					public void run() {
						App.this.btnRun.setEnabled(true);
					}
				});
			}
		}		
	}
	
	private String getBundleString(String key) {
		return this.bundle.getString(key);
	}
	
	private void displayException(final String msg, final Exception e) {
		System.out.println(e);
		displayMessage(msg + "\n" + e.toString(), getBundleString("Interni chyba"));
	}
	
	private void displayMessage(final String msg, final String title) {
		this.shell.getDisplay().syncExec(new Runnable() {
			public void run() {
				MessageBox messageBox = new MessageBox(App.this.shell, SWT.OK);
				messageBox.setText(title);
				messageBox.setMessage(msg);
				messageBox.open();				
			}
		});		
	}

	private void saveHotels() {
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
        	displayException(getBundleString("Chyba pri ukladani nazvu hotelu"), e);
        }		
	}
	
	private String hotelsDir() {
		return new File(System.getProperty("user.home"), getCustomer().toString()).getPath();
	}
	
	private void loadHotels() throws IOException {
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

	private int irow = 0;
	private Text txtDuration;
	private Button btnRun;
	private Date start;
	private boolean shellDisposed;
	
	private void writeXlsHotelNames(WritableWorkbook workbook) throws RowsExceededException, WriteException {
		WritableSheet sheet = workbook.createSheet("nazvy hotelu", 2);
		int icol = 0;
		for(WebStruct webStruct : getOurHotel().webStructs) {
			sheet.setColumnView(icol, 40);
			sheet.addCell(new jxl.write.Label(icol, 0, webStruct.label));
			this.irow = 1;
			for(String hotel : new TreeSet<String>(webStruct.prices.data.keySet())) {
				sheet.addCell(new jxl.write.Label(icol, this.irow, hotel));
				this.irow ++;
			}
			icol ++;
		}
	}
	
	private OurHotel getOurHotel() {
		return this.ourHotels.get(this.ourHotelIndex);
	}

    private NumberFormat priceFormat = new NumberFormat("#");
    private NumberFormat orderFormat = new NumberFormat("#");
    private WritableFont orderFont = new WritableFont(WritableFont.TAHOMA, 7); 
    private WritableFont orderBoldFont = new WritableFont(WritableFont.TAHOMA, 7, WritableFont.BOLD);
    //private WritableFont firstHotelFont = new WritableFont(WritableFont.TAHOMA, 10, WritableFont.BOLD);
    private WritableFont priceFont = new WritableFont(WritableFont.TAHOMA, 10, WritableFont.BOLD);
    private WritableFont redPriceFont = new WritableFont(WritableFont.TAHOMA, 10, WritableFont.BOLD);
    private WritableFont bigFont = new WritableFont(WritableFont.TAHOMA, 12, WritableFont.BOLD);
    private int lastHotel = 0;
    /** text box showing log */
	public Text txtLog;
	/** tab folder (our hotels) */
	TabFolder tabFolder;
	private Font biggerFont;
	
	private WritableCellFormat getCellFormat(Date date, int ihotel, int col, int row, WritableFont font) throws WriteException {
		WritableCellFormat cellFormat = new WritableCellFormat(col % 2 == 1 ? this.orderFormat : this.priceFormat);
		if(date != null && ihotel >= 0) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			if(calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
				cellFormat.setBackground(Colour.YELLOW);
			}
		}
		if(ihotel == 0) {
			//cellFormat.setFont(this.firstHotelFont);
			cellFormat.setBorder(Border.TOP, BorderLineStyle.MEDIUM);
			cellFormat.setBorder(Border.BOTTOM, BorderLineStyle.MEDIUM);
		}
		if(ihotel == this.lastHotel) {
			cellFormat.setBorder(Border.BOTTOM, BorderLineStyle.MEDIUM);			
		}
		if(col % (getOurHotel().webStructs.size() * 2) == 1) {
			cellFormat.setBorder(Border.LEFT, BorderLineStyle.MEDIUM);
		}		
		if(col % (getOurHotel().webStructs.size() * 2) == 0) {
			cellFormat.setBorder(Border.RIGHT, BorderLineStyle.MEDIUM);			
		}
		if(row == 0) {
			cellFormat.setBorder(Border.TOP, BorderLineStyle.MEDIUM);
		}
		if(col == 0) {
			cellFormat.setBorder(Border.LEFT, BorderLineStyle.MEDIUM);
		}
		if(font != null) {
			cellFormat.setFont(font);
		}
		else if(row >= 2) {
			if(col % 2 == 1) {
				if(row == 2) {
					cellFormat.setFont(this.orderBoldFont);
				} else {
					cellFormat.setFont(this.orderFont);
				}
			} else if(col > 1) {
				cellFormat.setFont(this.priceFont);
			}
		}
		if(row >= 2 && col >= 1) {
			cellFormat.setAlignment(Alignment.CENTRE);
		}
		return cellFormat;
	}
	
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
	
	private boolean createXls() throws IOException, RowsExceededException, WriteException {
		if(! this.redPriceFont.getColour().equals(Colour.RED)) {
			this.redPriceFont.setColour(Colour.RED);
		}

		String filename = "robotemil.xls";
		WritableWorkbook workbook = Workbook.createWorkbook(new File(filename));
		boolean retval = writeXlsPrices(workbook);
		serialize();
		writeXlsHotelNames(workbook);
        workbook.write();
        workbook.close();
        Runtime.getRuntime().exec("cmd /c \"" + filename + "\"");
		return retval;
	}

	@SuppressWarnings("boxing")
	private boolean writeXlsPrices(WritableWorkbook workbook) throws RowsExceededException, WriteException, IOException {
		if(! TEST) {
			saveHotels();
			loadHotels();
		}
		WritableSheet sheet = workbook.createSheet("ceny za pokoj", 1);
		sheet.setColumnView(0, 40);
        Calendar cal = Calendar.getInstance();
        //sheet.addCell(new jxl.write.Label(0, this.irow, "Www", new WritableCellFormat(font)));
        int icol = 1;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd. M. EEEE");
        OurHotel ourHotel = getOurHotel();
        // column widths
        for(icol = 1; icol < 1 + getDates().size() * ourHotel.webStructs.size() * 2; icol += 2) {
        	sheet.setColumnView(icol, 6);
        	sheet.setColumnView(icol + 1, 5);
        }
        icol = 1;
        // dates
        for(Date date : getDates()) {
        	cal.setTime(date);
        	sheet.addCell(new jxl.write.Label(
        			icol, this.irow, simpleDateFormat.format(cal.getTime()), 
        			getCellFormat(date, -1, icol, this.irow, this.bigFont)));
        	icol += ourHotel.webStructs.size() * 2;
        }
        this.irow += 2;
        // find last hotel
        for(int ihotel = 0; ihotel < ourHotel.webStructs.get(0).hotelTexts.size(); ihotel ++) {
        	for(WebStruct webStruct : ourHotel.webStructs) {
        		if(webStruct.hasHotel(ihotel)) {
                	this.lastHotel = ihotel;
        		}
        	}
        }
        // hotel names
        for(int ihotel = 0; ihotel <= this.lastHotel; ihotel ++) {
        	WebStruct webStruct = ourHotel.webStructs.get(0);
    		String hotelName = webStruct.hasHotel(ihotel) ? webStruct.hotelTexts.get(ihotel).getText() : "";
        	sheet.addCell(new jxl.write.Label(0, this.irow + ihotel, hotelName,
        			getCellFormat(null, ihotel, 0, this.irow + ihotel, null)));
        }
        icol = 1;
        // order and price
        for(Date date : getDates()) {
        	for(WebStruct webStruct : ourHotel.webStructs) {
        		sheet.addCell(new jxl.write.Label(icol, this.irow - 1, webStruct.excelName,
        				getCellFormat(date, -1, icol, this.irow - 1, null)));
        		//sheet.mergeCells(icol, this.irow - 1, icol + 1, this.irow - 1);
        		double firstHotelPrice = 0;
		        for(int ihotel = 0; ihotel <= this.lastHotel; ihotel ++) {
	    			String hotel = webStruct.hasHotel(ihotel) ? webStruct.hotelTexts.get(ihotel).getText() : "";
	        		boolean noResult = true;
	    			if(hotel.trim().length() > 0) {
		        		//String match = "";
			        	for(String hotelKey : webStruct.prices.data.keySet()) {
		    				//if(hotelKey.contains(hotel)) {
			    			if(hotelKey.equals(hotel)) {
		    					/*if(match != "") {
		    						displayMessage(MessageFormat.format(getBundleString("Nejednoznacny nazev"), 
		    								new Object[] { hotel, webStruct.label, match + ", " + hotelKey }), 
		    								"Chyba");
		    						return false;
		    					}*/
		    					//match = hotelKey;
		    					PriceAndOrder priceAndOrder = webStruct.prices.data.get(hotelKey).get(date);
		    					if(priceAndOrder != null) {
		    						sheet.addCell(new jxl.write.Number(icol, this.irow + ihotel, priceAndOrder.order,
		    								getCellFormat(date, ihotel, icol, this.irow + ihotel, null)));
		    						sheet.addCell(new jxl.write.Number(icol + 1, this.irow + ihotel, priceAndOrder.price,
		    								getCellFormat(date, ihotel, icol + 1, this.irow + ihotel, firstHotelPrice > priceAndOrder.price ? this.redPriceFont : null)));
		    			        	if(ihotel == 0) {
		    			        		firstHotelPrice = priceAndOrder.price;
		    			        	}
		    			        	noResult = false;
		    					} 
		    				}	    				
		    			}
	    			}
		        	if(noResult) {
						sheet.addCell(new jxl.write.Label(icol, this.irow + ihotel, "",
								getCellFormat(date, ihotel, icol, this.irow + ihotel, null)));
		        		sheet.addCell(new jxl.write.Label(icol + 1, this.irow + ihotel, "X",
		        				getCellFormat(date, ihotel, icol + 1, this.irow + ihotel, null)));
		        	}
		        }
		        icol += 2;
        	}
        }
        splitDates(sheet);
        return true;
	}

	/**
	 * Splits the table so that there are only 3 days per row
	 * 
	 * @param sheet
	 * @throws RowsExceededException
	 * @throws WriteException
	 */
	private void splitDates(WritableSheet sheet) throws RowsExceededException, WriteException {
		OurHotel ourHotel = getOurHotel();
		int moveWidth = ourHotel.webStructs.size() * 2 * DAYS_PER_ROW;
		WritableCellFormat cellFormat = new WritableCellFormat();
		cellFormat.setBorder(Border.TOP, BorderLineStyle.MEDIUM);
		// top border
		for(int icol = 1; icol < getDates().size() * 2 * ourHotel.webStructs.size() + 1; icol ++) {
			WritableCell cell = sheet.getWritableCell(icol, 0);
			if(cell instanceof EmptyCell) {
				sheet.addCell(new jxl.write.Label(icol, 0, "", cellFormat));
			}
		}
		// border for A1
		cellFormat = new WritableCellFormat();
		cellFormat.setBorder(Border.LEFT, BorderLineStyle.MEDIUM);
		cellFormat.setBorder(Border.TOP, BorderLineStyle.MEDIUM);
		sheet.addCell(new jxl.write.Label(0, 0, "", cellFormat));
		// border for A2
		cellFormat = new WritableCellFormat();
		cellFormat.setBorder(Border.LEFT, BorderLineStyle.MEDIUM);
		sheet.addCell(new jxl.write.Label(0, 1, "", cellFormat));
		// split rows
		int nrows = (getDates().size() + 2) / DAYS_PER_ROW;
		for(int idateRow = 0; idateRow < nrows; idateRow ++) {
			int toRow = idateRow * (this.lastHotel + 3 + EMPTY_ROWS_EXCEL);
			copyCells(sheet, 0, 0, 0, this.lastHotel + 1 + EMPTY_ROWS_EXCEL, 0, toRow);
			int colStart = idateRow * moveWidth + 1;
			// for last row: decrease moveWidth
			int overhead = (idateRow + 1) * DAYS_PER_ROW - getDates().size(); 
			if(overhead > 0) {
				moveWidth -= overhead * ourHotel.webStructs.size() * 2; 
			}
			if(idateRow > 0) {
				copyCells(sheet, colStart, 0, colStart + moveWidth - 1, this.lastHotel + EMPTY_ROWS_EXCEL - 1, 1, toRow);
				clearCells(sheet, colStart, 0, colStart + moveWidth - 1, this.lastHotel + EMPTY_ROWS_EXCEL - 1);
		        // last border
		        for(int row = 0; row < 2; row ++) {
		        	sheet.addCell(new jxl.write.Label(moveWidth + 1, toRow + row, "", getCellFormat(null, -1, moveWidth + 1, toRow + row, null)));
		        }			
			}
			// merge date cells
			for(int idate = 0; idate < DAYS_PER_ROW; idate ++) {
				//System.out.println(idate * DAYS_PER_ROW + 1+", "+ toRow+", "+ (idate + 1) * DAYS_PER_ROW+", "+ toRow);
				sheet.mergeCells(idate * DAYS_PER_ROW * 2 + 1, toRow, (idate + 1) * DAYS_PER_ROW * 2, toRow);
			}
		}
		moveWidth = ourHotel.webStructs.size() * 2 * Math.min(getDates().size(), DAYS_PER_ROW);
        // last border on first row
        for(int row = 0; row < 2; row ++) {
        	sheet.addCell(new jxl.write.Label(moveWidth + 1, row, "", cellFormat));
        }			
	}

	private void copyCells(WritableSheet sheet, int fromCol1, int fromRow1, int fromCol2, int fromRow2, int toCol, int toRow) throws RowsExceededException, WriteException {
		for(int col = fromCol1; col <= fromCol2; col ++) {
			for(int row = fromRow1; row <= fromRow2; row ++) {
				sheet.addCell(sheet.getWritableCell(col, row).copyTo(toCol + col - fromCol1, toRow + row - fromRow1));
			}
		}
	}

	private void clearCells(WritableSheet sheet, int fromCol1, int fromRow1, int fromCol2, int fromRow2) throws RowsExceededException, WriteException {
		for(int col = fromCol1; col <= fromCol2; col ++) {
			for(int row = fromRow1; row <= fromRow2; row ++) {
				sheet.addCell(new jxl.write.Label(col, row, ""));
			}
		}
	}
}
