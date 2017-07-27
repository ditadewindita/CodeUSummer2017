// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package codeu.chat;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

import codeu.chat.client.commandline.Chat;
import codeu.chat.client.core.Context;
import codeu.chat.util.*;
import codeu.chat.util.connections.ClientConnectionSource;
import codeu.chat.util.connections.ConnectionSource;

import javax.swing.*;

final class ClientMain {

  private static final Logger.Log LOG = Logger.newLog(ClientMain.class);

  private static Time lastLogBackup;
  private static final long BACKUP_RATE_IN_MS = 30000;

  private static Chat chat;
//
  private static void reloadOldInterests() throws IOException {
    // Open the transaction log file for reading
    BufferedReader bufferedReader = new BufferedReader(new FileReader("data/transaction_log.txt"));

    // Read the header lines of each transaction log
    String line;

    System.out.println("Loading interest system...");

    JFrame window = new JFrame("CodeU Chat App");
    window.setSize(700, 700);

    //Overall panel that will hold all smaller panels
    JPanel backingPanel = new JPanel();
    backingPanel.setLayout(new BorderLayout(0,0));
    window.add(backingPanel);
    window.setContentPane(backingPanel);

    //Panel to hold top/main panels containing chat text and user buttons
    // panels will be switched based on where the user is in the chat app
    JPanel switchPanel = new JPanel(new CardLayout());
    CardLayout panelSwitcher = new CardLayout();
    switchPanel.setLayout(panelSwitcher);
    backingPanel.add(switchPanel, BorderLayout.CENTER);

    //Root panel holding root panel's GUI
    JPanel rootPanel = new JPanel();
    rootPanel.setLayout(new GridBagLayout());
    GridBagConstraints rootConstraints = new GridBagConstraints();
    rootConstraints.fill = GridBagConstraints.BOTH;
    switchPanel.add(rootPanel, "rootPanel");
    panelSwitcher.show(switchPanel, "rootPanel");

    JPanel userList = new JPanel();
    userList.setLayout(new BoxLayout(userList, BoxLayout.Y_AXIS));
    JButton dummy = new JButton("user 1");
    userList.add(dummy);
    //TODO (optional?) add a display of users currently registered to Server to userList panel
    //would be displayed to the left of the panel of messages in the chat
    rootConstraints.gridx = 0;
    rootConstraints.gridy = 0;
    rootConstraints.gridwidth = 1;
    rootConstraints.gridheight = 3;
    rootPanel.add(userList, rootConstraints);

    JTextArea rootTextDisplay = new JTextArea("Chat Messages", 20, 20);
    rootTextDisplay.setLayout(new FlowLayout());
    //In root panel there are no messages being sent so this will remain blank
    rootConstraints.gridx = 1;
    rootConstraints.gridy = 0;
    rootConstraints.gridwidth = 2;
    rootConstraints.gridheight = 2;
    rootPanel.add(rootTextDisplay, rootConstraints);

    //Panel with command buttons and text input
    JPanel rootInputPanel = new JPanel();
    rootInputPanel.setLayout(new GridLayout(2, 1));
    rootConstraints.gridx = 1;
    rootConstraints.gridy = 2;
    rootConstraints.gridwidth = 2;
    rootConstraints.gridheight = 1;
    rootPanel.add(rootInputPanel, rootConstraints);

    //Text input to sign in/add user
    JTextField rootTextInput = new JTextField(1);
    rootInputPanel.add(rootTextInput);
    //TODO parse input and link it to the u-sign-in and u-add commands

    //Command buttons
    JPanel rootButtonPanel = new JPanel();
    rootButtonPanel.setLayout(new GridLayout(1, 6));
    rootInputPanel.add(rootButtonPanel);
    JButton rootHelp = new JButton("Help");
    rootButtonPanel.add(rootHelp);
    JButton listUsers = new JButton("List Users");
    rootButtonPanel.add(listUsers);
    JButton addUser = new JButton("Add User");
    rootButtonPanel.add(addUser);
    JButton signIn = new JButton("Sign In");
    signIn.addActionListener(new ActionListener(){
      @Override
      public void actionPerformed(ActionEvent arg0) {
        panelSwitcher.show(switchPanel, "userPanel");
      }

    });
    rootButtonPanel.add(signIn);
    JButton info = new JButton("Info");
    rootButtonPanel.add(info);
    JButton exit = new JButton("Exit");
    exit.addActionListener(new ActionListener(){
      @Override
      public void actionPerformed(ActionEvent arg0) {
        window.dispose();
        System.exit(0);
      }

    });
    rootButtonPanel.add(exit);


    //User panel holding user panel's GUI, same as root panel but different buttons
    JPanel userPanel = new JPanel();
    userPanel.setLayout(new GridBagLayout());
    GridBagConstraints userConstraints = new GridBagConstraints();
    userConstraints.fill = GridBagConstraints.BOTH;
    switchPanel.add(userPanel, "userPanel");
//	    panelSwitcher.show(switchPanel, "userPanel");

    JPanel convoList = new JPanel();
    convoList.setLayout(new BoxLayout(convoList, BoxLayout.Y_AXIS));
    JButton dummyConvo = new JButton("convo 1");
    convoList.add(dummyConvo);
    //TODO (optional?) add a display of convos currently registered to Server to convoList panel
    //would be displayed to the left of the panel of messages in the chat
    userConstraints.gridx = 0;
    userConstraints.gridy = 0;
    userConstraints.gridwidth = 1;
    userConstraints.gridheight = 3;
    userPanel.add(convoList, userConstraints);

    //message/text display window in user panel, should not display any messages in user panel
    JTextArea userTextDisplay = new JTextArea("Chat Messages", 20, 20);
    userTextDisplay.setLayout(new FlowLayout());
    userConstraints.gridx = 1;
    userConstraints.gridy = 0;
    userConstraints.gridwidth = 2;
    userConstraints.gridheight = 2;
    userPanel.add(userTextDisplay, userConstraints);

    //Panel with command buttons and text input
    JPanel userInputPanel = new JPanel();
    userInputPanel.setLayout(new GridLayout(2, 1));
    userConstraints.gridx = 1;
    userConstraints.gridy = 2;
    userConstraints.gridwidth = 2;
    userConstraints.gridheight = 1;
    userPanel.add(userInputPanel, userConstraints);

    //Text input for user panel commands
    JTextField userTextInput = new JTextField(1);
    userInputPanel.add(userTextInput);
    //TODO parse input and link it to the user panel's commands

    //Command buttons
    JPanel userButtonPanel = new JPanel();
    userButtonPanel.setLayout(new GridLayout(2, 7));
    userInputPanel.add(userButtonPanel);
    JButton userHelp = new JButton("Help");
    userButtonPanel.add(userHelp);
    JButton listConvos = new JButton("List Conversations");
    userButtonPanel.add(listConvos);
    JButton addConvo = new JButton("Add Conversation");
    userButtonPanel.add(addConvo);
    JButton joinConvo = new JButton("Join Conversation");
    joinConvo.addActionListener(new ActionListener(){
      @Override
      public void actionPerformed(ActionEvent arg0) {
        panelSwitcher.show(switchPanel, "convoPanel");
      }

    });
    userButtonPanel.add(joinConvo);
    JButton listInterestConvos = new JButton("List Interest Conversations");
    userButtonPanel.add(listInterestConvos);
    JButton addInterestConvo = new JButton("Add Interest Conversation");
    userButtonPanel.add(addInterestConvo);
    JButton removeInterestConvo = new JButton("Remove Interest Conversation");
    userButtonPanel.add(removeInterestConvo);
    JButton listInterestUsers = new JButton("List Interest Users");
    userButtonPanel.add(listInterestUsers);
    JButton addInterestUser = new JButton("Add Interest User");
    userButtonPanel.add(addInterestUser);
    JButton removeInterestUser = new JButton("Remove Interest User");
    userButtonPanel.add(removeInterestUser);
    JButton statusUpdate = new JButton("Status Update");
    userButtonPanel.add(statusUpdate);
    JButton userInfo = new JButton("Info");
    userButtonPanel.add(userInfo);
    JButton userBack = new JButton("Back");
    userBack.addActionListener(new ActionListener(){
      @Override
      public void actionPerformed(ActionEvent arg0) {
        panelSwitcher.show(switchPanel, "rootPanel");
      }

    });
    userButtonPanel.add(userBack);
    JButton userExit = new JButton("Exit");
    userExit.addActionListener(new ActionListener(){
      @Override
      public void actionPerformed(ActionEvent arg0) {
        window.dispose();
        System.exit(0);
      }

    });
    userButtonPanel.add(userExit);


    //Conversation panel holding conversation panel's GUI, same as root panel but different buttons
    JPanel convoPanel = new JPanel();
    convoPanel.setLayout(new GridBagLayout());
    GridBagConstraints convoConstraints = new GridBagConstraints();
    convoConstraints.fill = GridBagConstraints.BOTH;
    switchPanel.add(convoPanel, "convoPanel");
//		panelSwitcher.show(switchPanel, "convoPanel");

    JPanel convoUsersList = new JPanel();
    convoUsersList.setLayout(new BoxLayout(convoUsersList, BoxLayout.Y_AXIS));
    JButton dummyConvoUser = new JButton("convo 1");
    convoUsersList.add(dummyConvoUser);
    //TODO (optional?) add a display of users currently in the covo to the convoUsersList panel
    //would be displayed to the left of the panel of messages in the chat
    convoConstraints.gridx = 0;
    convoConstraints.gridy = 0;
    convoConstraints.gridwidth = 1;
    convoConstraints.gridheight = 3;
    convoPanel.add(convoUsersList, convoConstraints);

    //message/text display window in user panel, should display any messages while in convo panel
    JTextArea messages = new JTextArea("Chat Messages", 20, 20);
    messages.setLayout(new FlowLayout());
    convoConstraints.gridx = 1;
    convoConstraints.gridy = 0;
    convoConstraints.gridwidth = 2;
    convoConstraints.gridheight = 2;
    convoPanel.add(messages, convoConstraints);

    //Panel with command buttons and text input
    JPanel convoInputPanel = new JPanel();
    convoInputPanel.setLayout(new GridLayout(2, 1));
    convoConstraints.gridx = 1;
    convoConstraints.gridy = 2;
    convoConstraints.gridwidth = 2;
    convoConstraints.gridheight = 1;
    convoPanel.add(convoInputPanel, convoConstraints);

    //Text input for user's messages/other conversation commands
    JTextField convoTextInput = new JTextField(1);
    convoInputPanel.add(convoTextInput);
    //TODO parse input and link it to the conversation panel's commands

    //Command buttons
    JPanel convoButtonPanel = new JPanel();
    convoButtonPanel.setLayout(new GridLayout(1, 9));
    convoInputPanel.add(convoButtonPanel);
    JButton convoHelp = new JButton("Help");
    convoButtonPanel.add(convoHelp);
    JButton addMessage = new JButton("Add Message");
    convoButtonPanel.add(addMessage);
    JButton addMember = new JButton("Add Member");
    convoButtonPanel.add(addMember);
    JButton removeMember = new JButton("Remove Member");
    convoButtonPanel.add(removeMember);
    JButton removeOwner = new JButton("Remove Owner");
    convoButtonPanel.add(removeOwner);
    JButton addOwner = new JButton("Add Owner");
    convoButtonPanel.add(addOwner);
    JButton convoInfo = new JButton("Info");
    convoButtonPanel.add(convoInfo);
    JButton convoBack = new JButton("Back");
    convoBack.addActionListener(new ActionListener(){
      @Override
      public void actionPerformed(ActionEvent arg0) {
        panelSwitcher.show(switchPanel, "userPanel");
      }

    });
    convoButtonPanel.add(convoBack);
    JButton convoExit = new JButton("Exit");
    convoExit.addActionListener(new ActionListener(){
      @Override
      public void actionPerformed(ActionEvent arg0) {
        window.dispose();
        System.exit(0);
      }

    });
    convoButtonPanel.add(convoExit);

    window.pack();
    window.setVisible(true);
    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    while((line = bufferedReader.readLine()) != null) {

      // Instantiate a Tokenizer to parse through log's data
      Tokenizer logInfo = new Tokenizer(line);

      String commandType = logInfo.next();

      if(commandType.equals("ADD-INTEREST-USER")){
        Uuid owner = Uuid.parse(logInfo.next());
        Uuid follow = Uuid.parse(logInfo.next());

        chat.addUserInterest(owner, follow);
      }
      else if(commandType.equals("REMOVE-INTEREST-USER")){
        Uuid owner = Uuid.parse(logInfo.next());
        Uuid follow = Uuid.parse(logInfo.next());

        chat.removeUserInterest(owner, follow);
      }
      else if(commandType.equals("ADD-INTEREST-CONVERSATION")){
        Uuid owner = Uuid.parse(logInfo.next());
        Uuid follow = Uuid.parse(logInfo.next());

        chat.addConvoInterest(owner, follow);
      }
      else if(commandType.equals("REMOVE-INTEREST-CONVERSATION")){
        Uuid owner = Uuid.parse(logInfo.next());
        Uuid follow = Uuid.parse(logInfo.next());

        chat.removeConvoInterest(owner, follow);
      }
    }

    LOG.info("Successfully restored last logged interest system state.");

    System.out.println("Successfully loaded interest system!");

    bufferedReader.close();
  }

