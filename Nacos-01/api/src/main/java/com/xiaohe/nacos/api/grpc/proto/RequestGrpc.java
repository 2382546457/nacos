package com.xiaohe.nacos.api.grpc.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.57.2)",
    comments = "Source: nacos_grpc_service.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class RequestGrpc {

  private RequestGrpc() {}

  public static final java.lang.String SERVICE_NAME = "Request";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.xiaohe.nacos.api.grpc.proto.Payload,
      com.xiaohe.nacos.api.grpc.proto.Payload> getRequestMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "request",
      requestType = com.xiaohe.nacos.api.grpc.proto.Payload.class,
      responseType = com.xiaohe.nacos.api.grpc.proto.Payload.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.xiaohe.nacos.api.grpc.proto.Payload,
      com.xiaohe.nacos.api.grpc.proto.Payload> getRequestMethod() {
    io.grpc.MethodDescriptor<com.xiaohe.nacos.api.grpc.proto.Payload, com.xiaohe.nacos.api.grpc.proto.Payload> getRequestMethod;
    if ((getRequestMethod = RequestGrpc.getRequestMethod) == null) {
      synchronized (RequestGrpc.class) {
        if ((getRequestMethod = RequestGrpc.getRequestMethod) == null) {
          RequestGrpc.getRequestMethod = getRequestMethod =
              io.grpc.MethodDescriptor.<com.xiaohe.nacos.api.grpc.proto.Payload, com.xiaohe.nacos.api.grpc.proto.Payload>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "request"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.xiaohe.nacos.api.grpc.proto.Payload.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.xiaohe.nacos.api.grpc.proto.Payload.getDefaultInstance()))
              .setSchemaDescriptor(new RequestMethodDescriptorSupplier("request"))
              .build();
        }
      }
    }
    return getRequestMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static RequestStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<RequestStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<RequestStub>() {
        @java.lang.Override
        public RequestStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new RequestStub(channel, callOptions);
        }
      };
    return RequestStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static RequestBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<RequestBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<RequestBlockingStub>() {
        @java.lang.Override
        public RequestBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new RequestBlockingStub(channel, callOptions);
        }
      };
    return RequestBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static RequestFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<RequestFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<RequestFutureStub>() {
        @java.lang.Override
        public RequestFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new RequestFutureStub(channel, callOptions);
        }
      };
    return RequestFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     * <pre>
     * Sends a commonRequest
     * </pre>
     */
    default void request(com.xiaohe.nacos.api.grpc.proto.Payload request,
        io.grpc.stub.StreamObserver<com.xiaohe.nacos.api.grpc.proto.Payload> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRequestMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service Request.
   */
  public static abstract class RequestImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return RequestGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service Request.
   */
  public static final class RequestStub
      extends io.grpc.stub.AbstractAsyncStub<RequestStub> {
    private RequestStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RequestStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new RequestStub(channel, callOptions);
    }

    /**
     * <pre>
     * Sends a commonRequest
     * </pre>
     */
    public void request(com.xiaohe.nacos.api.grpc.proto.Payload request,
        io.grpc.stub.StreamObserver<com.xiaohe.nacos.api.grpc.proto.Payload> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRequestMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service Request.
   */
  public static final class RequestBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<RequestBlockingStub> {
    private RequestBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RequestBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new RequestBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Sends a commonRequest
     * </pre>
     */
    public com.xiaohe.nacos.api.grpc.proto.Payload request(com.xiaohe.nacos.api.grpc.proto.Payload request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRequestMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service Request.
   */
  public static final class RequestFutureStub
      extends io.grpc.stub.AbstractFutureStub<RequestFutureStub> {
    private RequestFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RequestFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new RequestFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Sends a commonRequest
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.xiaohe.nacos.api.grpc.proto.Payload> request(
        com.xiaohe.nacos.api.grpc.proto.Payload request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRequestMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_REQUEST = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_REQUEST:
          serviceImpl.request((com.xiaohe.nacos.api.grpc.proto.Payload) request,
              (io.grpc.stub.StreamObserver<com.xiaohe.nacos.api.grpc.proto.Payload>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getRequestMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.xiaohe.nacos.api.grpc.proto.Payload,
              com.xiaohe.nacos.api.grpc.proto.Payload>(
                service, METHODID_REQUEST)))
        .build();
  }

  private static abstract class RequestBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    RequestBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.xiaohe.nacos.api.grpc.proto.NacosGrpcService.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("Request");
    }
  }

  private static final class RequestFileDescriptorSupplier
      extends RequestBaseDescriptorSupplier {
    RequestFileDescriptorSupplier() {}
  }

  private static final class RequestMethodDescriptorSupplier
      extends RequestBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    RequestMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (RequestGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new RequestFileDescriptorSupplier())
              .addMethod(getRequestMethod())
              .build();
        }
      }
    }
    return result;
  }
}
