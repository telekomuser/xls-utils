package com.intech.cms.utils.sxssfwriter;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.intech.cms.utils.sxssfwriter.dom.AbstractTemplateNode;
import com.intech.cms.utils.sxssfwriter.dom.WritableNode;

/**
 * @author epotanin
 */
public class SXSSFWriter {

	public void write(WritableNode templateRoot, XSSFWorkbook source, Map<String, Object> vars, OutputStream out)
			throws Exception {

		removeAllSheets(source);
		
		//Use existing workbook as a template and re-use global objects such as cell styles, formats, images, etc (scenario 3).
		//@see http://poi.apache.org/apidocs/org/apache/poi/xssf/streaming/SXSSFWorkbook.html#SXSSFWorkbook%28org.apache.poi.xssf.usermodel.XSSFWorkbook%29
		// keep 50 rows in memory, exceeding rows will be flushed to disk
		SXSSFWorkbook wb = new SXSSFWorkbook(source, 50); 

		// styles
		// addStyles(parsedStyles, wb);

		JexlContext context = new MapContext(vars);
		
		Map<String, Object> params= new HashMap<String, Object>();
		params.put(AbstractTemplateNode.WORKBOOK, wb);
		params.put(AbstractTemplateNode.JEXL_CONTEXT, context);
		params.put(AbstractTemplateNode.JEXL_ENGINE, new JexlEngine());
		
		templateRoot.write(params);
		
		wb.write(out);
		wb.dispose();// dispose of temporary files backing this workbook on disk
	}

	private void removeAllSheets(Workbook book) {
		boolean exception = false;
		while (!exception) {
			try {
				book.removeSheetAt(0);
			}
			catch (Exception e) {
				exception = true;
			}
		}
	}

	@SuppressWarnings("unused")
	private void addStyles(List<CellStyle> stylesToAdd, Workbook dest) {
		for (CellStyle style : stylesToAdd) {
			CellStyle newcs = dest.createCellStyle();
			newcs.cloneStyleFrom(style);
			System.out.println(Utils.cellStyleToString(style));
		}
		System.out.println("Added styles: " + stylesToAdd.size());
	}
}
