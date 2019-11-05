package com.intech.cms.utils.csvwriter;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.supercsv.cellprocessor.FmtBool;
import org.supercsv.cellprocessor.FmtNumber;
import org.supercsv.cellprocessor.ift.CellProcessor;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CsvReportViewTest {

    private static final String FILENAME = "somefile.csv";

    private static CsvReportView view;
    private static List<List<?>> listOfLists;
    private static List<Map<String,?>> listOfMaps;
    private static List<SomeBean> listOfBeans;
    private static CellProcessor[] processors;

    @BeforeClass
    public static void beforec(){
        view = new CsvReportView();
        view.setCharset("UTF-8");
        view.setDelimiterChar(',');
        view.setFilename(FILENAME);

        listOfLists = new ArrayList<>();
        listOfLists.add(Arrays.asList("qwerty",123,true));
        listOfLists.add(Arrays.asList("uiyrrg",2.5,false));

        listOfMaps = new ArrayList<>();
        listOfMaps.add(new HashMap<String,Object>(){{
            put("a","qwerty");
            put("b",123);
            put("c",true);
        }});
        listOfMaps.add(new HashMap<String,Object>(){{
            put("a","uiyrrg");
            put("b",2.5);
            put("c",false);
        }});

        listOfBeans = new ArrayList<>();
        listOfBeans.add(new SomeBean("qwerty",123,true));
        listOfBeans.add(new SomeBean("uiyrrg",2.5,false));

        processors = new CellProcessor[] {
                null,
                new FmtNumber("00.00"),
                new FmtBool("да","нет")
        };
    }

    @Test
    public void testListNoColumnsNoHeaders() throws Exception {

        Map<String, Object> model = new HashMap<>();
        model.put("no_headers",true);
        model.put("writer","CsvListWriter");
        model.put("fields",new String[3]);
        model.put("objects",listOfLists);

        MockHttpServletResponse response = new MockHttpServletResponse();
        view.renderMergedOutputModel(model,null, response );

        assertTrue(response.getHeader("Content-disposition").contains(FILENAME));
        assertEquals("qwerty,123,true\r\nuiyrrg,2.5,false\r\n",response.getContentAsString());
    }

    @Test
    public void testListNoColumns() throws Exception {

        Map<String, Object> model = new HashMap<>();
        model.put("writer","CsvListWriter");
        model.put("fields",new String[]{"a","b","c"});
        model.put("processors",processors);
        model.put("objects",listOfLists);

        MockHttpServletResponse response = new MockHttpServletResponse();
        view.renderMergedOutputModel(model,null, response );

        assertTrue(response.getHeader("Content-disposition").contains(FILENAME));
        assertEquals("a,b,c\r\nqwerty,\"123,00\",да\r\nuiyrrg,\"02,50\",нет\r\n",response.getContentAsString());
    }

    @Test
    public void testList() throws Exception {

        Map<String, Object> model = new HashMap<>();
        model.put("writer","CsvListWriter");

        List<CsvReportColumnBean> columns = new ArrayList<>();
        columns.add(new CsvReportColumnBean("","Z",null));
        columns.add(new CsvReportColumnBean("","Y",new FmtNumber("00.00")));
        columns.add(new CsvReportColumnBean("","X",new FmtBool("да","нет")));
        model.put("columns",columns);
        model.put("objects",listOfLists);

        MockHttpServletResponse response = new MockHttpServletResponse();
        view.renderMergedOutputModel(model,null, response );

        assertTrue(response.getHeader("Content-disposition").contains(FILENAME));
        assertEquals("Z,Y,X\r\nqwerty,\"123,00\",да\r\nuiyrrg,\"02,50\",нет\r\n",response.getContentAsString());
    }


    @Test
    public void testMapNoColumnsNoHeaders() throws Exception {

        Map<String, Object> model = new HashMap<>();
        model.put("no_headers",true);
        model.put("writer","CsvMapWriter");
        model.put("fields",new String[]{"a","b","c"});
        model.put("objects",listOfMaps);

        MockHttpServletResponse response = new MockHttpServletResponse();
        view.renderMergedOutputModel(model,null, response );

        assertTrue(response.getHeader("Content-disposition").contains(FILENAME));
        assertEquals("qwerty,123,true\r\nuiyrrg,2.5,false\r\n",response.getContentAsString());
    }

    @Test
    public void testMapNoColumns() throws Exception {

        Map<String, Object> model = new HashMap<>();
        model.put("writer","CsvMapWriter");
        model.put("fields",new String[]{"a","b","c"});
        model.put("processors",processors);
        model.put("objects",listOfMaps);

        MockHttpServletResponse response = new MockHttpServletResponse();
        view.renderMergedOutputModel(model,null, response );

        assertTrue(response.getHeader("Content-disposition").contains(FILENAME));
        assertEquals("a,b,c\r\nqwerty,\"123,00\",да\r\nuiyrrg,\"02,50\",нет\r\n",response.getContentAsString());
    }

    @Test
    public void testMap() throws Exception {

        Map<String, Object> model = new HashMap<>();
        model.put("writer","CsvMapWriter");

        List<CsvReportColumnBean> columns = new ArrayList<>();
        columns.add(new CsvReportColumnBean("a","Z",null));
        columns.add(new CsvReportColumnBean("b","Y",new FmtNumber("00.00")));
        columns.add(new CsvReportColumnBean("c","X",new FmtBool("да","нет")));
        model.put("columns",columns);
        model.put("objects",listOfMaps);

        MockHttpServletResponse response = new MockHttpServletResponse();
        view.renderMergedOutputModel(model,null, response );

        assertTrue(response.getHeader("Content-disposition").contains(FILENAME));
        assertEquals("Z,Y,X\r\nqwerty,\"123,00\",да\r\nuiyrrg,\"02,50\",нет\r\n",response.getContentAsString());
    }



    @Test
    public void testBeansNoColumnsNoHeaders() throws Exception {

        Map<String, Object> model = new HashMap<>();
        model.put("no_headers",true);
        model.put("fields",new String[]{"a","b","c"});
        model.put("objects",listOfBeans);

        MockHttpServletResponse response = new MockHttpServletResponse();
        view.renderMergedOutputModel(model,null, response );

        assertTrue(response.getHeader("Content-disposition").contains(FILENAME));
        assertEquals("qwerty,123,true\r\nuiyrrg,2.5,false\r\n",response.getContentAsString());
    }

    @Test
    public void testBeansNoColumns() throws Exception {

        Map<String, Object> model = new HashMap<>();
        model.put("fields",new String[]{"a","b","c"});
        model.put("processors",processors);
        model.put("objects",listOfBeans);

        MockHttpServletResponse response = new MockHttpServletResponse();
        view.renderMergedOutputModel(model,null, response );

        assertTrue(response.getHeader("Content-disposition").contains(FILENAME));
        assertEquals("a,b,c\r\nqwerty,\"123,00\",да\r\nuiyrrg,\"02,50\",нет\r\n",response.getContentAsString());
    }

    @Test
    public void testBeans() throws Exception {

        Map<String, Object> model = new HashMap<>();

        List<CsvReportColumnBean> columns = new ArrayList<>();
        columns.add(new CsvReportColumnBean("a","Z",null));
        columns.add(new CsvReportColumnBean("b","Y",new FmtNumber("00.00")));
        columns.add(new CsvReportColumnBean("c","X",new FmtBool("да","нет")));
        model.put("columns",columns);
        model.put("objects",listOfBeans);

        MockHttpServletResponse response = new MockHttpServletResponse();
        view.renderMergedOutputModel(model,null, response );

        assertTrue(response.getHeader("Content-disposition").contains(FILENAME));
        assertEquals("Z,Y,X\r\nqwerty,\"123,00\",да\r\nuiyrrg,\"02,50\",нет\r\n",response.getContentAsString());
    }

    @Test
    public void testBeansStream() throws Exception {

        Map<String, Object> model = new HashMap<>();

        List<CsvReportColumnBean> columns = new ArrayList<>();
        columns.add(new CsvReportColumnBean("a","Z",null));
        columns.add(new CsvReportColumnBean("b","Y",new FmtNumber("00.00")));
        columns.add(new CsvReportColumnBean("c","X",new FmtBool("да","нет")));
        model.put("columns",columns);
        model.put("objects", Stream.of(new SomeBean("qwerty",123,true),new SomeBean("uiyrrg",2.5,false)));

        MockHttpServletResponse response = new MockHttpServletResponse();
        view.renderMergedOutputModel(model,null, response );

        assertTrue(response.getHeader("Content-disposition").contains(FILENAME));
        assertEquals("Z,Y,X\r\nqwerty,\"123,00\",да\r\nuiyrrg,\"02,50\",нет\r\n",response.getContentAsString());
    }

    @Data
    @AllArgsConstructor
    public static class SomeBean{
        private String a;
        private Number b;
        private boolean c;
    }
}
