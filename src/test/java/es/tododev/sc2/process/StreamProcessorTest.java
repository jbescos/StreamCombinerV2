package es.tododev.sc2.process;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import es.tododev.sc2.common.CompareCache;
import es.tododev.sc2.common.ErrorCodes;
import es.tododev.sc2.common.StreamCombinerException;

public class StreamProcessorTest {

	private final Comparator<Long> comparatorCache = new CompareCache();
	
	@Test
	public void inSequence() throws StreamCombinerException {
		OutputVerifier output = new OutputVerifier();
		try(IStreamProcessor streamProcessor = new StreamProcessor(comparatorCache, output);
			IClientInfo clientInfo1 = new ClientInfo(streamProcessor, comparatorCache);
			IClientInfo clientInfo2 = new ClientInfo(streamProcessor, comparatorCache);
		){
			clientInfo1.add(createDto(1, "10.0"));
			clientInfo2.add(createDto(20, "5.0"));
			clientInfo2.add(createDto(21, "1.0"));
			clientInfo1.add(createDto(23, "10.0"));
			clientInfo2.add(createDto(25, "2.0"));
			clientInfo1.add(createDto(27, "-80.0"));
			output.verify("10.0");
		}
		output.verify("-52.0");
	}
	
	@Test
	public void repeatingTimestamps() throws StreamCombinerException {
		OutputVerifier output = new OutputVerifier();
		
		try(
			IStreamProcessor streamProcessor = new StreamProcessor(comparatorCache, output);
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
		output.verify("250.2656");
	}
	
	@Test
	public void bugRepeatedOutput() throws StreamCombinerException {
		OutputVerifier output = new OutputVerifier();
		try(
			IStreamProcessor streamProcessor = new StreamProcessor(comparatorCache, output);
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
		IStreamProcessor streamProcessor = new StreamProcessor(comparatorCache, output);
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
		streamProcessor.close();
		output.verify(Integer.toString(totalAmount)+".0");
	}
	
	@Test
	public void concurrentRandom() throws InterruptedException {
		int clientsLimit = 100;
		ExecutorService es = Executors.newFixedThreadPool(clientsLimit);
		OutputVerifier output = new OutputVerifier();
		CountDownLatch waitTillAllPrepared = new CountDownLatch(1);
		CountDownLatch waitExecution = new CountDownLatch(clientsLimit);
		AtomicInteger totalAmount = new AtomicInteger(0);
		IStreamProcessor streamProcessor = new StreamProcessor(comparatorCache, output);
		for(int i=0;i<clientsLimit;i++) {
			es.execute(new ClientThread(streamProcessor, waitTillAllPrepared, waitExecution, totalAmount));
		}
		waitTillAllPrepared.countDown();
		waitExecution.await();
		streamProcessor.close();
		output.verify(Integer.toString(totalAmount.get())+".0");
	}
	
	@Test
	public void connectAndDisconnectOnDifferentMoments() throws StreamCombinerException {
		OutputVerifier output = new OutputVerifier();
		IStreamProcessor streamProcessor = new StreamProcessor(comparatorCache, output);
		try(IClientInfo clientInfo1 = new ClientInfo(streamProcessor, comparatorCache)){
			clientInfo1.add(createDto(1, "1.0"));
			clientInfo1.add(createDto(2, "2.0"));
			clientInfo1.add(createDto(3, "3.0"));
			output.verify(Integer.toString(1)+".0");
			try(IClientInfo clientInfo2 = new ClientInfo(streamProcessor, comparatorCache)){
				try {
					clientInfo2.add(createDto(1, "2.0"));
					fail("It is old");
				}catch(StreamCombinerException e) {
					assertEquals(ErrorCodes.OBSOLETE.getCode() + ": " + ErrorCodes.OBSOLETE.getMessage(), e.getMessage());
				}
				clientInfo2.add(createDto(2, "2.0"));
			}
			clientInfo1.add(createDto(3, "3.0"));
			clientInfo1.add(createDto(4, "1.0"));
		}
		streamProcessor.close();
		output.verify(Integer.toString(12)+".0");
	}
	
	@Test
	public void kick() throws StreamCombinerException, InterruptedException {
		int kickLimitSize = 2;
		OutputVerifier output = new OutputVerifier();
		
		try(
				IStreamProcessor streamProcessor = new StreamProcessor(comparatorCache, transactions -> transactions.size() == kickLimitSize, output);
				IClientInfo clientInfo1 = new ClientInfo(streamProcessor, comparatorCache);
				IClientInfo clientInfo2 = new ClientInfo(streamProcessor, comparatorCache);
			){
			clientInfo2.add(createDto(0, "10.0"));
			clientInfo1.add(createDto(1, "2.0"));
			clientInfo1.add(createDto(2, "3.0"));
			clientInfo1.add(createDto(3, "4.0"));
			// Kick process is running to kick clientInfo2
			Thread.sleep(2000L);
			output.verify(Integer.toString(12)+".0");
			// But he can still send new data if the timestamp is valid
			try {
				System.out.println("Add wrong");
				clientInfo2.add(createDto(1, "-100.0"));
				fail("Timestamp is expired");
			}catch(StreamCombinerException e) {
				assertEquals(ErrorCodes.OBSOLETE.getCode() + ": " + ErrorCodes.OBSOLETE.getMessage(), e.getMessage());
			}
			clientInfo2.add(createDto(8, "8.0"));
			clientInfo1.add(createDto(9, "2.0"));
			clientInfo2.add(createDto(8, "1.0"));
			output.verify(Integer.toString(12)+".0");
			
		}
		output.verify(Integer.toString(30)+".0");
		
	}
	
	private Dto createDto(long timestamp, String amount) {
		return new Dto(timestamp, new BigDecimal(amount));
	}
	
	private int getRandomNumberInRange(int min, int max) {
		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}
	
	private class ClientThread implements Runnable {

		private final IClientInfo clients;
		private final CountDownLatch waitTillAllPrepared;
		private final CountDownLatch waitExecution;
		private final AtomicInteger totalAmount;
		private final int iterations = 10000;
		
		public ClientThread(IStreamProcessor streamProcessor, CountDownLatch waitTillAllPrepared, CountDownLatch waitExecution, AtomicInteger totalAmount) {
			this.clients = new ClientInfo(streamProcessor, comparatorCache);
			this.waitTillAllPrepared = waitTillAllPrepared;
			this.waitExecution = waitExecution;
			this.totalAmount = totalAmount;
		}

		@Override
		public void run() {
			// +1 or -1 second to emulate different computers time stamp
			int randomDelay = getRandomNumberInRange(-1000, 1000);
			try {
				waitTillAllPrepared.await();
				for(int i=0; i<iterations;i++) {
					long timestamp = System.currentTimeMillis() + randomDelay;
					int randomAmount = getRandomNumberInRange(0, 100);
					totalAmount.addAndGet(randomAmount);
					Dto dto = createDto(timestamp, Integer.toString(randomAmount));
					clients.add(dto);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (StreamCombinerException e) {
				e.printStackTrace();
			} finally {
				clients.close();
				waitExecution.countDown();
			}
			
		}
		
	}

	
}
