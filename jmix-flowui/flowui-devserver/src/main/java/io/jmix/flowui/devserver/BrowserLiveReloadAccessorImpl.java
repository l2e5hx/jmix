/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.jmix.flowui.devserver;

import com.vaadin.flow.internal.BrowserLiveReload;
import com.vaadin.flow.internal.BrowserLiveReloadAccessor;
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.VaadinService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrowserLiveReloadAccessorImpl
        implements BrowserLiveReloadAccessor {

    @Override
    public BrowserLiveReload getLiveReload(VaadinContext context) {
        return context.getAttribute(BrowserLiveReload.class,
                () -> new DebugWindowConnection(context));
    }

    @Override
    public BrowserLiveReload getLiveReload(VaadinService service) {
        if (service.getDeploymentConfiguration().isProductionMode()) {
            getLogger().debug(
                    "BrowserLiveReloadAccessImpl::getLiveReload is called in production mode.");
            return null;
        }
        return getLiveReload(service.getContext());
    }

    private static Logger getLogger() {
        return LoggerFactory.getLogger(BrowserLiveReloadAccessor.class);
    }
}
