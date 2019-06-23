package es.tododev.sc2.process;

public interface IStreamProcessor {

	void push(Dto dto, IClientInfo clientInfo);
	int pendingTransactions();
	
}
