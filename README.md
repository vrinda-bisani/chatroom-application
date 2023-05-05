# Chatroom
It will consist of a client and a server. The server will be responsible for managing the client connections 
(up to 10 clients can be connected and in a chat room at one time), accepting messages from one client and sending
the messages to all attached clients. Clients will be able to either send a message that is public in the chat room, or that goes directly to a single, specified client. 

## Server 
The server is responsible for allowing clients to connect, get a list of all other clients connected,
and disconnect. It also handles sending messages to all clients or just one.
When the server starts up, it starts listening on an available port. Once it is up and
listening, it prints a reasonable message on the console that includes what port it is
listening on (so clients can connect).
The server will continually listen for more connections (handling them appropriately),
as well as handle each connected client. The server can handle up to 10 clients connected at
a single time.

## Client
The client will open a socket to communicate with the server. It will maintain the socket to
listen for incoming messages from the server (public or private messages), as well as listen to
the UI (terminal) for messages from the user to send to the server.
When starting the client, you will need to pass in the IP address (or localhost) and port for
the server. Not providing these details should result in a graceful failure.

## Client Interface
In addition to client allowing a user to send messages and all chat room messages to be
displayed, it will require additional commands.
Your client must list all commands when the user types a ? (a question mark alone on a line—
you can ignore question marks as part of a message).
Other commands for client:
- logoff: sends a DISCONNECT_MESSAGE to the server
- who: sends a QUERY_CONNECTED_USERS to the server
- @user: sends a DIRECT_MESSAGE to the specified user to the server
- @all: sends a BROADCAST_MESSAGE to the server, to be sent to all users connected
- !user: sends a SEND_INSULT message to the server, to be sent to the specified user
- login: sends a CONNECT_MESSAGE to the server
- Example:
  SAMPLE                            Result
  @bob hello bob, how are you?      hello bob, how are you? is sent to user bob
  @all Hello Everyone!              Hello Everyone! sent to all connected clients
  !bob                              You are not good at this job! randomly generated insult sent to user bob

## Chatroom Protocol
### Connect message:
- int Message Identifier: CONNECT_MESSAGE
- int size of username: integer denoting size of the username being sent 
- byte[]: username

### Connect response:
- int Message Identifier: CONNECT_RESPONSE
- boolean success: true if connection was successful
- int msgSize: size of message sent in response
- byte[] message: String in byte[]. If connect was successful, should respond with a
  message such as “There are X other connected clients”. If connect failed, a message explaining.

### Disconnect message:
- int Message Identifier: DISCONNECT_MESSAGE
- int size of username: integer denoting size of the username being sent
- byte[]: username

### Disconnect response:
Send back a CONNECT_RESPONSE
- The success field will return “true” if the disconnect is successful (valid user)
- The message field is set as follows:
  - If the disconnect was successful, the message should be “You are no longer connected.”. If the disconnect failed, a message explaining. 

### Query users:
- int Message Identifier: QUERY_CONNECTED_USERS
- int size of username: integer noting the size of the username
- byte[] username: username (who’s requesting)
- Query response:
- int Message Identifier: QUERY_USER_RESPONSE
- int numberOfUsers: if the request fails this will be 0. If there are no other users connected, this will be 0.
- int usernameSize1: length of the first username
- byte[] username: username1
- int usernameSize2: length of the last username
- byte[] username: usernameX
  When responding to a query, the server must make sure the request is coming from an
  already connected user. A list of all the OTHER connected users will be sent.

> Note: The server will need to send a block of data for every connected user.

### Broadcast Message:
- int Message Identifier: BROADCAST_MESSAGE
- int sender username size: length of sender’s username
- byte[]: sender username
- int message size: length of message
- byte[]: Message
Server will broadcast this message to all connected users, specifying that it came from
sender. If the sender username is invalid, the server will respond with a  FAILED_MESSAGE.

### Direct Message:
- int Message Identifier: DIRECT_MESSAGE
- int sender username size: length of sender’s username
- byte[]: sender username
- int recipient username size: length of recipient’s username
- byte[]: recipient username
- int message size: length of message
- byte[]: Message
  Sending a direct message will fail if the sender or recipient ID is invalid. 

### Failed Message:
- int Message Identifier: FAILED_MESSAGE
- int message size: length of message
- byte[]: Message describing the failure.

### Send Insult:
- int Message Identifier: SEND_INSULT
- int sender username size: length of sender’s username
- byte[]: sender username
- int recipient username size: length of recipient’s username
- byte[]: recipient username

