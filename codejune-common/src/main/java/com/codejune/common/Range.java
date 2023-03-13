package com.codejune.common;

import com.codejune.common.exception.InfoException;

/**
 * 范围
 *
 * @author ZJ
 * */
public class Range {

    private final Long start;

    private final Long end;

    public Range(Long start, Long end) {
        if (start == null) {
            start = 0L;
        }
        if (end != null && end < start) {
            throw new InfoException("end < start");
        }
        this.start = start;
        this.end = end;
    }

    public Long getStart() {
        return start;
    }

    public Long getEnd() {
        return end;
    }

}