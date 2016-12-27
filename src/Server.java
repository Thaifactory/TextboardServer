import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	public static void main(String[] args) {
		int port = 0;
		ServerSocket server = null;
		if(args.length > 1) {
			throw new IllegalArgumentException("Too many argumants!");
		}
		try {
			port = Integer.parseInt(args[0]);
			server = new ServerSocket(port);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Format of argumant is not Integer!");
		} catch (IOException e) {
			e.printStackTrace();
		}
		while (true) {
			Socket newClient = null;
			try {
				newClient = server.accept();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			new HandleConnection(newClient).start();
		}
		
	}
	
	private class HandleConnection extends Thread {
		Socket clientSocket = null;
		BufferedReader input = null;
		PrintStream output = null;
		boolean terminate = false;
		
		public HandleConnection(Socket socket) {
			this.clientSocket = socket;
			try {
				this.input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				this.output = new PrintStream(clientSocket.getOutputStream(), true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void terminate() {
			terminate = true;
		}

		public void run() {
			while (!terminate) {
				if (clientSocket != null && output != null && input != null) {

				}
			}
		}
	}
}
