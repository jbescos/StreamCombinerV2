package es.tododev.sc2.process;

import es.tododev.sc2.common.StreamCombinerException;

public interface IClientInfo extends AutoCloseable {

	void add(Dto dto) throws StreamCombinerException;
	@Override
	void close();
	
}
