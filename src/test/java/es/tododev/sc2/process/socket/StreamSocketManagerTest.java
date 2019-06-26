package es.tododev.sc2.process.socket;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.Executors;

import org.junit.Test;

import es.tododev.sc2.process.OutputVerifier;
import es.tododev.sc2.socket.StreamSocketManager;

public class StreamSocketManagerTest {
	
	private final static int PORT = 23456;
	private final static int SOCKETS = 20;
	private final static long WAIT_TIME = 3000L;
	
	@Test
	public void socketsExample() throws InterruptedException, UnknownHostException, IOException {
		int interactions = 1000;
		int totalAmount = 0;
		OutputVerifier output = new OutputVerifier();
		StreamSocketManager mgr = new StreamSocketManager(SOCKETS, PORT, output);
		Executors.newSingleThreadExecutor().execute(() -> mgr.start());
		Socket sockets[] = new Socket[SOCKETS];
		Long timestamps[] = new Long[sockets.length];
		for(int i=0; i<sockets.length; i++) {
			sockets[i] = new Socket(InetAddress.getLocalHost(), PORT);
			timestamps[i] = 0L;
		}
		for(int i=0;i<interactions;i++) {
			int randomIndex = getRandomNumberInRange(0, sockets.length - 1);
			Socket socket = sockets[randomIndex];
			boolean increaseTimeStamp = getRandomNumberInRange(0, 1) == 0;
			if(increaseTimeStamp) {
				timestamps[randomIndex] = timestamps[randomIndex] + 1;
			}
			int randomAmount = getRandomNumberInRange(0, 100);
			String xml = generateXml(timestamps[randomIndex], Integer.toString(randomAmount));
			writeInSocket(socket, xml);
			totalAmount = totalAmount + randomAmount;
		}
		// Give some time to the sockets to send before switching off
		Thread.sleep(WAIT_TIME);
		switchOff();
		// Give sometime to the StreamProcessor to finish before doing the check
		Thread.sleep(WAIT_TIME);
		output.verify(Integer.toString(totalAmount)+".0");
		
		
	}
	
	@Test
	public void reachLimitOfSockets() throws UnknownHostException, IOException, InterruptedException {
		int sockets = 1;
		OutputVerifier output = new OutputVerifier();
		StreamSocketManager mgr = new StreamSocketManager(sockets, PORT, output);
		Executors.newSingleThreadExecutor().execute(() -> mgr.start());
		Socket socket1 = new Socket(InetAddress.getLocalHost(), PORT);
		writeInSocket(socket1, generateXml(0, Integer.toString(1)));
		Socket socket2 = new Socket(InetAddress.getLocalHost(), PORT);
		// This will not be processed
		writeInSocket(socket2, generateXml(0, Integer.toString(1)));
		Thread.sleep(WAIT_TIME);
		switchOff();
		Thread.sleep(WAIT_TIME);
		output.verify(Integer.toString(1)+".0");
	}
	
	private void switchOff() throws UnknownHostException, IOException {
		Socket socket = new Socket(InetAddress.getLocalHost(), StreamSocketManager.PORT_TO_SWITH_OFF);
	}
	
	private int getRandomNumberInRange(int min, int max) {
		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}
	
	private void writeInSocket(Socket socket, String message) throws IOException {
		PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
		System.out.println("Sent message: "+message);
		writer.write(message+"\n");
		writer.flush();
	}
	
	private String generateXml(long timestamp, String amount) {
		String xml = "<data> <timestamp>"+timestamp+"</timestamp> <amount>"+amount+".0</amount> </data>";
		return xml;
	}

}
