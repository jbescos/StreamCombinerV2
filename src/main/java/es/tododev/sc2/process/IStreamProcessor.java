package es.tododev.sc2.process;

import es.tododev.sc2.common.StreamCombinerException;

public interface IStreamProcessor {

	void push(Dto dto, IClientInfo clientInfo) throws StreamCombinerException;
	void register(IClientInfo clientInfo);
	void unregister(IClientInfo clientInfo);
	
}
