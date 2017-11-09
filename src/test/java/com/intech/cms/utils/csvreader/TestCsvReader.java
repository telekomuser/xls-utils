package com.intech.cms.utils.csvreader;

import com.intech.cms.utils.csvwriter.CsvReportColumnBean;
import com.intech.cms.utils.sxssfwriter.JxlsSXSSFTest;
import org.junit.Assert;
import org.junit.Test;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestCsvReader {

    @Test
    public void testReadBean(){


        InputStream inputStream = TestCsvReader.class.getResourceAsStream("/test-csvreader.csv");

        Set<Integer> emptyFileds = new HashSet<>();
        List<CsvReportColumnBean> fields = new ArrayList<>();
        fields.add(new CsvReportColumnBean("id", "id", new Optional(new ParseInt())));
        fields.add(new CsvReportColumnBean("message", "message", new SuppressAdaptor(new NotNull(),emptyFileds)));

        CsvReader<TestReaderBean> testReaderBeanCsvReader = new CsvReader<>(TestReaderBean.class);
        testReaderBeanCsvReader.setWithHeader(true);
        List<TestReaderBean> resultList = null;
        try {
            resultList = testReaderBeanCsvReader.importCsvFile(inputStream, fields, emptyFileds);
            Assert.assertEquals(resultList.size(), 5);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (EmptyFieldException e) {
            e.printStackTrace();
        }
    }
}
