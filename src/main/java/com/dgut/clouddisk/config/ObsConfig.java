package com.dgut.clouddisk.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;


@Configuration
@PropertySource(value="classpath:obsConfig.properties")
public class ObsConfig {
    @Value("${obs.user_name}")
    private String userName;
    @Value("${obs.access_key_id}")
    private String accessKeyId;
    @Value("${obs.secret_access_key}")
    private String secretAccessKey;
    @Value("${obs.endpoint}")
    private String endpoint;
    @Value("${obs.domain_name}")
    private String domainName;
    @Value("${obs.bucket_name}")
    private String bucketName;

    public String getBucketName() {
        return bucketName;
    }
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getAccessKeyId() {
        return accessKeyId;
    }
    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }
    public String getSecretAccessKey() {
        return secretAccessKey;
    }
    public void setSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;
    }
    public String getEndpoint() {
        return endpoint;
    }
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
    public String getDomainName() {
        return domainName;
    }
    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public ObsConfig() {
        super();
        System.out.println(this.getClass().getSimpleName() + "实例化........");
    }

    @Override
    public String toString() {
        return "ObsConfig{" +
                "userName='" + userName + '\'' +
                ", accessKeyId='" + accessKeyId + '\'' +
                ", secretAccessKey='" + secretAccessKey + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", domainName='" + domainName + '\'' +
                ", bucketName='" + bucketName + '\'' +
                '}';
    }
}
