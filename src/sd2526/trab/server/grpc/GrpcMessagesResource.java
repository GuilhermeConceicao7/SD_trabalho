package sd2526.trab.server.grpc;

import java.util.logging.Logger;

import com.google.protobuf.Empty;

import io.grpc.stub.StreamObserver;
import sd2526.trab.api.grpc.GrpcMessagesGrpc;
import sd2526.trab.api.grpc.Messages.*;
import sd2526.trab.api.java.Messages;
import sd2526.trab.server.java.JavaMessages;
import sd2526.trab.server.util.DataModelAdaptor;

public class GrpcMessagesResource extends GrpcMessagesGrpc.GrpcMessagesImplBase {

    private static Logger Log = Logger.getLogger(GrpcMessagesResource.class.getName());

    final Messages impl;

    public GrpcMessagesResource() {
        this.impl = new JavaMessages();
    }

    @Override
    public void postMessage(PostMessageArgs request, StreamObserver<PostMessageResult> responseObserver) {
        Log.info("postMessage");

        var msg = DataModelAdaptor.fromGrpc(request.getMessage());
        var result = impl.postMessage(request.getPwd(), msg);

        GrpcController.toGrpcResult(responseObserver, result,
                mid -> PostMessageResult.newBuilder().setMid(mid).build());
    }

    @Override
    public void getInboxMessage(GetInboxMessageArgs request, StreamObserver<GrpcMessage> responseObserver) {
        Log.info("getInboxMessage : name=" + request.getName() + " mid=" + request.getMid());

        var result = impl.getInboxMessage(request.getName(), request.getMid(), request.getPwd());

        GrpcController.toGrpcResult(responseObserver, result, DataModelAdaptor::toGrpc);
    }

    @Override
    public void getAllInboxMessages(GetAllInboxMessagesArgs request,
            StreamObserver<GetAllInboxMessagesResult> responseObserver) {
        Log.info("getAllInboxMessages : name=" + request.getName());

        var result = impl.getAllInboxMessages(request.getName(), request.getPwd());

        GrpcController.toGrpcResult(responseObserver, result,
                mids -> GetAllInboxMessagesResult.newBuilder().addAllMids(mids).build());
    }

    @Override
    public void removeInboxMessage(RemoveInboxMessageArgs request, StreamObserver<Empty> responseObserver) {
        Log.info("removeInboxMessage : name=" + request.getName() + " mid=" + request.getMid());

        var result = impl.removeInboxMessage(request.getName(), request.getMid(), request.getPwd());

        GrpcController.toGrpcResult(responseObserver, result, v -> Empty.getDefaultInstance());
    }

    @Override
    public void deleteMessage(DeleteMessageArgs request, StreamObserver<Empty> responseObserver) {
        Log.info("deleteMessage : name=" + request.getName() + " mid=" + request.getMid());

        var result = impl.deleteMessage(request.getName(), request.getMid(), request.getPwd());

        GrpcController.toGrpcResult(responseObserver, result, v -> Empty.getDefaultInstance());
    }

    @Override
    public void searchInbox(SearchInboxArgs request, StreamObserver<SearchInboxResult> responseObserver) {
        Log.info("searchInbox : name=" + request.getName() + " query=" + request.getQuery());

        var result = impl.searchInbox(request.getName(), request.getPwd(), request.getQuery());

        GrpcController.toGrpcResult(responseObserver, result,
                mids -> SearchInboxResult.newBuilder().addAllMids(mids).build());
    }
}