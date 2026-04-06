package sd2526.trab.server.grpc;

import java.util.logging.Logger;

import com.google.protobuf.Empty;

import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.StreamObserver;

import sd2526.trab.api.java.Messages;
import sd2526.trab.api.java.Users;
import sd2526.trab.server.grpc.generated_java.GrpcMessagesGrpc;
import sd2526.trab.server.grpc.generated_java.GrpcUsersGrpc;
import sd2526.trab.server.grpc.generated_java.MessagesProtoBuf.DeleteMessageArgs;
import sd2526.trab.server.grpc.generated_java.MessagesProtoBuf.GetAllInboxMessagesArgs;
import sd2526.trab.server.grpc.generated_java.MessagesProtoBuf.GetAllInboxMessagesResult;
import sd2526.trab.server.grpc.generated_java.MessagesProtoBuf.GetInboxMessageArgs;
import sd2526.trab.server.grpc.generated_java.MessagesProtoBuf.GrpcMessage;
import sd2526.trab.server.grpc.generated_java.MessagesProtoBuf.PostMessageArgs;
import sd2526.trab.server.grpc.generated_java.MessagesProtoBuf.PostMessageResult;
import sd2526.trab.server.grpc.generated_java.MessagesProtoBuf.RemoveInboxMessageArgs;
import sd2526.trab.server.grpc.generated_java.MessagesProtoBuf.SearchInboxArgs;
import sd2526.trab.server.grpc.generated_java.MessagesProtoBuf.SearchInboxResult;
import sd2526.trab.server.java.JavaMessages;
import sd2526.trab.server.java.JavaUsers;
import sd2526.trab.server.util.DataModelAdaptor;

public class GrpcMessagesController extends GrpcController implements GrpcMessagesGrpc.AsyncService, BindableService {

    Messages impl;

    public GrpcMessagesController(String domain) {
        this.impl = new JavaMessages(domain);
    }

    @Override
    public final ServerServiceDefinition bindService() {
        return GrpcMessagesGrpc.bindService(this);
    }

    @Override
    public void postMessage(PostMessageArgs request, StreamObserver<PostMessageResult> responseObserver) {
        super.toGrpcResult(responseObserver,
                impl.postMessage(request.getPwd(), DataModelAdaptor.GrpcMessage_to_Message(request.getMessage())),
                (mid) -> PostMessageResult.newBuilder().setMid(mid).build());
    }

    @Override
    public void getInboxMessage(GetInboxMessageArgs request, StreamObserver<GrpcMessage> responseObserver) {
        super.toGrpcResult(responseObserver,
                impl.getInboxMessage(request.getName(), request.getMid(), request.getPwd()),
                (msg) -> DataModelAdaptor.Message_to_GrpcMessage(msg));
    }

    @Override
    public void getAllInboxMessages(GetAllInboxMessagesArgs request,
            StreamObserver<GetAllInboxMessagesResult> responseObserver) {
        super.toGrpcResult(responseObserver,
                impl.getAllInboxMessages(request.getName(), request.getPwd()),
                (mids) -> GetAllInboxMessagesResult.newBuilder().addAllMids(mids).build());
    }

    @Override
    public void removeInboxMessage(RemoveInboxMessageArgs request, StreamObserver<Empty> responseObserver) {
        super.toGrpcResult(responseObserver,
                impl.removeInboxMessage(request.getName(), request.getMid(), request.getPwd()),
                (v) -> Empty.getDefaultInstance());
    }

    @Override
    public void deleteMessage(DeleteMessageArgs request, StreamObserver<Empty> responseObserver) {
        super.toGrpcResult(responseObserver,
                impl.deleteMessage(request.getName(), request.getMid(), request.getPwd()),
                (v) -> Empty.getDefaultInstance());
    }

    @Override
    public void searchInbox(SearchInboxArgs request, StreamObserver<SearchInboxResult> responseObserver) {
        super.toGrpcResult(responseObserver,
                impl.searchInbox(request.getName(), request.getPwd(), request.getQuery()),
                (mids) -> SearchInboxResult.newBuilder().addAllMids(mids).build());
    }
}
