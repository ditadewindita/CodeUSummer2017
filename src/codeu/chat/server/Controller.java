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

package codeu.chat.server;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import codeu.chat.client.core.ConversationContext;
import codeu.chat.common.BasicController;
import codeu.chat.common.ConversationHeader;
import codeu.chat.common.ConversationPayload;
import codeu.chat.common.Message;
import codeu.chat.common.RandomUuidGenerator;
import codeu.chat.common.RawController;
import codeu.chat.common.User;
import codeu.chat.util.Logger;
import codeu.chat.util.Time;
import codeu.chat.util.Uuid;

public final class Controller implements RawController, BasicController {

  private final static Logger.Log LOG = Logger.newLog(Controller.class);

  private final Model model;
  private final Uuid.Generator uuidGenerator;

  public Controller(Uuid serverId, Model model) {
    this.model = model;
    this.uuidGenerator = new RandomUuidGenerator(serverId, System.currentTimeMillis());
  }

  @Override
  public Message newMessage(Uuid author, Uuid conversation, String body) {
    return newMessage(createId(), author, conversation, body, Time.now());
  }

  @Override
  public User newUser(String name) {
    return newUser(createId(), name, Time.now());
  }

  @Override
  public ConversationHeader newConversation(String title, Uuid owner) {
    return newConversation(createId(), title, owner, Time.now());
  }

  @Override
  public Integer toggleRemovedBit(Uuid convo, Uuid user){
    final ConversationHeader foundConvo = model.conversationById().first(convo);
    final User foundUser = model.userById().first(user);

    Integer result = 0;

    if(foundConvo != null && foundUser != null) {
      Integer access = foundConvo.accessControls.getOrDefault(user, 0);
      // Flag is set to true if the user is removed from a conversation.
      // Flag stays true once it's set.
      Integer newAccess = access | ConversationHeader.REMOVED;
      result = newAccess;

      foundConvo.accessControls.put(user, newAccess);

      LOG.info(
              "toggleRemovedBit success (user.id=%s conversation.id=%s access=%s)",
              foundUser.id,
              foundConvo.id,
              newAccess);
    } else {
      LOG.info(
              "toggleRemovedBit fail (user.id=%s conversation.id=%s)",
              foundUser.id,
              foundConvo.id);
    }

    return result;
  }

  @Override
  public Integer toggleCreatorBit(Uuid convo, Uuid user, Boolean flag){
    final ConversationHeader foundConvo = model.conversationById().first(convo);
    final User foundUser = model.userById().first(user);

    Integer result = 0;

    if(foundConvo != null && foundUser != null) {
      Integer access = foundConvo.accessControls.getOrDefault(user, 0);
      Integer newAccess;

      // If user is to be set as a Creator, it will also be it's children controls
      if(flag) {
        newAccess = access | ConversationHeader.CREATOR;
        newAccess |= ConversationHeader.OWNER;
        newAccess |= ConversationHeader.MEMBER;
      }
      else
        newAccess = access & ~ConversationHeader.CREATOR;

      result = newAccess;
      foundConvo.accessControls.put(user, newAccess);

      LOG.info(
              "toggleCreatorBit success (user.id=%s conversation.id=%s access=%s)",
              foundUser.id,
              foundConvo.id,
              newAccess);
    } else {
      LOG.info(
              "toggleCreatorBit fail (user.id=%s conversation.id=%s)",
              foundUser.id,
              foundConvo.id);
    }

    return result;
  }

  @Override
  public Integer toggleOwnerBit(Uuid convo, Uuid user, Boolean flag){
    final ConversationHeader foundConvo = model.conversationById().first(convo);
    final User foundUser = model.userById().first(user);

    Integer result = 0;

    if(foundConvo != null && foundUser != null) {
      Integer access = foundConvo.accessControls.getOrDefault(user, 0);
      Integer newAccess;

      // If user is to be set as an Owner, it will also be it's children controls
      if(flag){
        newAccess = access | ConversationHeader.OWNER;
        newAccess |= ConversationHeader.MEMBER;
      }
      // If user is not an Owner anymore, it is also not it's parent controls anymore
      else {
        newAccess = access & ~ConversationHeader.OWNER;
        newAccess &= ~ConversationHeader.CREATOR;
      }

      result = newAccess;
      foundConvo.accessControls.put(user, newAccess);

      LOG.info(
              "toggleOwnerBit success (user.id=%s conversation.id=%s access=%s)",
              foundUser.id,
              foundConvo.id,
              newAccess);
    } else {
      LOG.info(
              "toggleOwnerBit fail (user.id=%s conversation.id=%s)",
              foundUser.id,
              foundConvo.id);
    }

    return result;
  }

