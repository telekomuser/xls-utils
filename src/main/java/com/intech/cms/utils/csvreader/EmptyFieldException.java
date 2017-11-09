package com.intech.cms.utils.csvreader;

import java.util.Set;
import java.util.stream.Collectors;

public class EmptyFieldException extends Exception {

    public EmptyFieldException() {
        super();
    }

    public EmptyFieldException(String message, Set<Integer> fields, String[] headers) {
        super(message + fields.stream().map(f -> headers[f-1]).collect(Collectors.joining(", "))); // формирование сообщения с добавлением названий полей которые были пусты
    }

}