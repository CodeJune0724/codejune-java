package com.codejune.uiauto.flow;

import java.util.ArrayList;
import java.util.List;
/**
 * StepConfig
 *
 * @author ZJ
 * */
public final class StepConfig {

    private final String id;

    private int wait = 0;

    private int successCount = 0;

    private int completeSuccessCount = 0;

    private int errorCount = 0;

    private int completeErrorCount = 0;

    private int equalCount = 0;

    private final List<Object> completeEqualCount = new ArrayList<>();

    public StepConfig(String id) {
        this.id = id;
    }

    public StepConfig() {
        this.id = null;
    }

    public String getId() {
        return this.id;
    }

    public int getWait() {
        return this.wait;
    }

    public StepConfig setWait(int wait) {
        this.wait = wait;
        return this;
    }

    public int getSuccessCount() {
        return this.successCount;
    }

    public StepConfig setSuccessCount(int successCount) {
        this.successCount = successCount;
        return this;
    }

    public int getCompleteSuccessCount() {
        return this.completeSuccessCount;
    }

    public StepConfig setCompleteSuccessCount(int completeSuccessCount) {
        this.completeSuccessCount = completeSuccessCount;
        return this;
    }

    public int getErrorCount() {
        return this.errorCount;
    }

    public StepConfig setErrorCount(int errorCount) {
        this.errorCount = errorCount;
        return this;
    }

    public int getCompleteErrorCount() {
        return this.completeErrorCount;
    }

    public StepConfig setCompleteErrorCount(int completeErrorCount) {
        this.completeErrorCount = completeErrorCount;
        return this;
    }

    public int getEqualCount() {
        return this.equalCount;
    }

    public StepConfig setEqualCount(int equalCount) {
        this.equalCount = equalCount;
        return this;
    }

    public List<Object> getCompleteEqualCount() {
        return this.completeEqualCount;
    }

    public void initCount() {
        this.setCompleteSuccessCount(0);
        this.setCompleteErrorCount(0);
        this.completeEqualCount.clear();
    }

}