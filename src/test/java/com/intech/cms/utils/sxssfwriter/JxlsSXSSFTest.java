package com.intech.cms.utils.sxssfwriter;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Test;
import org.jxls.area.Area;
import org.jxls.builder.AreaBuilder;
import org.jxls.builder.xls.XlsCommentAreaBuilder;
import org.jxls.common.CellRef;
import org.jxls.common.Context;
import org.jxls.transform.poi.PoiTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

/**
 * Created by arakushin on 27.12.16.
 */
public class JxlsSXSSFTest {

    private static final Logger log			= LoggerFactory.getLogger(JxlsSXSSFTest.class);
    private static Collection<TestBean> testList;

    @Test
    public void testWrite() throws Exception {

        testList = new RandomGeneratedCollection(100);

        try(InputStream in = JxlsSXSSFTest.class.getResourceAsStream("/test-template3.xlsx")) {
            try (OutputStream os = new FileOutputStream("target/test_jxls10.xlsx")) {

                Context context = new Context();
                context.putVar("entities", testList);
                context.putVar("su", new StringUtils());

                Workbook workbook = WorkbookFactory.create(in);
                PoiTransformer transformer = PoiTransformer.createSxssfTransformer(workbook, 5, false);

                AreaBuilder areaBuilder = new XlsCommentAreaBuilder(transformer);
                List<Area> xlsAreaList = areaBuilder.build();
                Area xlsArea = xlsAreaList.get(0);
                xlsArea.applyAt(new CellRef("Result!A1"), context);
                Workbook workbook2 = transformer.getWorkbook();
                workbook2.removeSheetAt(0);
                workbook2.write(os);

            }
        }
    }

    @Test
    public void testWrite1() throws Exception {

        testList = new RandomGeneratedCollection(100000);

        try(InputStream in = JxlsSXSSFTest.class.getResourceAsStream("/test-template3.xlsx")) {

            Context context = new Context();
            context.putVar("entities", testList);
            context.putVar("su", new StringUtils());

            Workbook workbook = WorkbookFactory.create(in);
            PoiTransformer transformer = PoiTransformer.createSxssfTransformer(workbook, 50, false);

            AreaBuilder areaBuilder = new XlsCommentAreaBuilder(transformer);
            List<Area> xlsAreaList = areaBuilder.build();
            Area xlsArea = xlsAreaList.get(0);
            long startTime = System.nanoTime();
            xlsArea.applyAt(new CellRef("Result!A1"), context);
            long endTime = System.nanoTime();
            System.out.println("Stress Sxssf demo 100000 rows time (s): " + (endTime - startTime) / 1000000000);
            try (OutputStream os = new FileOutputStream("target/test_jxls11.xlsx")) {
                transformer.getWorkbook().write(os);
            }
        }
    }

}
