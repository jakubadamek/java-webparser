package com.jakubadamek.robotemil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import jxl.CellFormat;
import jxl.Workbook;
import jxl.biff.EmptyCell;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.NumberFormat;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import com.jakubadamek.robotemil.entities.PriceAndOrder;

public class ExportExcel {
    private final Logger logger = Logger.getLogger(getClass());
    /** days per row in the Excel result */
    private static final int DAYS_PER_ROW = 3;
    private static final int EMPTY_ROWS_EXCEL = 3;

    private int irow = 0;
    private NumberFormat priceFormat = new NumberFormat("#");
    private NumberFormat orderFormat = new NumberFormat("#");
    private WritableFont orderFont = new WritableFont(WritableFont.TAHOMA, 7);
    private WritableFont orderBoldFont = new WritableFont(WritableFont.TAHOMA, 7, WritableFont.BOLD);
    // private WritableFont firstHotelFont = new
    // WritableFont(WritableFont.TAHOMA, 10, WritableFont.BOLD);
    private WritableFont priceFont = new WritableFont(WritableFont.TAHOMA, 10, WritableFont.BOLD);
    private WritableFont redPriceFont = new WritableFont(WritableFont.TAHOMA, 10, WritableFont.BOLD);
    private WritableFont bigFont = new WritableFont(WritableFont.TAHOMA, 12, WritableFont.BOLD);
    private int lastHotel = 0;
    private OurHotel ourHotel;
    private App app;
    private int iSheetNumber = 1;

    /**
     * Constructor
     * 
     * @param ourHotel
     */
    public ExportExcel(OurHotel ourHotel, App app) {
        this.ourHotel = ourHotel;
        this.app = app;
    }

