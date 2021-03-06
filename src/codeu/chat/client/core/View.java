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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import codeu.chat.common.*;
import codeu.chat.util.Logger;
import codeu.chat.util.Serializers;
import codeu.chat.util.Time;
import codeu.chat.util.Uuid;
import codeu.chat.util.connections.Connection;
import codeu.chat.util.connections.ConnectionSource;

// VIEW
//
// This is the view component of the Model-View-Controller pattern used by the
// the client to retrieve readonly data from the server. All methods are blocking
// calls.
public class View implements BasicView {

  private final static Logger.Log LOG = Logger.newLog(View.class);

  private final ConnectionSource source;

  public View(ConnectionSource source) {
    this.source = source;
  }

  @Override
  public Integer getUserAccessControl(Uuid convo, Uuid user){
    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.GET_USER_ACCESS_CONTROL_REQUEST);
      Uuid.SERIALIZER.write(connection.out(), convo);
      Uuid.SERIALIZER.write(connection.out(), user);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.GET_USER_ACCESS_CONTROL_RESPONSE) {
        return Serializers.INTEGER.read(connection.in());
      } else {
        LOG.error("Response from server failed.");
      }

    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return 0;
  }

  @Override
  public Integer getUnseenMessagesCount(Uuid user, Uuid convo){
    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.GET_USER_MESSAGE_COUNT_REQUEST);
      Uuid.SERIALIZER.write(connection.out(), user);
      Uuid.SERIALIZER.write(connection.out(), convo);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.GET_USER_MESSAGE_COUNT_RESPONSE) {
        return Serializers.INTEGER.read(connection.in());
      } else {
        LOG.error("Response from server failed.");
      }

    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return 0;
  }

  @Override
  public Time getLastStatusUpdate(Uuid user){
    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.GET_USER_LAST_STATUS_UPDATE_REQUEST);
      Uuid.SERIALIZER.write(connection.out(), user);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.GET_USER_LAST_STATUS_UPDATE_RESPONSE) {
        return Time.SERIALIZER.read(connection.in());
      } else {
        LOG.error("Response from server failed.");
      }

    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return null;
  }

  @Override
  public Map<Uuid, Time> getUpdatedConversations(Uuid user){
    final Map<Uuid, Time> conversations = new HashMap<>();

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.GET_UPDATED_CONVERSATIONS_REQUEST);
      Uuid.SERIALIZER.write(connection.out(), user);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.GET_UPDATED_CONVERSATIONS_RESPONSE) {
        conversations.putAll(Serializers.map(Uuid.SERIALIZER, Time.SERIALIZER).read(connection.in()));
      } else {
        LOG.error("Response from server failed.");
      }

    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return conversations;
  }

  @Override
  public Collection<Uuid> getUserInterests(Uuid user){
    final Collection<Uuid> interests = new ArrayList<>();

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.GET_USER_INTERESTS_REQUEST);
      Uuid.SERIALIZER.write(connection.out(), user);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.GET_USER_INTERESTS_RESPONSE) {
        interests.addAll(Serializers.collection(Uuid.SERIALIZER).read(connection.in()));
      } else {
        LOG.error("Response from server failed.");
      }

    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return interests;
  }

  @Override
  public Collection<Uuid> getConversationInterests(Uuid user){
    final Collection<Uuid> interests = new ArrayList<>();

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.GET_CONVERSATION_INTERESTS_REQUEST);
      Uuid.SERIALIZER.write(connection.out(), user);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.GET_CONVERSATION_INTERESTS_RESPONSE) {
        interests.addAll(Serializers.collection(Uuid.SERIALIZER).read(connection.in()));
      } else {
        LOG.error("Response from server failed.");
      }

    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return interests;
  }

  @Override
  public Collection<User> getUsers() {

    final Collection<User> users = new ArrayList<>();

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.GET_USERS_REQUEST);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.GET_USERS_RESPONSE) {
        users.addAll(Serializers.collection(User.SERIALIZER).read(connection.in()));
      } else {
        LOG.error("Response from server failed.");
      }

    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return users;
  }

  @Override
  public Collection<ConversationHeader> getConversations() {

    final Collection<ConversationHeader> summaries = new ArrayList<>();

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.GET_ALL_CONVERSATIONS_REQUEST);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.GET_ALL_CONVERSATIONS_RESPONSE) {
        summaries.addAll(Serializers.collection(ConversationHeader.SERIALIZER).read(connection.in()));
      } else {
        LOG.error("Response from server failed.");
      }

    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return summaries;
  }

  @Override
  public Collection<ConversationPayload> getConversationPayloads(Collection<Uuid> ids) {

    final Collection<ConversationPayload> conversations = new ArrayList<>();

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.GET_CONVERSATIONS_BY_ID_REQUEST);
      Serializers.collection(Uuid.SERIALIZER).write(connection.out(), ids);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.GET_CONVERSATIONS_BY_ID_RESPONSE) {
        conversations.addAll(Serializers.collection(ConversationPayload.SERIALIZER).read(connection.in()));
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return conversations;
  }

  @Override
  public Collection<Message> getMessages(Collection<Uuid> ids) {

    final Collection<Message> messages = new ArrayList<>();

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.GET_MESSAGES_BY_ID_REQUEST);
      Serializers.collection(Uuid.SERIALIZER).write(connection.out(), ids);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.GET_MESSAGES_BY_ID_RESPONSE) {
        messages.addAll(Serializers.collection(Message.SERIALIZER).read(connection.in()));
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return messages;
  }

  public ServerInfo getInfo() {

    try (final Connection connection = this.source.connect()) {
      Serializers.INTEGER.write(connection.out(), NetworkCode.SERVER_INFO_REQUEST);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.SERVER_INFO_RESPONSE) {
        final Uuid version = Uuid.SERIALIZER.read(connection.in());
        final Time startTime = Time.SERIALIZER.read(connection.in());

        return new ServerInfo(version, startTime);
      }
      else {
        // Communicate this error - the server did not respond with the type of
        // response we expected.
        LOG.error("Server did not respond with server info.");
      }
    } catch (Exception ex) {
      // Communicate this error - something went wrong with the connection.
      LOG.error(ex, "Connection error.");
    }
    // If we get here it means something went wrong and null should be returned
    return null;
  }
}
