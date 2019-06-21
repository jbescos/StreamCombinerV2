package es.tododev.sc2.process;

import es.tododev.sc2.common.StreamCombinerException;

public interface IClientInfo {

	void add(Dto dto) throws StreamCombinerException;
	void close();
	void start();
	
}
