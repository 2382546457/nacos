package com.xiaohe.nacos.common.remote;

/**
 * tls配置，默认不启用
 */
public class TlsConfig {
    /**
     * ssl provider,default OPENSSL,JDK,OPENSSL_REFCNT.
     */
    private String sslProvider = "";

    /**
     * enable tls.默认不启动tls
     */
    private Boolean enableTls = false;

    /**
     * tls version: TLSv1.1,TLSv1.2,TLSv1.3
     * if want to support multi protocol, use comma  seperated. like TLSv1.1,TLSv1.2,TLSv1.3
     */
    private String protocols;

    /**
     * cipherList,  same of usage protocols.
     */
    private String ciphers;

    /**
     * private key.
     */
    private String certPrivateKey;

    /**
     * certificate file.
     */
    private String certChainFile;

    /**
     * read certPrivateKey file when need password.
     */
    private String certPrivateKeyPassword;

    /**
     * mutualAuth,if true,need provider certPrivateKey and certChainFile.
     */
    private Boolean mutualAuthEnable = false;

    /**
     * ignore certificate valid.
     */
    private Boolean trustAll = false;

    /**
     * collection of trust certificate file.
     */
    private String trustCollectionCertFile;

    public Boolean getEnableTls() {
        return enableTls;
    }

    public void setEnableTls(Boolean enableTls) {
        this.enableTls = enableTls;
    }

    public Boolean getMutualAuthEnable() {
        return mutualAuthEnable;
    }

    public void setMutualAuthEnable(Boolean mutualAuthEnable) {
        this.mutualAuthEnable = mutualAuthEnable;
    }

    public String getProtocols() {
        return protocols;
    }

    public void setProtocols(String protocols) {
        this.protocols = protocols;
    }

    public Boolean getTrustAll() {
        return trustAll;
    }

    public void setTrustAll(Boolean trustAll) {
        this.trustAll = trustAll;
    }

    public String getCiphers() {
        return ciphers;
    }

    public void setCiphers(String ciphers) {
        this.ciphers = ciphers;
    }

    public String getTrustCollectionCertFile() {
        return trustCollectionCertFile;
    }

    public void setTrustCollectionCertFile(String trustCollectionCertFile) {
        this.trustCollectionCertFile = trustCollectionCertFile;
    }

    public String getCertPrivateKeyPassword() {
        return certPrivateKeyPassword;
    }

    public void setCertPrivateKeyPassword(String certPrivateKeyPassword) {
        this.certPrivateKeyPassword = certPrivateKeyPassword;
    }

    public String getCertPrivateKey() {
        return certPrivateKey;
    }

    public void setCertPrivateKey(String certPrivateKey) {
        this.certPrivateKey = certPrivateKey;
    }

    public String getCertChainFile() {
        return certChainFile;
    }

    public void setCertChainFile(String certChainFile) {
        this.certChainFile = certChainFile;
    }

    public String getSslProvider() {
        return sslProvider;
    }

    public void setSslProvider(String sslProvider) {
        this.sslProvider = sslProvider;
    }

}
