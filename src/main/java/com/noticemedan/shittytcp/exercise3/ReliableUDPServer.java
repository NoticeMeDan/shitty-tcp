package com.noticemedan.shittytcp.exercise3;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

public class ReliableUDPServer {
    private String lastAckKey;
    private Boolean isRunning = true;

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
            String[] data = new String(requestPacket.getData()).split("__");

            // Get response address
            SocketAddress responseAddress = requestPacket.getSocketAddress();

            // Send ACK
            byte[] ack = data[1].getBytes();
            DatagramPacket responsePacket = new DatagramPacket(ack, ack.length, responseAddress);
            server.send(responsePacket);

            System.out.println("Received packet with timestamp: " + data[1].trim());
            // Check for duplicate packets
            if (this.lastAckKey != null && !this.lastAckKey.equals(data[1])) {
                System.out.println("Received packet with data:");
                System.out.println(data[0]);
                System.out.println();
            } else {
                this.lastAckKey = data[1];
            }
        }
    }
}
