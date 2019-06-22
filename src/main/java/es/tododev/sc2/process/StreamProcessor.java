package es.tododev.sc2.process;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class StreamProcessor implements IStreamProcessor {
	
	private final Map<IClientInfo, Thread> activeClients = new HashMap<>();
	private final TreeMap<Long, List<Entry<Dto, IClientInfo>>> transactions;
	private final IKickPolicy kickPolicy;
	private final IOutput output;
	
	public StreamProcessor(Comparator<Long> comparatorCache, IKickPolicy kickPolicy, IOutput output) {
		this.kickPolicy = kickPolicy;
		this.transactions = new TreeMap<>(comparatorCache);
		this.output = output;
	}
	
	
	public StreamProcessor(Comparator<Long> comparatorCache, IOutput output) {
		this(comparatorCache, new LimitQueuePolicy(), output);
	}

	@Override
	public synchronized void push(Dto dto, IClientInfo clientInfo) {
		if(dto == Dto.LAST_TO_SEND) {
			activeClients.remove(clientInfo);
		} else {
			if (!activeClients.containsKey(clientInfo)) {
				activeClients.put(clientInfo, Thread.currentThread());
			}
			addTransaction(dto, clientInfo);
		}
		List<Dto> dtos = getTransactionsToProcess();
		output.process(dtos);
		
		if(kickPolicy.isKickRequired(transactions)) {
			kick(clientToKick().iterator().next());
		}
	}
	
	private List<Dto> getTransactionsToProcess() {
		List<Dto> transactionsToProcess = new LinkedList<>();
		boolean startProcessing = false;
		Map<IClientInfo,Integer> totalCounts = new HashMap<>();
		Set<IClientInfo> activeClientsSet = activeClients.keySet();
		for(List<Entry<Dto, IClientInfo>> clientInfos : new ArrayList<>(transactions.descendingMap().values())) {
			if(!startProcessing) {
				// Find if it is possible to send to output
				for(Entry<Dto, IClientInfo> info : clientInfos) {
					if(activeClientsSet.contains(info.getValue())) {
						Integer count = totalCounts.get(info.getValue());
						if(count == null) {
							count = 0;
						}
						count++;
						totalCounts.put(info.getValue(), count);
					}
					startProcessing = isAllClientMoreThanOne(totalCounts);
					if(startProcessing) {
						moveToTransactionsToProcess(transactionsToProcess, clientInfos);
					}
				}
			} else {
				// Prepares the output 
				moveToTransactionsToProcess(transactionsToProcess, clientInfos);
			}
		}
		Collections.reverse(transactionsToProcess);
		return transactionsToProcess;
	}
	
	private void moveToTransactionsToProcess(List<Dto> transactionsToProcess, List<Entry<Dto, IClientInfo>> clientInfos) {
		// Prepares the output 
		Dto combined = null;
		for(Entry<Dto, IClientInfo> info : clientInfos) {
			if(combined == null) {
				combined = info.getKey();
			} else {
				BigDecimal combinedAmount = info.getKey().getAmount().add(combined.getAmount());
				combined.setAmount(combinedAmount);
			}
		}
		transactions.remove(combined.getTimestamp());
		transactionsToProcess.add(combined);
	}
	
	private boolean isAllClientMoreThanOne(Map<IClientInfo,Integer> totalCounts) {
		for(Integer count : totalCounts.values()) {
			if(count < 2) {
				return false;
			}
		}
		return true;
	}
	
	private void addTransaction(Dto dto, IClientInfo clientInfo) {
		List<Entry<Dto, IClientInfo>> transaction = transactions.get(dto.getTimestamp());
		if(transaction == null) {
			transaction = new ArrayList<>();
			transactions.put(dto.getTimestamp(), transaction);
		}
		transaction.add(new AbstractMap.SimpleEntry<>(dto, clientInfo));
	}
	
	
	private Set<IClientInfo> clientToKick() {
		Set<IClientInfo> clients = new HashSet<>(activeClients.keySet());
		for(List<Entry<Dto, IClientInfo>> clientInfos : transactions.descendingMap().values()) {
			for(Entry<Dto, IClientInfo> info : clientInfos) {
				clients.remove(info.getValue());
				if(clients.size() == 1) {
					return clients;
				}
			}
		}
		return clients;
	}
	
	private void kick(IClientInfo client) {
		activeClients.get(client).interrupt();
	}

	@Override
	public void close() {
		for(IClientInfo client : new HashSet<>(activeClients.keySet())) {
			kick(client);
		}
	}
	

}
