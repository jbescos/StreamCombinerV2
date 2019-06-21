package es.tododev.sc2.process;

import java.math.BigDecimal;
import java.util.Comparator;

import org.junit.Test;

import es.tododev.sc2.common.CompareCache;
import es.tododev.sc2.common.StreamCombinerException;

public class StreamProcessorTest {

	private final Comparator<Long> comparatorCache = new CompareCache();
	
	@Test
	public void inSequence() throws StreamCombinerException {
		OutputList output = new OutputList();
		try(IStreamProcessor streamProcessor = new StreamProcessor(comparatorCache, output)){
			IClientInfo clientInfo1 = new ClientInfo(streamProcessor, comparatorCache);
			IClientInfo clientInfo2 = new ClientInfo(streamProcessor, comparatorCache);
			clientInfo1.start();
			clientInfo2.start();
			clientInfo1.add(createDto(1, "10"));
			output.verify("0");
			clientInfo2.add(createDto(20, "5"));
			output.verify("0");
			clientInfo2.add(createDto(21, "1"));
			output.verify("0");
			clientInfo1.add(createDto(23, "10"));
			output.verify("15");
		}
		output.verify("26");
	}
	
	private Dto createDto(long timestamp, String amount) {
		return new Dto(timestamp, new BigDecimal(amount));
	}
	
}
