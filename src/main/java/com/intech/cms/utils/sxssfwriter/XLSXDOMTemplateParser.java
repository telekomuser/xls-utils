package com.intech.cms.utils.sxssfwriter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intech.cms.utils.sxssfwriter.dom.CopyRowNode;
import com.intech.cms.utils.sxssfwriter.dom.NewSheetNode;
import com.intech.cms.utils.sxssfwriter.dom.Node;
import com.intech.cms.utils.sxssfwriter.dom.TagNode;

/**
 * @author epotanin
 */
public class XLSXDOMTemplateParser {

	private static final Logger	log	= LoggerFactory.getLogger(XLSXDOMTemplateParser.class);

	public Node parse(InputStream is) throws Exception {

		Workbook workbook = WorkbookFactory.create(is);
		// fillStyles(stylesToFill, workbook);

		Sheet sheet = workbook.getSheetAt(0);/* всегда берется 1й лист */

		Node root = new NewSheetNode(null);
		Node currentParent = root;
		int maxColumnNumber = 0;

		if (sheet != null) {
			int rowStart = sheet.getFirstRowNum();
			int rowEnd = sheet.getLastRowNum();
			DataFormatter formatter = new DataFormatter(new Locale("ru", "RU"));

			for (int ri = rowStart; ri <= rowEnd; ri++) {
				Row row = sheet.getRow(ri);
				if (row == null) {
					break;
				}

				// Количество колонок определено как максимальный номер столбца из всех строк
				maxColumnNumber = Math.max(maxColumnNumber, row.getLastCellNum());
				String stringValue = getFirstCellStringValue(row, formatter);

				if (stringValue != null) {

					if (TagNodeFactory.isStartTag(stringValue)) {
						TagNode node = TagNodeFactory.createTagNode(stringValue, currentParent);
						currentParent.addChild((Node) node);
						currentParent = (Node) node;
					}
					else if (TagNodeFactory.isEndTag(stringValue)) {
						String closingtag = TagNodeFactory.getEndTagName(stringValue);
						String currenttag = TagNodeFactory.getTagName((TagNode) currentParent);

						if (!closingtag.equals(currenttag)) {
							throw new Exception("Attempt to close " + closingtag + " tag instead of open " + currenttag);
						}
						currentParent = currentParent.getParent();
					}
					else {
						currentParent.addChild(new CopyRowNode(row, currentParent));
					}
				}
				else {
					// при пустом ряде или любой ошибке - просто копируем этот ряд
					currentParent.addChild(new CopyRowNode(row, currentParent));
				}
			}

			if (currentParent.getParent() != null) {
				TagNode tn = (TagNode) currentParent;
				String name = TagNodeFactory.getTagName(tn);
				throw new Exception("Cannot find closing tag for: " + name);
			}

			// ширины каждой колонки.
			log.debug("Retrieving widths for {} columns", maxColumnNumber);
			List<Integer> columnWidths = new ArrayList<Integer>();
			for (int i = 0; i < maxColumnNumber; i++) {
				columnWidths.add(sheet.getColumnWidth(i));
			}
			((NewSheetNode) root).setColumnWidths(columnWidths);
		}
		return root;
	}

	/**
	 * @param row
	 * @param formatter
	 * @return null on any error
	 */
	private String getFirstCellStringValue(Row row, DataFormatter formatter) {

		String stringValue = null;
		try {
			Cell cell = row.getCell(0, MissingCellPolicy.RETURN_BLANK_AS_NULL);/* пустые ячейки возвращаются как null */
			stringValue = formatter.formatCellValue(cell);
		}
		catch (Exception e) {
			/* довольно странно, но эта штука иногда падает */
			log.warn("exception while getFirstCellStringValue: ", e);
		}
		return stringValue;
	}

	@SuppressWarnings("unused")
	private void fillStyles(List<CellStyle> stylesToFill, Workbook from) {

		for (short i = 0; i < from.getNumCellStyles(); i++) {
			stylesToFill.add(from.getCellStyleAt(i));
		}

	}
}
