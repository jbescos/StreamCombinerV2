package es.tododev.sc2.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Main {

	private final static String HELP_FILE = "/help.txt";
	private final static int PORT = 22222;
	private final static int SOCKETS = 4;
	private final static String SOCKETS_PARAM = "-sockets";
	private final static String PORT_PARAM = "-port";
	
	public static void main(String[] args) throws IOException {
		printHelp();
		int nSockets = getValue(args, SOCKETS_PARAM, SOCKETS, s -> Integer.parseInt(s));
		int port = getValue(args, PORT_PARAM, PORT, s -> Integer.parseInt(s));
		StreamSocketManager mgr = new StreamSocketManager(nSockets, port);
		mgr.start();
	}
	
	private static <T> T getValue(String[] args, String parameter, T defaultValue, Function<String, T> function){
		for(int i=0;i<args.length-1;i++){
			if(parameter.equals(args[i])){
				return function.apply(args[i+1]);
			}
		}
		return defaultValue;
	}
	
	private static void printHelp() throws IOException{
		try(InputStream stream = Main.class.getResourceAsStream(HELP_FILE)){
			String result = new BufferedReader(new InputStreamReader(stream)).lines().collect(Collectors.joining("\n"));
			System.out.println(result);
		}
	}

}
