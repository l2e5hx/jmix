/*
 * Copyright (c) 2008-2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.imap;


import io.jmix.core.security.Authenticator;
import io.jmix.imap.data.ImapDataProvider;
import io.jmix.imap.entity.ImapMailBox;
import io.jmix.imap.sync.events.ImapEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component("imap_SyncManager")
public class ImapSyncManager {

    private final static Logger log = LoggerFactory.getLogger(ImapSyncManager.class);

    @Autowired
    protected ImapDataProvider dao;
    @Autowired
    protected ImapEvents imapEvents;
    @Autowired
    protected Authenticator authentication;

    private final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactory() {
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(@Nonnull Runnable r) {
            Thread thread = new Thread(
                    r, "ImapMailBoxSync-" + threadNumber.getAndIncrement()
            );
            thread.setDaemon(true);
            return thread;
        }
    });

    private final ConcurrentMap<UUID, ScheduledExecutorService> syncRefreshers = new ConcurrentHashMap<>();


    @EventListener
    public void applicationStarted(ContextStartedEvent event) {
        authentication.begin();
        try {
            for (ImapMailBox mailBox : dao.findMailBoxes()) {
                log.debug("{}: synchronizing", mailBox);
                CompletableFuture.runAsync(() -> imapEvents.init(mailBox), executor);
            }
        } finally {
            authentication.end();
        }
    }

    @EventListener
    public void applicationStopped(ContextStoppedEvent contextStoppedEvent) {
        try {
            executor.shutdownNow();
            executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Exception while shutting down executor", e);
        }

        for (Map.Entry<UUID, ScheduledExecutorService> scheduledExecutorServiceEntry : syncRefreshers.entrySet()) {
            ScheduledExecutorService scheduledExecutorService = scheduledExecutorServiceEntry.getValue();
            try {
                scheduledExecutorService.shutdownNow();
                scheduledExecutorService.awaitTermination(1, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.warn("Exception while shutting down scheduled executor for mailBox#" + scheduledExecutorServiceEntry.getKey(), e);
            }
        }

        authentication.begin();
        try {
            for (ImapMailBox mailBox : dao.findMailBoxes()) {
                try {
                    imapEvents.shutdown(mailBox);
                } catch (Exception e) {
                    log.warn("Exception while shutting down imapEvents for mailbox " + mailBox, e);
                }
            }
        } finally {
            authentication.end();
        }
    }
}
