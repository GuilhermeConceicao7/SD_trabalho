package sd2526.trab.client.grpc;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

import sd2526.trab.api.Message;
import sd2526.trab.server.grpc.generated_java.GrpcMessagesGrpc;
import sd2526.trab.server.grpc.generated_java.MessagesProtoBuf.DeleteMessageArgs;
import sd2526.trab.server.grpc.generated_java.MessagesProtoBuf.GetAllInboxMessagesArgs;
import sd2526.trab.server.grpc.generated_java.MessagesProtoBuf.GetInboxMessageArgs;
import sd2526.trab.server.grpc.generated_java.MessagesProtoBuf.PostMessageArgs;
import sd2526.trab.server.grpc.generated_java.MessagesProtoBuf.RemoveInboxMessageArgs;
import sd2526.trab.server.grpc.generated_java.MessagesProtoBuf.SearchInboxArgs;
import sd2526.trab.server.grpc.generated_java.MessagesProtoBuf.ForwardMessageArgs;
import sd2526.trab.server.grpc.generated_java.MessagesProtoBuf.DeleteMessageInternalArgs;
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
        return super.processResponse(() -> stub.postMessage(PostMessageArgs.newBuilder()
                .setPwd(pwd)
                .setMessage(DataModelAdaptor.Message_to_GrpcMessage(msg))
                .build()).getMid());
    }

    @Override
    public Result<Message> getInboxMessage(String name, String mid, String pwd) {
        return super.processResponse(() -> DataModelAdaptor.GrpcMessage_to_Message(
                stub.getInboxMessage(GetInboxMessageArgs.newBuilder()
                        .setName(name)
                        .setMid(mid)
                        .setPwd(pwd)
                        .build())));
    }

    @Override
    public Result<List<String>> getAllInboxMessages(String name, String pwd) {
        return super.processResponse(() -> stub.getAllInboxMessages(GetAllInboxMessagesArgs.newBuilder()
                .setName(name)
                .setPwd(pwd)
                .build()).getMidsList());
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
        return super.processResponse(() -> stub.searchInbox(SearchInboxArgs.newBuilder()
                .setName(name)
                .setPwd(pwd)
                .setQuery(query)
                .build()).getMidsList());
    }

   @Override
public Result<Void> forwardMessage(String msgId, Message msg) {
    msg.setId(msgId);
    return super.processResponse(() -> {
        stub.forwardMessage(ForwardMessageArgs.newBuilder()
                .setMid(msgId)
                .setMessage(DataModelAdaptor.Message_to_GrpcMessage(msg))
                .build());
        return null;
    });
}

    @Override
public Result<Void> deleteMessageInternal(String mid) {
    return super.processResponse(() -> {
        stub.deleteMessageInternal(DeleteMessageInternalArgs.newBuilder()
                .setMid(mid)
                .build());
        return null;
    });
}
}