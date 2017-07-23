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

import codeu.chat.util.Uuid;

import java.util.Collection;

// BASIC CONTROLLER
//
//   The controller component in the Model-View-Controller pattern. This
//   component is used to write information to the model where the model
//   is the current state of the server. Data returned from the controller
//   should be treated as read only data as manipulating any data returned
//   from the controller may have no effect on the server's state.
public interface BasicController {

  // NEW MESSAGE
  //
  //   Create a new message on the server. All parameters must be provided
  //   or else the server won't apply the change. If the operation is
  //   successful, a Message object will be returned representing the full
  //   state of the message on the server.
  Message newMessage(Uuid author, Uuid conversation, String body);

  // NEW USER
  //
  //   Create a new user on the server. All parameters must be provided
  //   or else the server won't apply the change. If the operation is
  //   successful, a User object will be returned representing the full
  //   state of the user on the server. Whether user names can be shared
  //   is undefined.
  User newUser(String name);

  // NEW CONVERSATION
  //
  //  Create a new conversation on the server. All parameters must be
  //  provided or else the server won't apply the change. If the
  //  operation is successful, a Conversation object will be returned
  //  representing the full state of the conversation on the server.
  //  Whether conversations can have the same title is undefined.
  ConversationHeader newConversation(String title, Uuid owner);

  // NEW CONVERSATION INTEREST
  //
  //  Add a conversation to a user's list of interested conversations.
  //  If successful, the method will return the collection of conversations
  //  the user is currently interested in.
  Collection<Uuid> newConversationInterest(Uuid user, Uuid convo);

  // REMOVE CONVERSATION INTEREST
  //
  //  Remove a conversation to a user's list of interested conversations.
  //  If successful, the method will return the collection of conversations
  //  the user is currently interested in.
  Collection<Uuid> removeConversationInterest(Uuid user, Uuid convo);

  // NEW USER INTEREST
  //
  //  Add a user to a user's list of interested users.
  //  If successful, the method will return the collection of users
  //  the user is currently interested in.
  Collection<Uuid> newUserInterest(Uuid user1, Uuid user2);

  // REMOVE USER INTEREST
  //
  //  Remove a user in a user's list of interested users.
  //  If successful, the method will return the collection of users
  //  the user is currently interested in.
  Collection<Uuid> removeUserInterest(Uuid user1, Uuid user2);
}
