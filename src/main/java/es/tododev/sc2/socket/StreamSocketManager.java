package es.tododev.sc2.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import es.tododev.sc2.common.CompareCache;
import es.tododev.sc2.common.ConsoleJsonOutput;
import es.tododev.sc2.common.IInput;
import es.tododev.sc2.common.IOutput;
import es.tododev.sc2.common.StreamCombinerException;
import es.tododev.sc2.common.XmlInput;
import es.tododev.sc2.process.ClientInfo;
import es.tododev.sc2.process.Dto;
import es.tododev.sc2.process.IClientInfo;
import es.tododev.sc2.process.IStreamProcessor;
import es.tododev.sc2.process.StreamProcessor;

public class StreamSocketManager {

	private final int nSockets;
	private final int port;
	private final Comparator<Long> comparatorCache = new CompareCache();
	private final Set<Socket> openSockets = Collections.synchronizedSet(new HashSet<>());
	public final static String SHUTDOWN_COMMAND = "SHUTDOWN";
	private final IOutput output = new ConsoleJsonOutput();
	private final IInput<Dto> input = new XmlInput();
	private final ExecutorService service;
	
	public StreamSocketManager(int nSockets, int port) {
		this.nSockets = nSockets;
		this.port = port;
		this.service = Executors.newFixedThreadPool(nSockets);
	}
	
	public void start() throws IOException {
		boolean firstConnection = true;
		try(ServerSocket serverSocket = new ServerSocket(port);
			IStreamProcessor streamProcessor = new StreamProcessor(comparatorCache, output);){
			/*
			 * It will accept new connections the first time always. Additionally meanwhile there is active sockets.
			 * This means that to stop it, you need to connect at least once and disconnect all the sockets.
			 */
			while(firstConnection || !openSockets.isEmpty()) {
				firstConnection = false;
				if (openSockets.size() < nSockets) {
					Socket socket = serverSocket.accept();
					openSockets.add(socket);
					writeInSocket(socket, "Connected: "+socket);
					service.execute(() -> handleRequest(socket, streamProcessor));
				}
			}
			service.shutdown();
			
		}
	}
	
	private void handleRequest(Socket socket, IStreamProcessor streamProcessor) {
		try(IClientInfo clientInfo = new ClientInfo(streamProcessor, comparatorCache)){
	    	BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		    String message = null;
			while ((message = reader.readLine()) != null) {
				try {
					Dto dto = input.toObject(message);
					clientInfo.add(dto);
				} catch (StreamCombinerException e) {
					writeInSocket(socket, e.getMessage() + ". " + message);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		closeSocket(socket);
	}
	
	private void writeInSocket(Socket socket, String message) {
		try{
			PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
			writer.write(message+"\n");
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
			closeSocket(socket);
		}
	}
	
	private void closeSocket(Socket socket) {
		try {
			socket.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		openSockets.remove(socket);
	}
	
	
			
	
}
