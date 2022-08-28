package com.codejune.common;

import com.codejune.common.exception.InfoException;

/**
 * 范围
 *
 * @author ZJ
 * */
public class Range {

    private final Integer start;

    private final Integer end;

    public Range(Integer start, Integer end) {
        if (start == null) {
            start = 0;
        }
        if (end != null && end < start) {
            throw new InfoException("end < start");
        }
        this.start = start;
        this.end = end;
    }

    public Integer getStart() {
        return start;
    }

    public Integer getEnd() {
        return end;
    }

}