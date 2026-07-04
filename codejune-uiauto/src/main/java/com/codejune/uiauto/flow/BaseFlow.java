package com.codejune.uiauto.flow;

import com.codejune.core.BaseException;
import com.codejune.core.util.ObjectUtil;
import com.codejune.core.util.StringUtil;
import com.codejune.core.util.ThreadUtil;
import com.codejune.uiauto.match.MatchConfig;
import com.codejune.uiauto.match.MatchResult;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.*;

/**
 * 基础流程
 *
 * @author ZJ
 * */
public abstract class BaseFlow {

    private Status status = Status.ERROR;

    private FlowExecutor flowExecutor;

    private final Map<String, StepConfig> stepConfigMap = new HashMap<>();

    private static int wait = 500;

    /**
     * 过程
     * */
    protected abstract void process();

    /**
     * 匹配
     *
     * @param template template
     * @param matchConfig findConfig
     *
     * @return MatchResult
     * */
    protected abstract MatchResult match(Object template, MatchConfig matchConfig);

    /**
     * 执行
     *
     * @return Status
     * */
    public final Status run() {
        this.status = Status.ERROR;
        try {
            this.process();
        } catch (Throwable _) {
            this.status = Status.ERROR;
        }
        return this.status;
    }

    /**
     * 设置 FlowExecutor
     *
     * @param flowExecutor flowExecutor
     * */
    protected final void setFlowExecutor(FlowExecutor flowExecutor) {
        this.flowExecutor = flowExecutor;
    }

    /**
     * 单步
     *
     * @param startMatch 开始匹配
     * @param stopMatch 停止匹配
     * @param successRunner 成功回调
     * @param errorRunner 失败回调
     * @param stepConfig stepConfig
     * @param <T> T
     *
     * @return T
     * */
    protected final <T> T step(Supplier<T> startMatch, Supplier<?> stopMatch, Consumer<T> successRunner, Runnable errorRunner, StepConfig stepConfig) {
        this.checkStop();
        if (startMatch == null) {
            startMatch = () -> null;
        }
        if (successRunner == null) {
            successRunner = _ -> {};
        }
        if (errorRunner == null) {
            errorRunner = () -> {};
        }
        if (stepConfig == null) {
            stepConfig = new StepConfig();
        }
        if (!StringUtil.isEmpty(stepConfig.getId())) {
            if (!this.stepConfigMap.containsKey(stepConfig.getId())) {
                this.stepConfigMap.put(stepConfig.getId(), stepConfig);
            }
            stepConfig = stepConfigMap.get(stepConfig.getId());
        }
        T result;
        if (stepConfig.getWait() <= 0) {
            result = startMatch.get();
        } else {
            result = this.wait(startMatch, stepConfig.getWait());
        }
        if (!pass(result)) {
            if (stepConfig.getCompleteErrorCount() < stepConfig.getErrorCount()) {
                stepConfig.setCompleteErrorCount(stepConfig.getCompleteErrorCount() + 1);
                return result;
            }
            errorRunner.run();
            stepConfig.initCount();
            return result;
        }
        if (stepConfig.getCompleteSuccessCount() < stepConfig.getSuccessCount()) {
            stepConfig.setCompleteSuccessCount(stepConfig.getCompleteSuccessCount() + 1);
            return result;
        }
        if (stepConfig.getEqualCount() > 0) {
            List<Object> completeEqualCount = stepConfig.getCompleteEqualCount();
            if (completeEqualCount.size() < stepConfig.getEqualCount()) {
                completeEqualCount.add(result);
            }
            if (completeEqualCount.size() < stepConfig.getEqualCount()) {
                return result;
            }
            for (int i = 0; i < completeEqualCount.size(); i++) {
                for (int j = i + 1; j < completeEqualCount.size(); j++) {
                    if (!ObjectUtil.equals(completeEqualCount.get(i), completeEqualCount.get(j))) {
                        stepConfig.initCount();
                        return result;
                    }
                }
            }
        }
        this.status = Status.SUCCESS;
        if (stopMatch == null || stepConfig.getWait() <= 0) {
            successRunner.accept(result);
        } else {
            long currentTimeMillis = System.currentTimeMillis();
            boolean stopPass = false;
            while (System.currentTimeMillis() - currentTimeMillis < stepConfig.getWait() * 5L) {
                this.checkStop();
                successRunner.accept(result);
                if (pass(stopMatch.get())) {
                    stopPass = true;
                    break;
                }
                ThreadUtil.sleep(500);
            }
            if (!stopPass) {
                result = null;
            }
        }
        stepConfig.initCount();
        return result;
    }

