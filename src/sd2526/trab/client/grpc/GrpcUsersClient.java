package sd2526.trab.client.grpc;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import sd2526.trab.api.User;
import sd2526.trab.api.grpc.GrpcUsersGrpc;
import sd2526.trab.api.grpc.Users.*;
import sd2526.trab.api.java.Result;
import sd2526.trab.api.java.Users;
import sd2526.trab.server.util.DataModelAdaptor;

public class GrpcUsersClient extends GrpcClient implements Users {

    private static Logger Log = Logger.getLogger(GrpcUsersClient.class.getName());

    final GrpcUsersGrpc.GrpcUsersBlockingStub stub;

    public GrpcUsersClient(URI serverURI) {
        super(serverURI, Log);
        stub = GrpcUsersGrpc.newBlockingStub(channel);
    }

    @Override
    public Result<String> postUser(User user) {
        return super.processResponse(() -> {
            var reply = stub.postUser(DataModelAdaptor.toGrpc(user));
            return reply.getUserAddress();
        });
    }

    @Override
    public Result<User> getUser(String name, String pwd) {
        return super.processResponse(() -> {
            var reply = stub.getUser(GetUserArgs.newBuilder()
                    .setName(name)
                    .setPwd(pwd)
                    .build());
            return DataModelAdaptor.fromGrpc(reply.getUser());
        });
    }

    @Override
    public Result<User> updateUser(String name, String pwd, User info) {
        return super.processResponse(() -> {
            var reply = stub.updateUser(UpdateUserArgs.newBuilder()
                    .setName(name)
                    .setPwd(pwd)
                    .setInfo(DataModelAdaptor.toGrpc(info))
                    .build());
            return DataModelAdaptor.fromGrpc(reply.getUser());
        });
    }

    @Override
    public Result<User> deleteUser(String name, String pwd) {
        return super.processResponse(() -> {
            var reply = stub.deleteUser(DeleteUserArgs.newBuilder()
                    .setName(name)
                    .setPwd(pwd)
                    .build());
            return DataModelAdaptor.fromGrpc(reply.getUser());
        });
    }

    @Override
    public Result<List<User>> searchUsers(String name, String pwd, String query) {
        return super.processResponse(() -> {
            
            Iterator<GrpcUser> it = stub.searchUsers(SearchUsersArgs.newBuilder()
                    .setName(name)
                    .setPwd(pwd)
                    .setQuery(query)
                    .build());

            List<User> users = new ArrayList<>();
            while (it.hasNext()) {
                users.add(DataModelAdaptor.fromGrpc(it.next()));
            }
            return users;
        });
    }

    @Override
    public Result<User> authenticateUser(String name, String pwd) {
        return getUser(name, pwd);
    }
}