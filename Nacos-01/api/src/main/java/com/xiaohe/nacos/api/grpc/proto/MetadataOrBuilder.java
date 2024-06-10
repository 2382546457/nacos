// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: nacos_grpc_service.proto

package com.xiaohe.nacos.api.grpc.proto;

public interface MetadataOrBuilder extends
    // @@protoc_insertion_point(interface_extends:Metadata)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string type = 3;</code>
   * @return The type.
   */
  String getType();
  /**
   * <code>string type = 3;</code>
   * @return The bytes for type.
   */
  com.google.protobuf.ByteString
      getTypeBytes();

  /**
   * <code>string clientIp = 8;</code>
   * @return The clientIp.
   */
  String getClientIp();
  /**
   * <code>string clientIp = 8;</code>
   * @return The bytes for clientIp.
   */
  com.google.protobuf.ByteString
      getClientIpBytes();

  /**
   * <code>map&lt;string, string&gt; headers = 7;</code>
   */
  int getHeadersCount();
  /**
   * <code>map&lt;string, string&gt; headers = 7;</code>
   */
  boolean containsHeaders(
      String key);
  /**
   * Use {@link #getHeadersMap()} instead.
   */
  @Deprecated
  java.util.Map<String, String>
  getHeaders();
  /**
   * <code>map&lt;string, string&gt; headers = 7;</code>
   */
  java.util.Map<String, String>
  getHeadersMap();
  /**
   * <code>map&lt;string, string&gt; headers = 7;</code>
   */
  /* nullable */
String getHeadersOrDefault(
      String key,
      /* nullable */
String defaultValue);
  /**
   * <code>map&lt;string, string&gt; headers = 7;</code>
   */
  String getHeadersOrThrow(
      String key);
}
