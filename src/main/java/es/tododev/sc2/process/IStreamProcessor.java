package es.tododev.sc2.process;

public interface IStreamProcessor {

	void notify(Dto dto, IClientInfo clientInfo);
	void close();
	
}