    /**
     * 匹配
     *
     * @param template template
     *
     * @return MatchResult
     * */
    protected final MatchResult match(Object template) {
        return this.match(template, null);
    }

    /**
     * 匹配
     *
     * @param template template
     * @param matchConfig findConfig
     * @param wait wait
     *
     * @return MatchResult
     * */
    protected final MatchResult matchWait(Object template, MatchConfig matchConfig, int wait) {
        return this.step(
                () -> this.match(template, matchConfig),
                null,
                null,
                null,
                new StepConfig().setWait(wait)
        );
    }

    /**
     * 匹配
     *
     * @param template template
     * @param matchConfig findConfig
     *
     * @return MatchResult
     * */
    protected final MatchResult matchWait(Object template, MatchConfig matchConfig) {
        return this.matchWait(template, matchConfig, wait);
    }

    /**
     * 匹配
     *
     * @param template template
     * @param wait wait
     *
     * @return MatchResult
     * */
    protected final MatchResult matchWait(Object template, int wait) {
        return this.matchWait(template, null, wait);
    }

    /**
     * 匹配
     *
     * @param template template
     *
     * @return MatchResult
     * */
    protected final MatchResult matchWait(Object template) {
        return this.matchWait(template, null, wait);
    }

    /**
     * 快速开始匹配
     *
     * @param template template
     * @param matchConfig matchConfig
     * @param runner runner
     *
     * @return 是否成功
     * */
    protected final boolean fastStartStep(Object template, MatchConfig matchConfig, Consumer<MatchResult> runner) {
        return this.step(
                () -> this.match(template, matchConfig),
                () -> !pass(this.match(template, matchConfig)),
                runner,
                null,
                new StepConfig().setWait(wait * 5)
        ) != null;
    }

    /**
     * 快速开始匹配
     *
     * @param template template
     * @param runner runner
     *
     * @return 是否成功
     * */
    protected final boolean fastStartStep(Object template, Consumer<MatchResult> runner) {
        return this.fastStartStep(template, null, runner);
    }

    /**
     * 快速结束匹配
     *
     * @param template template
     * @param matchConfig matchConfig
     * @param runner runner
     *
     * @return 是否成功
     * */
    protected final boolean fastStopStep(Object template, MatchConfig matchConfig, Runnable runner) {
        return this.step(
                () -> true,
                () -> pass(this.match(template, matchConfig)),
                (_) -> {
                    if (runner == null) {
                        return;
                    }
                    runner.run();
                },
                null,
                new StepConfig().setWait(wait * 5)
        ) != null;
    }

    /**
     * 快速结束匹配
     *
     * @param template template
     * @param runner runner
     *
     * @return 是否成功
     * */
    protected final boolean fastStopStep(Object template, Runnable runner) {
        return this.fastStopStep(template, null, runner);
    }

    /**
     * 等待
     *
     * @param supplier supplier
     * @param timeout timeout
     * @param <T> T
     *
     * @return T
     * */
    private <T> T wait(Supplier<T> supplier, long timeout) {
        if (supplier == null) {
            return null;
        }
        T result = supplier.get();
        long time = System.currentTimeMillis();
        while (!pass(result) && System.currentTimeMillis() - time < timeout) {
            this.checkStop();
            result = supplier.get();
            ThreadUtil.sleep(0);
        }
        return result;
    }

    /**
     * 检查停止
     * */
    protected final void checkStop() {
        if (this.flowExecutor.getStatus() == FlowExecutor.Status.STOP) {
            throw new BaseException("stop");
        }
    }

    /**
     * setWait
     *
     * @param wait wait
     * */
    public static void setWait(int wait) {
        BaseFlow.wait = wait;
    }

    /**
     * pass
     *
     * @param object object
     *
     * @return boolean
     * */
    private static boolean pass(Object object) {
        if (object == null) {
            return false;
        }
        if (object instanceof Boolean b) {
            return b;
        }
        return true;
    }

    /**
     * 流程状态
     *
     * @author ZJ
     * */
    public enum Status {

        SUCCESS,

        ERROR

    }

}