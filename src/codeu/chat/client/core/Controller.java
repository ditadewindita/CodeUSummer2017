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

package codeu.chat.client.core;

import java.security.spec.ECField;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import codeu.chat.common.BasicController;
import codeu.chat.common.ConversationHeader;
import codeu.chat.common.Message;
import codeu.chat.common.NetworkCode;
import codeu.chat.common.User;
import codeu.chat.util.Logger;
import codeu.chat.util.Serializers;
import codeu.chat.util.Time;
import codeu.chat.util.Uuid;
import codeu.chat.util.connections.Connection;
import codeu.chat.util.connections.ConnectionSource;

public class Controller implements BasicController {

  private final static Logger.Log LOG = Logger.newLog(Controller.class);

  private final ConnectionSource source;

  public Controller(ConnectionSource source) {
    this.source = source;
  }

  private Integer toggleAccessControlBit(Uuid convo, Uuid user, Boolean flag, int request, int response){
    Integer accessControl = 0;

    try(final Connection connection = source.connect()){

      Serializers.INTEGER.write(connection.out(), request);
      Uuid.SERIALIZER.write(connection.out(), convo);
      Uuid.SERIALIZER.write(connection.out(), user);
      Serializers.BOOLEAN.write(connection.out(), flag);

      if(Serializers.INTEGER.read(connection.in()) == response){
        accessControl = Serializers.INTEGER.read(connection.in());
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex){
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return accessControl;
  }

  @Override
  public Integer toggleRemovedBit(Uuid convo, Uuid user){
    Integer response = 0;

    try(final Connection connection = source.connect()){

      Serializers.INTEGER.write(connection.out(), NetworkCode.TOGGLE_REMOVED_BIT_REQUEST);
      Uuid.SERIALIZER.write(connection.out(), convo);
      Uuid.SERIALIZER.write(connection.out(), user);

      if(Serializers.INTEGER.read(connection.in()) == NetworkCode.TOGGLE_REMOVED_BIT_RESPONSE){
        response = Serializers.INTEGER.read(connection.in());
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex){
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  @Override
  public Integer toggleMemberBit(Uuid convo, Uuid user, Boolean flag){
    return toggleAccessControlBit(convo, user, flag, NetworkCode.TOGGLE_MEMBER_BIT_REQUEST, NetworkCode.TOGGLE_MEMBER_BIT_RESPONSE);
  }

  @Override
  public Integer toggleOwnerBit(Uuid convo, Uuid user, Boolean flag){
    return toggleAccessControlBit(convo, user, flag, NetworkCode.TOGGLE_OWNER_BIT_REQUEST, NetworkCode.TOGGLE_OWNER_BIT_RESPONSE);
  }

  @Override
  public Integer toggleCreatorBit(Uuid convo, Uuid user, Boolean flag){
    return toggleAccessControlBit(convo, user, flag, NetworkCode.TOGGLE_CREATOR_BIT_REQUEST, NetworkCode.TOGGLE_CREATOR_BIT_RESPONSE);
  }

  @Override
  public Integer updateUsersUnseenMessagesCount(Uuid user, Uuid convo, Integer count){
    Integer response = 0;

    try(final Connection connection = source.connect()){

      Serializers.INTEGER.write(connection.out(), NetworkCode.UPDATE_USER_MESSAGE_COUNT_REQUEST);
      Uuid.SERIALIZER.write(connection.out(), user);
      Uuid.SERIALIZER.write(connection.out(), convo);
      Serializers.INTEGER.write(connection.out(), count);

      if(Serializers.INTEGER.read(connection.in()) == NetworkCode.UPDATE_USER_MESSAGE_COUNT_RESPONSE){
        response = Serializers.INTEGER.read(connection.in());
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex){
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  @Override
  public Time updateUsersLastStatusUpdate(Uuid user, Time time){
    Time response = null;

    try(final Connection connection = source.connect()){

      Serializers.INTEGER.write(connection.out(), NetworkCode.UPDATE_USER_LAST_STATUS_UPDATE_REQUEST);
      Uuid.SERIALIZER.write(connection.out(), user);
      Time.SERIALIZER.write(connection.out(), time);

      if(Serializers.INTEGER.read(connection.in()) == NetworkCode.UPDATE_USER_LAST_STATUS_UPDATE_RESPONSE){
        response = Time.SERIALIZER.read(connection.in());
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex){
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  @Override
  public Map<Uuid, Time> newUpdatedConversation(Uuid user, Uuid convo, Time time){
    Map<Uuid, Time> response = null;

    try(final Connection connection = source.connect()){

      Serializers.INTEGER.write(connection.out(), NetworkCode.NEW_UPDATED_CONVERSATION_REQUEST);
      Uuid.SERIALIZER.write(connection.out(), user);
      Uuid.SERIALIZER.write(connection.out(), convo);
      Time.SERIALIZER.write(connection.out(), time);

      if(Serializers.INTEGER.read(connection.in()) == NetworkCode.NEW_UPDATED_CONVERSATION_RESPONSE){
        response = Serializers.map(Uuid.SERIALIZER, Time.SERIALIZER).read(connection.in());
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex){
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  @Override
  public Collection<Uuid> newUserInterest(Uuid user1, Uuid user2){
    Collection<Uuid> response = null;

    try(final Connection connection = source.connect()){

      Serializers.INTEGER.write(connection.out(), NetworkCode.NEW_USER_INTEREST_REQUEST);
      Uuid.SERIALIZER.write(connection.out(), user1);
      Uuid.SERIALIZER.write(connection.out(), user2);

      if(Serializers.INTEGER.read(connection.in()) == NetworkCode.NEW_USER_INTEREST_RESPONSE){
        response = Serializers.collection(Uuid.SERIALIZER).read(connection.in());
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex){
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  @Override
  public Collection<Uuid> removeUserInterest(Uuid user1, Uuid user2){
    Collection<Uuid> response = null;

    try(final Connection connection = source.connect()){

      Serializers.INTEGER.write(connection.out(), NetworkCode.REMOVE_USER_INTEREST_REQUEST);
      Uuid.SERIALIZER.write(connection.out(), user1);
      Uuid.SERIALIZER.write(connection.out(), user2);

      if(Serializers.INTEGER.read(connection.in()) == NetworkCode.REMOVE_USER_INTEREST_RESPONSE){
        response = Serializers.collection(Uuid.SERIALIZER).read(connection.in());
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex){
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  @Override
  public Collection<Uuid> newConversationInterest(Uuid user, Uuid convo){
    Collection<Uuid> response = null;

    try(final Connection connection = source.connect()){

      Serializers.INTEGER.write(connection.out(), NetworkCode.NEW_CONVERSATION_INTEREST_REQUEST);
      Uuid.SERIALIZER.write(connection.out(), user);
      Uuid.SERIALIZER.write(connection.out(), convo);

      if(Serializers.INTEGER.read(connection.in()) == NetworkCode.NEW_CONVERSATION_INTEREST_RESPONSE){
        response = Serializers.collection(Uuid.SERIALIZER).read(connection.in());
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex){
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  @Override
  public Collection<Uuid> removeConversationInterest(Uuid user, Uuid convo){
    Collection<Uuid> response = null;

    try(final Connection connection = source.connect()){

      Serializers.INTEGER.write(connection.out(), NetworkCode.REMOVE_CONVERSATION_INTEREST_REQUEST);
      Uuid.SERIALIZER.write(connection.out(), user);
      Uuid.SERIALIZER.write(connection.out(), convo);

      if(Serializers.INTEGER.read(connection.in()) == NetworkCode.REMOVE_CONVERSATION_INTEREST_RESPONSE){
        response = Serializers.collection(Uuid.SERIALIZER).read(connection.in());
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex){
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  @Override
  public Message newMessage(Uuid author, Uuid conversation, String body) {

    Message response = null;

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.NEW_MESSAGE_REQUEST);
      Uuid.SERIALIZER.write(connection.out(), author);
      Uuid.SERIALIZER.write(connection.out(), conversation);
      Serializers.STRING.write(connection.out(), body);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.NEW_MESSAGE_RESPONSE) {
        response = Serializers.nullable(Message.SERIALIZER).read(connection.in());
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  @Override
  public User newUser(String name) {

    User response = null;

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.NEW_USER_REQUEST);
      Serializers.STRING.write(connection.out(), name);
      LOG.info("newUser: Request completed.");

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.NEW_USER_RESPONSE) {
        response = Serializers.nullable(User.SERIALIZER).read(connection.in());
        LOG.info("newUser: Response completed.");
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  @Override
  public ConversationHeader newConversation(String title, Uuid owner)  {

    ConversationHeader response = null;

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.NEW_CONVERSATION_REQUEST);
      Serializers.STRING.write(connection.out(), title);
      Uuid.SERIALIZER.write(connection.out(), owner);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.NEW_CONVERSATION_RESPONSE) {
        response = Serializers.nullable(ConversationHeader.SERIALIZER).read(connection.in());
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }
}
