package sd2526.trab.client.grpc;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import sd2526.trab.api.User;

import sd2526.trab.api.java.Result;
import sd2526.trab.api.java.Users;
import sd2526.trab.server.grpc.generated_java.GrpcUsersGrpc;
import sd2526.trab.server.grpc.generated_java.UsersProtoBuf.DeleteUserArgs;
import sd2526.trab.server.grpc.generated_java.UsersProtoBuf.DeleteUserResult;
import sd2526.trab.server.grpc.generated_java.UsersProtoBuf.GetUserArgs;
import sd2526.trab.server.grpc.generated_java.UsersProtoBuf.GetUserResult;
import sd2526.trab.server.grpc.generated_java.UsersProtoBuf.GrpcUser;
import sd2526.trab.server.grpc.generated_java.UsersProtoBuf.SearchUsersArgs;
import sd2526.trab.server.grpc.generated_java.UsersProtoBuf.UpdateUserArgs;
import sd2526.trab.server.grpc.generated_java.UsersProtoBuf.UpdateUserResult;
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
        return super.processResponse(() -> stub.postUser(DataModelAdaptor.User_to_GrpcUser(user))
                .getUserAddress());
    }

   @Override
public Result<User> getUser(String name, String pwd) {
    return super.processResponse(() -> {
        GetUserResult res = stub.getUser(GetUserArgs.newBuilder()
                .setName(name)
                .setPwd(pwd)
                .build());
        return DataModelAdaptor.GrpcUser_to_User(res.getUser());
    });
}

    @Override
    public Result<User> updateUser(String name, String pwd, User info) {
        UpdateUserResult res = stub.updateUser(UpdateUserArgs.newBuilder()
                .setName(name)
                .setPwd(pwd)
                .setInfo(DataModelAdaptor.User_to_GrpcUser(info))
                .build());
        return super.processResponse(() -> DataModelAdaptor.GrpcUser_to_User(res.getUser()));

    }

    @Override
    public Result<User> deleteUser(String name, String pwd) {
        DeleteUserResult res = stub.deleteUser(DeleteUserArgs.newBuilder()
                .setName(name)
                .setPwd(pwd)
                .build());
        return super.processResponse(() -> DataModelAdaptor.GrpcUser_to_User(res.getUser()));

    }

    @Override
    public Result<List<User>> searchUsers(String name, String pwd, String query) {
        Iterator<GrpcUser> it = stub.searchUsers(SearchUsersArgs.newBuilder()
                .setName(name)
                .setPwd(pwd)
                .setQuery(query)
                .build());
        return super.processResponse(() -> {
            List<User> users = new ArrayList<>();
            while (it.hasNext()) {
                users.add(DataModelAdaptor.GrpcUser_to_User(it.next()));
            }
            return users;
        });
    }

    @Override
public Result<User> authenticateUser(String name, String pwd) {
    return super.processResponse(() -> {
        GetUserResult res = stub.authenticateUser(GetUserArgs.newBuilder()
                .setName(name)
                .setPwd(pwd)
                .build());
        return DataModelAdaptor.GrpcUser_to_User(res.getUser());
    });
}
}