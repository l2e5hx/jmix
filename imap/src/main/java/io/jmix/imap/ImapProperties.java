/*
 * Copyright 2020 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.imap;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "jmix.imap")
@ConstructorBinding
public class ImapProperties {
    boolean trustAllCertificates;
    int updateBatchSize;
    boolean clearCustomFlags;
    boolean debug;
    int timeoutSeconds;
    int syncTimeout;
    int eventsBatchSize;

    public ImapProperties(@DefaultValue("false") boolean trustAllCertificates,
                          @DefaultValue("100") int updateBatchSize,
                          @DefaultValue("false") boolean clearCustomFlags,
                          @DefaultValue("false") boolean debug,
                          @DefaultValue("5") int timeoutSeconds,
                          @DefaultValue("5") int syncTimeout,
                          @DefaultValue("20") int eventsBatchSize) {
        this.trustAllCertificates = trustAllCertificates;
        this.updateBatchSize = updateBatchSize;
        this.clearCustomFlags = clearCustomFlags;
        this.debug = debug;
        this.timeoutSeconds = timeoutSeconds;
        this.syncTimeout = syncTimeout;
        this.eventsBatchSize = eventsBatchSize;
    }

    public boolean isTrustAllCertificates() {
        return trustAllCertificates;
    }

    public int getUpdateBatchSize() {
        return updateBatchSize;
    }

    public boolean isClearCustomFlags() {
        return clearCustomFlags;
    }

    public boolean isDebug() {
        return debug;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public int getSyncTimeout() {
        return syncTimeout;
    }

    public int getEventsBatchSize() {
        return eventsBatchSize;
    }
}
