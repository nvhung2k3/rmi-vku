package rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Iterator;
import java.util.Scanner;

public class Client {
	
	public static void main(String[] args) {
        try {
        	ChatClientImpl chatClientImpl = new ChatClientImpl("Chat peer to peer");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
