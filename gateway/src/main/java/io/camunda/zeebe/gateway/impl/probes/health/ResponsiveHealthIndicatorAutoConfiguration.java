/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.camunda.zeebe.gateway.impl.probes.health;

import io.camunda.zeebe.gateway.impl.configuration.GatewayCfg;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.autoconfigure.health.HealthContributorAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** {@link EnableAutoConfiguration Auto-configuration} for {@link ResponsiveHealthIndicator}. */
@Configuration(proxyBeanMethods = false)
@ConditionalOnEnabledHealthIndicator("gateway-responsive")
@AutoConfigureBefore(HealthContributorAutoConfiguration.class)
@EnableConfigurationProperties(ResponsiveHealthIndicatorProperties.class)
public class ResponsiveHealthIndicatorAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(name = "gatewayResponsiveHealthIndicator")
  public ResponsiveHealthIndicator gatewayResponsiveHealthIndicator(
      final GatewayCfg gatewayCfg, final ResponsiveHealthIndicatorProperties properties) {
    return new ResponsiveHealthIndicator(gatewayCfg, properties.getHealthZeebeClientProperties());
  }
}
