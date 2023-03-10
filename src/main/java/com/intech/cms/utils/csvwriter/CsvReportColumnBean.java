package com.intech.cms.utils.csvwriter;

import org.supercsv.cellprocessor.ift.CellProcessor;

/**
 * Бин для настройки списка колонок при экспорте в CSV 
 * cellProcessor - форматирование и проверка данных библиотеки Super CSV
 * @see http://super-csv.github.io/super-csv/cell_processors.html
 * 
 * @author arakushin
 *
 */
public class CsvReportColumnBean {

	private String fieldName;	
	private String caption;	
	private CellProcessor cellProcessor;

	public CsvReportColumnBean() {
	}

	public CsvReportColumnBean(String fieldName, String caption, CellProcessor cellProcessor) {
		super();
		this.fieldName = fieldName;
		this.caption = caption;
		this.cellProcessor = cellProcessor;
	}
	
	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public CellProcessor getCellProcessor() {
		return cellProcessor;
	}

	public void setCellProcessor(CellProcessor cellProcessor) {
		this.cellProcessor = cellProcessor;
	}

	@Override
	public String toString() {
		return "CsvReportColumnBean [fieldName=" + fieldName + ", caption=" + caption + ", cellProcessor="
				+ cellProcessor + "]";
	}
	
}
