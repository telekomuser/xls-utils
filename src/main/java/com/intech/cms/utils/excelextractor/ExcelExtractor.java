package com.intech.cms.utils.excelextractor;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationshipCollection;
import org.apache.poi.poifs.filesystem.DocumentFactoryHelper;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRelation;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

public class ExcelExtractor {

    private boolean withHeader = true;

    public void setWithHeader(boolean withHeader) {
        this.withHeader = withHeader;
    }

    private static final Logger log	= LoggerFactory.getLogger(ExcelExtractor.class);

    public Stream<String[]> iterateRows(MultipartFile mp, int lineCountLimit) throws Exception {

        Stream.Builder<String[]> builder = Stream.<String[]> builder();

        try (InputStream is = mp.getInputStream()) {
            PushbackInputStream pbis = new PushbackInputStream(is, 8);//!!!

            if (NPOIFSFileSystem.hasPOIFSHeader(pbis)) {// XLS
                parseXLS(pbis, builder, lineCountLimit);
            }
            else if (DocumentFactoryHelper.hasOOXMLHeader(pbis)) {// XLSX
                parseXLSX(pbis, builder, lineCountLimit);
            }
            else {
                throw new IllegalArgumentException("Your InputStream was neither an OLE2 stream, nor an OOXML stream");
            }
            return builder.build();
        }
        // java.lang.IllegalArgumentException: Your InputStream was neither an OLE2 stream, nor an OOXML stream
        catch (IllegalArgumentException e1) {
            log.error("", e1);
            throw new NotSupportedFileException("Неверный формат файла. Выберите файл формата xls или xlsx");
        }
        catch (Exception e2) {
            log.error("", e2);
            throw e2;
        }
    }

    /**
     * XLS extractor
     *
     * @param is
     * @param builder
     * @param lineCountLimit
     * @throws IOException
     * @throws LineCountLimitExceededException
     * @see org.apache.poi.hssf.extractor.ExcelExtractor
     */
    @SuppressWarnings("resource")
    private void parseXLS(InputStream is, Stream.Builder<String[]> builder, int lineCountLimit) throws IOException, LineCountLimitExceededException {

        String[] sa = new String[] {};
        HSSFDataFormatter _formatter = new HSSFDataFormatter();
        HSSFWorkbook wb = new HSSFWorkbook(new NPOIFSFileSystem(is).getRoot(), true);
        HSSFSheet sheet = wb.getSheetAt(0);// only 1st sheet

        int rn = 0 ;
        for (int j = 0; j <= sheet.getLastRowNum(); j++) {
            HSSFRow row = sheet.getRow(j);
            if (row == null) {
                continue;
            }

            if (withHeader && row.getRowNum() == 0) {
                continue;// skip first row, as it contains column names
            }

            List<String> strings = new ArrayList<String>();
            for (int k = 0; k < row.getLastCellNum(); k++) {
                HSSFCell cell = row.getCell(k);

                if (cell == null) {
                    //strings.add("");//do not return empty cells
                }
                else {
                    switch (cell.getCellTypeEnum()) {
                        case BLANK:
                            // strings.add(""); //do not return empty cells
                            break;
                        case STRING:
                            strings.add(cell.getRichStringCellValue().getString());
                            break;
                        case NUMERIC:
                            HSSFCellStyle style = cell.getCellStyle();
                            double nVal = cell.getNumericCellValue();
                            short df = style.getDataFormat();
                            String dfs = style.getDataFormatString();
                            strings.add(_formatter.formatRawCellContents(nVal, df, dfs));
                            break;
                        default:
                            throw new RuntimeException("Unexpected cell type (" + cell.getCellTypeEnum() + ")");
                    }
                }
            }
            if (!strings.isEmpty()) {//do not return empty rows
                if (++rn>lineCountLimit) {
                    throw new LineCountLimitExceededException(String.format("Файл не должен содержать более %d строк", lineCountLimit));
                }
                strings.add(0,Integer.toString(rn));// prepend row number (numbers from 1)
                builder.add(strings.toArray(sa));
            }
        }
    }

    /**
     * XLSX extractor
     *
     * @param is
     * @param builder
     * @param lineCountLimit
     * @throws Exception
     * @see org.apache.poi.xssf.extractor.XSSFExcelExtractor
     */
    @SuppressWarnings("resource")
    private void parseXLSX(InputStream is, Stream.Builder<String[]> builder, int lineCountLimit) throws Exception {

        String[] sa = new String[] {};
        OPCPackage pkg = OPCPackage.open(is);
        PackageRelationshipCollection core = pkg.getRelationshipsByType(ExtractorFactory.CORE_DOCUMENT_REL);

        if (core.size() != 1) {
            throw new IllegalArgumentException("Invalid OOXML Package received - expected 1 core document, found " + core.size());
        }

        PackagePart corePart = pkg.getPart(core.getRelationship(0));
        if (!corePart.getContentType()
                .equals(XSSFRelation.WORKBOOK.getContentType())) {
            throw new IllegalArgumentException("No supported documents found in the OOXML package");
        }

        DataFormatter formatter = new DataFormatter();
        XSSFWorkbook wb = new XSSFWorkbook(pkg);
        Sheet sh = wb.sheetIterator().next();// only 1st sheet
        XSSFSheet sheet = (XSSFSheet) sh;

        int j = 0;
        for (Object rawR : sheet) {

            List<String> strings = new ArrayList<String>();
            Row row = (Row) rawR;
            if (withHeader && row.getRowNum() == 0) {
                continue;// skip first row, as it contains column names
            }
            for (Iterator<Cell> ri = row.cellIterator(); ri.hasNext();) {
                Cell cell = ri.next();

                switch (cell.getCellTypeEnum()) {
                    case STRING:
                        strings.add(cell.getRichStringCellValue()
                                .getString());
                        break;
                    case BLANK:
                        // strings.add(""); //do not return empty cells
                        break;
                    case NUMERIC:{
                        CellStyle cs = cell.getCellStyle();

                        if (cs != null && cs.getDataFormatString() != null) {
                            String contents = formatter.formatRawCellContents(
                                    cell.getNumericCellValue(), cs.getDataFormat(), cs.getDataFormatString());
                            strings.add(contents);
                        }
                    }
                    break;
                    // No supported styling applies to this cell
                    default:
                        String contents = ((XSSFCell)cell).getRawValue();
                        if (contents != null) {
                            strings.add(contents);
                        }
                        //throw new Exception(String.format("Cell is not text (row %d)",j));
                }
            }
            if (!strings.isEmpty()) {//do not return empty rows
                if (++j>lineCountLimit) {
                    throw new LineCountLimitExceededException(String.format("Файл не должен содержать более %d строк", lineCountLimit));
                }
                strings.add(0,Integer.toString(j));// prepend row number (numbers from 1)
                builder.add(strings.toArray(sa));
            }
        }
    }

    @SuppressWarnings("serial")
    public static class LineCountLimitExceededException extends Exception {

        public LineCountLimitExceededException(String msg) {

            super(msg);
        }

    }

    @SuppressWarnings("serial")
    public static class NotSupportedFileException extends Exception {

        public NotSupportedFileException(String msg) {

            super(msg);
        }

    }

}
