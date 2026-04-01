package sd2526.trab.server.rest;

import java.util.List;
import java.util.logging.Logger;

import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;
import sd2526.trab.api.Message;
import sd2526.trab.api.rest.RestMessages;
import sd2526.trab.api.java.Messages;
import sd2526.trab.server.java.JavaMessages;

@Singleton
@Path(RestMessages.PATH)
public class RestMessagesResource extends RestResource implements RestMessages {

    private static Logger Log = Logger.getLogger(RestMessagesResource.class.getName());

    final Messages impl;

    public RestMessagesResource() {
        impl = new JavaMessages();
    }

    @Override
    public String postMessage(String pwd, Message msg) {
        Log.info("postMessage : pwd = " + pwd + "; msg = " + msg);

        return super.unwrapResultOrThrow(impl.postMessage(pwd, msg));
    }

    @Override
    public Message getMessage(String name, String mid, String pwd) {
        Log.info("getMessage : name = " + name + "; mid = " + mid + "; pwd = " + pwd);

        return super.unwrapResultOrThrow(impl.getInboxMessage(name, mid, pwd));
    }

    @Override
    public List<String> getMessages(String name, String pwd, String query) {
        Log.info("getMessages : name = " + name + "; pwd = " + pwd + "; query = " + query);

        if (query == null || query.isEmpty()) {
            return super.unwrapResultOrThrow(impl.getAllInboxMessages(name, pwd));
        }
        return super.unwrapResultOrThrow(impl.searchInbox(name, query, pwd));
    }

    @Override
    public void removeFromUserInbox(String name, String mid, String pwd) {
        Log.info("removeFromUserInbox : name = " + name + "; mid = " + mid + "; pwd = " + pwd);

        super.unwrapResultOrThrow(impl.removeInboxMessage(name, mid, pwd));
    }

    @Override
    public void deleteMessage(String name, String mid, String pwd) {
        Log.info("deleteMessage : name = " + name + "; mid = " + mid + "; pwd = " + pwd);

        super.unwrapResultOrThrow(impl.deleteMessage(name, mid, pwd));
    }

    @Override
    public void forwardMessage(String mid, Message msg) {

        super.unwrapResultOrThrow(impl.forwardMessage(mid, msg));
    }

}
