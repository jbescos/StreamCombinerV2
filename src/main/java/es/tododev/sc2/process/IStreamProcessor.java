package es.tododev.sc2.process;

import es.tododev.sc2.common.StreamCombinerException;

public interface IStreamProcessor extends AutoCloseable {

	void push(Dto dto, IClientInfo clientInfo) throws StreamCombinerException;
	@Override
 	void close();
	long getLastProcessedTimestamp();
	
}
