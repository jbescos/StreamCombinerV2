package es.tododev.sc2.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import es.tododev.sc2.common.CompareCache;
import es.tododev.sc2.common.ConsoleJsonOutput;
import es.tododev.sc2.common.ErrorCodes;
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

	public final static int PORT_TO_SWITH_OFF = 40356;
	private final int nSockets;
	private final int port;
	// This is because of the bonus: "imagine that timestamps comparing operation is VERY expensive - try to minimize it's usage."
	private final Comparator<Long> comparatorCache = new CompareCache();
	private final IOutput output;
	private final IInput<Dto> input = new XmlInput();
	private final Set<Socket> openSockets = Collections.synchronizedSet(new HashSet<>());
	
	public StreamSocketManager(int nSockets, int port) {
		this(nSockets, port, new ConsoleJsonOutput());
	}
	
	public StreamSocketManager(int nSockets, int port, IOutput output) {
		this.nSockets = nSockets;
		this.port = port;
		this.output = output;
	}
	
	private void switchOffListener(ServerSocket serverSocket) {
		try(ServerSocket switchOffSocket = new ServerSocket(PORT_TO_SWITH_OFF)){
			Socket socket = switchOffSocket.accept();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			stop(serverSocket);
		}
	}
	
	public void start() {
		ExecutorService pool = Executors.newFixedThreadPool(nSockets);
		IStreamProcessor streamProcessor = new StreamProcessor(comparatorCache, output);
		try(ServerSocket serverSocket = new ServerSocket(port)){
			Executors.newSingleThreadExecutor().execute(() -> switchOffListener(serverSocket));
			while(true) {
				Socket socket = serverSocket.accept();
				openSockets.add(socket);
				if(openSockets.size() <= nSockets) {
					writeInSocket(socket, "Connected: "+socket);
					pool.execute(() -> handleRequest(socket, streamProcessor));
				} else {
					writeInSocket(socket, ErrorCodes.LIMIT_OF_CONNECTIONS.getCode()+": "+ErrorCodes.LIMIT_OF_CONNECTIONS.getMessage());
					closeSocket(socket);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			for(Socket socket : new ArrayList<>(openSockets)) {
				closeSocket(socket);
			}
			while(streamProcessor.pendingTransactions() > 0) {}
		}
		
	}
	
	private void stop(ServerSocket serverSocket) {
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
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
					e.printStackTrace();
					writeInSocket(socket, e.getMessage() + ". " + message);
				}
			}
		} catch (IOException e) {
			// Don't print anything. It is normal to have IO exceptions when client or server closes the socket
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
			if(openSockets.remove(socket)) {
				System.out.println("Closed: "+socket);
				socket.close();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
	}
	
	
			
	
}
