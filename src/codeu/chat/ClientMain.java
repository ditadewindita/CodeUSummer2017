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
    backingPanel.setLayout(new GridBagLayout());
    GridBagConstraints backingConstraints = new GridBagConstraints();
    backingConstraints.fill = GridBagConstraints.BOTH;
    backingConstraints.gridx = 0;
    backingConstraints.gridy = 0;
    backingConstraints.gridwidth = 3;
    backingConstraints.gridheight = 3;
    backingPanel.setSize(700, 700);
    window.add(backingPanel);
    window.setContentPane(backingPanel);

    //Panel to hold top/main panels containing chat text and user buttons
    // panels will be switched based on where the user is in the chat app
    JPanel switchPanel = new JPanel(new CardLayout());
    switchPanel.setSize(700, 700);
    CardLayout panelSwitcher = new CardLayout();
    switchPanel.setLayout(panelSwitcher);
    backingPanel.add(switchPanel, backingConstraints);

    //Conversation panel holding buttons for chat panel's commands and chat's messages
    JPanel rootPanel = new JPanel();
    rootPanel.setLayout(new GridBagLayout());
    GridBagConstraints conversationConstraints = new GridBagConstraints();
    conversationConstraints.fill = GridBagConstraints.BOTH;
    switchPanel.add(rootPanel, "rootPanel");
    panelSwitcher.show(switchPanel, "rootPanel");

    JPanel userList = new JPanel();
    userList.setLayout(new BoxLayout(userList, BoxLayout.Y_AXIS));
    JButton dummy = new JButton("user 1");
    userList.add(dummy);
    //TODO (optional?) add a display of users in the current conversation to userList panel
    //would be displayed to the left of the panel of messages in the chat
    conversationConstraints.gridx = 0;
    conversationConstraints.gridy = 0;
    conversationConstraints.gridwidth = 1;
    conversationConstraints.gridheight = 3;
    rootPanel.add(userList, conversationConstraints);

    JTextArea messages = new JTextArea("Chat messages:");
    messages.setLayout(new FlowLayout());
    //TODO display the messages users are sending onto this messages JTextArea (I'm not sure if
    //a JTextArea is ideal for this, but this is just a placeholder for now
    conversationConstraints.gridx = 1;
    conversationConstraints.gridy = 0;
    conversationConstraints.gridwidth = 2;
    conversationConstraints.gridheight = 2;
    rootPanel.add(messages, conversationConstraints);

    //Panel with command buttons and text input
    JPanel inputPanel = new JPanel();
    inputPanel.setLayout(new GridLayout(2, 1));
    conversationConstraints.gridx = 1;
    conversationConstraints.gridy = 2;
    conversationConstraints.gridwidth = 2;
    conversationConstraints.gridheight = 1;
    rootPanel.add(inputPanel, conversationConstraints);

    //Text input to send messages
    JTextField messageInput = new JTextField(1);
    inputPanel.add(messageInput);
    //TODO action listening to read user's input text

    //Command buttons
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(1, 5));
    inputPanel.add(buttonPanel);
    JButton listUsers = new JButton("List Users");
    buttonPanel.add(listUsers);
    JButton addUser = new JButton("Add User");
    buttonPanel.add(addUser);
    JButton signIn = new JButton("Sign In");
    buttonPanel.add(signIn);
    JButton info = new JButton("Info");
    buttonPanel.add(info);
    JButton exit = new JButton("Exit");
    buttonPanel.add(exit);

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