  public static void main(String [] args) {

    try {
      Logger.enableFileOutput("data/chat_client_log.log");
    } catch (IOException ex) {
      LOG.error(ex, "Failed to set logger to write to file");
    }

    LOG.info("============================= START OF LOG =============================");

    LOG.info("Starting chat client...");

    final RemoteAddress address = RemoteAddress.parse(args[0]);

    final ConnectionSource source = new ClientConnectionSource(address.host, address.port);

    LOG.info("Creating client...");

    chat = new Chat(new Context(source));

    LOG.info("Created client");

    // Reload old interests
    try {
      reloadOldInterests();
    } catch (Exception e) {
      LOG.info("Could not reload last logged interest system.");
    }

    boolean keepRunning = true;

    lastLogBackup = Time.now();

    try (final BufferedReader input = new BufferedReader(new InputStreamReader(System.in))) {
      while (keepRunning) {
        System.out.print(">>> ");

        // Evaluate if it is time to transfer data from queue to disk, then call the transferQueueToLog() method defined
        // above and update the last backup time
        Time currentTime = Time.now();
        if(currentTime.inMs() - lastLogBackup.inMs() >= BACKUP_RATE_IN_MS){
          chat.transferQueueToLog();
          lastLogBackup = currentTime;
        }

        keepRunning = chat.handleCommand(input.readLine().trim());
      }
    } catch (IOException ex) {
      LOG.error("Failed to read from input");
    }

    LOG.info("chat client has exited.");
  }
}
