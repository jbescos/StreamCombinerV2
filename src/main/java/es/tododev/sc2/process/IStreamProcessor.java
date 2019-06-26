package es.tododev.sc2.process;

public interface IStreamProcessor extends AutoCloseable {

	void push(IClientInfo clientInfo, Dto ... dto);
	void register(IClientInfo clientInfo);
	void unregister(IClientInfo clientInfo);
	long getLastProcessedTimestamp();
	@Override
 	void close();
	
}
