package com.intech.cms.utils.excelextractor;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

public class ExcelExtractorTest {

	@Test
	public void testIterateRowsXLSX() throws Exception {
		MultipartFile mp = new MockMultipartFile("mock",ExcelExtractorTest.class.getResourceAsStream("/test-extract1.xlsx"));
		ExcelExtractor ex = new ExcelExtractor();
				
		test(ex,mp,false,false,false,"[1, c1][3, a3, b3, c3, d3][4, a4, c4, d4]");
		test(ex,mp,false,false,true, "[1, c1][2][3, a3, b3, c3, d3][4, a4, c4, d4]");
		test(ex,mp,false,true, false,"[1, , , c1][3, a3, b3, c3, d3][4, a4, , c4, d4]");
		test(ex,mp,false,true, true, "[1, , , c1][2][3, a3, b3, c3, d3][4, a4, , c4, d4]");
		test(ex,mp,true, false,false,"[3, a3, b3, c3, d3][4, a4, c4, d4]");
		test(ex,mp,true, false,true, "[2][3, a3, b3, c3, d3][4, a4, c4, d4]");
		test(ex,mp,true, true, false,"[3, a3, b3, c3, d3][4, a4, , c4, d4]");
		test(ex,mp,true, true, true, "[2][3, a3, b3, c3, d3][4, a4, , c4, d4]");
	}
	
	@Test
	public void testIterateRowsXLS() throws Exception {
		MultipartFile mp = new MockMultipartFile("mock",ExcelExtractorTest.class.getResourceAsStream("/test-extract1.xls"));
		ExcelExtractor ex = new ExcelExtractor();
				
		test(ex,mp,false,false,false,"[1, c1][3, a3, b3, c3, d3][4, a4, c4, d4]");
		test(ex,mp,false,false,true, "[1, c1][2][3, a3, b3, c3, d3][4, a4, c4, d4]");
		test(ex,mp,false,true, false,"[1, , , c1][3, a3, b3, c3, d3][4, a4, , c4, d4]");
		test(ex,mp,false,true, true, "[1, , , c1][2][3, a3, b3, c3, d3][4, a4, , c4, d4]");
		test(ex,mp,true, false,false,"[3, a3, b3, c3, d3][4, a4, c4, d4]");
		test(ex,mp,true, false,true, "[2][3, a3, b3, c3, d3][4, a4, c4, d4]");
		test(ex,mp,true, true, false,"[3, a3, b3, c3, d3][4, a4, , c4, d4]");
		test(ex,mp,true, true, true, "[2][3, a3, b3, c3, d3][4, a4, , c4, d4]");
	}
	
	
	private void test(ExcelExtractor instance, MultipartFile mp,  boolean withHeader,
			boolean returnEmptyCells, boolean returnEmptyRows,String expected) throws Exception {
		instance.setWithHeader(withHeader);
		instance.setReturnEmptyCells(returnEmptyCells);
		instance.setReturnEmptyRows(returnEmptyRows);
		
		assertEquals(expected,
				instance.iterateRows(mp, 100).map(Arrays::toString).collect(Collectors.joining("")));
	}

}
