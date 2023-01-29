package com.etransportation.schedulingtasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.ResourceUtils;

import com.etransportation.model.Book;

public class ExcelExporter {

    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<Book> list;

    public ExcelExporter(List<Book> list) {
        this.list = list;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine() {
        sheet = workbook.createSheet("Books");

        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(16);
        style.setFont(font);

        createCell(row, 0, "id", style);
        createCell(row, 1, "book date", style);
        createCell(row, 2, "end date", style);
        createCell(row, 3, "price", style);
        createCell(row, 4, "status", style);
        createCell(row, 5, "car id", style);

    }

    private void createCell(Row row, int columnCount, Object value, CellStyle style) {
        sheet.autoSizeColumn(columnCount);
        Cell cell = row.createCell(columnCount);
        if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof Date) {
            cell.setCellValue((Date) value);
        } else if (value instanceof Long) {
            cell.setCellValue((Long) value);
        } else {
            cell.setCellValue(value.toString());
        }
        cell.setCellStyle(style);
    }

    private void writeDataLines() {
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(14);
        style.setFont(font);

        for (Book book : list) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row, columnCount++, book.getId(), style);
            createCell(row, columnCount++, book.getBookDate(), style);
            createCell(row, columnCount++, book.getEndDate(), style);
            createCell(row, columnCount++, book.getPrice(), style);
            createCell(row, columnCount++, book.getStatus(), style);
            createCell(row, columnCount++, book.getCar().getId(), style);

        }
    }

    public void export() throws IOException {

        writeHeaderLine();
        writeDataLines();

        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateTime = dateFormatter.format(new Date());

        String pathname = "books_" + currentDateTime + ".xlsx";

        File file = new File("reports/" + pathname);

        if (!file.exists()) {
            file.createNewFile();
        }
        OutputStream outputStream = new FileOutputStream(file);
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();

    }
}
