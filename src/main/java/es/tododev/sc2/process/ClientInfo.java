package es.tododev.sc2.process;

import es.tododev.sc2.common.StreamCombinerException;

public class ClientInfo implements IClientInfo {

	private final IStreamProcessor streamProcessor;
	
	public ClientInfo(IStreamProcessor streamProcessor) {
		this.streamProcessor = streamProcessor;
		streamProcessor.register(this);
	}
	
	/**
	 * Could be invoked by other threads (for example KickPolicy)
	 * @throws StreamCombinerException 
	 */
	@Override
	public void add(Dto dto) throws StreamCombinerException {
		streamProcessor.push(dto, this);
	}

	@Override
	public void close() {
		streamProcessor.unregister(this);
	}

	@Override
	public String toString() {
		return "Client: "+super.toString().split("@")[1];
	}
	
}
