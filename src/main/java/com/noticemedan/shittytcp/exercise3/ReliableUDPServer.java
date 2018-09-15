package com.noticemedan.shittytcp.exercise3;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.zip.CRC32;

public class ReliableUDPServer {
	private String lastAckKey;
	private Boolean isRunning = true; // Will never change

	public static void main(String[] args) {
		if (args.length == 1) {
			new ReliableUDPServer(Integer.parseInt(args[0]));
		} else {
			System.out.println("Please include the wanted port");
		}
	}

	public ReliableUDPServer(Integer port) {
		try {
			this.run(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void run(Integer port) throws IOException {
		DatagramSocket server = new DatagramSocket(port);
		byte[] receiveBuffer = new byte[2048];

		while (this.isRunning) {
			// Receive packet
			DatagramPacket requestPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
			server.receive(requestPacket);

			// Extract data
			String[] data = new String(requestPacket.getData()).trim().split("__");

			// Integrity check
			CRC32 checksum = new CRC32();
			checksum.update(data[0].getBytes(), 0, data[0].getBytes().length);
			if (checksum.getValue() != Long.valueOf(data[2])) {
				// If data is corrupt, then wait for next package.
				// This is simpler, but slower, than reporting the corrupt package to the client
				continue;
			}

			// Get response address
			SocketAddress responseAddress = requestPacket.getSocketAddress();

			// Send ACK
			byte[] ack = data[1].getBytes();
			DatagramPacket responsePacket = new DatagramPacket(ack, ack.length, responseAddress);
			server.send(responsePacket);

			System.out.println("Received packet with timestamp: " + data[1]);
			// Check for duplicate packets
			if (this.lastAckKey == null || !this.lastAckKey.equals(data[1])) {
				System.out.println("Received packet with data:");
				System.out.println(data[0]);
				System.out.println();
			} else {
				this.lastAckKey = data[1];
			}
		}
	}
}
