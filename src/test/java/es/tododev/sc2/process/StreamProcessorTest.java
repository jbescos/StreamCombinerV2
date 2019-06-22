package es.tododev.sc2.process;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Test;

import es.tododev.sc2.common.CompareCache;
import es.tododev.sc2.common.StreamCombinerException;

public class StreamProcessorTest {

	private final Comparator<Long> comparatorCache = new CompareCache();
	
	@Test
	public void inSequence() throws StreamCombinerException {
		OutputVerifier output = new OutputVerifier();
		try(IStreamProcessor streamProcessor = new StreamProcessor(comparatorCache, output)){
			try(
				IClientInfo clientInfo1 = new ClientInfo(streamProcessor, comparatorCache);
				IClientInfo clientInfo2 = new ClientInfo(streamProcessor, comparatorCache);
			){
				clientInfo1.add(createDto(1, "10.0"));
				clientInfo2.add(createDto(20, "5.0"));
				clientInfo2.add(createDto(21, "1.0"));
				clientInfo1.add(createDto(23, "10.0"));
				clientInfo2.add(createDto(25, "2.0"));
				clientInfo1.add(createDto(27, "80.0"));
				output.verify("10.0");
			}
		}
		output.verify("108.0");
	}
	
	@Test
	public void repeatingTimestamps() throws StreamCombinerException {
		OutputVerifier output = new OutputVerifier();
		try(IStreamProcessor streamProcessor = new StreamProcessor(comparatorCache, output)){
			try(
				IClientInfo clientInfo1 = new ClientInfo(streamProcessor, comparatorCache);
				IClientInfo clientInfo2 = new ClientInfo(streamProcessor, comparatorCache);
			){
				clientInfo1.add(createDto(1, "10.0"));
				clientInfo1.add(createDto(1, "20.0"));
				clientInfo1.add(createDto(1, "30.0"));
				clientInfo1.add(createDto(1, "10.0"));
				clientInfo1.add(createDto(2, "10.0"));
				clientInfo2.add(createDto(1, "5.0"));
				clientInfo2.add(createDto(1, "1.0"));
				clientInfo2.add(createDto(25, "2.0"));
				clientInfo1.add(createDto(27, "80.0"));
				clientInfo2.add(createDto(26, "2.0"));
				clientInfo1.add(createDto(28, "80.2656"));
				output.verify("76.0");
			}
		}
		output.verify("250.2656");
	}
	
	@Test
	public void bugRepeatedOutput() throws StreamCombinerException {
		OutputVerifier output = new OutputVerifier();
		try(IStreamProcessor streamProcessor = new StreamProcessor(comparatorCache, output)){
			try(
				IClientInfo clientInfo1 = new ClientInfo(streamProcessor, comparatorCache);
				IClientInfo clientInfo2 = new ClientInfo(streamProcessor, comparatorCache);
			){
				clientInfo1.add(createDto(1, "15.0"));
				clientInfo1.add(createDto(1, "63.0"));
				clientInfo2.add(createDto(0, "13.0"));
				clientInfo1.add(createDto(2, "28.0"));
				clientInfo2.add(createDto(1, "58.0"));
				clientInfo1.add(createDto(2, "50.0"));
				clientInfo1.add(createDto(3, "71.0"));
				clientInfo2.add(createDto(2, "15.0"));
				clientInfo2.add(createDto(2, "18.0"));
				clientInfo2.add(createDto(2, "10.0"));
			}
		}
		output.verify("341.0");
	}
	
	@Test
	public void random() throws StreamCombinerException {
		int interactions = 10000;
		int clientsLimit = 100;
		IClientInfo clients[] = new IClientInfo[clientsLimit];
		Long timestamps[] = new Long[clients.length];
		OutputVerifier output = new OutputVerifier();
		int totalAmount = 0;
		try(IStreamProcessor streamProcessor = new StreamProcessor(comparatorCache, output)){
			for(int i=0;i<clients.length;i++) {
				clients[i] = new ClientInfo(streamProcessor, comparatorCache);
				timestamps[i] = 0L;
			}
			for(int i=0;i<interactions;i++) {
				int randomIndex = getRandomNumberInRange(0, clients.length - 1);
				IClientInfo randomClient = clients[randomIndex];
				boolean increaseTimeStamp = getRandomNumberInRange(0, 1) == 0;
				if(increaseTimeStamp) {
					timestamps[randomIndex] = timestamps[randomIndex] + 1;
				}
				int randomAmount = getRandomNumberInRange(0, 100);
				randomClient.add(createDto(timestamps[randomIndex], Integer.toString(randomAmount)));
				totalAmount = totalAmount + randomAmount;
			}
			for(IClientInfo client : clients) {
				client.close();
			}
		}
		output.verify(Integer.toString(totalAmount)+".0");
	}
	
	private Dto createDto(long timestamp, String amount) {
		return new Dto(timestamp, new BigDecimal(amount));
	}
	
	private int getRandomNumberInRange(int min, int max) {
		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}

	
}
