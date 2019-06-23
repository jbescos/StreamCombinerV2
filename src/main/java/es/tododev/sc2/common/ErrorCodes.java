package es.tododev.sc2.common;

public enum ErrorCodes {

	OBSOLETE("ERROR-001", "The new timestamp is older than last one"),
	CLOSED("ERROR-002", "This client has been closed"),
	DESERIALIZE_OUTPUT("ERROR-003", "Error deserializing output"),
	DESERIALIZE_INPUT("ERROR-004", "Error serializing input"),
	LIMIT_OF_CONNECTIONS("ERROR-005", "Reached the limit of connections. Try it later.");
	
	private final String message;
	private final String code;
	
	private ErrorCodes(String code, String message) {
		this.code = code;
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public String getCode() {
		return code;
	}
	
}
