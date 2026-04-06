package sd2526.trab.server.grpc.util;

import sd2526.trab.api.User;

import java.util.HashSet;

import sd2526.trab.api.Message;
import sd2526.trab.server.grpc.generated_java.UsersProtoBuf.GrpcUser;
import sd2526.trab.server.grpc.generated_java.UsersProtoBuf.GrpcUser.Builder;
import sd2526.trab.server.grpc.generated_java.MessagesProtoBuf.GrpcMessage;

public class DataModelAdaptor {

    public static User GrpcUser_to_User(GrpcUser from) {
        return new User(
                from.getName(),
                from.hasPwd() ? from.getPwd() : null,
                from.hasDisplayName() ? from.getDisplayName() : null,
                from.hasDomain() ? from.getDomain() : null);
    }

    public static GrpcUser User_to_GrpcUser(User from) {
        Builder b = GrpcUser.newBuilder();

        if (from.getName() != null)
            b.setName(from.getName());

        if (from.getPwd() != null)
            b.setPwd(from.getPwd());

        if (from.getDisplayName() != null)
            b.setDisplayName(from.getDisplayName());

        if (from.getDomain() != null)
            b.setDomain(from.getDomain());

        return b.build();
    }

    public static Message GrpcMessage_to_Message(GrpcMessage from) {
        Message msg = new Message(
                from.getId(),
                from.getSender(),
                new HashSet<>(from.getDestinationList()),
                from.getSubject(),
                from.getContents());
        msg.setCreationTime(from.getCreationTime());
        return msg;
    }

    public static GrpcMessage Message_to_GrpcMessage(Message from) {
        GrpcMessage.Builder b = GrpcMessage.newBuilder();

        if (from.getId() != null)
            b.setId(from.getId());

        if (from.getSender() != null)
            b.setSender(from.getSender());

        if (from.getDestination() != null)
            b.addAllDestination(from.getDestination());

        if (from.getSubject() != null)
            b.setSubject(from.getSubject());

        if (from.getContents() != null)
            b.setContents(from.getContents());

        b.setCreationTime(from.getCreationTime());

        return b.build();
    }
}
