package sd2526.trab.client.grpc;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

import sd2526.trab.api.Message;
import sd2526.trab.api.grpc.GrpcMessagesGrpc;

import sd2526.trab.api.java.Messages;
import sd2526.trab.api.java.Result;
import sd2526.trab.api.java.Result.ErrorCode;

public class GrpcMessagesClient extends GrpcClient implements Messages {

  private static Logger Log = Logger.getLogger(GrpcMessagesClient.class.getName());

  final GrpcMessagesGrpc.GrpcMessagesBlockingStub stub;

  public GrpcMessagesClient(URI serverURI) {
    super(serverURI, Log);

    stub = GrpcMessagesGrpc.newBlockingStub(channel);
  }

  @Override
  public Result<String> postMessage(String pwd, Message msg) {
    throw new RuntimeException(ErrorCode.NOT_IMPLEMENTED.toString());
  }

  @Override
  public Result<Message> getInboxMessage(String name, String mid, String pwd) {
    throw new RuntimeException(ErrorCode.NOT_IMPLEMENTED.toString());
  }

  @Override
  public Result<List<String>> getAllInboxMessages(String name, String pwd) {
    throw new RuntimeException(ErrorCode.NOT_IMPLEMENTED.toString());
  }

  @Override
  public Result<Void> removeInboxMessage(String name, String mid, String pwd) {
    throw new RuntimeException(ErrorCode.NOT_IMPLEMENTED.toString());
  }

  @Override
  public Result<Void> deleteMessage(String name, String mid, String pwd) {
    throw new RuntimeException(ErrorCode.NOT_IMPLEMENTED.toString());
  }

  @Override
  public Result<List<String>> searchInbox(String name, String pwd, String query) {
    throw new RuntimeException(ErrorCode.NOT_IMPLEMENTED.toString());
  }

  @Override
  public Result<Void> forwardMessage(String msgId, Message msg) {
    throw new RuntimeException(ErrorCode.NOT_IMPLEMENTED.toString());
  }

}
