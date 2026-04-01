package sd2526.trab.client.grpc;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

import sd2526.trab.api.Message;
import sd2526.trab.api.grpc.GrpcMessagesGrpc;
import sd2526.trab.api.grpc.Messages.*;
import sd2526.trab.api.java.Messages;
import sd2526.trab.api.java.Result;
import sd2526.trab.server.util.DataModelAdaptor;

public class GrpcMessagesClient extends GrpcClient implements Messages {

    private static Logger Log = Logger.getLogger(GrpcMessagesClient.class.getName());

    final GrpcMessagesGrpc.GrpcMessagesBlockingStub stub;

    public GrpcMessagesClient(URI serverURI) {
        super(serverURI, Log);
        stub = GrpcMessagesGrpc.newBlockingStub(channel);
    }

    @Override
    public Result<String> postMessage(String pwd, Message msg) {
        return super.processResponse(() -> {
            var reply = stub.postMessage(PostMessageArgs.newBuilder()
                    .setPwd(pwd)
                    .setMessage(DataModelAdaptor.toGrpc(msg))
                    .build());
            return reply.getMid();
        });
    }

    @Override
    public Result<Message> getInboxMessage(String name, String mid, String pwd) {
        return super.processResponse(() -> {
            var reply = stub.getInboxMessage(GetInboxMessageArgs.newBuilder()
                    .setName(name)
                    .setMid(mid)
                    .setPwd(pwd)
                    .build());
            return DataModelAdaptor.fromGrpc(reply);
        });
    }

    @Override
    public Result<List<String>> getAllInboxMessages(String name, String pwd) {
        return super.processResponse(() -> {
            var reply = stub.getAllInboxMessages(GetAllInboxMessagesArgs.newBuilder()
                    .setName(name)
                    .setPwd(pwd)
                    .build());
            return reply.getMidsList();
        });
    }

    @Override
    public Result<Void> removeInboxMessage(String name, String mid, String pwd) {
        return super.processResponse(() -> {
            stub.removeInboxMessage(RemoveInboxMessageArgs.newBuilder()
                    .setName(name)
                    .setMid(mid)
                    .setPwd(pwd)
                    .build());
            return null;
        });
    }

    @Override
    public Result<Void> deleteMessage(String name, String mid, String pwd) {
        return super.processResponse(() -> {
            stub.deleteMessage(DeleteMessageArgs.newBuilder()
                    .setName(name)
                    .setMid(mid)
                    .setPwd(pwd)
                    .build());
            return null;
        });
    }

    @Override
    public Result<List<String>> searchInbox(String name, String pwd, String query) {
        return super.processResponse(() -> {
            var reply = stub.searchInbox(SearchInboxArgs.newBuilder()
                    .setName(name)
                    .setPwd(pwd)
                    .setQuery(query)
                    .build());
            return reply.getMidsList();
        });
    }

    @Override
    public Result<Void> forwardMessage(String msgId, Message msg) {
        return super.processResponse(() -> {
            msg.setId(msgId);
            stub.postMessage(PostMessageArgs.newBuilder()
                    .setPwd("") 
                    .setMessage(DataModelAdaptor.toGrpc(msg))
                    .build());
            return null;
        });
    }
}