package es.tododev.sc2.process;

import java.util.Map;

public interface IKickPolicy {

	boolean isKickRequired(Map<Dto, IClientInfo> queue);
	
}