  @Override
  public Integer toggleMemberBit(Uuid convo, Uuid user, Boolean flag){
    final ConversationHeader foundConvo = model.conversationById().first(convo);
    final User foundUser = model.userById().first(user);

    Integer result = 0;

    if(foundConvo != null && foundUser != null) {
      Integer access = foundConvo.accessControls.getOrDefault(user, 0);
      Integer newAccess;

      if(flag)
        newAccess = access | ConversationHeader.MEMBER;
        // If member bit is to be 'turned off', also turn off it's parent controls
      else {
        newAccess = access & ~ConversationHeader.MEMBER;
        newAccess &= ~ConversationHeader.OWNER;
        newAccess &= ~ConversationHeader.CREATOR;
      }

      result = newAccess;
      foundConvo.accessControls.put(user, newAccess);

      LOG.info(
              "toggleMemberBit success (user.id=%s conversation.id=%s access=%s)",
              foundUser.id,
              foundConvo.id,
              newAccess);
    } else {
      LOG.info(
              "toggleMemberBit fail (user.id=%s conversation.id=%s)",
              foundUser.id,
              foundConvo.id);
    }

    return result;
  }

  @Override
  public Integer updateUsersUnseenMessagesCount(Uuid user, Uuid convo, Integer count){
    final User foundUser = model.userById().first(user);
    final ConversationHeader foundConvo = model.conversationById().first(convo);

    Integer result = 0;

    if(foundUser != null && foundConvo != null){
      Integer currentCount = foundConvo.unseenMessages.getOrDefault(foundUser, 0);
      Integer setCount = Math.max(0, currentCount + count);

      foundConvo.unseenMessages.put(foundUser.id, setCount);

      result = foundConvo.unseenMessages.get(foundUser.id);

      LOG.info(
              "updateUsersUnseenMessagesCount success (user.id=%s conversation.id=%s count=%s)",
              foundUser.id,
              foundConvo.id,
              setCount);
    } else {
      LOG.info(
              "updateUsersUnseenMessagesCount fail (user.id=%s conversation.id=%s)",
              foundUser.id,
              foundConvo.id);
    }

    return result;
  }

  @Override
  public Time updateUsersLastStatusUpdate(Uuid user, Time time){
    final User foundUser = model.userById().first(user);

    Time update = null;

    if(foundUser != null && time.inMs() > foundUser.creation.inMs()){
      foundUser.lastStatusUpdate = time;
      update = foundUser.lastStatusUpdate;

      LOG.info(
              "updateUsersLastStatusUpdate success (user.id=%s time=%s)",
              foundUser.id,
              time.inMs());
    } else {

      LOG.info(
              "updateUsersLastStatusUpdate fail (user.id=%s time=%s)",
              foundUser.id,
              time.inMs());
    }

    return update;
  }

  @Override
  public Map<Uuid, Time> newUpdatedConversation(Uuid user, Uuid convo, Time time){
    final User foundUser = model.userById().first(user);
    final ConversationHeader foundConversation = model.conversationById().first(convo);

    Map<Uuid, Time> map = null;

    if(foundUser != null && foundConversation != null){
      foundUser.updatedConversations.put(foundConversation.id, time);
      map = foundUser.updatedConversations;

      LOG.info(
              "newUpdatedConversation success (user.id=%s conversation.id=%s time=%s)",
              foundUser.id,
              foundConversation.id,
              time.inMs());
    } else {
      LOG.info(
              "newUpdatedConversation fail (user.id=%s conversation.id=%s time=%s)",
              foundUser.id,
              foundConversation.id,
              time.inMs());
    }

    return map;
  }

  @Override
  public Collection<Uuid> newUserInterest(Uuid user1, Uuid user2){
    final User foundUser = model.userById().first(user1);
    final User followedUser = model.userById().first(user2);

    Collection<Uuid> interests = null;

    if(foundUser != null && followedUser != null) {
      foundUser.userInterests.add(followedUser.id);
      interests = foundUser.userInterests;

      LOG.info(
              "newUserInterest success (user.id=%s user.id=%s)",
              foundUser.id,
              followedUser.id);
    } else {
      LOG.info(
              "newUserInterest fail - user/followed user doesn't exist (user.id=%s user.id=%s)",
              foundUser.id,
              followedUser.id);
    }

    return interests;
  }

  @Override
  public Collection<Uuid> removeUserInterest(Uuid user1, Uuid user2){
    final User foundUser = model.userById().first(user1);
    final User followedUser = model.userById().first(user2);

    Collection<Uuid> interests = null;

    if(foundUser != null && followedUser != null) {
      foundUser.userInterests.remove(followedUser.id);
      interests = foundUser.userInterests;

      LOG.info(
              "removeUserInterest success (user.id=%s user.id=%s)",
              foundUser.id,
              followedUser.id);
    } else {
      LOG.info(
              "removeUserInterest fail - user/followed user doesn't exist (user.id=%s user.id=%s)",
              foundUser.id,
              followedUser.id);
    }

    return interests;
  }

