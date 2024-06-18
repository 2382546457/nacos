package com.xiaohe.nacos.common.remote.client.grpc;

import com.google.common.util.concurrent.ListenableFuture;
import com.xiaohe.nacos.api.exception.NacosException;
import com.xiaohe.nacos.api.grpc.auto.Payload;
import com.xiaohe.nacos.api.grpc.auto.RequestGrpc;
import com.xiaohe.nacos.api.remote.RequestCallBack;
import com.xiaohe.nacos.api.remote.RequestFuture;
import com.xiaohe.nacos.api.remote.request.Request;
import com.xiaohe.nacos.api.remote.response.ErrorResponse;
import com.xiaohe.nacos.api.remote.response.Response;
import com.xiaohe.nacos.common.remote.client.Connection;
import com.xiaohe.nacos.common.remote.client.RpcClient;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class GrpcConnection extends Connection {


    /**
     * grpc 提供的 channel
     */
    protected ManagedChannel channel;

    Executor executor;

    /**
     * 发送 grpc 请求给服务端的工具
     */
    protected RequestGrpc.RequestFutureStub grpcFutureServiceStub;

    /**
     * 向 grpc 服务端回复响应
     */
    protected StreamObserver<Payload> payloadStreamObserver;

    public GrpcConnection(RpcClient.ServerInfo serverInfo, Executor executor) {
        super(serverInfo);
        this.executor = executor;
    }


    @Override
    public Response request(Request request, long timeoutMills) throws NacosException {
        Payload grpcRequest = GrpcUtils.convert(request);
        ListenableFuture<Payload> requestFuture = grpcFutureServiceStub.request(grpcRequest);
        Payload grpcResponse;
        try {
            if (timeoutMills <= 0) {
                grpcResponse = requestFuture.get();
            } else {
                grpcResponse = requestFuture.get(timeoutMills, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            throw new NacosException(NacosException.SERVER_ERROR, e);
        }
        return (Response) GrpcUtils.parse(grpcResponse);
    }

    @Override
    public RequestFuture requestFuture(Request request) throws NacosException {
        Payload grpcRequest = GrpcUtils.convert(request);
        ListenableFuture<Payload> requestFuture = grpcFutureServiceStub.request(grpcRequest);
        return new RequestFuture() {
            @Override
            public boolean isDone() {
                return requestFuture.isDone();
            }

            @Override
            public Response get() throws Exception {
                Payload grpcResponse = requestFuture.get();
                Response response = (Response) GrpcUtils.parse(grpcResponse);
                if (response instanceof ErrorResponse) {
                    throw new NacosException(response.getErrorCode(), response.getMessage());
                }
                return response;
            }

            @Override
            public Response get(long timeout) throws Exception {
                Payload grpcResponse = requestFuture.get(timeout, TimeUnit.MILLISECONDS);
                Response response = (Response) GrpcUtils.parse(grpcResponse);
                if (response instanceof ErrorResponse) {
                    throw new NacosException(response.getErrorCode(), response.getMessage());
                }
                return response;
            }
        };
    }

    public void sendResponse(Response response) {
        Payload convert = GrpcUtils.convert(response);
        payloadStreamObserver.onNext(convert);
    }

    public void sendRequest(Request request) {
        Payload convert = GrpcUtils.convert(request);
        payloadStreamObserver.onNext(convert);
    }

    @Override
    public void asyncRequest(Request request, RequestCallBack requestCallBack) throws NacosException {

    }

    @Override
    public void close() {
        if (this.payloadStreamObserver != null) {
            try {
                payloadStreamObserver.onCompleted();
            } catch (Throwable ignored) {

            }
        }
    }


    public ManagedChannel getChannel() {
        return channel;
    }


    public void setChannel(ManagedChannel channel) {
        this.channel = channel;
    }


    public RequestGrpc.RequestFutureStub getGrpcFutureServiceStub() {
        return grpcFutureServiceStub;
    }


    public void setGrpcFutureServiceStub(RequestGrpc.RequestFutureStub grpcFutureServiceStub) {
        this.grpcFutureServiceStub = grpcFutureServiceStub;
    }


    public StreamObserver<Payload> getPayloadStreamObserver() {
        return payloadStreamObserver;
    }


    public void setPayloadStreamObserver(StreamObserver<Payload> payloadStreamObserver) {
        this.payloadStreamObserver = payloadStreamObserver;
    }
}
