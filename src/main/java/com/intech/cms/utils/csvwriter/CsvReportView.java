package com.intech.cms.utils.csvwriter;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
 * @see <a href="http://super-csv.github.io/super-csv/index.html">Super-CSV</a>
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

		boolean noHeaders = Boolean.TRUE.equals(model.get("no_headers"));

		//we need these finals for silly lambda java restriction
		final String[] fields = fieldsFromModel(model);
		final CellProcessor[] processors = cellProcessorsFromModel(model,fields.length) ;

		response.setCharacterEncoding(charset);
		response.setContentType("text/csv;charset=" + charset);
		response.setHeader("Content-disposition", "attachment; filename*=utf-8''" + filename + ";");

        try (ICsvWriter writer = writerFromModel(model,response.getWriter())) {

			if (!noHeaders){ // header columns
				String[] headers = headersFromModel(model);
				writer.writeHeader(headers == null ? fields : headers);
			}

			// data should be Stream or Iterable
			Object objects = model.get("objects");
			if (!(objects instanceof Stream) && !(objects instanceof Iterable)){
				throw new Exception("Only Stream or Iterable data supported");
			}

			try (Stream stream = objects instanceof Stream
                    ? (Stream) objects
                    : StreamSupport.stream(((Iterable)objects).spliterator(),false)) {
                
			    stream.forEach(obj->{
                    try {
                        if (writer instanceof ICsvListWriter) {
                            ((ICsvListWriter) writer).write((List<?>) obj, processors);
                        }
                        else if (writer instanceof ICsvMapWriter) {
                            ((ICsvMapWriter) writer).write((Map<String, ?>) obj, fields, processors);
                        }
                        else{
                            ((ICsvBeanWriter) writer).write(obj, fields, processors);
                        }
                    }
                    catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
	}

	@SuppressWarnings("unchecked")
	private String[] headersFromModel(Map<String, Object> model){
		return model.containsKey("columns")
				? ((List<CsvReportColumnBean>) model.get("columns"))
				    .stream()
					.map(CsvReportColumnBean::getCaption)
					.toArray(String[]::new)
				: (String[]) model.get("headers");
	}

	@SuppressWarnings("unchecked")
	private String[] fieldsFromModel(Map<String, Object> model){
		return model.containsKey("columns")
		    ? ((List<CsvReportColumnBean>) model.get("columns"))
				.stream()
				.map(CsvReportColumnBean::getFieldName)
				.toArray(String[]::new)
			: (String[]) model.get("fields");
	}

	@SuppressWarnings("unchecked")
	private CellProcessor[] cellProcessorsFromModel(Map<String, Object> model, int defaultLength){

		CellProcessor[] pp = model.containsKey("columns")
				? ((List<CsvReportColumnBean>) model.get("columns"))
					.stream()
					.map(CsvReportColumnBean::getCellProcessor)
					.toArray(CellProcessor[]::new)
				: (CellProcessor[]) model.get("processors");
		return pp == null
				? new CellProcessor[defaultLength]
				: pp;
	}

	private ICsvWriter writerFromModel(Map<String, Object> model, Writer output){
		CsvPreference	preference	= CsvPreference.STANDARD_PREFERENCE;
		if (delimiterChar != null) {
			preference = new CsvPreference.Builder('"', delimiterChar, "\r\n").build();
		}

		String writerClassName = (String) model.get("writer");
		ICsvWriter writer;
		if ("CsvListWriter".equals(writerClassName)) {
			writer = new CsvListWriter(output, preference);
		}
		else if ("CsvMapWriter".equals(writerClassName)) {
			writer = new CsvMapWriter(output, preference);
		}
		else {
			writer = new CsvBeanWriter(output, preference);// CsvBeanWriter by default
		}
		return writer;
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
