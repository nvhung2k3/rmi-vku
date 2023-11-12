package rmi;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;



public class ChatClientImpl extends UnicastRemoteObject implements HandleEventClient,
	ActionListener, ListSelectionListener, KeyListener, WindowListener {
	
    private String username;
    private JFrame containerBox;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private JPanel chatContainer;
    private JTextArea chatTextArea;
    private JTextField messageField;
    private JPanel enterNamePanel;
    private JButton enterButton;
    private JTextField nameField;
    private DefaultListModel<DataClient> userListModel;
    private JList<DataClient> userList;
    private JTextField chatHeaderTextField;
    private JPanel boxChatPanel;
    private JButton sendButton;
    
    // Biến server
    private HandleEventServer server;
    private HandleEventClient client;
    private DataClient userReceive;
    private Map<String, HandleEventClient> clients = new HashMap<>();
	private Map<String, List<String>> chatsInChater = new HashMap<>();
    
    class DataClient {
    	String username;
    	HandleEventClient clientKey;
    	
    	public DataClient(String username, HandleEventClient clientKey) {
			super();
			this.username = username;
			this.clientKey = clientKey;
		}

    	

		public String getUsername() {
			return username;
		}



		public void setUsername(String username) {
			this.username = username;
		}



		public HandleEventClient getClientKey() {
			return clientKey;
		}

		public void setClientKey(HandleEventClient clientKey) {
			this.clientKey = clientKey;
		}

		@Override
    	public String toString() {
    		return this.username;
    	}
    }
    
    private void createLayoutLogin() {
        enterNamePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        nameField = new JTextField(20);
        enterButton = new JButton("Enter");
        enterNamePanel.add(new JLabel("Enter your name: "));
        enterNamePanel.add(nameField);
        enterNamePanel.add(enterButton);

        enterButton.addActionListener(this);
        
        nameField.addKeyListener(this);
	}

	public ChatClientImpl(String title) throws RemoteException, NotBoundException {
    	// chạy cái server cái đã rồi tính tiếp
    	Registry registry = LocateRegistry.getRegistry("localhost", 4545);
    	server = (HandleEventServer) registry.lookup("ChatServer");
    	
    	containerBox = new JFrame();
    	containerBox.addWindowListener(this);
    	
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        createLayoutLogin();
        
        chatContainer = new JPanel(new BorderLayout());
        chatContainer.setMinimumSize(new Dimension(300, 0));

        boxChatPanel = new JPanel(new BorderLayout());

        JPanel chatHeader = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel chatHeaderLabel = new JLabel("Chatting with: ");
        chatHeader.add(chatHeaderLabel);
        chatHeaderTextField = new JTextField(20);
        chatHeaderTextField.setEditable(false);
        chatHeader.add(chatHeaderTextField);

        chatTextArea = new JTextArea(15, 40);
        chatTextArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatTextArea);

        userListModel = new DefaultListModel<>();
        userList =  new JList<>(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        userList.addListSelectionListener(this);

        boxChatPanel.add(chatHeader, BorderLayout.NORTH);
        boxChatPanel.add(chatScrollPane, BorderLayout.CENTER);

        chatContainer.add(boxChatPanel, BorderLayout.CENTER);
        boxChatPanel.setVisible(false);

        JPanel messagePanel = new JPanel(new BorderLayout());

        messageField = new JTextField(30);
        sendButton = new JButton("Send");
        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);

        sendButton.addActionListener(this);

        messageField.addKeyListener(this);

        boxChatPanel.add(messagePanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chatContainer, userList);
        splitPane.setDividerLocation(0.7);

        cardPanel.add(enterNamePanel, "EnterName");
        cardPanel.add(splitPane, "Chat");

        containerBox.add(cardPanel);
        containerBox.setTitle(title);
        containerBox.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        containerBox.pack();
        containerBox.setLocationRelativeTo(null);
        containerBox.setVisible(true);
    }
    
    public void registerClient() {
    	try {
			username = nameField.getText();
            cardLayout.next(cardPanel);
	        server.registerClient(username, this);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
    public void sendMessage() {
    	try {
    		if ("".equals(messageField.getText())) {
    			System.out.println("Không được để trống");
    			return;
			}
    		
    		userReceive.getClientKey().receiveMessage(username, messageField.getText());

    		ArrayList<String> chatInChater = (ArrayList<String>) chatsInChater.get(userReceive.getUsername());
            if (chatInChater == null) {
    			chatInChater = new ArrayList<>();
    		}
            chatInChater.add("Bạn: " + messageField.getText());
            if (!chatsInChater.containsKey(userReceive.getUsername())) {
            	chatsInChater.put(userReceive.getUsername(), chatInChater);
    		}
            String messages = chatInChater.stream().collect(Collectors.joining("\n"));
            chatTextArea.setText(messages);
            messageField.setText("");
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
    @Override
    public void receiveMessage(String sender, String message) throws RemoteException {
    	ArrayList<String> chatInChater = (ArrayList<String>) chatsInChater.get(sender);
        if (chatInChater == null) {
			chatInChater = new ArrayList<>();
		}
        chatInChater.add(sender + ": " + message);
        if (!chatsInChater.containsKey(sender)) {
        	chatsInChater.put(sender, chatInChater);
		}
        if (userReceive == null) {
			return;
		}
        if (sender.equals(userReceive.getUsername())) {
        	String messages = chatInChater.stream().collect(Collectors.joining("\n"));
            chatTextArea.setText(messages);
		}
    }

    private void repaintChaters() {
		userListModel.removeAllElements();
		for (Map.Entry<String, HandleEventClient> entry : clients.entrySet()) {
            String name = entry.getKey();
            HandleEventClient value = entry.getValue();
            DataClient item = new DataClient(name, value);
            userListModel.addElement(item);
        }
		userList.repaint();
	}
    
	@Override
	public void addClient(String nickname, HandleEventClient client) {
		clients.put(nickname, client);
		repaintChaters();
	}

	@Override
	public void addAllClient(Map<String, HandleEventClient> clientsRecive) {
		clients = clientsRecive;
		repaintChaters();
	}

	@Override
	public void removeClient(String nickname) throws RemoteException {
		clients.remove(nickname);
		repaintChaters();
	}
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			if (e.getSource() == nameField) {
	            registerClient();
	            return;
			}
        	
			if (e.getSource() == messageField) {
				sendMessage();
				return;
			}
        }
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
            DataClient selectedUser = (DataClient) userList.getSelectedValue();
            if (selectedUser == null) {
				return;
			}
      
            chatHeaderTextField.setText(selectedUser.getUsername());
            boxChatPanel.setVisible(true);
            ArrayList<String> chatInChater = (ArrayList<String>) chatsInChater.get(selectedUser.getUsername());
            if (chatInChater == null) {
				chatInChater = new ArrayList<>();
			}
            
            String messages = chatInChater.stream().collect(Collectors.joining("\n"));
            chatTextArea.setText(messages);
            
            userReceive = selectedUser;
        }
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == enterButton) {
            registerClient();
            return;
		}
		if (e.getSource() == sendButton) {
			sendMessage();
			return;
		}
	}
	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
			try {
				System.out.println("closed");
				
				server.unregisterClient(username);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				System.out.println("Có lỗi: " + e1.getMessage());
			} finally {
				for (HandleEventClient clientItem: clients.values()) {
	    			try {
						clientItem.removeClient(username);
					} catch (RemoteException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
	    		}
				System.exit(0);
			}
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	    
}
