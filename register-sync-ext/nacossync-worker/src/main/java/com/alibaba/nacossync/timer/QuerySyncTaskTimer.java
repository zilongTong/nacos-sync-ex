/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.nacossync.timer;

import com.alibaba.nacossync.cache.SkyWalkerCacheServices;
import com.alibaba.nacossync.constant.MetricsStatisticsType;
import com.alibaba.nacossync.constant.TaskStatusEnum;
import com.alibaba.nacossync.dao.TaskAccessService;
import com.alibaba.nacossync.event.DeleteTaskEvent;
import com.alibaba.nacossync.event.SyncTaskEvent;
import com.alibaba.nacossync.extension.ha.ConsistentHashSyncShardingEtcdProxy;
import com.alibaba.nacossync.extension.jetcd.EtcdProxy;
import com.alibaba.nacossync.monitor.MetricsManager;
import com.alibaba.nacossync.pojo.model.TaskDO;
import com.google.common.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author NacosSync
 * @version $Id: SkyWalkerServices.java, v 0.1 2018-09-26 AM1:39 NacosSync Exp $$
 */
@Slf4j
@Service
public class QuerySyncTaskTimer implements CommandLineRunner {
    @Autowired
    private MetricsManager metricsManager;

    @Autowired
    private SkyWalkerCacheServices skyWalkerCacheServices;

    @Autowired
    private TaskAccessService taskAccessService;

    @Autowired
    private ConsistentHashSyncShardingEtcdProxy syncShardingProxy;

    @Autowired
    private EventBus eventBus;

    @Autowired
    private EtcdProxy manager;

    @Qualifier("executorService")
    @Autowired
    private ScheduledExecutorService scheduledExecutorService;


    @Override
    public void run(String... args) {
        /** Fetch the task list from the database every 5 seconds */
        scheduledExecutorService.scheduleWithFixedDelay(new CheckRunningStatusThread(), 0, 5000,
                TimeUnit.MILLISECONDS);
    }

    private class CheckRunningStatusThread implements Runnable {

        @Override
        public void run() {
            Long start = System.currentTimeMillis();
            try {
                log.info("register cluster state,{}", syncShardingProxy.switchState());
                if (syncShardingProxy.switchState() == null || syncShardingProxy.switchState().equalsIgnoreCase(Boolean.FALSE.toString())) {
                    log.info("register state is Not activated , please active the register first");
                    return;
                }
                List<TaskDO> taskDOSAfter = taskAccessService.buildTaskCaffeineCache();
                testBalance(taskDOSAfter);
                taskDOSAfter.stream().forEach(taskDO -> {
                    /**
                     * 根据一致性hash找到服务的路由节点，只路由sync指令，delete则需要广播.
                     */
                    if (TaskStatusEnum.SYNC.getCode().equals(taskDO.getTaskStatus()) && !syncShardingProxy.isProcessNode(taskDO.getServiceName()))
                        return;
                    if ((null != skyWalkerCacheServices.getFinishedTask(taskDO))) {
                        return;
                    }
                    if (TaskStatusEnum.SYNC.getCode().equals(taskDO.getTaskStatus())) {
                        eventBus.post(new SyncTaskEvent(taskDO));
                        log.info("从数据库中查询到一个同步任务，发出一个同步事件:" + taskDO);
                    }
                    if (TaskStatusEnum.DELETE.getCode().equals(taskDO.getTaskStatus())) {
                        eventBus.post(new DeleteTaskEvent(taskDO));
                        log.info("从数据库中查询到一个删除任务，发出一个同步事件:" + taskDO);
                    }
                });
            } catch (Exception e) {
                log.warn("CheckRunningStatusThread Exception", e);
            }
            metricsManager.record(MetricsStatisticsType.DISPATCHER_TASK, System.currentTimeMillis() - start);
        }
    }

    private void testBalance(List<TaskDO> taskDOSAfter) {

        Set<String> strings = syncShardingProxy.getNodeCaches();
        Map<String, AtomicInteger> map = new HashMap<>();
        for (String s : strings) {
            map.put(s, new AtomicInteger(0));
        }
        taskDOSAfter.stream().forEach(t -> {
            String node = syncShardingProxy.shardingNode(t.getServiceName());
            strings.stream().forEach(s -> {
                if (s.equalsIgnoreCase(node))
                    map.get(node).incrementAndGet();
            });
        });
        log.info("show up consistent hash sharding balance .");
        for (Map.Entry entry : map.entrySet()) {
            log.info(entry.getKey() + ":" + entry.getValue().toString());
        }
    }
}
