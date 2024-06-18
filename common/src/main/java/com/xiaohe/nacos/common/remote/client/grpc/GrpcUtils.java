package com.xiaohe.nacos.common.remote.client.grpc;

import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.UnsafeByteOperations;
import com.xiaohe.nacos.api.exception.NacosException;
import com.xiaohe.nacos.api.grpc.auto.Metadata;
import com.xiaohe.nacos.api.grpc.auto.Payload;
import com.xiaohe.nacos.api.remote.request.Request;
import com.xiaohe.nacos.api.remote.request.RequestMeta;
import com.xiaohe.nacos.api.remote.response.Response;
import com.xiaohe.nacos.api.utils.NetUtils;
import com.xiaohe.nacos.common.remote.PayloadRegistry;
import com.xiaohe.nacos.common.remote.exception.RemoteException;
import com.xiaohe.nacos.common.utils.JacksonUtils;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class GrpcUtils {

    /**
     * convert request to payload.
     *
     * @param request request.
     * @param meta    request meta.
     * @return payload.
     */
    public static Payload convert(Request request, RequestMeta meta) {
        //meta.
        Payload.Builder payloadBuilder = Payload.newBuilder();
        Metadata.Builder metaBuilder = Metadata.newBuilder();
        if (meta != null) {
            metaBuilder.putAllHeaders(request.getHeaders()).setType(request.getClass().getSimpleName());
        }
        metaBuilder.setClientIp(NetUtils.localIP());
        payloadBuilder.setMetadata(metaBuilder.build());

        // request body .
        byte[] jsonBytes = convertRequestToByte(request);
        return payloadBuilder.setBody(Any.newBuilder().setValue(UnsafeByteOperations.unsafeWrap(jsonBytes))).build();

    }

    /**
     * convert request to payload.
     *
     * @param request request.
     * @return payload.
     */
    public static Payload convert(Request request) {

        Metadata newMeta = Metadata.newBuilder().setType(request.getClass().getSimpleName())
                .setClientIp(NetUtils.localIP()).putAllHeaders(request.getHeaders()).build();

        byte[] jsonBytes = convertRequestToByte(request);

        Payload.Builder builder = Payload.newBuilder();

        return builder.setBody(Any.newBuilder().setValue(UnsafeByteOperations.unsafeWrap(jsonBytes)))
                .setMetadata(newMeta).build();

    }

    /**
     * convert response to payload.
     *
     * @param response response.
     * @return payload.
     */
    public static Payload convert(Response response) {
        byte[] jsonBytes = JacksonUtils.toJsonBytes(response);

        Metadata.Builder metaBuilder = Metadata.newBuilder().setType(response.getClass().getSimpleName());
        return Payload.newBuilder().setBody(Any.newBuilder().setValue(UnsafeByteOperations.unsafeWrap(jsonBytes)))
                .setMetadata(metaBuilder.build()).build();
    }

    private static byte[] convertRequestToByte(Request request) {
        Map<String, String> requestHeaders = new HashMap<>(request.getHeaders());
        request.clearHeaders();
        byte[] jsonBytes = JacksonUtils.toJsonBytes(request);
        request.putAllHeader(requestHeaders);
        return jsonBytes;
    }

    /**
     * parse payload to request/response model.
     *
     * @param payload payload to be parsed.
     * @return payload
     */
    public static Object parse(Payload payload) {
        Class classType = PayloadRegistry.getClassByType(payload.getMetadata().getType());
        if (classType != null) {
            ByteString byteString = payload.getBody().getValue();
            ByteBuffer byteBuffer = byteString.asReadOnlyByteBuffer();
            Object obj = JacksonUtils.toObj(new ByteBufferBackedInputStream(byteBuffer), classType);
            if (obj instanceof Request) {
                ((Request) obj).putAllHeader(payload.getMetadata().getHeadersMap());
            }
            return obj;
        } else {
            throw new RemoteException(NacosException.SERVER_ERROR, "Unknown payload type:" + payload.getMetadata().getType());
        }
    }
}
