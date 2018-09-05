package com.noticemedan.shittytcp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class QuestionableDatagramSocket extends DatagramSocket {
    public QuestionableDatagramSocket() throws SocketException {
    }

    public static void main(String[] args) {
        System.out.println("God i'm questionable");

    }

    public void executeRandomDatagram(DatagramPacket p) throws IOException {
        int random = (int) (Math.random() * 4);

        switch (random) {
            case(0): // Discard
                break;
            case(1): // Create duplicates
                super.send(p);
                super.send(p);
                break;
            case(2): // Simply send the datagram
                super.send(p);
                break;
            case(3): // Reorders the datagram
                super.setSoTimeout(10000);
                super.send(p);

                super.setSoTimeout(1);
                super.send(p);
                break;
        }
    }
}
