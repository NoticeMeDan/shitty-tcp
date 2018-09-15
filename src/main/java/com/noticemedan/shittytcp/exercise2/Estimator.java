package com.noticemedan.shittytcp.exercise2;

import com.noticemedan.shittytcp.exercise1.QuestionableDatagramSocket;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;


public class Estimator {

    private int datagramSize, numOfDatagrams;

    private ArrayList<DatagramPacket> send;
    private LinkedList<DatagramPacket> sendList;
    private ArrayList<DatagramPacket> received;


    private QuestionableDatagramSocket sendSocket;
    private DatagramSocket receiveSocket;

    public Estimator(int datagramSize, int numOfDatagrams){
        this.datagramSize = datagramSize > 60000 ? 60000 : datagramSize;
        this.numOfDatagrams = numOfDatagrams;
        this.received = new ArrayList<>();
        try {
            this.sendSocket = new QuestionableDatagramSocket();
            this.receiveSocket = new DatagramSocket(7007);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }


    public static void estimate(int datagramSize, int numOfDatagrams, int interval) {
        Estimator est = new Estimator(datagramSize, numOfDatagrams);
        est.createRandomDatagrams();

        Runnable sender = () -> {
            try {
                est.sendSocket.send(est.sendList.pop());
            } catch (Exception e) {
                System.out.println("No more elements in list!");
            }
        };

        Runnable receiver = () -> {
            while(true) {
                try {
                    byte[] buffer = new byte[est.datagramSize];
                    DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                    est.receiveSocket.receive(reply);
                    est.received.add(reply);
                } catch (SocketTimeoutException e) {
                    System.out.println("Socket timeout. Closing...");
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        //Open the receiver thread and start listening for Packets on the DatagramSocket.
        Thread receiveThread = new Thread(receiver);
        receiveThread.start();

        //Start a Thread with an Executor Service that runs the packet-sending method with the given interval
        ScheduledExecutorService sendScheduler = Executors.newScheduledThreadPool(1);
        ScheduledFuture handler = sendScheduler.scheduleAtFixedRate(sender, interval, interval, TimeUnit.MILLISECONDS);
        sendScheduler.schedule(() -> { handler.cancel(true);
                                        est.printResults();
                                       try {
                                           receiveThread.join();   //Close the receive thread.
                                       } catch (InterruptedException e) { e.printStackTrace(); }

                                      }, interval * est.send.size(), TimeUnit.MILLISECONDS);


    }

    private void printResults() {
        for (DatagramPacket msg: send) {
            System.out.println(new String(msg.getData()));
        }
        System.out.println("---------------");
        for (DatagramPacket msg: received) {
            System.out.println(new String(msg.getData()));
        }

        //System.out.println("######## STATISTICS ########");
        //System.out.println("Duplicates: " + duplicatePackets);
        //System.out.println("Reordered: " + reorderedPackets);
        //System.out.println("Discarded: " + discardedPackets);
        //System.out.println("Packets sent: " + packetsSend);
        //System.out.println("Packets received: " + packetsReceived);
    }

    private void createRandomDatagrams() {
        InetAddress aHost = null;
        try {
            aHost = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.send = new ArrayList<>();
        this.sendList = new LinkedList<>();

        if(this.datagramSize > 60000){
            throw new IllegalArgumentException("Please provide a datagram size less than 60.000");
        }
        for(int i = 0; i<this.numOfDatagrams; i++) {
            byte[] m = generateRandomByteArray(this.datagramSize);
            DatagramPacket packet = new DatagramPacket(m,this.numOfDatagrams, aHost, 7007);
            this.send.add(packet);
            this.sendList.add(packet);
        }
    }

    private byte[] generateRandomByteArray(int s){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < s; i++) {
            char c = (char)((int) ThreadLocalRandom.current().nextInt(50, 110 + 1));
            sb.append(c);
        }
        return sb.toString().getBytes();
    }

    public static void main(String args[]) {
        estimate(20,20,100);
    }

}