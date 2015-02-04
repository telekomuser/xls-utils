package com.intech.cms.utils.sxssfwriter;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import com.intech.cms.utils.sxssfwriter.SXSSFWriter;
import com.intech.cms.utils.sxssfwriter.XLSXDOMTemplateParser;
import com.intech.cms.utils.sxssfwriter.dom.Node;
import com.intech.cms.utils.sxssfwriter.dom.WritableNode;

public class SXSSFWriterTest {

	private static InputStream testIS1;
	private static FileOutputStream testOS1;
	private static Collection<TestBean> testList;

	@Test
	public void testWrite() throws Exception {
		
		XLSXDOMTemplateParser parser = new XLSXDOMTemplateParser();
		
		testIS1 = SXSSFWriterTest.class.getResourceAsStream("/test-template2.xlsx");
		Node root = parser.parse(testIS1);
		
		
		testList = new RandomGeneratedCollection(100);
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("entities", testList);
		map.put("su", new StringUtils());
		
		testOS1 = new FileOutputStream("/home/user/test9.xlsx");
		
		testIS1 = SXSSFWriterTest.class.getResourceAsStream("/test-template2.xlsx");
		XSSFWorkbook book = (XSSFWorkbook) WorkbookFactory.create(testIS1);
		
		SXSSFWriter writer = new SXSSFWriter();
		writer.write((WritableNode) root, book, map, testOS1);
		
		testOS1.close();
		
	}

}
