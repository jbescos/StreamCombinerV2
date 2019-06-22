package es.tododev.sc2.process;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;

import es.tododev.sc2.common.ErrorCodes;
import es.tododev.sc2.common.StreamCombinerException;

public class ClientInfo implements IClientInfo {

	private final Comparator<Long> comparatorCache;
	private final IStreamProcessor streamProcessor;
	private Dto last;
	
	public ClientInfo(IStreamProcessor streamProcessor, Comparator<Long> comparatorCache) {
		this.streamProcessor = streamProcessor;
		this.comparatorCache = comparatorCache;
	}
	
	/**
	 * Could be invoked by other threads (for example KickPolicy)
	 */
	@Override
	public void add(Dto dto) throws StreamCombinerException {
		if(last == Dto.LAST_TO_SEND) {
			// Avoids to process more if it has been closed
			throw new StreamCombinerException(ErrorCodes.CLOSED);
		} else {
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
			}
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
	
}
