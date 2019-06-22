package es.tododev.sc2.process.socket;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import es.tododev.sc2.process.OutputVerifier;
import es.tododev.sc2.socket.StreamSocketManager;

public class StreamSocketManagerTest {
	
	private final static int PORT = 23456;
	private final static int SOCKETS = 20;
	
	@Test
	public void manager() throws InterruptedException, UnknownHostException, IOException {
		int interactions = 10000;
		int totalAmount = 0;
		OutputVerifier output = new OutputVerifier();
		ExecutorService es = Executors.newFixedThreadPool(1);
		StreamSocketManager mgr = new StreamSocketManager(SOCKETS, PORT, output);
		es.execute(() -> {
			try {
				mgr.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		Socket sockets[] = new Socket[SOCKETS];
		Long timestamps[] = new Long[sockets.length];
		for(int i=0; i<sockets.length; i++) {
			sockets[i] = new Socket(InetAddress.getLocalHost(), PORT);
			waitTillServerSocketIsReady(sockets[i]);
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
		for(int i=0; i<sockets.length; i++) {
			sockets[i].close();
		}
		output.verify(Integer.toString(totalAmount)+".0");
	}
	
	private int getRandomNumberInRange(int min, int max) {
		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}
	
	private void writeInSocket(Socket socket, String message) throws IOException {
		try(ObjectOutputStream  oos = new ObjectOutputStream(socket.getOutputStream());){
			oos.writeObject(message);
		}
		
	}
	
	private String generateXml(long timestamp, String amount) {
		return "<data> <timestamp>"+timestamp+"</timeStamp> <amount>"+amount+".0</amount> </data>\n";
	}
	
	private void waitTillServerSocketIsReady(Socket socket) {
		while(socket.isClosed()) {
		}
	}

}
