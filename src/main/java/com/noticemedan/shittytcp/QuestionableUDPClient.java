package com.noticemedan.shittytcp;

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class QuestionableUDPClient {
    private static int serverPort = 7007;
    private static Boolean isRunning = true;

    public static void main(String args[]) throws IOException {
        QuestionableDatagramSocket aSocket = new QuestionableDatagramSocket();
        Scanner msgScan = new Scanner(System.in);

        System.out.println("Ready boi");
        while (QuestionableUDPClient.isRunning) { //Keep ask user for messages.
            InetAddress aHost = InetAddress.getByName("localhost");
            String msg = msgScan.nextLine();

            if (msg.equals("/quit")) {
                QuestionableUDPClient.isRunning = false;
            } else {
                byte[] m = msg.getBytes();
                DatagramPacket request = new DatagramPacket(m, msg.length(), aHost, serverPort);
                aSocket.send(request);
            }
        }
        aSocket.close();
    }
}
