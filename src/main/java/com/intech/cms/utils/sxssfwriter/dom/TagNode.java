package com.intech.cms.utils.sxssfwriter.dom;

//TODO maybe inherit from WritableNode ?
public interface TagNode {

	String getAttributeValue(String attributeName);
	
	void setAttributeValue(String attributeName, String attributeValue) throws Exception;
}
