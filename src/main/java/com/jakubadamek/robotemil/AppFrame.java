package com.jakubadamek.robotemil;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

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


public class AppFrame
{
	private static final int MIN_HOTEL_ROWS = 15;
	private static final int ADDITIONAL_ROWS = 10;
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
	Button useCache;
	App app;
	private Date start;
	private boolean shellDisposed;
	boolean showDuration;
	Text txtCacheLength;
	private Button btnCacheLength;

    public AppFrame(App app) {
		this.app = app;
	}

	void runGui() {
        Display display = new Display ();
        this.shell = new Shell (display);
        this.shell.setText(title());
        this.shell.setImage(new Image(display, this.getClass().getClassLoader().getResourceAsStream("images.jpg")));
		initWidgets(this.shell);
        display.timerExec(500, new Runnable() {
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

    private String title() {
        switch(app.getCustomer()) {
        case JALTA:
            return "Robot Emil";
        default:
            return "Trick Benchmark";
        }
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

        // new row
        Composite row5 = new Composite(cmp, SWT.NONE);
        GridLayout row5Layout = new GridLayout();
        row5Layout.numColumns = 4;
        row5.setLayout(row5Layout);
        Label cacheLengthLabel = new Label(row5, SWT.NONE);
        cacheLengthLabel.setText(app.getBundleString("Vytvaret cache"));
        cacheLengthLabel.setFont(biggerFont);
        this.txtCacheLength = new Text(row5, SWT.BORDER);
        this.shell.getDisplay().asyncExec(new Runnable() {
        	public void run() {
        		txtCacheLength.setText("" + App.getCacheLength());
        	}
        });
		this.txtCacheLength.setFont(biggerFont);
        Label cacheLengthLabel2 = new Label(row5, SWT.NONE);
        cacheLengthLabel2.setText(app.getBundleString("Vytvaret cache 2"));
        cacheLengthLabel2.setFont(biggerFont);
        this.btnCacheLength = new Button(row5, SWT.BORDER);
        this.btnCacheLength.setText(app.getBundleString("Zmenit"));
        this.btnCacheLength.setFont(biggerFont);

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

        this.btnCacheLength.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				boolean ok = false;
				try {
					final Long cacheLength = Long.valueOf(txtCacheLength.getText());
					if(cacheLength >= 0 && cacheLength < 100) {
						Runnable runnable = new Runnable() {
							public void run() {
								app.setCacheLength(cacheLength.intValue());
							}
						};
						new Thread(runnable).start();
						ok = true;
					}
				} catch (Exception e) {
					// do nothing
				}
				if(! ok) {
					txtCacheLength.setText("" + App.getCacheLength());
				}
			}
        });
	}

