package sd2526.trab.server.grpc;

import java.util.List;
import java.util.logging.Logger;

import io.grpc.stub.StreamObserver;
import sd2526.trab.api.User;
import sd2526.trab.api.grpc.GrpcUsersGrpc;
import sd2526.trab.api.grpc.Users.*;
import sd2526.trab.api.java.Users;
import sd2526.trab.server.java.JavaUsers;
import sd2526.trab.server.util.DataModelAdaptor;

public class GrpcUsersResource extends GrpcUsersGrpc.GrpcUsersImplBase {

    private static Logger Log = Logger.getLogger(GrpcUsersResource.class.getName());

    final Users impl;

    public GrpcUsersResource() {
        this.impl = new JavaUsers();
    }

    @Override
    public void postUser(GrpcUser request, StreamObserver<PostUserResult> responseObserver) {
        Log.info("postUser : " + request.getName());

        var user = DataModelAdaptor.fromGrpc(request);
        var result = impl.postUser(user);

        GrpcController.toGrpcResult(responseObserver, result,
                addr -> PostUserResult.newBuilder().setUserAddress(addr).build());
    }

    @Override
    public void getUser(GetUserArgs request, StreamObserver<GetUserResult> responseObserver) {
        Log.info("getUser : name=" + request.getName());

        var result = impl.getUser(request.getName(), request.getPwd());

        GrpcController.toGrpcResult(responseObserver, result,
                user -> GetUserResult.newBuilder().setUser(DataModelAdaptor.toGrpc(user)).build());
    }

    @Override
    public void updateUser(UpdateUserArgs request, StreamObserver<UpdateUserResult> responseObserver) {
        Log.info("updateUser : name=" + request.getName());

        var info = DataModelAdaptor.fromGrpc(request.getInfo());
        var result = impl.updateUser(request.getName(), request.getPwd(), info);

        GrpcController.toGrpcResult(responseObserver, result,
                user -> UpdateUserResult.newBuilder().setUser(DataModelAdaptor.toGrpc(user)).build());
    }

    @Override
    public void deleteUser(DeleteUserArgs request, StreamObserver<DeleteUserResult> responseObserver) {
        Log.info("deleteUser : name=" + request.getName());

        var result = impl.deleteUser(request.getName(), request.getPwd());

        GrpcController.toGrpcResult(responseObserver, result,
                user -> DeleteUserResult.newBuilder().setUser(DataModelAdaptor.toGrpc(user)).build());
    }

    @Override
    public void searchUsers(SearchUsersArgs request, StreamObserver<GrpcUser> responseObserver) {
        Log.info("searchUsers : name=" + request.getName() + " query=" + request.getQuery());

        var result = impl.searchUsers(request.getName(), request.getPwd(), request.getQuery());

        if (!result.isOK()) {
            responseObserver.onError(GrpcController.errorCodeToStatus(result.error()));
            return;
        }

        // searchUsers usa server-streaming — envia um GrpcUser de cada vez
        for (User user : result.value()) {
            responseObserver.onNext(DataModelAdaptor.toGrpc(user));
        }
        responseObserver.onCompleted();
    }
}