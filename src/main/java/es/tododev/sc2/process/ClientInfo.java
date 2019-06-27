package es.tododev.sc2.process;

import java.util.Comparator;

import es.tododev.sc2.common.ErrorCodes;
import es.tododev.sc2.common.StreamCombinerException;

public class ClientInfo implements IClientInfo {

	private final Comparator<Long> comparatorCache;
	private final IStreamProcessor streamProcessor;
	private Dto[] buffer;
	private boolean registered = false;
	private long lastValidTimestamp = -1;
	private int idx = -1;
	
	public ClientInfo(IStreamProcessor streamProcessor, Comparator<Long> comparatorCache) {
		this(streamProcessor, comparatorCache, 50);
	}
	
	public ClientInfo(IStreamProcessor streamProcessor, Comparator<Long> comparatorCache, int bufferSize) {
		if(bufferSize < 2) {
			throw new IllegalArgumentException("Minimun buffer size is 2");
		}
		this.streamProcessor = streamProcessor;
		this.comparatorCache = comparatorCache;
		this.buffer = new Dto[bufferSize];
		register();
	}
	
	private boolean register() {
		boolean canRepeatTimestamp;
		if(!registered) {
			streamProcessor.register(this);
			lastValidTimestamp = streamProcessor.getLastProcessedTimestamp();
			registered = true;
			canRepeatTimestamp = false;
		} else {
		    if(idx == -1) {
		        // First time the client sends a transaction, we force to not repeat
		        canRepeatTimestamp = false;
		    } else {
		        canRepeatTimestamp = true;
		    }
		}
		return canRepeatTimestamp;
	}
	
	@Override
	public synchronized void add(Dto dto) throws StreamCombinerException {
		boolean canRepeatTimestamp = register();
		int compareResult = comparatorCache.compare(lastValidTimestamp, dto.getTimestamp());
		if(compareResult == 1) {
			throw new StreamCombinerException(ErrorCodes.OBSOLETE, "Last valid timestamp was "+lastValidTimestamp+" but new transaciton has "+dto.getTimestamp());
		}else if(compareResult == 0) {
			if(!canRepeatTimestamp) {
				throw new StreamCombinerException(ErrorCodes.OBSOLETE, "Last valid timestamp was "+lastValidTimestamp+" but new transaciton has "+dto.getTimestamp());
			} else {
				Dto inBuffer = buffer[idx];
				inBuffer.setAmount(inBuffer.getAmount().add(dto.getAmount()));
			}
		} else {
			idx++;
			if(idx == buffer.length) {
				int lastIdx = idx - 1;
				Dto last = buffer[lastIdx];
				buffer[lastIdx] = null;
				sendToProcessor();
				buffer[0] = last;
				idx = 1;
			}
			buffer[idx] = dto;
			lastValidTimestamp = dto.getTimestamp();
		}
	}
	
	private void sendToProcessor() {
		streamProcessor.push(this, buffer);
		int size = buffer.length;
		buffer = new Dto[size];
	}

	@Override
	public synchronized void close() {
		if(registered) {
			sendToProcessor();
			streamProcessor.unregister(this);
			registered = false;
			idx = -1;
			streamProcessor.push(this, new Dto[0]);
		}
	}

	@Override
	public String toString() {
		return "Client: "+super.toString().split("@")[1];
	}
	
	
	
}
