package com.intech.cms.utils.sxssfwriter.dom;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.jexl2.JexlContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author epotanin
 * @see http://jett.sourceforge.net/tags/forEach.html
 */
@Tag(name = "jt:forEach", requiredAttributes = { "items", "var" })//TODO , optionalAttributes = { "limit", "indexVar" })
public class JtForEachTagNode extends AbstractTemplateNode implements WritableNode, TagNode {

	private static final Logger	log	= LoggerFactory.getLogger(JtForEachTagNode.class);

	private String				items;
	private String				var;
	private LinkedList<Node>	subitems;
	private Node				parent;

	public JtForEachTagNode() throws Exception {

		this.subitems = new LinkedList<Node>();
	}

	public String getAttributeValue(String attributeName) {

		String value = null;
		if ("items".equalsIgnoreCase(attributeName)) {
			value = items;
		}
		else if ("var".equalsIgnoreCase(attributeName)) {
			value = var;
		}
		else {
			log.warn("Unknown attribute '{}' requested, returning null", attributeName);
		}
		return value;
	}

	public void setAttributeValue(String attributeName, String attributeValue) throws Exception {

		if ("items".equalsIgnoreCase(attributeName)) {
			this.items = attributeValue;
		}
		else if ("var".equalsIgnoreCase(attributeName)) {
			this.var = attributeValue;
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

		Object o = processExpression(items, params);
		if (!(o instanceof Collection)) {
			throw new Exception("values expression of jx:forEach tag evaluated to not Collection");
		}
		Collection<?> items = (Collection<?>) o;

		for (Object colItem : items) {
			context.set(var, colItem);
			for (Node node : subitems) {

				if (node instanceof WritableNode) {
					((WritableNode) node).write(params);
				}
				else {
					log.warn("Unknown child while writing JxForEachTagNode '{}', skipping", node);
				}
			}
		}
	}

	@Override
	public String toString() {

		return "JxForEachTagNode [items=" + items + ", var=" + var + "]";
	}

}
