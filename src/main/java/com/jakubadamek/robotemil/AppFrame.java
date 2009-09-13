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
	/** shell */
	public Shell shell;
	/** progress bar */
	ProgressBar progressBar;
	private Font searchFont;
    /** text box showing log */
	public Text txtLog;
	/** tab folder (our hotels) */
	TabFolder tabFolder;
	private Font biggerFont;
	private DateTime dateTime;
	private Spinner spinnerDays;
	private Text txtDuration;
	private Button btnRun;
	private App app;
	private Date start;
	private boolean shellDisposed;
	private int progress;

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

    private void initWidgets(Shell shell) {
    	this.shell = shell;
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
	}

	private void initTabFolder(Composite cmp) {
		if(app.ourHotels.size() > 1) {
			this.tabFolder = new TabFolder(cmp, SWT.NONE);
	        this.tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
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
        	Composite cmpWebs;
        	if(app.ourHotels.size() > 1) {
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
					app.saveHotels();
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

    /** show the progress
     * @param finishedCount */
    void progress(final int finishedCount) {
		this.shell.getDisplay().syncExec(new Runnable() {
			@SuppressWarnings("synthetic-access")
			public void run() {
				AppFrame.this.progressBar.setSelection(finishedCount + 1);
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
        this.progress = 0;
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
        			progress(AppFrame.this.progress);
        			AppFrame.this.shell.getDisplay().timerExec(1, this);
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
	        final Calendar start = Calendar.getInstance();
	        this.shell.getDisplay().syncExec(new Runnable() {
	        	public void run() {
	    	        start.set(dateTime.getYear(), dateTime.getMonth(), dateTime.getDay());
	    	        app.startDate = start.getTime();
	    	        app.dayCount = spinnerDays.getSelection();
	        	}
	        });
	        this.app.workBody();
	    	// dispose shell
			this.shell.getDisplay().syncExec(new Runnable() {
				@SuppressWarnings("synthetic-access")
				public void run() {
					boolean retval = false;
			        while(true) {
			    		try {
			    			ExportExcel exportExcel = new ExportExcel(app.getOurHotel(), app);
			    			retval = exportExcel.createXls();
			    			break;
			    		} catch(Exception e) {
			    			e.printStackTrace();
			    			displayException(app.getBundleString("Doslo k chybe Excel"), e);
			    		}
			        }
					if(retval) {
						AppFrame.this.shell.dispose();
						AppFrame.this.shellDisposed = true;
					}
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
					txtLog.setText(row);
				}
			});
		}
	}
}

