package com.intech.cms.utils.sxssfwriter;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.intech.cms.utils.sxssfwriter.XLSXDOMTemplateParser;
import com.intech.cms.utils.sxssfwriter.dom.Node;

public class XLSXDOMTemplateParser2Test {

	private static InputStream	testIS1;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {

	}

	@Test(expected = Exception.class)
	public void test1() throws Exception {

		try {
			testIS1 = XLSXDOMTemplateParser2Test.class.getResourceAsStream("/test-template-err1.xlsx");
			assertNotNull(testIS1);
			new XLSXDOMTemplateParser().parse(testIS1);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			testIS1.close();
		}
	}

	@Test(expected = Exception.class)
	public void test2() throws Exception {

		try {
			testIS1 = XLSXDOMTemplateParser2Test.class.getResourceAsStream("/test-template-err2.xlsx");
			assertNotNull(testIS1);
			new XLSXDOMTemplateParser().parse(testIS1);		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			testIS1.close();
		}
	}
	
	@Test
	public void testParse() throws Exception {

		testIS1 = XLSXDOMTemplateParser2Test.class.getResourceAsStream("/test-template2.xlsx");
		assertNotNull(testIS1);
		XLSXDOMTemplateParser parser = new XLSXDOMTemplateParser();

		Node root = parser.parse(testIS1);
		printNode(root, "");
		testIS1.close();
	}

	private void printNode(Node node, String prefix) {

		if (node.isLeafNode()) {
			System.out.println(prefix + node.toString());
		}
		else {
			System.out.println(prefix + node.toString());
			for (Node child : node.getChildren()) {
				printNode(child, prefix + "   ");
			}
		}
	}
}
