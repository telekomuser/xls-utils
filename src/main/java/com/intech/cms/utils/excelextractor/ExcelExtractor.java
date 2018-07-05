package com.intech.cms.utils.excelextractor;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Stream;

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
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRelation;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;


public class ExcelExtractor {

	private static final Logger log	= LoggerFactory.getLogger(ExcelExtractor.class);
	
	private boolean withHeader = true;
	private boolean returnEmptyCells = false;
	private boolean returnEmptyRows = false;
	private String dateFormat = "dd/MM/yyyy HH:mm:ss";
	private DateTimeFormatter dformatter = DateTimeFormatter.ofPattern(dateFormat);

    public ExcelExtractor() {
    	
    }
    
    public ExcelExtractor(boolean withHeader, boolean returnEmptyCells, boolean returnEmptyRows) {
		super();
		this.withHeader = withHeader;
		this.returnEmptyCells = returnEmptyCells;
		this.returnEmptyRows = returnEmptyRows;
	}

	public void setWithHeader(boolean withHeader) {
        this.withHeader = withHeader;
    }
    
	public void setReturnEmptyCells(boolean returnEmptyCells) {
		this.returnEmptyCells = returnEmptyCells;
	}

	public void setReturnEmptyRows(boolean returnEmptyRows) {
		this.returnEmptyRows = returnEmptyRows;
	}
	
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
		this.dformatter = DateTimeFormatter.ofPattern(dateFormat);
	}

	public Stream<String[]> iterateRows(MultipartFile mp, int lineCountLimit) throws Exception {

        Stream.Builder<String[]> builder = Stream.<String[]> builder();

        try (InputStream is = mp.getInputStream()) {
            InputStream pbis = new BufferedInputStream(is);//stream must support mark

            if (FileMagic.valueOf(pbis) == FileMagic.OLE2) {// XLS
                parseXLS(pbis, builder, lineCountLimit);
            }
            else if (FileMagic.valueOf(pbis) == FileMagic.OOXML) {// XLSX
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

        for (int j = 0; j <= sheet.getLastRowNum(); j++) {
        	
        	if (withHeader && j == 0) {
        		continue;// skip first row, as it contains column names
        	}

			List<String> strings = new ArrayList<String>();
			HSSFRow row = sheet.getRow(j);
			if (row != null) {
				for (int k = 0; k < row.getLastCellNum(); k++) {
					HSSFCell cell = row.getCell(k);

					if (cell == null) {
						if (returnEmptyCells) {
							strings.add("");
						}
					} else {
						switch (cell.getCellTypeEnum()) {
						case BLANK:
							if (returnEmptyCells) {
								strings.add("");
							}
							break;
						case STRING:
							strings.add(cell.getRichStringCellValue().getString());
							break;
						case NUMERIC:
							double nVal = cell.getNumericCellValue();
							if (DateUtil.isCellDateFormatted(cell)) {//dates
								Calendar cln = DateUtil.getJavaCalendar(nVal);
								LocalDateTime ldt = LocalDateTime.ofInstant(cln.toInstant(),ZoneId.systemDefault());
								strings.add(ldt.format(dformatter));
							}
							else {//numbers
								HSSFCellStyle style = cell.getCellStyle();
								short df = style.getDataFormat();
								String dfs = style.getDataFormatString();
								strings.add(_formatter.formatRawCellContents(nVal, df, dfs));
							}
							break;
						default:
							throw new RuntimeException("Unexpected cell type (" + cell.getCellTypeEnum() + ")");
						}
					}
				}
			}
            
            if (returnEmptyRows || !strings.isEmpty()) {//do not return empty rows
                if (j>lineCountLimit) {
                    throw new LineCountLimitExceededException(String.format("Файл не должен содержать более %d строк", lineCountLimit));
                }
                strings.add(0,Integer.toString(j+1));// prepend row number (numbers from 1)
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

        int lrn = sheet.getLastRowNum();
        for (int j = 0; j<=lrn;j++) {
        
            List<String> strings = new ArrayList<String>();
            Row row = sheet.getRow(j);            
            
            if (withHeader && j == 0) {
                continue;// skip first row, as it contains column names
            }
            
			if (row != null) {
				short lcn = row.getLastCellNum();
				if (lcn != -1) {
					for (short colIx = 0; colIx < lcn; colIx++) {
						Cell cell = row.getCell(colIx, MissingCellPolicy.RETURN_BLANK_AS_NULL);
						if (cell == null) {
							if (returnEmptyCells) {
								strings.add("");
							}
							continue;
						}

						switch (cell.getCellTypeEnum()) {
						case STRING:
							strings.add(cell.getRichStringCellValue().getString());
							break;
						case BLANK:
							if (returnEmptyCells) {
								strings.add("");
							}
							break;
						case NUMERIC: {
							if (DateUtil.isCellDateFormatted(cell)) {//dates
								Calendar cln = DateUtil.getJavaCalendar(cell.getNumericCellValue());
								LocalDateTime ldt = LocalDateTime.ofInstant(cln.toInstant(),ZoneId.systemDefault());
								strings.add(ldt.format(dformatter));
							}
							else {//numbers
								CellStyle cs = cell.getCellStyle();
								
								if (cs != null && cs.getDataFormatString() != null) {
									String contents = formatter.formatRawCellContents(cell.getNumericCellValue(),
											cs.getDataFormat(), cs.getDataFormatString());
									strings.add(contents);
								}
							}
						}
							break;
						// No supported styling applies to this cell
						default:
							String contents = ((XSSFCell) cell).getRawValue();
							if (contents != null) {
								strings.add(contents);
							}
						}
					}
				}
			}

            if ( returnEmptyRows || !strings.isEmpty()) {//do not return empty rows
                if (j>lineCountLimit) {
                    throw new LineCountLimitExceededException(String.format("Файл не должен содержать более %d строк", lineCountLimit));
                }
                strings.add(0,Integer.toString(j+1));// prepend row number (numbers from 1)
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
