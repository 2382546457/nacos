package com.xiaohe.nacos.api.grpc.auto;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.57.2)",
    comments = "Source: nacos_grpc_service.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class BiRequestStreamGrpc {

  private BiRequestStreamGrpc() {}

  public static final java.lang.String SERVICE_NAME = "BiRequestStream";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.xiaohe.nacos.api.grpc.auto.Payload,
      com.xiaohe.nacos.api.grpc.auto.Payload> getRequestBiStreamMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "requestBiStream",
      requestType = com.xiaohe.nacos.api.grpc.auto.Payload.class,
      responseType = com.xiaohe.nacos.api.grpc.auto.Payload.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<com.xiaohe.nacos.api.grpc.auto.Payload,
      com.xiaohe.nacos.api.grpc.auto.Payload> getRequestBiStreamMethod() {
    io.grpc.MethodDescriptor<com.xiaohe.nacos.api.grpc.auto.Payload, com.xiaohe.nacos.api.grpc.auto.Payload> getRequestBiStreamMethod;
    if ((getRequestBiStreamMethod = BiRequestStreamGrpc.getRequestBiStreamMethod) == null) {
      synchronized (BiRequestStreamGrpc.class) {
        if ((getRequestBiStreamMethod = BiRequestStreamGrpc.getRequestBiStreamMethod) == null) {
          BiRequestStreamGrpc.getRequestBiStreamMethod = getRequestBiStreamMethod =
              io.grpc.MethodDescriptor.<com.xiaohe.nacos.api.grpc.auto.Payload, com.xiaohe.nacos.api.grpc.auto.Payload>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "requestBiStream"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.xiaohe.nacos.api.grpc.auto.Payload.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.xiaohe.nacos.api.grpc.auto.Payload.getDefaultInstance()))
              .setSchemaDescriptor(new BiRequestStreamMethodDescriptorSupplier("requestBiStream"))
              .build();
        }
      }
    }
    return getRequestBiStreamMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static BiRequestStreamStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<BiRequestStreamStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<BiRequestStreamStub>() {
        @java.lang.Override
        public BiRequestStreamStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new BiRequestStreamStub(channel, callOptions);
        }
      };
    return BiRequestStreamStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static BiRequestStreamBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<BiRequestStreamBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<BiRequestStreamBlockingStub>() {
        @java.lang.Override
        public BiRequestStreamBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new BiRequestStreamBlockingStub(channel, callOptions);
        }
      };
    return BiRequestStreamBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static BiRequestStreamFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<BiRequestStreamFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<BiRequestStreamFutureStub>() {
        @java.lang.Override
        public BiRequestStreamFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new BiRequestStreamFutureStub(channel, callOptions);
        }
      };
    return BiRequestStreamFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     * <pre>
     * Sends a biStreamRequest
     * </pre>
     */
    default io.grpc.stub.StreamObserver<com.xiaohe.nacos.api.grpc.auto.Payload> requestBiStream(
        io.grpc.stub.StreamObserver<com.xiaohe.nacos.api.grpc.auto.Payload> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getRequestBiStreamMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service BiRequestStream.
   */
  public static abstract class BiRequestStreamImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return BiRequestStreamGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service BiRequestStream.
   */
  public static final class BiRequestStreamStub
      extends io.grpc.stub.AbstractAsyncStub<BiRequestStreamStub> {
    private BiRequestStreamStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BiRequestStreamStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new BiRequestStreamStub(channel, callOptions);
    }

    /**
     * <pre>
     * Sends a biStreamRequest
     * </pre>
     */
    public io.grpc.stub.StreamObserver<com.xiaohe.nacos.api.grpc.auto.Payload> requestBiStream(
        io.grpc.stub.StreamObserver<com.xiaohe.nacos.api.grpc.auto.Payload> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncBidiStreamingCall(
          getChannel().newCall(getRequestBiStreamMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service BiRequestStream.
   */
  public static final class BiRequestStreamBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<BiRequestStreamBlockingStub> {
    private BiRequestStreamBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BiRequestStreamBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new BiRequestStreamBlockingStub(channel, callOptions);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service BiRequestStream.
   */
  public static final class BiRequestStreamFutureStub
      extends io.grpc.stub.AbstractFutureStub<BiRequestStreamFutureStub> {
    private BiRequestStreamFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BiRequestStreamFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new BiRequestStreamFutureStub(channel, callOptions);
    }
  }

  private static final int METHODID_REQUEST_BI_STREAM = 0;

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
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_REQUEST_BI_STREAM:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.requestBiStream(
              (io.grpc.stub.StreamObserver<com.xiaohe.nacos.api.grpc.auto.Payload>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getRequestBiStreamMethod(),
          io.grpc.stub.ServerCalls.asyncBidiStreamingCall(
            new MethodHandlers<
              com.xiaohe.nacos.api.grpc.auto.Payload,
              com.xiaohe.nacos.api.grpc.auto.Payload>(
                service, METHODID_REQUEST_BI_STREAM)))
        .build();
  }

  private static abstract class BiRequestStreamBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    BiRequestStreamBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.xiaohe.nacos.api.grpc.auto.NacosGrpcService.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("BiRequestStream");
    }
  }

  private static final class BiRequestStreamFileDescriptorSupplier
      extends BiRequestStreamBaseDescriptorSupplier {
    BiRequestStreamFileDescriptorSupplier() {}
  }

  private static final class BiRequestStreamMethodDescriptorSupplier
      extends BiRequestStreamBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    BiRequestStreamMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (BiRequestStreamGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new BiRequestStreamFileDescriptorSupplier())
              .addMethod(getRequestBiStreamMethod())
              .build();
        }
      }
    }
    return result;
  }
}
