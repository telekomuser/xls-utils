package com.intech.cms.utils.csvreader;

import com.intech.cms.utils.csvwriter.CsvReportColumnBean;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.*;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
/**
 * Класс используемый для парсинга csv файла
 *  @param { CsvReader#typeParameterClass} - тип получаемого бина
 *  @throws IOException
 *  @throws EmptyFieldException - наличие пустого поля, которое обязательно для заполнения
 * **/
public class CsvReader<T> {

    private final Class<T> typeParameterClass;
    private Character		delimiterChar;
    private boolean withHeader;

    public CsvReader(Class<T> typeParameterClass) {
        this.typeParameterClass = typeParameterClass;
    }

    public List<T> importCsvFile(InputStream inputStream, List<CsvReportColumnBean> fields, Set<Integer> emptyFields) throws IOException, EmptyFieldException {
        if(fields==null){
            throw new NullPointerException("Do not CsvReportColumnBean");
        }
        if(emptyFields==null){
            throw new NullPointerException("Do not emptyFields");
        }
        List<T> resultBeanList = new ArrayList<>();
        String[] headers = fields.stream().map(f -> f.getFieldName()).toArray(String[]::new);
        CellProcessor[] processors = fields.stream().map(f -> f.getCellProcessor()).toArray(CellProcessor[]::new);
        ICsvBeanReader reader = null;
        try {
            CsvPreference	preference	= CsvPreference.STANDARD_PREFERENCE;
            if (delimiterChar != null) {
                preference = new CsvPreference.Builder('"', delimiterChar, "\r\n").build();
            }

            reader = new CsvBeanReader(new InputStreamReader(inputStream), preference);
            reader.getHeader(withHeader);

            // data
            if (processors == null) {
                processors = new CellProcessor[fields.size()];
            }
            T resultBean;
            while((resultBean = reader.read(typeParameterClass, headers, processors)) != null ) {
                resultBeanList.add(resultBean);
            }
        }
        finally {
            if( reader != null ) {
                reader.close();
            }
        }

        if(!emptyFields.isEmpty()){
            throw new EmptyFieldException("Ошибка импорта. Проверьте корректность данных в полях: ", emptyFields, headers);
        }
        return resultBeanList;
    }

    public void setWithHeader(boolean withHeader) {
        this.withHeader = withHeader;
    }

    public void setDelimiterChar(Character delimiterChar) {
        this.delimiterChar = delimiterChar;
    }
}
