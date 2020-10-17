/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.alibaba.nacossync.extension.annotation;

import com.alibaba.nacossync.constant.ClusterTypeEnum;
import com.alibaba.nacossync.extension.SyncManagerService;
import com.alibaba.nacossync.extension.SyncService;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 *  Mainly used to mark the SyncService implementation
 *  of which source cluster to which destination cluster
 * 
 * @author paderlol
 * @date 2018-12-31 15:28
 * @see SyncService
 * @see SyncManagerService
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface NacosSyncService {

    @AliasFor(annotation = Component.class)
    String value() default "";

    ClusterTypeEnum sourceCluster();

    ClusterTypeEnum destinationCluster();
}
