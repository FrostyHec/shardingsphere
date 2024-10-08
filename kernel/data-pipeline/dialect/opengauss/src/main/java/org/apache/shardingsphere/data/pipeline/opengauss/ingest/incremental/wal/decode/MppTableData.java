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

package org.apache.shardingsphere.data.pipeline.opengauss.ingest.incremental.wal.decode;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.util.json.JsonConfiguration;

/**
 * Mppdb decoding json related class.
 */
@Setter
@Getter
public final class MppTableData implements JsonConfiguration {
    
    @JsonProperty("table_name")
    private String tableName;
    
    @JsonProperty("op_type")
    private String opType;
    
    @JsonProperty("columns_name")
    private String[] columnsName;
    
    @JsonProperty("columns_type")
    private String[] columnsType;
    
    @JsonProperty("columns_val")
    private String[] columnsVal;
    
    @JsonProperty("old_keys_name")
    private String[] oldKeysName;
    
    @JsonProperty("old_keys_type")
    private String[] oldKeysType;
    
    @JsonProperty("old_keys_val")
    private String[] oldKeysVal;
}
