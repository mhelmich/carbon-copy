package org.distbc;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class DistBufferCacheConfiguration extends Configuration {
    @NotEmpty
    private String defaultPeerXml = "./config/peer.xml";

    @NotEmpty
    private String defaultPeerProperties = "./config/peer.properties";

    @JsonProperty
    public String getDefaultPeerXml() {
        return defaultPeerXml;
    }

    @JsonProperty
    public void setDefaultPeerXml(String defaultPeerXml) {
        this.defaultPeerXml = defaultPeerXml;
    }

    @JsonProperty
    public String getDefaultPeerProperties() {
        return defaultPeerProperties;
    }

    @JsonProperty
    public void setDefaultPeerProperties(String defaultPeerProperties) {
        this.defaultPeerProperties = defaultPeerProperties;
    }
}