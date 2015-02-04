package com.intech.cms.utils.sxssfwriter.dom;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NewSheetNode extends AbstractTemplateNode implements WritableNode {

	private static final Logger	log	= LoggerFactory.getLogger(JtForTagNode.class);

	private List<Node>			rows;
	private List<Integer>		columnWidths;
	private Node				parent;

	public NewSheetNode(Node parent) {

		super();
		this.rows = new LinkedList<Node>();
		this.parent = parent;
	}

	public Node getParent() {

		return parent;
	}

	public void setParent(Node node) {

		this.parent = node;
	}

	public List<Integer> getColumnWidths() {

		return columnWidths;
	}

	public void setColumnWidths(List<Integer> columnWidths) {

		this.columnWidths = columnWidths;
	}

	public boolean hasChildren() {

		return !rows.isEmpty();
	}

	public List<Node> getChildren() {

		return rows;
	}

	public boolean isLeafNode() {

		return false;
	}

	public void addChild(Node node) {

		rows.add(node);
	}

	public void write(Map<String, Object> params) throws Exception {

		Sheet sh = getWorkbook(params).createSheet();
		writeColumnWidths(columnWidths, sh);

		params.put(NEW_ROW_INDEX, new AtomicInteger());
		params.put(DESTINATION_SHEET, sh);

		for (Node node : rows) {

			if (node instanceof WritableNode) {
				((WritableNode) node).write(params);
			}
			else {
				log.warn("Unknown child while writing NewSheetNode'{}', skipping", node);
			}
		}
	}

	private void writeColumnWidths(List<Integer> columnWidths, Sheet sh) {

		for (int i = 0; i < columnWidths.size(); i++) {
			sh.setColumnWidth(i, columnWidths.get(i));
		}
	}

	@Override
	public String toString() {

		return "NewSheetNode [columnWidths=" + columnWidths + "]";
	}

}
