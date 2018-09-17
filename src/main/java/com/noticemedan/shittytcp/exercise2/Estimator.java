package com.noticemedan.shittytcp.exercise2;

import com.noticemedan.shittytcp.exercise1.QuestionableDatagramSocket;

import java.net.*;
import java.util.*;

public class Estimator {
    private int datagramSize, numOfDatagrams;

    private DatagramPacket[] sendList; // This will be used to pop to add to receivedList
    private ArrayList<Integer> receivedList; // A list of receivedList packets

    private QuestionableDatagramSocket sendSocket;
    private DatagramSocket receiveSocket;

    // Count actions
	private int duplicatedCounter, reorderedCounter, droppedCounter, receivedCounter;

    public Estimator(int datagramSize, int numOfDatagrams){
		this.datagramSize = datagramSize > 60000 ? 60000 : datagramSize;
		this.numOfDatagrams = numOfDatagrams;
		this.receivedList = new ArrayList<>();

    	this.createRandomDatagrams();

		this.duplicatedCounter = 0; this.reorderedCounter = 0; this.droppedCounter = 0; this.receivedCounter = 0;
        try {
            this.sendSocket = new QuestionableDatagramSocket();
            this.receiveSocket = new DatagramSocket(7007);
        } catch (SocketException e) {
            e.printStackTrace();
        }

		Thread send = new Thread(new Send());
		Thread receive = new Thread(new Receive());

		receive.start();
        send.start();

     	try{
			receive.join();
		} catch (Exception e){
			System.out.println("Whoops oh no");
		}
		try {
			this.receiveSocket.close();
			this.sendSocket.close();
		} catch (Exception e){
			System.out.println("Couldn't close properly");
		}

		this.analyzeResult();
    }

    public static void estimate(int datagramSize, int numOfDatagrams, int interval) {
        Estimator est = new Estimator(datagramSize, numOfDatagrams);

    }

    private void analyzeResult(){
		for (Integer i = 0; i < this.receivedList.size() - 1 ; i++){
			if (!this.receivedList.contains(i)){
				this.droppedCounter++;
			}
			if (count(this.receivedList, i) == 2){
				this.duplicatedCounter++;
			} else if (count(this.receivedList, i) == 1){
				if (this.receivedList.get(i) > this.receivedList.get(i+1)){
					this.reorderedCounter++;
				} else {
					this.receivedCounter++;
				}
			}
		}

    	this.printResults();
	}
	private static double count (ArrayList<Integer> list, int val) {
		int count = 0;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) == val) {
				count++;
			}
		}
		return count;
	}

	private void printResults() {
		System.out.println("######## STATISTICS ########");
		System.out.println("Discarded: " + this.droppedCounter);
		System.out.println("Duplicates: " + this.duplicatedCounter);
		System.out.println("Received: " + this.receivedCounter);
		System.out.println("Reordered: " + this.reorderedCounter);
        System.out.println("Packets sent: " + this.sendList.length);
        System.out.println("Packets receivedList: " + this.receivedList.size());
    }

    private void createRandomDatagrams() {
        InetAddress aHost = null;
        try {
            aHost = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        if (this.datagramSize > 60000){
            throw new IllegalArgumentException("Please provide a datagram size less than 60.000");
        }
        this.sendList = generateByteArray(datagramSize, numOfDatagrams, aHost);
    }

    private DatagramPacket[] generateByteArray(int size, int numOfDatagrams, InetAddress host){
    	DatagramPacket[] packets = new DatagramPacket[numOfDatagrams];
    	for (int i = 0; i < numOfDatagrams; i++){
    		byte[] sequenceNumber = ("" + i).getBytes();
			byte[] message = new byte[size];

			System.arraycopy(sequenceNumber, 0, message, 0, sequenceNumber.length);

			DatagramPacket packet = new DatagramPacket(message, sequenceNumber.length, host, 7007);
			packets[i] = packet;
		}
		return packets;
	}

	private class Send implements Runnable {
		public void run() {
			for(DatagramPacket msg: Estimator.this.sendList){
				try {
					Estimator.this.sendSocket.send(msg);
				} catch (Exception e) { System.out.println("Exception at send thread..."); }
			}
		}
	}

	private class Receive implements Runnable {
		public void run() {
			while(true){
				try {
					Estimator.this.receiveSocket.setSoTimeout(5000);
					byte[] buffer = new byte[1000]; // Hardcoded length right now
					DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
					Estimator.this.receiveSocket.receive(reply);

					Estimator.this.receivedList.add(Integer.parseInt(new String(Arrays.copyOfRange(reply.getData(), 0, reply.getLength()))));
				} catch(SocketTimeoutException e) {
					System.out.println("Socket timeout. Closing...");
					break;
				} catch(Exception e) { System.out.println("Exception at send thread..."); }
			}
		}
	}

    public static void main(String args[]) {
        estimate(1000,1000,10);
    }
}
