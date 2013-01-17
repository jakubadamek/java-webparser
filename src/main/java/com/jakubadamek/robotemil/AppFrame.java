package com.jakubadamek.robotemil;

import java.awt.SplashScreen;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;


public class AppFrame
{
    private final Logger logger = Logger.getLogger(getClass());
	private static final int MIN_HOTEL_ROWS = 15;
	private static final int ADDITIONAL_ROWS = 10;
	private static final int MAX_LENGTH_OF_STAY = 3;
	/** shell */
	public Shell shell;
	/** progress bar */
	ProgressBar progressBar;
	Font searchFont;
    /** text box showing log */
	public Text txtLog;
	/** tab folder (our hotels) */
	TabFolder tabFolder;
	Font biggerFont;
	DateTime dateTime;
	Spinner spinnerDays;
	private Text txtDuration;
	private Button btnRun;
	private Button btnImport;
	Button useCache;
	App app;
	private Date start;
	private boolean shellDisposed;
	boolean showDuration;
	private List<Button> enabledWebs = new ArrayList<Button>();
	private List<Button> lengthsOfStay = new ArrayList<Button>();

    public AppFrame(App app) {
		this.app = app;
	}

    private void closeSplashscreen() {
        SplashScreen splash = SplashScreen.getSplashScreen();
        if(splash != null) {
            splash.close();
        }
    }
    
	void runGui() {
	    closeSplashscreen();
        Display display = new Display ();
        this.shell = new Shell (display);
        this.shell.setText(app.getSettingsModel().getAppTitle());
        this.shell.setImage(new Image(display, this.getClass().getClassLoader().getResourceAsStream("images.jpg")));
		initWidgets(this.shell);
        display.timerExec(500, new Runnable() {
        	@Override
			public void run() {
        	    AppFrame.this.shell.forceActive();
        	    AppFrame.this.shell.setActive();
        	}
        });
        this.shell.open ();
        while (!this.shell.isDisposed ()) {
           if (!display.readAndDispatch ()) display.sleep ();
        }
        display.dispose ();
    }

