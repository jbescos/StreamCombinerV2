package es.tododev.sc2.process;

public interface IStreamProcessor extends AutoCloseable {

	void push(Dto dto, IClientInfo clientInfo);
	@Override
	void close();
	
}
