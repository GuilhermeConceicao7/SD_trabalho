package sd2526.trab.server.resources;
 
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.net.InetAddress;
import java.net.URI;
 
import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.Path;
 
import sd2526.trab.api.Message;
import sd2526.trab.api.User;
import sd2526.trab.api.rest.RestMessages;
import sd2526.trab.server.persistence.Hibernate;
import sd2526.trab.server.discovery.Discovery;
import sd2526.trab.client.rest.RestUsersClient;
import sd2526.trab.api.Result;
import sd2526.trab.server.discovery.Discovery;
import sd2526.trab.client.rest.RestUsersClient;
 
@Singleton
@Path(RestMessages.PATH)
public class MessagesResource implements RestMessages {

    private final Hibernate hibernate;
    private final String domain;
 
    private static Logger Log = Logger.getLogger(MessagesResource.class.getName());
 
    // Guarda todas as mensagens: msgId -> Message
    private final Map<String, Message> allMessages = new ConcurrentHashMap<>();
 
    // Guarda as inboxes: userName -> Set de msgIds
    private final Map<String, Set<String>> inboxes = new ConcurrentHashMap<>();

    private final Discovery discovery;
 
    public MessagesResource() {
    this.hibernate = Hibernate.getInstance();
    try {
        String hostname = InetAddress.getLocalHost().getHostName();
        this.domain = hostname.substring(hostname.indexOf('.') + 1);
         this.discovery = new Discovery(); // javardice
        this.discovery.start(); // javardice
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}

    @Override
    public String postMessage(String pwd, Message msg) {
        if(pwd == null || msg == null || msg.getSender() == null || msg.getDestination() == null 
        || msg.getDestination().isEmpty() || msg.getContents() == null) {
            throw new WebApplicationException(Status.BAD_REQUEST);
        }

        String senderName = msg.getSender();
        if(senderName.contains("@")){
            String[] sender = senderName.split("@");
                senderName = sender[0];
    } 

    URI[] uris = discovery.knownUrisOf("Users", 1); // chat
    RestUsersClient usersClient = new RestUsersClient(uris[0]); // chat
    Result<User> result = usersClient.getUser(senderName, pwd); // chat
    if (!result.isOK()) // chat
    throw new WebApplicationException(Status.FORBIDDEN); // chat
    User senderUser = result.value(); // chat

    if (msg.getId() != null && allMessages.containsKey(msg.getId())){
        return msg.getId();
    }

    String msgId = UUID.randomUUID().toString();
    msg.setId(msgId);

    msg.setSender(senderUser.getDisplayName() + " <" + senderName + "@" + domain + ">");

    allMessages.put(msgId, msg);

     for (String dest : msg.getDestination()) {
        String destName = dest;
        String destDomain = domain;

        if (dest.contains("@")) {
            String[] parts = dest.split("@");
            destName = parts[0];
            destDomain = parts[1];
        }

        if (destDomain.equals(domain)) {
            if(!inboxes.containsKey(destName)) {
                inboxes.put(destName, ConcurrentHashMap.newKeySet());
            }
            inboxes.get(destName).add(msgId);
        }
        
    }

    
    return msgId;
}


@Override
public Message getMessage(String name, String mid, String pwd) {
    throw new WebApplicationException(Status.NOT_IMPLEMENTED);
}

@Override
public List<String> getMessages(String name, String pwd, String query) {
    throw new WebApplicationException(Status.NOT_IMPLEMENTED);
}

@Override
public void removeFromUserInbox(String name, String mid, String pwd) {
    throw new WebApplicationException(Status.NOT_IMPLEMENTED);
}

@Override
public void deleteMessage(String name, String mid, String pwd) {
    throw new WebApplicationException(Status.NOT_IMPLEMENTED);
}




}
        
