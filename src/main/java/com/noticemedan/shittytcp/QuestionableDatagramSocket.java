package com.noticemedan.shittytcp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class QuestionableDatagramSocket extends DatagramSocket {
    private DatagramPacket oldPacket;
    private Boolean isReordering = false;

    public QuestionableDatagramSocket(Integer port) throws SocketException {
        super(port);
    }

    public QuestionableDatagramSocket() throws SocketException {
        super();
    }

    public void send(DatagramPacket p) throws IOException {
        System.out.println("Sending some shit");
        if (this.isReordering) {
            this.sendReorder(p);
            return;
        }

        int random = (int) (Math.random() * 4);
        switch (random) {
            case(0): // Discard
                System.out.println("Shit disappeared boi");
                break;
            case(1): // Create duplicates
                System.out.println("Double boi");
                this.sendDuplicates(p);
                break;
            case(2): // Simply send the datagram
                System.out.println("Send boi");
                super.send(p);
                break;
            case(3): // Reorders the datagram
                System.out.println("Reorder boi");
                this.sendReorder(p);
                break;
        }
    }

    private void sendDuplicates(DatagramPacket p) throws IOException {
        super.send(p);
        super.send(p);
    }

    private void sendReorder(DatagramPacket p) throws IOException {
        if (this.isReordering) {
            super.send(p);
            super.send(this.oldPacket);
            System.out.println("Reordering packets boi");
            this.isReordering = false;
        } else {
            System.out.println("Saving packet boi");
            this.oldPacket = p;
            this.isReordering = true;
        }
    }
}
