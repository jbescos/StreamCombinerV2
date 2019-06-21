package es.tododev.sc2.process;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import es.tododev.sc2.common.CompareCache;

import java.util.Set;
import java.util.TreeMap;

public class StreamProcessor implements IStreamProcessor {
	
	private final Set<IClientInfo> activeClients = new HashSet<>();
	private final TreeMap<Dto, IClientInfo> transactions;
	private final IKickPolicy kickPolicy;
	
	public StreamProcessor(Comparator<Long> comparatorCache, IKickPolicy kickPolicy) {
		this.kickPolicy = kickPolicy;
		this.transactions = new TreeMap<>((var1, var2) -> comparatorCache.compare(var1.getTimestamp(), var2.getTimestamp()));
	}
	
	public StreamProcessor(IKickPolicy kickPolicy) {
		this(new CompareCache(), kickPolicy);
	}
	
	public StreamProcessor() {
		this(new CompareCache(), new LimitQueuePolicy());
	}

	@Override
	public synchronized void notify(Dto dto, IClientInfo clientInfo) {
		if(dto == Dto.LAST_TO_SEND) {
			activeClients.remove(clientInfo);
		} else if (dto == Dto.FIRST_TO_SEND) {
			activeClients.add(clientInfo);
		} else {
			transactions.put(dto, clientInfo);
		}
		// TODO pull
		
		if(kickPolicy.isKickRequired(transactions)) {
			kick();
		}
	}
	
	/**
	 * Kicks the client who pushed the last
	 */
	private void kick() {
		Set<IClientInfo> clients = new HashSet<>(activeClients);
		for(IClientInfo clientInfo : transactions.descendingMap().values()) {
			clients.remove(clientInfo);
			if(clients.size() == 1) {
				break;
			}
		}
		clients.iterator().next().close();
	}

	@Override
	public void close() {
		activeClients.stream().forEach(client -> client.close());
	}
	

}
