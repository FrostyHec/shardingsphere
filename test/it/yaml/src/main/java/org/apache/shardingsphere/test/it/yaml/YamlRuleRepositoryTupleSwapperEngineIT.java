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

package org.apache.shardingsphere.test.it.yaml;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlGlobalRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.mode.node.rule.tuple.RuleRepositoryTuple;
import org.apache.shardingsphere.mode.node.rule.tuple.YamlRuleRepositoryTupleSwapperEngine;
import org.apache.shardingsphere.mode.node.rule.tuple.annotation.RuleRepositoryTupleEntity;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public abstract class YamlRuleRepositoryTupleSwapperEngineIT {
    
    private final File yamlFile;
    
    private final YamlRuleRepositoryTupleSwapperEngine engine;
    
    protected YamlRuleRepositoryTupleSwapperEngineIT(final String yamlFileName) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(yamlFileName);
        assertNotNull(url);
        yamlFile = new File(url.getFile());
        engine = new YamlRuleRepositoryTupleSwapperEngine();
    }
    
    @Test
    void assertSwapToTuples() throws IOException {
        YamlRuleConfiguration yamlRuleConfig = loadYamlRuleConfiguration();
        assertRepositoryTuples(new ArrayList<>(engine.swapToTuples(yamlRuleConfig)), yamlRuleConfig);
    }
    
    private YamlRuleConfiguration loadYamlRuleConfiguration() throws IOException {
        YamlRootConfiguration yamlRootConfig = YamlEngine.unmarshal(yamlFile, YamlRootConfiguration.class);
        assertThat(yamlRootConfig.getRules().size(), is(1));
        return yamlRootConfig.getRules().iterator().next();
    }
    
    protected abstract void assertRepositoryTuples(List<RuleRepositoryTuple> actualTuples, YamlRuleConfiguration expectedYamlRuleConfig);
    
    protected void assertRepositoryTuple(final RuleRepositoryTuple actual, final String expectedKey, final Object expectedValue) {
        assertThat(actual.getKey(), is(expectedKey));
        assertThat(actual.getValue(), is(isSimpleObject(expectedValue) ? expectedValue : YamlEngine.marshal(expectedValue)));
    }
    
    private boolean isSimpleObject(final Object expectedValue) {
        return expectedValue instanceof Boolean || expectedValue instanceof Integer || expectedValue instanceof Long || expectedValue instanceof String;
    }
    
    @Test
    void assertSwapToYamlRuleConfiguration() throws IOException {
        String actualYamlContent = getActualYamlContent();
        String expectedYamlContent = getExpectedYamlContent();
        if (!sameYamlContext(actualYamlContent, expectedYamlContent)) {
            assertThat(actualYamlContent, is(expectedYamlContent));
        }
    }
    
    private String getActualYamlContent() throws IOException {
        YamlRuleConfiguration yamlRuleConfig = loadYamlRuleConfiguration();
        String ruleType = Objects.requireNonNull(yamlRuleConfig.getClass().getAnnotation(RuleRepositoryTupleEntity.class)).value();
        Collection<RuleRepositoryTuple> tuples = engine.swapToTuples(yamlRuleConfig).stream()
                .map(each -> new RuleRepositoryTuple(getRepositoryTupleKey(yamlRuleConfig instanceof YamlGlobalRuleConfiguration, ruleType, each), each.getValue())).collect(Collectors.toList());
        Optional<YamlRuleConfiguration> actualYamlRuleConfig = engine.swapToYamlRuleConfiguration(tuples, yamlRuleConfig.getClass());
        assertTrue(actualYamlRuleConfig.isPresent());
        YamlRootConfiguration yamlRootConfig = new YamlRootConfiguration();
        yamlRootConfig.setRules(Collections.singletonList(actualYamlRuleConfig.get()));
        return YamlEngine.marshal(yamlRootConfig);
    }
    
    private String getRepositoryTupleKey(final boolean isGlobalRule, final String ruleType, final RuleRepositoryTuple tuple) {
        return isGlobalRule ? String.format("/rules/%s/versions/0", ruleType) : String.format("/metadata/foo_db/rules/%s/%s/versions/0", ruleType, tuple.getKey());
    }
    
    private String getExpectedYamlContent() throws IOException {
        String content = Files.readAllLines(yamlFile.toPath()).stream()
                .filter(each -> !each.contains("#") && !each.isEmpty()).collect(Collectors.joining(System.lineSeparator())) + System.lineSeparator();
        return YamlEngine.marshal(YamlEngine.unmarshal(content, Map.class));
    }
    
    private boolean sameYamlContext(final String actualYamlContext, final String expectedYamlContext) {
        char[] actualArray = actualYamlContext.toCharArray();
        char[] expectedArray = expectedYamlContext.toCharArray();
        Arrays.sort(actualArray);
        Arrays.sort(expectedArray);
        return Arrays.equals(actualArray, expectedArray);
    }
}
