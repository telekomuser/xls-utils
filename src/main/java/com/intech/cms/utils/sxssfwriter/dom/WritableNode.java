package com.intech.cms.utils.sxssfwriter.dom;

import java.util.Map;

public interface WritableNode extends Node{

	void write(Map<String, Object> params) throws Exception;
}
