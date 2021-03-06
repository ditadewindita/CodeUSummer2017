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

package codeu.chat.common;

import java.util.Collection;
import java.util.Map;

import codeu.chat.common.ConversationHeader;
import codeu.chat.common.ConversationPayload;
import codeu.chat.common.Message;
import codeu.chat.common.User;
import codeu.chat.util.Time;
import codeu.chat.util.Uuid;

// BASIC VIEW
//
//   The view component in the Model-View-Controller pattern. This component
//   is used to read information from the model where the model is the current
//   state of the server. Data returned from the view should be treated as
//   read only data as manipulating any data returned from the view may
//   have no effect on the server's state.

public interface BasicView {

  // GET USERS
  //
  //   Return all users whose id is found in the given collection.
  Collection<User> getUsers();

  // GET ALL CONVERSATIONS
  //
  //   Return a summary of each converation.
  Collection<ConversationHeader> getConversations();

  // GET CONVERSATIONS
  //
  //   Return all conversations whose id is found in the given collection.
  Collection<ConversationPayload> getConversationPayloads(Collection<Uuid> ids);

  // GET MESSAGES
  //
  //   Return all messages whose id is found in the given collection.
  Collection<Message> getMessages(Collection<Uuid> ids);

  // GET SERVER INFO
  //
  //  Return the current server information
  ServerInfo getInfo();

  // GET CONVERSATION INTERESTS
  //
  //  Return the passed user's list of interested conversations
  Collection<Uuid> getConversationInterests(Uuid user);

  // GET USER INTERESTS
  //
  //  Return the passed user's list of interested conversations
  Collection<Uuid> getUserInterests(Uuid user);

  // GET UPDATED CONVERSATIONS
  //
  //  Return the passsed user's list of updated conversations
  Map<Uuid, Time> getUpdatedConversations(Uuid user);

  // GET LAST STATUS UPDATE
  //
  //  Return the last time a user requested their status update
  Time getLastStatusUpdate(Uuid user);

  // GET UNSEEN MESSAGES COUNT
  //
  //  Returns the number of messages this user hasn't seen in a specific conversation
  Integer getUnseenMessagesCount(Uuid user, Uuid convo);

  // GET ACCESS CONTROL OF SPECIFIC USER
  //
  //  Return the access control integer of a specific user for a specific conversation
  Integer getUserAccessControl(Uuid convo, Uuid user);
}