	private void initTabFolder(Composite cmp) {
		if(app.ourHotels.size() > 1) {
			this.tabFolder = new TabFolder(cmp, SWT.NONE);
			GridData tabFolderData = new GridData(SWT.FILL, SWT.TOP, true, false);
			tabFolderData.heightHint = 250;
	        this.tabFolder.setLayoutData(tabFolderData);
	        this.tabFolder.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent arg0) {
					app.ourHotelIndex = AppFrame.this.tabFolder.getSelectionIndex();
				}
				public void widgetSelected(SelectionEvent arg0) {
					widgetDefaultSelected(arg0);
				}
	        });
	        this.tabFolder.setFont(biggerFont);
		}
        for(OurHotel ourHotel : app.ourHotels) {
        	ScrolledCompositeWrapper scrolled;
        	if(app.ourHotels.size() > 1) {
	        	TabItem tabItem = new TabItem(this.tabFolder, SWT.NONE);
	        	tabItem.setText(ourHotel.ourHotelName);
	        	scrolled = new ScrolledCompositeWrapper(this.tabFolder);
		        tabItem.setControl(scrolled.getParent());
        	} else {
        		scrolled = new ScrolledCompositeWrapper(cmp);
        	}
    		Composite cmpWebs = scrolled.getComposite();
	        GridLayout glWebs = new GridLayout();
	        glWebs.numColumns = ourHotel.webStructs.size() * 2;
	        //glWebs.marginWidth = glWebs.marginHeight = 0;
	        cmpWebs.setLayout(glWebs);
	        GridData gdWebs = new GridData(SWT.LEFT, SWT.TOP, false, false);
	        gdWebs.heightHint = 300;
	        scrolled.getParent().setLayoutData(gdWebs);

	        // row 2
	        for(WebStruct webStruct : ourHotel.webStructs) {
	        	Image icon = new Image(this.shell.getDisplay(), getClass().getClassLoader().getResourceAsStream(webStruct.iconName));
	        	new Button(cmpWebs, SWT.FLAT).setImage(icon);
	            new Label(cmpWebs, SWT.NONE).setText(webStruct.label);
	        }

	        KeyListener hotelsListener =  new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent arg0) {
					app.saveHotels();
				}
	        };

	        // row 3
	        int nhotels = 0;
	        for(WebStruct webStruct : ourHotel.webStructs) {
	        	if(webStruct.hotelList.size() > nhotels) {
	        		nhotels = webStruct.hotelList.size();
	        	}
	        }
	        if(nhotels < MIN_HOTEL_ROWS) {
	        	nhotels = MIN_HOTEL_ROWS;
	        }

	        for(int i=0; i < nhotels + ADDITIONAL_ROWS; i ++) {
	        	for(WebStruct webStruct : ourHotel.webStructs) {
		        	Text text = new Text(cmpWebs, SWT.NONE);
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
	        scrolled.setSize();
        }
	}

    /** show the progress
     * @param finishedCount */
    void progress() {
		this.shell.getDisplay().syncExec(new Runnable() {
			@SuppressWarnings("synthetic-access")
			public void run() {
				AppFrame.this.progressBar.setSelection(progressBar.getMaximum() - app.workQueue.size());
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

		public void run() {
			authorized = true;
	    	for(OurHotel ourHotel : app.ourHotels) {
	    		for(WebStruct webStruct : ourHotel.webStructs) {
	    			if(webStruct.hotelTexts.size() > 0) {
	    				String hotel = DiacriticsRemover.removeDiacritics(webStruct.hotelTexts.get(0).getText()).toUpperCase();
	    				if(! hotel.contains(ourHotel.ourHotelName.toUpperCase())) {
	    					String msg = MessageFormat.format(app.getBundleString("Neopravneny pristup"), webStruct.label, ourHotel.ourHotelName);
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
		System.out.println(e);
		displayMessage(msg + "\n" + e.toString(), app.getBundleString("Interni chyba"));
	}

	public void runPrepare() {
		this.shellDisposed = false;
        this.start = new Date();
        App.stop = false;
		this.shell.getDisplay().syncExec(new Runnable() {
			public void run() {
				AppFrame.this.progressBar.setMaximum(AppFrame.this.app.workQueue.size() + 1);
				AppFrame.this.progressBar.setSelection(1);
			}
		});
	}

	public void runProgress() {
        Runnable runnableProgress = new Runnable() {
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

    private void onBtnRun() {
    	try {
	        if(App.stop) {
	        	return;
	        }
	        showDuration = true;
	        final Calendar startCalendar = Calendar.getInstance();
	        this.shell.getDisplay().syncExec(new Runnable() {
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
				public void run() {
			        while(true) {
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
					@SuppressWarnings("synthetic-access")
					public void run() {
						AppFrame.this.btnRun.setEnabled(true);
					}
				});
			}
		}
	}

	private void displayMessage(final String msg, final String title) {
		this.shell.getDisplay().syncExec(new Runnable() {
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
				public void run() {
					txtLog.append(row + "\n");
				}
			});
		}
	}
}

