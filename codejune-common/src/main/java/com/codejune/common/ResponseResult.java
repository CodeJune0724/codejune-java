package com.codejune.common;

/**
 * 响应结果
 *
 * @author ZJ
 * */
public class ResponseResult {

    private boolean flag;

    private Object code;

    private Object message;

    private Object result;

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public Object getCode() {
        return code;
    }

    public void setCode(Object code) {
        this.code = code;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    /**
     * 返回正确
     *
     * @param code code
     * @param message message
     * @param result result
     *
     * @return ResponseResult
     * */
    public static ResponseResult returnTrue(Object code, Object message, Object result) {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setFlag(true);
        responseResult.setCode(code);
        responseResult.setMessage(message);
        responseResult.setResult(result);
        return responseResult;
    }

    /**
     * 返回正确
     *
     * @param result result
     *
     * @return ResponseResult
     * */
    public static ResponseResult returnTrue(Object result) {
        return returnTrue(null, null, result);
    }

    /**
     * 返回正确
     *
     * @return ResponseResult
     * */
    public static ResponseResult returnTrue() {
        return returnTrue(null, null, null);
    }

    /**
     * 返回失败
     *
     * @param code code
     * @param message message
     * @param result result
     *
     * @return ResponseResult
     * */
    public static ResponseResult returnFalse(Object code, Object message, Object result) {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setFlag(false);
        responseResult.setCode(code);
        responseResult.setMessage(message);
        responseResult.setResult(result);
        return responseResult;
    }

}