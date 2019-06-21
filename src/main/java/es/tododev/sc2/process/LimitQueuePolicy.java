package es.tododev.sc2.process;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class LimitQueuePolicy implements IKickPolicy {
	
	private final int limitSize;

	public LimitQueuePolicy(int limitSize) {
		this.limitSize = limitSize;
	}
	
	public LimitQueuePolicy() {
		this(10000);
	}



	@Override
	public boolean isKickRequired(Map<Long, List<Entry<Dto, IClientInfo>>> transactions) {
		return transactions.size() > limitSize;
	}

}
