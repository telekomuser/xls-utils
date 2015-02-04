package com.intech.cms.utils.sxssfwriter.dom;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.jexl2.JexlContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author epotanin
 * @see http://jett.sourceforge.net/tags/for.html
 */
@Tag(name = "jt:for", requiredAttributes = { "start", "end", "var" }, optionalAttributes = { "step" })
public class JtForTagNode extends AbstractTemplateNode implements WritableNode, TagNode {

	private static final Logger log = LoggerFactory.getLogger(JtForTagNode.class);

	private String startExpression;
	private String endExpression;
	private String var;
	private String stepExpression;
	private LinkedList<Node> subitems;
	private Node parent;

	public JtForTagNode() throws Exception {
		this.subitems = new LinkedList<Node>();
	}

	public String getAttributeValue(String attributeName) {
		String value = null;
		if ("start".equalsIgnoreCase(attributeName)) {
			value = startExpression;
		}
		else if ("end".equalsIgnoreCase(attributeName)) {
			value = endExpression;
		}
		else if ("var".equalsIgnoreCase(attributeName)) {
			value = var;
		}
		else if ("step".equalsIgnoreCase(attributeName)) {
			value = stepExpression;
		}
		else {
			log.warn("Unknown attribute '{}' requested, returning null", attributeName);
		}

		return value;
	}

	public void setAttributeValue(String attributeName, String attributeValue) throws Exception {
		if ("start".equalsIgnoreCase(attributeName)) {
			this.startExpression = attributeValue;
		}
		else if ("end".equalsIgnoreCase(attributeName)) {
			this.endExpression = attributeValue;
		}
		else if ("var".equalsIgnoreCase(attributeName)) {
			this.var = attributeValue;
		}
		else if ("step".equalsIgnoreCase(attributeName)) {
			this.stepExpression = attributeValue;
		}
		else {
			log.warn("Setting unknown attribute '{}' requested, skipping", attributeName);
		}
	}

	public Node getParent() {

		return parent;
	}

	public void setParent(Node node) {

		this.parent = node;
	}

	public boolean hasChildren() {
		return !subitems.isEmpty();
	}

	public List<Node> getChildren() {

		return subitems;
	}

	public boolean isLeafNode() {

		return false;
	}

	public void addChild(Node node) {

		subitems.add(node);
	}

	public void write(Map<String, Object> params) throws Exception {

		JexlContext context = getJexlContext(params);

		int start = Integer.valueOf(processExpression(startExpression, params).toString());
		int end = Integer.valueOf(processExpression(endExpression, params).toString());
		int step = stepExpression == null ? 1 : Integer.valueOf(processExpression(stepExpression, params)
				.toString());

		if (step == 0) {
			throw new Exception("step expression evaluated to 0, it is prohibited");
		}
		if ((start < end && step < 0) || (start > end && step > 0)) {
			throw new Exception("for loop parameters imply endless execution, it is prohibited");
		}

		for (int i = start; i <= end; i += step) {
			context.set(var, i);
			for (Node node : subitems) {

				if (node instanceof WritableNode) {
					((WritableNode) node).write(params);
				}
				else {
					log.warn("Unknown child while writing JxForTagNode '{}', skipping", node);
				}
			}
		}
	}

	@Override
	public String toString() {
		return "JxForTagNode [startExpression=" + startExpression + ", endExpression=" + endExpression + ", var=" + var
				+ ", stepExpression=" + stepExpression + "]";
	}

}