    private void initWidgets(Shell aShell) {
    	this.shell = aShell;
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
        dayCount.setText (app.getBundleString("Pocet dnu:"));
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
        Composite row4 = new Composite(cmp, SWT.NONE);
        GridLayout row4Layout = new GridLayout();
        row4Layout.numColumns = 2;
        row4.setLayout(row4Layout);
        Label useCacheLabel = new Label(row4, SWT.NONE);
        useCacheLabel.setText(app.getBundleString("Pouzit cache"));
        useCacheLabel.setFont(biggerFont);
        this.useCache = new Button(row4, SWT.CHECK);
        this.useCache.setFont(biggerFont);
        this.useCache.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        this.useCache.setSelection(true);
        
        // new row: enabled webs
        Composite rowEnabledWebs = new Composite(cmp, SWT.NONE);
        GridLayout rowEnabledWebsLayout = new GridLayout();
        rowEnabledWebsLayout.numColumns = app.getOurHotel().getWebStructs().size() + 1;
        rowEnabledWebs.setLayout(rowEnabledWebsLayout);
        Label enabledWebsLabel = new Label(rowEnabledWebs, SWT.NONE);
        enabledWebsLabel.setText(app.getBundleString("Nacitat z techto webu"));
        enabledWebsLabel.setFont(biggerFont);
        for(WebStruct webStruct : app.getOurHotel().getWebStructs()) {
            Button check = new Button(rowEnabledWebs, SWT.CHECK);
            check.setFont(biggerFont);
            check.setSelection(webStruct.getParams().getEnabled());
            check.setText(webStruct.getParams().getLabel());
            enabledWebs.add(check);
        }
        
        // new row: length of stay
        Composite rowLengthOfStay = new Composite(cmp, SWT.NONE);
        GridLayout rowLengthOfStayLayout = new GridLayout();
        rowLengthOfStayLayout.numColumns = app.getOurHotel().getWebStructs().size() + 1;
        rowLengthOfStay.setLayout(rowLengthOfStayLayout);
        Label rowLengthOfStayLabel = new Label(rowLengthOfStay, SWT.NONE);
        rowLengthOfStayLabel.setText(app.getBundleString("Delka pobytu"));
        rowLengthOfStayLabel.setFont(biggerFont);
        for(int lengthOfStay = 1; lengthOfStay <= MAX_LENGTH_OF_STAY; lengthOfStay ++) {
            Button check = new Button(rowLengthOfStay, SWT.CHECK);
            check.setFont(biggerFont);
            check.setSelection(app.getLengthsOfStay().contains(new Integer(lengthOfStay)));
            check.setText("" + lengthOfStay);
            lengthsOfStay.add(check);
        }

        // new row
        Composite rowImport = new Composite(cmp, SWT.NONE);
        rowImport.setLayout(new GridLayout());
        importHotels(rowImport);
        
        // new row
        Composite row5 = new Composite(cmp, SWT.NONE);
        GridLayout row5Layout = new GridLayout();
        row5Layout.numColumns = 4;
        row5.setLayout(row5Layout);
        // new row
        initTabFolder(cmp);

        // new row
        Composite rowLog = new Composite(cmp, SWT.NONE);
        GridLayout rowLogLayout = new GridLayout();
        rowLogLayout.numColumns = 2;
        rowLog.setLayout(rowLogLayout);
        rowLog.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
        this.txtDuration = new Text(rowLog, SWT.BORDER | SWT.READ_ONLY);
        this.txtDuration.setTextLimit(5);
        this.progressBar = new ProgressBar(rowLog, SWT.BORDER);
        this.progressBar.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
        //this.txtDuration.setFont(biggerFont);

        // new row
        Composite rowLog2 = new Composite(cmp, SWT.NONE);
        GridLayout rowLog2Layout = new GridLayout();
        rowLog2.setLayout(rowLog2Layout);
        rowLog2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        this.txtLog = new Text(rowLog2, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        GridData txtLogGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        txtLogGridData.heightHint = 60;
        this.txtLog.setLayoutData(txtLogGridData);

        initListeners();

        this.shell.pack();
    }

	private void importHotels(Composite row4) {
		this.btnImport = new Button(row4, SWT.CENTER);
        this.btnImport.setText("Importovat hotely z Excelu, list 'nastaveni'");
        this.btnImport.setFont(searchFont);
        this.btnImport.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        this.btnImport.addSelectionListener(new SelectionAdapter() {			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
		        FileDialog fd = new FileDialog(shell, SWT.OPEN);
		        fd.setText("Open");
		        //fd.setFilterPath("C:/");
		        String[] filterExt = { "*.xls", "*.*" };
		        fd.setFilterExtensions(filterExt);
		        String filename = fd.open();
		        if(filename != null) {
			        try {
						new ImportExcel(app.getOurHotel(), app).readXlsSettings(filename);
					} catch (Exception e) {
						displayException("Chyba pri nacitani z Excelu", e);
					}
		        }
		 	}
		});
	}
	
