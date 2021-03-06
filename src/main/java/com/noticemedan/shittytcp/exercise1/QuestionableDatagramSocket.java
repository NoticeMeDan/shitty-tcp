package com.noticemedan.shittytcp.exercise1;

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
        if (this.isReordering) {
            this.sendReorder(p);
            return;
        }

        int random = (int) (Math.random() * 4);
        switch (random) {
            case(0): // Discard
                break;
            case(1): // Create duplicates
                this.sendDuplicates(p);
                break;
            case(2): // Simply send the datagram
                super.send(p);
                break;
            case(3): // Reorders the datagram
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
            this.isReordering = false;
        } else {
            this.oldPacket = p;
            this.isReordering = true;
        }
    }
}
