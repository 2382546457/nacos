package com.xiaohe.nacos.api.remote.request;

public class PushAckRequest extends InternalRequest {

    private String requestId;

    private boolean success;

    private Exception exception;

    public static PushAckRequest build(String requestId, boolean success) {
        PushAckRequest request = new PushAckRequest();
        request.requestId = requestId;
        request.success = success;
        return request;
    }


    /**
     * Getter method for property <tt>requestId</tt>.
     *
     * @return property value of requestId
     */
    @Override
    public String getRequestId() {
        return requestId;
    }

    /**
     * Setter method for property <tt>requestId</tt>.
     *
     * @param requestId value to be assigned to property requestId
     */
    @Override
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    /**
     * Getter method for property <tt>success</tt>.
     *
     * @return property value of success
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Setter method for property <tt>success</tt>.
     *
     * @param success value to be assigned to property success
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Setter method for property <tt>exception</tt>.
     *
     * @param exception value to be assigned to property exception
     */
    public void setException(Exception exception) {
        this.exception = exception;
    }

    /**
     * Getter method for property <tt>exception</tt>.
     *
     * @return property value of exception
     */
    public Exception getException() {
        return exception;
    }
}
