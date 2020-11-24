package com.epayslip.interview.vo;

public class RestResult {
	public static final String COMMON_ERROR_MESSAGE = "System Error, Please Contact Our Support.";
	private int returnCode;
	private String message;
	private Object result;

	public static RestResult success() {
		return success(1, null);
	}

	public static RestResult success(Object result) {
		return success(1, result);
	}

	public static RestResult success(int returnCode, String message, Object result) {
		RestResult restResult = new RestResult();
		restResult.setReturnCode(returnCode);
		restResult.setResult(result);
		restResult.setMessage(message);
		return restResult;
	}

	public static RestResult success(String message, Object result) {
		return success(1, message, result);
	}

	public static RestResult success(int returnCode, Object result) {
		return success(returnCode, null, result);
	}

	public static RestResult fail() {
		return fail(0, COMMON_ERROR_MESSAGE);
	}

	public static RestResult fail(String message) {
		return fail(0, message);
	}

	public static RestResult fail(Object result) {
		return fail(0, result);
	}

	public static RestResult fail(int returnCode, Object result) {
		RestResult restResult = new RestResult();
		restResult.setReturnCode(returnCode);
		restResult.setResult(result);
		return restResult;
	}

	public static RestResult fail(int returnCode, String message) {
		RestResult restResult = new RestResult();
		restResult.setReturnCode(returnCode);
		restResult.setMessage(message);
		return restResult;
	}

	public static RestResult fail(int returnCode, String message, Object result) {
		RestResult restResult = new RestResult();
		restResult.setReturnCode(returnCode);
		restResult.setMessage(message);
		restResult.setResult(result);
		return restResult;
	}

	public int getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(int returnCode) {
		this.returnCode = returnCode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public boolean isSuccess() {
		return returnCode == 1;
	}

	@Override
	public String toString() {
		return "RestResult [returnCode=" + returnCode + ", message=" + message + ", result=" + result + "]";
	}

}
