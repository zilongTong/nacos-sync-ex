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
package com.alibaba.nacossync.constant;

import java.nio.charset.Charset;

/**
 * @author NacosSync
 * @version $Id: SkyWalkerConstants.java, v 0.1 2018-09-26 AM12:07 NacosSync Exp $$
 */
public class SkyWalkerConstants {

    public final static String UNDERLINE = "_";
    public final static String SLASH = "/";

    public final static String DEST_CLUSTERID_KEY = "destClusterId";
    public final static String GROUP_NAME = "groupName";
    public final static String SYNC_SOURCE_KEY = "syncSource";
    public final static String SOURCE_CLUSTERID_KEY = "sourceClusterId";

    public final static String REGISTER_WORKER_PATH = "/nacos_sync/worker_ips";

    public final static String PER_WORKER_PROCESS_SERVICE = "/nacos_sync/worker/service";

    public final static String REGISTER_SWITCH = "/nacos_sync/worker_switch";

    public final static String ETCD_BEAT_TTL = "sync.etcd.register.ttl";

    public final static String SYNC_WORKER_ADDRESS = "sync.worker.address";

    public final static String SYNC_REGISTER_TYPE = "sync.register.type";

    public static final Charset UTF_8 = Charset.forName("UTF-8");


}
