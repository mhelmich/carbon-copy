/*
 *
 *  Copyright 2017 Marco Helmich
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.carbon.copy;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class CarbonCopyConfiguration extends Configuration {
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