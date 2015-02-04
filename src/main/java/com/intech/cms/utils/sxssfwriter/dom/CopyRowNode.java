package com.intech.cms.utils.sxssfwriter.dom;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class CopyRowNode extends AbstractTemplateNode implements WritableNode {

	private Row source;
	private Node parent;

	public CopyRowNode(Row source, Node parent) {
		super();
		this.source = source;
		this.parent = parent;
	}

	public Node getParent() {

		return parent;
	}

	public void setParent(Node node) {

		this.parent = node;
	}

	public boolean isLeafNode() {

		return true;
	}

	public boolean hasChildren() {
		return false;
	}

	public List<Node> getChildren() {
		return null;
	}

	public void addChild(Node node) {

	}

	public void write(Map<String, Object> params) {

		Row destRow = createClonedRow(source, getDestinationSheet(params), getNewRowIndex(params));

		int lastColumn = source.getLastCellNum();

		for (int ci = 0; ci < lastColumn; ci++) {

			Cell cell = source.getCell(ci);/* пустые ячейки возвращаются как null */
			if (cell != null) {
				createClonedCell(cell, destRow, ci, params);
			}
		}
	}

	private void createClonedCell(Cell source, Row destRow, int ci, Map<String, Object> params) {
		Cell cell = destRow.createCell(ci, source.getCellType());

		short csidx = source.getCellStyle().getIndex();
		CellStyle targetStyle = destRow.getSheet().getWorkbook().getCellStyleAt(csidx);
		cell.setCellStyle(targetStyle);

		// cell.setCellStyle(source.getCellStyle());

		// System.out.println(source.getCellType());
		switch (source.getCellType()) {
		case Cell.CELL_TYPE_BLANK:
			cell.setCellType(Cell.CELL_TYPE_BLANK);
			break;
		case Cell.CELL_TYPE_BOOLEAN:
			cell.setCellValue(source.getBooleanCellValue());
			break;
		case Cell.CELL_TYPE_ERROR:
			cell.setCellValue(source.getErrorCellValue());
			break;
		case Cell.CELL_TYPE_FORMULA:
			cell.setCellFormula(source.getStringCellValue());
			break;// !
		case Cell.CELL_TYPE_NUMERIC:
			cell.setCellValue(source.getNumericCellValue());
			break;
		case Cell.CELL_TYPE_STRING:
			String stringValue = source.getStringCellValue();
			Object o = processExpression(stringValue, params);
			String processed = o == null ? "" : o.toString();
			cell.setCellValue(processed);
			break;
		}

	}

	private Row createClonedRow(Row source, Sheet destinationSheet, AtomicInteger newRowIndex) {
		Row row = destinationSheet.createRow(newRowIndex.get());
		newRowIndex.incrementAndGet();

		row.setHeight(source.getHeight());
		row.setRowStyle(source.getRowStyle());

		return row;
	}

	@Override
	public String toString() {
		return "CopyRowNode [source=" + source + "]";
	}

}
