package com.intech.cms.utils.csvreader;

import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.exception.SuperCsvCellProcessorException;
import org.supercsv.util.CsvContext;

import java.util.Set;

/**
 * Адаптор для отлавливания пустых полей, которые обязательны для заполнения
 *
 * **/
public class SuppressAdaptor extends CellProcessorAdaptor {


    public Set<Integer> emptyFields;
    public SuppressAdaptor(CellProcessor next, Set<Integer> emptyFields) {
        super(next);
        this.emptyFields = emptyFields;
    }

    public Object execute(Object value, CsvContext context) {
        Object execute = null;
        try {
            execute = next.execute(value, context);
        } catch (SuperCsvCellProcessorException e) {
            emptyFields.add(context.getColumnNumber()); // добавление в Set номера колонки в котором произошло исключение
        }
        return execute;
    }

    public Set<Integer> getEmptyFields() {
        return emptyFields;
    }
}
