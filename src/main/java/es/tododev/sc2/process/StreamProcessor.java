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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import es.tododev.sc2.common.IOutput;

public class StreamProcessor implements IStreamProcessor {
	
	private final Set<IClientInfo> activeClients = new HashSet<>();
	private final TreeMap<Long, List<Entry<Dto, IClientInfo>>> transactions;
	private final IKickPolicy kickPolicy;
	private final IOutput output;
	private final Executor asyncTask = Executors.newSingleThreadExecutor();
	// Flag to know is asyncTask wants to kick.
	private AtomicBoolean kickIsRunning = new AtomicBoolean(false);
	
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
		} else if (dto == Dto.FIRST_TO_SEND){
			activeClients.add(clientInfo);
		} else {
			addTransaction(dto, clientInfo);
		}
		List<Dto> dtos = getTransactionsToProcess();
		if(!dtos.isEmpty()) {
			output.process(dtos);
		}
		// add solution in case if some input streams hang.
		if(!kickIsRunning.get() && kickPolicy.isKickRequired(transactions)) {
			kick(clientToKick());
		}
	}
	
	private Map<IClientInfo,Integer> createCounterMap(){
		Map<IClientInfo,Integer> totalCounts = new HashMap<>();
		for(IClientInfo client : activeClients) {
			totalCounts.put(client, 0);
		}
		return totalCounts;
	}
	
	private List<Dto> getTransactionsToProcess() {
		List<Dto> transactionsToProcess = new LinkedList<>();
		boolean startProcessing = false;
		Map<IClientInfo,Integer> totalCounts = createCounterMap();
		for(List<Entry<Dto, IClientInfo>> clientInfos : new ArrayList<>(transactions.descendingMap().values())) {
			if(!startProcessing) {
				// Find if it is possible to send to output
				for(Entry<Dto, IClientInfo> info : clientInfos) {
					int count = 0;
					if(activeClients.contains(info.getValue())) {
						count = totalCounts.get(info.getValue());
						count++;
					} else {
						// Client is gone but still have pending transactions.
						count = 2;
					}
					totalCounts.put(info.getValue(), count);
					startProcessing = isAllClientMoreThanOne(totalCounts);
					if(startProcessing) {
						moveToTransactionsToProcess(transactionsToProcess, clientInfos);
						break;
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
	
	
	private IClientInfo clientToKick() {
		Set<IClientInfo> clients = new HashSet<>(activeClients);
		for(List<Entry<Dto, IClientInfo>> clientInfos : transactions.descendingMap().values()) {
			for(Entry<Dto, IClientInfo> info : clientInfos) {
				clients.remove(info.getValue());
				if(clients.size() == 1) {
					return clients.iterator().next();
				}
			}
		}
		return null;
	}
	
	private void kick(IClientInfo client) {
		if(client != null) {
			kickIsRunning.set(true);
			// Avoid deadlocks
			asyncTask.execute(() -> {
				client.close();
				kickIsRunning.set(false);
			});
		}
	}

	@Override
	public synchronized int pendingTransactions() {
		return transactions.size();
	}
	

}
