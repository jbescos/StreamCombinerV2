package es.tododev.sc2.process;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class StreamProcessor implements IStreamProcessor {
	
	private final Set<IClientInfo> activeClients = new HashSet<>();
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
		} else if (dto == Dto.FIRST_TO_SEND) {
			activeClients.add(clientInfo);
		} else {
			addTransaction(dto, clientInfo);
		}
		List<Dto> dtos = getTransactionsToProcess();
		output.process(dtos);
		
		if(kickPolicy.isKickRequired(transactions)) {
			kick();
		}
	}
	
	private List<Dto> getTransactionsToProcess() {
		List<Dto> transactionsToProcess = new LinkedList<>();
		boolean startProcessing = false;
		Set<IClientInfo> processedClients = new HashSet<>();
		for(List<Entry<Dto, IClientInfo>> clientInfos : transactions.descendingMap().values()) {
			if(!startProcessing) {
				for(Entry<Dto, IClientInfo> info : clientInfos) {
					if(activeClients.contains(info.getValue())) {
						if(processedClients.contains(info.getValue())) {
							startProcessing = true;
						} else {
							processedClients.add(info.getValue());
						}
					}
				}
			} else {
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
		}
		Collections.reverse(transactionsToProcess);
		return transactionsToProcess;
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
		Set<IClientInfo> clients = new HashSet<>(activeClients);
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
	
	/**
	 * Kicks the client who pushed the last
	 */
	private void kick() {
		clientToKick().iterator().next().close();
	}

	@Override
	public void close() {
		new ArrayList<IClientInfo>(activeClients).stream().forEach(client -> client.close());
	}
	

}
