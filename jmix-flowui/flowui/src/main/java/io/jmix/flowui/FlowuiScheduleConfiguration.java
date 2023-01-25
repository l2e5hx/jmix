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

package io.jmix.flowui;

import io.jmix.flowui.executor.FlowuiBackgroundTaskProperties;
import io.jmix.flowui.executor.WatchDog;
import io.jmix.flowui.executor.watcher.TasksWatchDog;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration(proxyBeanMethods = false)
public class FlowuiScheduleConfiguration {

    @Bean("flowui_ThreadPoolTaskScheduler")
    public TaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setThreadNamePrefix("flowui_backgroundScheduler");
        threadPoolTaskScheduler.setPoolSize(1);
        threadPoolTaskScheduler.setDaemon(true);
        return threadPoolTaskScheduler;
    }

    @Bean("flowui_BackgroundWorker_WatchDog")
    public WatchDog watchDog(FlowuiBackgroundTaskProperties properties) {
        return new TasksWatchDog(properties);
    }
}