  @Override
  public Collection<Uuid> newConversationInterest(Uuid user, Uuid interest){
    final User foundUser = model.userById().first(user);
    final ConversationHeader foundConvo = model.conversationById().first(interest);

    Collection<Uuid> interests = null;

    if(foundUser != null && foundConvo != null) {
      foundUser.conversationInterests.add(foundConvo.id);
      foundConvo.unseenMessages.put(foundUser.id, 0);
      interests = foundUser.conversationInterests;

      LOG.info(
              "newConversationInterest success (user.id=%s conversation.id=%s)",
              foundUser.id,
              foundConvo.id);
    } else {
      LOG.info(
              "newConversationInterest fail - user/conversation does not exist (user.id=%s conversation.id=%s)",
              foundUser.id,
              foundConvo.id);
    }

    return interests;
  }

  @Override
  public Collection<Uuid> removeConversationInterest(Uuid user, Uuid interest){
    final User foundUser = model.userById().first(user);
    final ConversationHeader foundConvo = model.conversationById().first(interest);

    Collection<Uuid> interests = null;

    if(foundUser != null && foundConvo != null) {
      foundUser.conversationInterests.remove(foundConvo.id);
      interests = foundUser.conversationInterests;

      LOG.info(
              "removeConversationInterest success (user.id=%s conversation.id=%s)",
              foundUser.id,
              foundConvo.id);
    } else {
      LOG.info(
              "removeConversationInterest fail - user/conversation doesn't exist (user.id=%s conversation.id=%s)",
              foundUser.id,
              foundConvo.id);
    }

    return interests;
  }

  @Override
  public Message newMessage(Uuid id, Uuid author, Uuid conversation, String body, Time creationTime) {

    final User foundUser = model.userById().first(author);
    final ConversationPayload foundConversation = model.conversationPayloadById().first(conversation);

    Message message = null;

    if (foundUser != null && foundConversation != null && isIdFree(id)) {

      message = new Message(id, Uuid.NULL, Uuid.NULL, creationTime, author, body);
      model.add(message);
      LOG.info("Message added: %s", message.id);

      // Find and update the previous "last" message so that it's "next" value
      // will point to the new message.

      if (Uuid.equals(foundConversation.lastMessage, Uuid.NULL)) {

        // The conversation has no messages in it, that's why the last message is NULL (the first
        // message should be NULL too. Since there is no last message, then it is not possible
        // to update the last message's "next" value.

      } else {
        final Message lastMessage = model.messageById().first(foundConversation.lastMessage);
        lastMessage.next = message.id;
      }

      // If the first message points to NULL it means that the conversation was empty and that
      // the first message should be set to the new message. Otherwise the message should
      // not change.

      foundConversation.firstMessage =
          Uuid.equals(foundConversation.firstMessage, Uuid.NULL) ?
          message.id :
          foundConversation.firstMessage;

      // Update the conversation to point to the new last message as it has changed.

      foundConversation.lastMessage = message.id;
    }

    return message;
  }

  @Override
  public User newUser(Uuid id, String name, Time creationTime) {

    User user = null;

    if (isIdFree(id)) {

      user = new User(id, name, creationTime);
      model.add(user);

      LOG.info(
          "newUser success (user.id=%s user.name=%s user.time=%s)",
          id,
          name,
          creationTime);

    } else {

      LOG.info(
          "newUser fail - id in use (user.id=%s user.name=%s user.time=%s)",
          id,
          name,
          creationTime);
    }

    return user;
  }

  @Override
  public ConversationHeader newConversation(Uuid id, String title, Uuid owner, Time creationTime) {

    final User foundOwner = model.userById().first(owner);

    ConversationHeader conversation = null;

    if (foundOwner != null && isIdFree(id)) {
      conversation = new ConversationHeader(id, owner, creationTime, title);
      model.add(conversation);
      LOG.info("Conversation added: " + id);
    }

    return conversation;
  }

  private Uuid createId() {

    Uuid candidate;

    for (candidate = uuidGenerator.make();
         isIdInUse(candidate);
         candidate = uuidGenerator.make()) {

     // Assuming that "randomUuid" is actually well implemented, this
     // loop should never be needed, but just incase make sure that the
     // Uuid is not actually in use before returning it.

    }

    return candidate;
  }

  private boolean isIdInUse(Uuid id) {
    return model.messageById().first(id) != null ||
           model.conversationById().first(id) != null ||
           model.userById().first(id) != null;
  }

  private boolean isIdFree(Uuid id) { return !isIdInUse(id); }

}