## Message Identifiers
1. CONNECT_MESSAGE = 19
2. CONNECT_RESPONSE = 20
3. DISCONNECT_MESSAGE = 21
4. QUERY_CONNECTED_USERS = 22
5. QUERY_USER_RESPONSE = 23
6. BROADCAST_MESSAGE = 24
7. DIRECT_MESSAGE = 25
8. FAILED_MESSAGE = 26
9. SEND_INSULT = 27
10. DISCONNECT_RESPONSE = 28

## Entry point for server
[ChatRoomServer.java](src/main/java/ChatRoomServer.java)

## Entry point for client
[ChatRoomClient.java](src/main/java/ChatRoomClient.java)

## How to run application in intellij
### Step 1: Run ChatRoomServer.java > ```main method```
- If you are using intellij, go to menu ```Run > Edit configurations``` menu setting.
- A dialog box will appear as shown below.
- If you want to run server at desired port number, provide port number argument to the ```Program arguments``` input under build and run section.
- If no port number is provided to server, then it will automatically find free port
Screenshot:
![chatroomServer.png](chatroomServer.png)

### Step 2: Copy port printed in console/terminal of server 

### Step 3: Run ChatRoomClient.java > ```main method```
If you are using intellij, go to menu ```Run > Edit configurations``` menu setting.
- A dialog box will appear as shown below.
- provide copied port number from Step2 and hostname as arguments to the ```Program arguments``` input under build and run section.
![chatroomClient.png](chatroomClient.png)

> Note: If you want to run multiple clients from intellij, while edit configuration, click ```modify options``` and click ```Allow multiple instances``` as shown below
 ![img.png](instance.png)

## How to run application in terminal
### Step 1: open new terminal and run server gradle task
- Open terminal/cli and navigate to root project folder "Assignment6"
- Run the gradle server task with arguments ```gradle server --console=plain run --args='<port number>'``` and program will start running successfully.
>Note : port number should be provided in args

Example for server:
```shell
gradle server --console=plain run --args='12345'
```
### Step 2: Copy port printed in terminal of server
### Step 3: open another new terminal and run client gradle task
- Open terminal/cli and navigate to root project folder "Assignment6"
- Run the gradle client task with arguments ```gradle client --console=plain run --args='<port number> <hostname>'``` and program will start running successfully.
>Note : port number and hostname must be provided in args

Example for client:
```shell
gradle client --console=plain run --args='12345 localhost'
```

## Assumptions 
In server:
1. Port number provided should be greater than 1000
2. Server can connect upto 10 users only
3. If CONNECT_RESPONSE is provided true, then only client will be logoff

In client:
1. First input will be username, if that is already present in chatroom then need to provide another unique username
2. If multiple commands used at a time, then first one will be considered
3. If "hello" is provided without @all or @<username>, then this will be considered as error
4. Server is running on port number provided in argument of client while starting client

## Changes in build.gradle

- Added dependency and updated implementation
  ```
  dependency {
   .....
   implementation fileTree(dir: 'assignment4', include: ['*.jar'])
   implementation files('assignment4/Assignment4.jar')
  }
  ```

- Added plugins
  ```
  plugins {
  ....
  id 'application'
  }
  ```
- Added JavaExec tasks for server and client
  ```
  task(server, type: JavaExec) {
    main = 'assignment6.ChatRoomServer'
    classpath = sourceSets.main.runtimeClasspath
  }
  task(client, type: JavaExec) {
  main = 'assignment6.ChatRoomClient'
  classpath = sourceSets.main.runtimeClasspath
  standardInput = System.in
  }
  ```

## Assignment4 for sending insult
Assignment4 is used as a jar file for generating insults, [Assignment4.jar](assignment4/Assignment4.jar)

## High level description of classes
- The ChatRoomServer class contains a main method that creates a new thread by creating an instance of the ClientInterface class and keeps a map of concurrent clients.
- The ClientInterface implements runnable and processes input on the server, and then sends Message ID to chatRoomProtocol, which processes messages and updates dataInputStream.
- The ChatRoomClient class is created with a main method that forwards messages for encoding and decoding to the MessageCodec class and updates dataOutputStream and dataInputStream accordingly. 
  There is a method in this class that creates an additional thread to print messages to the terminal rather than just sending messages.
- The MessageCodec class encodes and decodes messages for the client
- There are two exception classes and one class for keeping message ID as constants.

## Steps to ensure correctness
- When a client sends logoff command, he is removed from the clients list in server
- If user does not provide the hostname and port number while starting client, InvalidArgException is thrown
- Client is provided with the choice of commands whenever he needs help to ensure he gives valid commands and client can enter '?' to see all the valid commands
- In case the user wants to send direct message or insult to a user who is not connected to the server, appropriate failure message will be sent to the user
- No more than 10 clients will be allowed to join the chatroom
- If user sends invalid command program in client will throw InvalidArgException