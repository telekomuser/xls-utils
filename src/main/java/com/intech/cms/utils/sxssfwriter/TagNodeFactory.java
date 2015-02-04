package com.intech.cms.utils.sxssfwriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.intech.cms.utils.sxssfwriter.dom.JtForEachTagNode;
import com.intech.cms.utils.sxssfwriter.dom.JtForTagNode;
import com.intech.cms.utils.sxssfwriter.dom.Node;
import com.intech.cms.utils.sxssfwriter.dom.Tag;
import com.intech.cms.utils.sxssfwriter.dom.TagNode;

/**
 * @author epotanin
 */
public class TagNodeFactory {

	private static final String		START_NODE_PATTERN	= "^<([^ /]+)([^>]*)>$";
	private static final String		ATTR_PATTERN		= "([a-zA-Z0-9]+)=\"([^\"]+)\"";
	private static final String		END_NODE_PATTERN	= "^</([^ ]+)>$";

	private static final Class<?>[]	knownTags			= new Class<?>[] { JtForEachTagNode.class, JtForTagNode.class };

	public static TagNode createTagNode(String text, Node parent) throws Exception {

		TagNode node = null;
		Matcher m = Pattern.compile(START_NODE_PATTERN)
							.matcher(text);
		if (m.matches()) {

			String tagName = m.group(1);
			node = findAndCreateTagNodeObject(tagName);
			((Node) node).setParent(parent);

			List<String> reqattrs = getNodeRequiredAttributes(node);

			if (m.groupCount() > 1) {

				Matcher m2 = Pattern.compile(ATTR_PATTERN)
									.matcher(m.group(2));
				while (m2.find()) {

					String attributeName = m2.group(1);
					String attributeValue = m2.group(2);
					node.setAttributeValue(attributeName, attributeValue);
					reqattrs.remove(attributeName);
				}
			}
			// check is all required attributes filled
			if (!reqattrs.isEmpty()) {
				throw new Exception("not all required attributes present: " + reqattrs.get(0));
			}
		}
		return node;
	}

	public static boolean isStartTag(String text) {

		return text.matches(START_NODE_PATTERN);
	}

	public static boolean isEndTag(String text) {

		return text.matches(END_NODE_PATTERN);
	}

	public static String getEndTagName(String text) {

		String name = null;
		Matcher m = Pattern.compile(END_NODE_PATTERN)
							.matcher(text);
		if (m.matches()) {
			name = m.group(1);
		}
		return name;
	}

	private static TagNode findAndCreateTagNodeObject(String tagname) throws Exception {

		TagNode result = null;
		for (Class<?> clazz : knownTags) {
			Tag anno = (Tag) clazz.getAnnotation(Tag.class);
			if (anno.name()
					.equalsIgnoreCase(tagname)) {
				result = (TagNode) clazz.newInstance();// must have no arg constructor
				break;
			}
		}
		if (result == null) {
			throw new Exception("Unknown tag: " + tagname);
		}
		return result;
	}

	private static List<String> getNodeRequiredAttributes(TagNode node) throws Exception {

		Tag anno = (Tag) node.getClass()
								.getAnnotation(Tag.class);
		String[] sa = anno.requiredAttributes();
		return new ArrayList<String>(Arrays.asList(sa));
	}

	@SuppressWarnings("unused")
	private static List<String> getNodeOptionalAttributes(TagNode node) throws Exception {

		Tag anno = (Tag) node.getClass()
								.getAnnotation(Tag.class);
		String[] sa = anno.optionalAttributes();
		return new ArrayList<String>(Arrays.asList(sa));
	}

	public static String getTagName(TagNode node) throws Exception {

		Tag anno = (Tag) node.getClass()
								.getAnnotation(Tag.class);
		return anno.name();
	}
}
