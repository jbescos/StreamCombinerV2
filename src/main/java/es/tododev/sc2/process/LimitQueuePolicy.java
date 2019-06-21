package es.tododev.sc2.process;

import java.util.Map;

public class LimitQueuePolicy implements IKickPolicy {
	
	private final int limitSize;

	public LimitQueuePolicy(int limitSize) {
		this.limitSize = limitSize;
	}
	
	public LimitQueuePolicy() {
		this(10000);
	}



	@Override
	public boolean isKickRequired(Map<Dto, IClientInfo> queue) {
		return queue.size() > limitSize;
	}

}
