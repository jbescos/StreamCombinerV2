package es.tododev.sc2.common;

public class StreamCombinerException extends Exception {

	private static final long serialVersionUID = 1L;

	public StreamCombinerException(ErrorCodes arg0, Throwable arg1) {
		super(getError(arg0), arg1);
	}

	public StreamCombinerException(ErrorCodes arg0) {
		super(getError(arg0));
	}
	
	private static String getError(ErrorCodes errorCode) {
		return errorCode.getCode() + ": " + errorCode.getMessage();
	}

}
