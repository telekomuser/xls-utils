package com.intech.cms.utils.csvwriter;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.view.AbstractView;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.io.ICsvWriter;
import org.supercsv.prefs.CsvPreference;

/**
 * Spring View для создания CSV файлов с помощью библиотеки Super CSV
 * @see http://super-csv.github.io/super-csv/index.html
 * 
 * @author arakushin
 *
 */
public class CsvReportView extends AbstractView {

	@SuppressWarnings("unused")
	private static final Logger	log			= LoggerFactory.getLogger(CsvReportView.class);
	
	private String			charset			= "windows-1251";
	private String			filename		= "report.csv";
	private Character		delimiterChar;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		String[] headers = null;
		String[] fields = null;
		CellProcessor[] processors = null;
		List<CsvReportColumnBean> columns = (List<CsvReportColumnBean>) model.get("columns");
		if (columns == null) {
			headers = (String[]) model.get("headers");
			fields = (String[]) model.get("fields");
			processors = (CellProcessor[]) model.get("processors");
		}
		else {
			headers = new String[columns.size()];
			fields = new String[columns.size()];
			processors = new CellProcessor[columns.size()];
			for (int i = 0; i < columns.size(); i++) {
				headers[i] = columns.get(i).getCaption();
				fields[i] = columns.get(i).getFieldName();
				processors[i] = columns.get(i).getCellProcessor();
			}
		}
		
		List<?> objects = (List<?>) model.get("objects");
		String writerClassName = (String) model.get("writer");
		if (writerClassName == null) {
			writerClassName = "CsvBeanWriter";
		}
		
		response.setCharacterEncoding(charset);
		response.setContentType("text/csv;charset=" + charset);
		response.setHeader("Content-disposition", "attachment; filename*=utf-8''" + filename + ";");

		ICsvWriter writer = null;
        try {
        	
        	CsvPreference	preference	= CsvPreference.STANDARD_PREFERENCE;
        	if (delimiterChar != null) {
        		preference = new CsvPreference.Builder('"', delimiterChar, "\r\n").build();
        	}
        	
        	switch (writerClassName) {
			case "CsvListWriter":
				writer = new CsvListWriter(response.getWriter(), preference);
				break;
			case "CsvMapWriter":
				writer = new CsvMapWriter(response.getWriter(), preference);
				break;
			default: // CsvBeanWriter by default
				writer = new CsvBeanWriter(response.getWriter(), preference);
				break;
			} 
	
			// header columns
			if (headers != null){
				writer.writeHeader(headers);
			} 
			else {
				writer.writeHeader(fields);
			}
			
			// data
			if (processors == null) {
				processors = new CellProcessor[fields.length];
			}			
			for (Object obj : objects) {
	        	switch (writerClassName) {
				case "CsvListWriter":
					((ICsvListWriter) writer).write((List<?>)obj, processors);
					break;
				case "CsvMapWriter":
					((ICsvMapWriter) writer).write((Map<String, ?>) obj, fields, processors);
					break;
				default: // CsvBeanWriter by default
					((ICsvBeanWriter) writer).write(obj, fields, processors);
					break;
				} 
			}
	
        }
        finally {
	    	if( writer != null ) {
	    		writer.close();
	    	}
	    }        
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Character getDelimiterChar() {
		return delimiterChar;
	}

	public void setDelimiterChar(Character delimiterChar) {
		this.delimiterChar = delimiterChar;
	}

}
