package com.noticemedan.shittytcp.exercise3;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ReliableUDPClient {
    DatagramSocket socket;

    public static void main(String[] args) {
        if (args.length == 3) {
            new ReliableUDPClient(args[0], Integer.parseInt(args[1]), args[2]);
        } else {
            System.out.println("Please include the wanted IP, port and message");
        }
    }

    public ReliableUDPClient (String ip, Integer port, String data) {
        try {
            this.run(InetAddress.getByName(ip), port, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void run (InetAddress ip, Integer port, String data) throws IOException {
        this.socket = new DatagramSocket();
        Long currentTimestamp = System.currentTimeMillis();
        boolean gotAck = false;

        // Send message
        this.send(data, currentTimestamp, ip, port);

        // Get ACK
        byte[] buffer = new byte[1024];
        DatagramPacket response = new DatagramPacket(buffer, buffer.length);

        while (!gotAck) {
            try {
                this.getAck(response);

                String ack = new String(response.getData()).trim(); // remove empties
                if (ack.equals(currentTimestamp.toString())) {
                    System.out.println("Message has been sent.");
                    gotAck = true;
                }
            } catch (IOException e) {
                currentTimestamp = System.currentTimeMillis();
                this.send(data, currentTimestamp, ip, port);
            }
        }
    }

    private void send (String data, Long timestamp, InetAddress ip, Integer port) throws IOException {
        // Create packet
        byte[] buffer = String.format("%s__%s", data, timestamp).getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ip, port);

        // Send packet
        this.socket.send(packet);
    }

    private void getAck (DatagramPacket p) throws IOException {
        this.socket.setSoTimeout(5000);
        this.socket.receive(p);
    }
}
