package com.noticemedan.shittytcp;

import java.net.*;

class RFC862 {
	public static void main(String args[]) throws Exception {
		DatagramSocket serverSocket = new DatagramSocket(7007);
		byte[] receiveData = new byte[1024];
		byte[] sendData;
		while(true) {
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receivePacket);

			String sentence = new String( receivePacket.getData());
			System.out.println("RECEIVED: " + sentence);

			InetAddress IPAddress = receivePacket.getAddress();
			int port = receivePacket.getPort();

			String capitalizedSentence = sentence.toUpperCase();
			sendData = capitalizedSentence.getBytes();

			DatagramPacket sendPacket = new DatagramPacket(
					sendData, sendData.length,
					IPAddress, port
			);
			serverSocket.send(sendPacket);
		}
	}
}

/*
public class RFC862 {
	public static void main(String[] args) {
		System.out.println("Starting server");
		RFC862.run();
	}

	private static void run() {
		try {
			DatagramChannel reader = DatagramChannel.open();
			reader.bind(new InetSocketAddress("0.0.0.0", 7007));
			ByteBuffer buffer = ByteBuffer.allocate(4096);
			while (true) {
				SocketAddress address = reader.receive(buffer);
				DatagramChannel write = reader.connect(address);
				buffer.flip();
				System.out.println("Received message: " + StandardCharsets.UTF_8.decode(buffer));
				buffer.rewind();
				write.write(buffer);
				write.disconnect();
				buffer.clear();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}*/
