package com.codejune.uiauto.flow;

import com.codejune.core.util.ObjectUtil;
import com.codejune.uiauto.WindowHandle;
import java.util.ArrayList;
import java.util.List;

/**
 * 流程执行器
 *
 * @author ZJ
 * */
public final class FlowExecutor {

    private final WindowHandle windowHandle;

    private final List<BaseFlow> flowList = new ArrayList<>();

    private Status status = Status.STOP;

    private Runnable errorHandler;

    public FlowExecutor(WindowHandle windowHandle, List<BaseFlow> flowList) {
        this.windowHandle = windowHandle;
        if (ObjectUtil.isEmpty(flowList)) {
            return;
        }
        for (BaseFlow baseFlow : flowList) {
            baseFlow.setFlowExecutor(this);
            this.flowList.addAll(flowList);
        }
    }

    public Status getStatus() {
        return this.status;
    }

    public WindowHandle getWindowHandle() {
        return this.windowHandle;
    }

    public void setErrorHandler(Runnable errorHandler) {
        this.errorHandler = errorHandler;
    }

    /**
     * 运行
     * */
    public synchronized void run() {
        if (this.status == Status.RUN) {
            return;
        }
        this.status = Status.RUN;
        while (true) {
            for (int i = 0; i < 3; i++) {
                for (BaseFlow baseFlow : this.flowList) {
                    BaseFlow.Status baseFlowStatus;
                    do {
                        if (this.status == Status.STOP) {
                            return;
                        }
                        try {
                            baseFlowStatus = baseFlow.run();
                        } catch (Throwable _) {
                            baseFlowStatus = BaseFlow.Status.ERROR;
                        }
                        if (baseFlowStatus == BaseFlow.Status.SUCCESS) {
                            i = 0;
                        }
                    } while (baseFlowStatus == BaseFlow.Status.SUCCESS);
                }
            }
            if (this.errorHandler == null) {
                continue;
            }
            if (this.status == Status.STOP) {
                return;
            }
            try {
                this.errorHandler.run();
            } catch (Throwable _) {}
        }
    }

    /**
     * 停止
     * */
    public void stop() {
        this.status = Status.STOP;
    }

    /**
     * 状态
     *
     * @author ZJ
     * */
    public enum Status {

        RUN,

        STOP

    }

}