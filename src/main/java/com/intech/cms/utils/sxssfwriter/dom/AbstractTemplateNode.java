package com.intech.cms.utils.sxssfwriter.dom;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTemplateNode {

	private static final Logger log = LoggerFactory.getLogger(AbstractTemplateNode.class);
	public static final String DESTINATION_SHEET = "DESTINATION_SHEET";
	public static final String JEXL_ENGINE = "JEXL_ENGINE";
	public static final String JEXL_CONTEXT = "JEXL_CONTEXT";
	public static final String NEW_ROW_INDEX = "NEW_ROW_INDEX";
	public static final String WORKBOOK = "NEW_ROW_INDEX";
	private static final String EXPRESSION_PATTERN = "\\$\\{(.*)\\}";

	protected Sheet getDestinationSheet(Map<String, Object> params) {
		return (Sheet) params.get(DESTINATION_SHEET);
	}

	protected JexlContext getJexlContext(Map<String, Object> params) {
		return (JexlContext) params.get(JEXL_CONTEXT);
	}

	protected JexlEngine getJexlEngine(Map<String, Object> params) {
		return (JexlEngine) params.get(JEXL_ENGINE);
	}

	protected AtomicInteger getNewRowIndex(Map<String, Object> params) {
		return (AtomicInteger) params.get(NEW_ROW_INDEX);
	}

	protected SXSSFWorkbook getWorkbook(Map<String, Object> params) {
		return (SXSSFWorkbook) params.get(WORKBOOK);
	}

	protected Object processExpression(String possibleExression, Map<String, Object> params) {
		Object result = possibleExression;

		Matcher m = Pattern.compile(EXPRESSION_PATTERN).matcher(possibleExression);
		if (m.matches()) {
			try {
				Expression expr = getJexlEngine(params).createExpression(m.group(1));
				result = expr.evaluate(getJexlContext(params));
			}
			catch (Exception ex) {
				log.error("unable to process expression '{}' ", possibleExression);
				log.error("", ex);
			}
		}
		return result;
	}
}