    private WritableCellFormat getCellFormat(Date date, int ihotel, int col, int row, WritableFont font) throws WriteException {
        WritableCellFormat cellFormat = new WritableCellFormat(col % 2 == 1 ? this.orderFormat : this.priceFormat);
        if (date != null && ihotel >= 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
                cellFormat.setBackground(Colour.YELLOW);
            }
        }
        if (ihotel == 0) {
            // cellFormat.setFont(this.firstHotelFont);
            cellFormat.setBorder(Border.TOP, BorderLineStyle.MEDIUM);
            cellFormat.setBorder(Border.BOTTOM, BorderLineStyle.MEDIUM);
        }
        if (ihotel == this.lastHotel) {
            cellFormat.setBorder(Border.BOTTOM, BorderLineStyle.MEDIUM);
        }
        if (col % (webCount() * 2) == 1) {
            cellFormat.setBorder(Border.LEFT, BorderLineStyle.MEDIUM);
        }
        if (col % (webCount() * 2) == 0) {
            cellFormat.setBorder(Border.RIGHT, BorderLineStyle.MEDIUM);
        }
        if (row == 0) {
            cellFormat.setBorder(Border.TOP, BorderLineStyle.MEDIUM);
        }
        if (col == 0) {
            cellFormat.setBorder(Border.LEFT, BorderLineStyle.MEDIUM);
        }
        if (font != null) {
            cellFormat.setFont(font);
        } else if (row >= 2) {
            if (col % 2 == 1) {
                if (row == 2) {
                    cellFormat.setFont(this.orderBoldFont);
                } else {
                    cellFormat.setFont(this.orderFont);
                }
            } else if (col > 1) {
                cellFormat.setFont(this.priceFont);
            }
        }
        if (row >= 2 && col >= 1) {
            cellFormat.setAlignment(Alignment.CENTRE);
        }
        return cellFormat;
    }
    
    /**
     * @return %APPDATA%/robotemil/robotemil_yyyyMMdd_HHmmss.xls
     */
    private String excelFileName() {
        String filename = app.getSettingsModel().getExcelFile();
        File dir = new File("d:\\temp"); //System.getenv("APPDATA"), filename);
        if(! dir.exists()) {
        	dir.mkdirs();
        }
        filename = new File(dir, filename).getPath();
        SimpleDateFormat format = new SimpleDateFormat("_yyyyMMdd_HHmmss");
        filename += format.format(new Date()) + ".xls";
        logger.info(filename);
        return filename;
    }

    boolean createXls() throws IOException, RowsExceededException, WriteException {
        if (!this.redPriceFont.getColour().equals(Colour.RED)) {
            this.redPriceFont.setColour(Colour.RED);
        }
        String filename = excelFileName();
        WritableWorkbook workbook = Workbook.createWorkbook(new File(filename));
        boolean retval = true;
        for(Integer lengthOfStay : app.getLengthsOfStay()) {
        	retval = writeXlsPrices(workbook, lengthOfStay);
        }
        // serialize();
        writeXlsFiltered(workbook);
        writeXlsHotelNames(workbook);
        writeXlsSettings(workbook);
        workbook.write();
        workbook.close();
        Runtime.getRuntime().exec("cmd /c \"" + filename + "\"");
        return retval;
    }

    @SuppressWarnings("boxing")
    private boolean writeXlsPrices(WritableWorkbook workbook, int lengthOfStay) throws RowsExceededException, WriteException {
        WritableSheet sheet = workbook.createSheet("ceny LOS " + lengthOfStay, iSheetNumber ++);
        sheet.setColumnView(0, 40);
        this.irow = 0;
        Calendar cal = Calendar.getInstance();
        // sheet.addCell(new jxl.write.Label(0, this.irow, "Www", new
        // WritableCellFormat(font)));
        int icol = 1;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd. M. EEEE");
        // column widths
        for (icol = 1; icol < 1 + this.app.getDates().size() * ourHotel.getEnabledWebStructs().size() * 2; icol += 2) {
            sheet.setColumnView(icol, 6);
            sheet.setColumnView(icol + 1, 5);
        }
        icol = 1;
        // dates
        for (Date date : this.app.getDates()) {
            cal.setTime(date);
            sheet.addCell(new jxl.write.Label(icol, this.irow, simpleDateFormat.format(cal.getTime()), getCellFormat(date, -1,
                    icol, this.irow, this.bigFont)));
            icol += ourHotel.getEnabledWebStructs().size() * 2;
        }
        this.irow += 2;
        // find last hotel
        for (int ihotel = 0; ihotel < ourHotel.getEnabledWebStructs().get(0).getHotelTexts().size(); ihotel++) {
            for (WebStruct webStruct : ourHotel.getEnabledWebStructs()) {
                if (webStruct.hasHotel(ihotel)) {
                    this.lastHotel = ihotel;
                }
            }
        }
        // hotel names
        for (int ihotel = 0; ihotel <= this.lastHotel; ihotel++) {
            sheet.addCell(new jxl.write.Label(0, this.irow + ihotel, hotelName(ihotel), getCellFormat(null, ihotel, 0,
                    this.irow + ihotel, null)));
        }
        icol = 1;
        // order and price
        for (Date date : this.app.getDates()) {
            for (WebStruct webStruct : ourHotel.getEnabledWebStructs()) {
                sheet.addCell(new jxl.write.Label(icol, this.irow - 1, webStruct.getParams().getExcelName(), getCellFormat(date,
                        -1, icol, this.irow - 1, null)));
                // sheet.mergeCells(icol, this.irow - 1, icol + 1, this.irow -
                // 1);
                double firstHotelPrice = 0;
                for (int ihotel = 0; ihotel <= this.lastHotel; ihotel++) {
                    String hotel = webStruct.hasHotel(ihotel) ? webStruct.getHotelTexts().get(ihotel).getText() : "";
                    boolean noResult = true;
                    if (hotel.trim().length() > 0) {
                        PriceAndOrder priceAndOrder = webStruct.getPrices().findHotel(hotel, new WorkUnitKey(date, lengthOfStay));
                        if (priceAndOrder != null) {
                            sheet.addCell(new jxl.write.Number(icol, this.irow + ihotel, priceAndOrder.order,
                                    getCellFormat(date, ihotel, icol, this.irow + ihotel, null)));
                            sheet.addCell(new jxl.write.Number(icol + 1, this.irow + ihotel, priceAndOrder.price,
                                    getCellFormat(date, ihotel, icol + 1, this.irow + ihotel,
                                            firstHotelPrice > priceAndOrder.price ? this.redPriceFont : null)));
                            if (ihotel == 0) {
                                firstHotelPrice = priceAndOrder.price;
                            }
                            noResult = false;
                        }
                    }
                    if (noResult) {
                        sheet.addCell(new jxl.write.Label(icol, this.irow + ihotel, "", getCellFormat(date, ihotel, icol,
                                this.irow + ihotel, null)));
                        sheet.addCell(new jxl.write.Label(icol + 1, this.irow + ihotel, "X", getCellFormat(date, ihotel,
                                icol + 1, this.irow + ihotel, null)));
                    }
                }
                icol += 2;
            }
        }
        splitDates(sheet);
        return true;
    }
      
    private boolean writeXlsFiltered(WritableWorkbook workbook) throws RowsExceededException, WriteException {
        WritableSheet sheet = workbook.createSheet("filtr", iSheetNumber ++);
        sheet.setColumnView(0, 40);
        this.irow = 0;
        Calendar cal = Calendar.getInstance();
        // sheet.addCell(new jxl.write.Label(0, this.irow, "Www", new
        // WritableCellFormat(font)));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d.M.");
        // column widths
        int icol = 0;
        // hotel name
        sheet.setColumnView(icol ++, 40);
        // LOS
        sheet.setColumnView(icol ++, 6);
        // Web
        sheet.setColumnView(icol ++, 12);
        // Dates
        for(int idate = 0; idate < this.app.getDates().size(); idate ++) {
        	sheet.setColumnView(icol ++, 7);
        }
        // Nadpisy:
        WritableCellFormat cellFormat = new WritableCellFormat();
        cellFormat.setFont(priceFont);
        icol = 0;
        sheet.addCell(new Label(icol ++, this.irow, "Hotel", cellFormat));
        sheet.addCell(new Label(icol ++, this.irow, "LOS", cellFormat));
        sheet.addCell(new Label(icol ++, this.irow, "Web", cellFormat));
        // dates
        for (Date date : this.app.getDates()) {
            cal.setTime(date);
            sheet.addCell(new jxl.write.Label(icol, this.irow, simpleDateFormat.format(cal.getTime()), cellFormat));
            icol ++;
        }
        // find last hotel
        for (int ihotel = 0; ihotel < ourHotel.getEnabledWebStructs().get(0).getHotelTexts().size(); ihotel++) {
            for (WebStruct webStruct : ourHotel.getEnabledWebStructs()) {
                if (webStruct.hasHotel(ihotel)) {
                    this.lastHotel = ihotel;
                }
            }
        }
        WritableCellFormat filterPriceFormat = new WritableCellFormat(this.priceFormat);
        for (WebStruct webStruct : ourHotel.getEnabledWebStructs()) {
        	for(int lengthOfStay : app.getLengthsOfStay()) {
                for (int ihotel = 0; ihotel <= this.lastHotel; ihotel++) {
                	if(webStruct.hasHotel(ihotel)) {
		                String hotel = webStruct.getPrices().findHotelName(webStruct.getHotelList().get(ihotel));
		                if(hotel != null) {
			                this.irow ++;
			                icol = 0;
			                sheet.addCell(new jxl.write.Label(icol ++, this.irow, hotelName(ihotel)));
			                sheet.addCell(new jxl.write.Number(icol ++, this.irow, lengthOfStay)); 
			                sheet.addCell(new jxl.write.Label(icol ++, this.irow, webStruct.getParams().getExcelName()));
			                for (Date date : this.app.getDates()) {
			                    PriceAndOrder priceAndOrder = webStruct.getPrices().findHotel(hotel, new WorkUnitKey(date, lengthOfStay));
			                    if (priceAndOrder != null) {
			                        sheet.addCell(new jxl.write.Number(icol, this.irow, priceAndOrder.price, filterPriceFormat));
			                    }
			                    icol ++;
			                }
		                }
                	}
                }        		
        	}
        }
        return true;
    }
  
	private String hotelName(int ihotel) {
		for (WebStruct webStruct : ourHotel.getEnabledWebStructs()) {
		    if (webStruct.hasHotel(ihotel)) {
		        return webStruct.getHotelTexts().get(ihotel).getText();
		    }
		}
		return "";
	}

    /**
     * Splits the table so that there are only 3 days per row
     * 
     * @param sheet
     * @throws RowsExceededException
     * @throws WriteException
     */
    private void splitDates(WritableSheet sheet) throws RowsExceededException, WriteException {
        int moveWidth = ourHotel.getEnabledWebStructs().size() * 2 * DAYS_PER_ROW;
        WritableCellFormat cellFormat = new WritableCellFormat();
        cellFormat.setBorder(Border.TOP, BorderLineStyle.MEDIUM);
        // top border
        for (int icol = 1; icol < this.app.getDates().size() * 2 * ourHotel.getEnabledWebStructs().size() + 1; icol++) {
            WritableCell cell = sheet.getWritableCell(icol, 0);
            if (cell instanceof EmptyCell) {
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
        int nrows = (this.app.getDates().size() + 2) / DAYS_PER_ROW;
        for (int idateRow = 0; idateRow < nrows; idateRow++) {
            int toRow = idateRow * (this.lastHotel + webCount() + EMPTY_ROWS_EXCEL);
            copyCells(sheet, 0, 0, 0, this.lastHotel + 1 + EMPTY_ROWS_EXCEL, 0, toRow);
            int colStart = idateRow * moveWidth + 1;
            // for last row: decrease moveWidth
            int overhead = (idateRow + 1) * DAYS_PER_ROW - this.app.getDates().size();
            if (overhead > 0) {
                moveWidth -= overhead * ourHotel.getEnabledWebStructs().size() * 2;
            }
            if (idateRow > 0) {
                copyCells(sheet, colStart, 0, colStart + moveWidth - 1, this.lastHotel + EMPTY_ROWS_EXCEL - 1, 1, toRow);
                clearCells(sheet, colStart, 0, colStart + moveWidth - 1, this.lastHotel + EMPTY_ROWS_EXCEL - 1);
                // last border
                for (int row = 0; row < 2; row++) {
                    sheet.addCell(new jxl.write.Label(moveWidth + 1, toRow + row, "", getCellFormat(null, -1, moveWidth + 1,
                            toRow + row, null)));
                }
            }
            // merge date cells
            for (int idate = 0; idate < DAYS_PER_ROW; idate++) {
                // logger.info(idate * DAYS_PER_ROW + 1+", "+ toRow+", "+
                // (idate + 1) * DAYS_PER_ROW+", "+
                // toRow);
                sheet.mergeCells(idate * webCount() * 2 + 1, toRow, (idate + 1) * webCount() * 2, toRow);
            }
        }
        moveWidth = ourHotel.getEnabledWebStructs().size() * 2 * Math.min(this.app.getDates().size(), DAYS_PER_ROW);
        // last border on first row
        for (int row = 0; row < 2; row++) {
            sheet.addCell(new jxl.write.Label(moveWidth + 1, row, "", cellFormat));
        }
    }

    private int webCount() {
        return this.ourHotel.getEnabledWebStructs().size();
    }

    private void copyCells(WritableSheet sheet, int fromCol1, int fromRow1, int fromCol2, int fromRow2, int toCol, int toRow)
            throws RowsExceededException, WriteException {
        for (int col = fromCol1; col <= fromCol2; col++) {
            for (int row = fromRow1; row <= fromRow2; row++) {
                sheet.addCell(sheet.getWritableCell(col, row).copyTo(toCol + col - fromCol1, toRow + row - fromRow1));
            }
        }
    }

    private void clearCells(WritableSheet sheet, int fromCol1, int fromRow1, int fromCol2, int fromRow2)
            throws RowsExceededException, WriteException {
        for (int col = fromCol1; col <= fromCol2; col++) {
            for (int row = fromRow1; row <= fromRow2; row++) {
                sheet.addCell(new jxl.write.Label(col, row, ""));
            }
        }
    }

    private void writeXlsHotelNames(WritableWorkbook workbook) throws RowsExceededException, WriteException {
        WritableSheet sheet = workbook.createSheet("nazvy hotelu", iSheetNumber ++);
        int icol = 0;
        for (WebStruct webStruct : this.ourHotel.getEnabledWebStructs()) {
            sheet.setColumnView(icol, 40);
            sheet.addCell(new jxl.write.Label(icol, 0, webStruct.getParams().getLabel()));
            this.irow = 1;
            for (String hotel : new TreeSet<String>(webStruct.getPrices().getData().keySet())) {
                sheet.addCell(new jxl.write.Label(icol, this.irow, hotel));
                this.irow++;
            }
            icol++;
        }
    }

    private void writeXlsSettings(WritableWorkbook workbook) throws RowsExceededException, WriteException {
        WritableSheet sheet = workbook.createSheet("nastaveni", iSheetNumber ++);
        int icol = 0;
        for (WebStruct webStruct : this.ourHotel.getEnabledWebStructs()) {
            sheet.setColumnView(icol, 40);
            sheet.addCell(new jxl.write.Label(icol, 0, webStruct.getParams().getLabel()));
            this.irow = 1;
            for (String hotel : webStruct.getHotelList()) {
                sheet.addCell(new jxl.write.Label(icol, this.irow, hotel));
                this.irow++;
            }
            icol++;
        }
    }

}
