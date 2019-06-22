package es.tododev.sc2.common;

public interface IInput<T> {
	
	public static final String DATA = "data";

	T toObject(String input) throws StreamCombinerException;
	
}
