package com.intech.cms.utils.sxssfwriter.dom;

import java.util.List;

public interface Node {

	boolean isLeafNode();

	boolean hasChildren();

	List<Node> getChildren();

	void addChild(Node node);

	Node getParent();
	
	void setParent(Node node);

}
