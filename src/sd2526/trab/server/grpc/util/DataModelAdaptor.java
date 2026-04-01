package sd2526.trab.server.util;

import java.util.HashSet;

import sd2526.trab.api.Message;
import sd2526.trab.api.User;
import sd2526.trab.api.grpc.Messages.GrpcMessage;
import sd2526.trab.api.grpc.Users.GrpcUser;

public class DataModelAdaptor {

    public static GrpcMessage toGrpc(Message msg) {
        var builder = GrpcMessage.newBuilder()
                .setSender(msg.getSender() != null ? msg.getSender() : "")
                .setCreationTime(msg.getCreationTime())
                .setSubject(msg.getSubject() != null ? msg.getSubject() : "")
                .setContents(msg.getContents() != null ? msg.getContents() : "");

        if (msg.getId() != null)
            builder.setId(msg.getId());

        if (msg.getDestination() != null)
            builder.addAllDestination(msg.getDestination());

        return builder.build();
    }

    public static Message fromGrpc(GrpcMessage grpc) {
        Message msg = new Message();
        msg.setId(grpc.getId());
        msg.setSender(grpc.getSender());
        msg.setCreationTime(grpc.getCreationTime());
        msg.setSubject(grpc.getSubject());
        msg.setContents(grpc.getContents());
        msg.setDestination(new HashSet<>(grpc.getDestinationList()));
        return msg;
    }


    public static GrpcUser toGrpc(User user) {
        var builder = GrpcUser.newBuilder();
        if (user.getName() != null)        builder.setName(user.getName());
        if (user.getPwd() != null)         builder.setPwd(user.getPwd());
        if (user.getDisplayName() != null) builder.setDisplayName(user.getDisplayName());
        if (user.getDomain() != null)      builder.setDomain(user.getDomain());
        return builder.build();
    }

    public static User fromGrpc(GrpcUser grpc) {
        return new User(
                grpc.getName(),
                grpc.hasPwd()         ? grpc.getPwd()         : null,
                grpc.hasDisplayName() ? grpc.getDisplayName() : null,
                grpc.hasDomain()      ? grpc.getDomain()      : null
        );
    }
}