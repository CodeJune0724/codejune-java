package com.codejune.readexcel;

import java.util.Iterator;

/**
 * Sheet
 *
 * @author ZJ
 * */
public final class Sheet implements Iterable<Row> {

    private final org.apache.poi.ss.usermodel.Sheet sheet;

    public Sheet(org.apache.poi.ss.usermodel.Sheet sheet) {
        this.sheet = sheet;
    }

    @Override
    public Iterator<Row> iterator() {
        Iterator<org.apache.poi.ss.usermodel.Row> iterator = sheet.iterator();
        return new Iterator<Row>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Row next() {
                return new Row(iterator.next());
            }
        };
    }

}