package hw0;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class EchoClient {
	public static void main(String argv[]) throws UnknownHostException, IOException{
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		int socket;
		socket = toInt(argv[1]);
		Socket client = new Socket(argv[0],socket);
		DataOutputStream out = new DataOutputStream(client.getOutputStream());
		BufferedReader response = new BufferedReader(new InputStreamReader(client.getInputStream()));
		String sentence = input.readLine();
		out.writeBytes(sentence+'\n');
		String repeat = response.readLine();
		System.out.println(repeat);
		client.close();
	}
	
	public static int toInt(String s){
		int ret = Integer.parseInt(s);
		return ret;
	}
}
