package es.tododev.sc2.process;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@FunctionalInterface
public interface IKickPolicy {

	boolean isKickRequired(Map<Long, List<Entry<Dto, IClientInfo>>> transactions);
	
}
