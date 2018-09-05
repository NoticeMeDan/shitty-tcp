package com.noticemedan.shittytcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;

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
}