	private void initListeners() {
		this.btnRun.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings({ "synthetic-access" })
			@Override
			public void widgetSelected(SelectionEvent arg0) {
		    	if(! authorize()) {
		    		return;
		    	}
				AppFrame.this.btnRun.setEnabled(false);
				new Thread() {
					@Override
					public void run() {
						try {
							onBtnRun();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}.start();
			}
        });

        this.shell.addDisposeListener(new DisposeListener() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void widgetDisposed(DisposeEvent e) {
				shellDisposed = true;
			    bindHotelNames();
			    bindEnabledWebs();
			    bindLengthsOfStay();
			    app.saveHotels();
			    app.storeEnabledWebs();
			    app.storeLengthsOfStay();
			    App.stop = true;
                biggerFont.dispose();
	            searchFont.dispose();
                app.workUnitsManager.shutdown();
			}
        });
	}

	private void initTabFolder(Composite cmp) {
		if(app.getSettingsModel().getOurHotels().size() > 1) {
			this.tabFolder = new TabFolder(cmp, SWT.NONE);
			GridData tabFolderData = new GridData(SWT.FILL, SWT.TOP, true, false);
			tabFolderData.heightHint = 250;
	        this.tabFolder.setLayoutData(tabFolderData);
	        this.tabFolder.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
					app.ourHotelIndex = AppFrame.this.tabFolder.getSelectionIndex();
				}
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					widgetDefaultSelected(arg0);
				}
	        });
	        this.tabFolder.setFont(biggerFont);
		}
        for(OurHotel ourHotel : app.getSettingsModel().getOurHotels()) {
        	ScrolledCompositeWrapper scrolled;
        	if(app.getSettingsModel().getOurHotels().size() > 1) {
	        	TabItem tabItem = new TabItem(this.tabFolder, SWT.NONE);
	        	tabItem.setText(ourHotel.getOurHotelName());
	        	scrolled = new ScrolledCompositeWrapper(this.tabFolder);
		        tabItem.setControl(scrolled.getParent());
        	} else {
        		scrolled = new ScrolledCompositeWrapper(cmp);
        	}
    		Composite cmpWebs = scrolled.getComposite();
	        GridLayout glWebs = new GridLayout();
	        glWebs.numColumns = ourHotel.getWebStructs().size() * 2;
	        //glWebs.marginWidth = glWebs.marginHeight = 0;
	        cmpWebs.setLayout(glWebs);
	        GridData gdWebs = new GridData(SWT.LEFT, SWT.TOP, false, false);
	        gdWebs.heightHint = 300;
	        scrolled.getParent().setLayoutData(gdWebs);

	        // row 2
	        for(WebStruct webStruct : ourHotel.getWebStructs()) {
	        	Image icon = new Image(this.shell.getDisplay(), getClass().getClassLoader().getResourceAsStream(webStruct.getParams().getIconName()));
	        	new Button(cmpWebs, SWT.FLAT).setImage(icon);
	            new Label(cmpWebs, SWT.NONE).setText(webStruct.getParams().getLabel());
	        }

	        KeyListener hotelsListener =  new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent arg0) {
				    bindHotelNames();
					app.saveHotels();
				}
	        };

	        // row 3
	        int nhotels = 0;
	        for(WebStruct webStruct : ourHotel.getWebStructs()) {
	        	if(webStruct.getHotelList().size() > nhotels) {
	        		nhotels = webStruct.getHotelList().size();
	        	}
	        }
	        if(nhotels < MIN_HOTEL_ROWS) {
	        	nhotels = MIN_HOTEL_ROWS;
	        }

	        for(int i=0; i < nhotels + ADDITIONAL_ROWS; i ++) {
	        	for(WebStruct webStruct : ourHotel.getWebStructs()) {
		        	Text text = new Text(cmpWebs, SWT.NONE);
		        	GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, false);
		        	gridData.horizontalSpan = 2;
		        	text.setLayoutData(gridData);
		        	text.addKeyListener(hotelsListener);
		        	if(i < webStruct.getHotelList().size()) {
		        		text.setText(webStruct.getHotelList().get(i));
		        	}
		        	webStruct.getHotelTexts().add(text);
	        	}
	        }
	        scrolled.setSize();
        }
	}
	
	void bindHotelNames() {
        for (OurHotel ourHotel : app.getSettingsModel().getOurHotels()) {
            for(WebStruct webStruct : ourHotel.getWebStructs()) {
                webStruct.getHotelList().clear();
                for(Text hotel : webStruct.getHotelTexts()) {
                    webStruct.getHotelList().add(hotel.getText());
                }
            }
        }	    
	}
	
	void bindEnabledWebs() {
	    for(int iWeb = 0; iWeb < app.getOurHotel().getWebStructs().size(); iWeb ++) {
	        for (OurHotel ourHotel : app.getSettingsModel().getOurHotels()) {
	            boolean enabled = enabledWebs.get(iWeb).getSelection();
	            ourHotel.getWebStructs().get(iWeb).getParams().setEnabled(enabled);
	        }	        
        }
	}
	
	void bindLengthsOfStay() {
		List<Integer> lengthsOfStayNumbers = new ArrayList<Integer>();
		int lengthOfStayNumber = 1;
		for(Button los : this.lengthsOfStay) {
			if(los.getSelection()) {
				lengthsOfStayNumbers.add(lengthOfStayNumber);
			}
			lengthOfStayNumber ++;
		}
		app.setLengthsOfStay(lengthsOfStayNumbers);
		logger.info("setLengthOfStay " + lengthsOfStayNumbers);
	}

    /** show the progress
     * @param finishedCount */
    void progress() {
		this.shell.getDisplay().syncExec(new Runnable() {
			@Override
			@SuppressWarnings("synthetic-access")
			public void run() {
				AppFrame.this.progressBar.setSelection(progressBar.getMaximum() - app.workUnitsManager.unfinishedCount());
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(new Date().getTime() - AppFrame.this.start.getTime()));
				final Formatter formatter = new Formatter(new StringBuilder(), Locale.getDefault());
				AppFrame.this.txtDuration.setText(formatter.format("%1$tM:%1$tS", cal).toString());
			}
		});
    }

    private class Authorizer implements Runnable {
    	// is the usage authorized?
    	boolean authorized = false;

		@Override
		public void run() {
			authorized = true;
	    	for(OurHotel ourHotel : app.getSettingsModel().getOurHotels()) {
	    		for(WebStruct webStruct : ourHotel.getWebStructs()) {
	    			if(webStruct.getHotelTexts().size() > 0) {
	    				String hotel = DiacriticsRemover.removeDiacritics(webStruct.getHotelTexts().get(0).getText()).toUpperCase();
	    				if(! hotel.contains(ourHotel.getOurHotelName().toUpperCase())) {
	    					String msg = MessageFormat.format(app.getBundleString("Neopravneny pristup"), webStruct.getParams().getLabel(), ourHotel.getOurHotelName());
	    					displayException(msg, new IllegalStateException());
	    					authorized = false;
	    					return;
	    				}
	    			}
	    		}
	    	}
		}
    }

    @SuppressWarnings("synthetic-access")
	private boolean authorize() {
    	Authorizer authorizer = new Authorizer();
    	this.shell.getDisplay().syncExec(authorizer);
    	return authorizer.authorized;
    }

	void displayException(final String msg, final Exception e) {
		logger.info(e);
		displayMessage(msg + "\n" + e.toString(), app.getBundleString("Interni chyba"));
	}

	public void runPrepare() {
		this.shellDisposed = false;
        this.start = new Date();
        App.stop = false;
		this.shell.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				AppFrame.this.progressBar.setMaximum(AppFrame.this.app.workUnitsManager.unfinishedCount() + 1);
				AppFrame.this.progressBar.setSelection(1);
			}
		});
	}

	public void runProgress() {
        Runnable runnableProgress = new Runnable() {
        	@Override
			public void run() {
        		if(! AppFrame.this.progressBar.isDisposed()) {
        			progress();
        			if(showDuration) {
        				AppFrame.this.shell.getDisplay().timerExec(1, this);
        			}
        		}
        	}
		};
		this.shell.getDisplay().asyncExec(runnableProgress);
	}

    private void onBtnRun() throws InterruptedException {
    	try {
            this.shell.getDisplay().syncExec(new Runnable() {
                @Override
                public void run() {                
            	    bindEnabledWebs();
            	    bindHotelNames();
            	    bindLengthsOfStay();
                }
            });
            if(app.getOurHotel().getEnabledWebStructs().size() == 0) {
                displayMessage(app.getBundleString("Aspon1web"), app.getBundleString("Aspon1web.title"));
                return;
            }
	        if(App.stop) {
	        	return;
	        }
	        showDuration = true;
	        final Calendar startCalendar = Calendar.getInstance();
	        this.shell.getDisplay().syncExec(new Runnable() {
	        	@Override
				public void run() {
	    	        startCalendar.set(dateTime.getYear(), dateTime.getMonth(), dateTime.getDay());
	    	        app.startDate = startCalendar.getTime();
	    	        app.dayCount = spinnerDays.getSelection();
	    	        app.setUseCache(useCache.getSelection());
	        	}
	        });
	        this.app.workBody();
	    	// dispose shell
			this.shell.getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
			        for(int trial = 0; trial < 3; trial ++) {
			    		try {
			    			ExportExcel exportExcel = new ExportExcel(app.getOurHotel(), app);
			    			exportExcel.createXls();
			    			break;
			    		} catch(Exception e) {
			    			e.printStackTrace();
			    			displayException(app.getBundleString("Doslo k chybe Excel"), e);
			    		}
			        }
			        showDuration = false;
					/*if(retval) {
						AppFrame.this.shell.dispose();
						AppFrame.this.shellDisposed = true;
					}*/
				}
			});
		} finally {
			if(! this.shellDisposed) {
				this.shell.getDisplay().syncExec(new Runnable() {
					@Override
					@SuppressWarnings("synthetic-access")
					public void run() {
						if(! AppFrame.this.btnRun.isDisposed()) {
							AppFrame.this.btnRun.setEnabled(true);
						}
					}
				});
			}
		}
	}

	private void displayMessage(final String msg, final String title) {
		this.shell.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				MessageBox messageBox = new MessageBox(AppFrame.this.shell, SWT.OK);
				messageBox.setText(title);
				messageBox.setMessage(msg);
				messageBox.open();
			}
		});
	}

	void showLog(final String row) {
		if(! this.shell.isDisposed()) {
			this.shell.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					txtLog.append(row + "\n");
				}
			});
		}
	}
}

