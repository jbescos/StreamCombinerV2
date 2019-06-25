package es.tododev.sc2.process;

import java.math.BigDecimal;
import java.util.Comparator;

import es.tododev.sc2.common.ErrorCodes;
import es.tododev.sc2.common.StreamCombinerException;

public class ClientInfo implements IClientInfo {

	private final Comparator<Long> comparatorCache;
	private final IStreamProcessor streamProcessor;
	// Keeps the value till a higher timestamp is coming
	private Dto last = Dto.FIRST_TO_SEND;
	
	public ClientInfo(IStreamProcessor streamProcessor, Comparator<Long> comparatorCache) {
		this.streamProcessor = streamProcessor;
		this.comparatorCache = comparatorCache;
		try {
			this.streamProcessor.push(Dto.FIRST_TO_SEND, this);
		} catch (StreamCombinerException e) {
			// Cannot happen
		}
	}
	
	/**
	 * Could be invoked by other threads (for example KickPolicy)
	 */
	@Override
	public synchronized void add(Dto dto) throws StreamCombinerException {
		if(last == Dto.FIRST_TO_SEND) {
			// Client reconnected, we need to check immediately the last processed time stamp
			int compareResult = comparatorCache.compare(streamProcessor.getLastProcessedTimestamp(), dto.getTimestamp());
			if(compareResult != -1) {
				throw new StreamCombinerException(ErrorCodes.OBSOLETE);
			}
		}
		if(last != null) {
			int compareResult = comparatorCache.compare(last.getTimestamp(), dto.getTimestamp());
			if(compareResult == 0) {
				BigDecimal total = last.getAmount().add(dto.getAmount());
				dto.setAmount(total);
			} else if(compareResult < 0) {
				streamProcessor.push(last, this);
			} else {
				throw new StreamCombinerException(ErrorCodes.OBSOLETE);
			}
		}
		if(dto == Dto.LAST_TO_SEND) {
			streamProcessor.push(Dto.LAST_TO_SEND, this);
			last = Dto.FIRST_TO_SEND;
		} else {
			last = dto;
		}
	}

	@Override
	public void close() {
		try {
			add(Dto.LAST_TO_SEND);
		} catch (StreamCombinerException e) {
			// Cannot happen
		}
	}

	@Override
	public String toString() {
		return "Client: "+super.toString().split("@")[1];
	}
	
	
	
}
