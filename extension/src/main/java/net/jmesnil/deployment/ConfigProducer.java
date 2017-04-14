/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package net.jmesnil.deployment;

import java.io.Serializable;
import java.util.Optional;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * CDI producer for {@link Config} bean.
 *
 * @author <a href="http://jmesnil.net/">Jeff Mesnil</a> (c) 2017 Red Hat inc.
 */
public class ConfigProducer implements Serializable{

    @Produces
    Config getConfig(InjectionPoint injectionPoint) {
        // return the Config for the TCCL
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        return ConfigProvider.getConfig(tccl);
    }

    // FIXME anything else that a String field would break...
    @Produces @ConfigProperty
    String getParamValue(InjectionPoint ip) {
        Config config = getConfig(ip);
        ConfigProperty configProperty = ip.getAnnotated().getAnnotation(ConfigProperty.class);
        String name = configProperty.name();
        String defaultValue = configProperty.defaultValue();
        Optional<String> value = config.getOptionalValue(name, String.class);
        if (value.isPresent()) {
            return value.get();
        } else {
            return defaultValue;
        }
    }
}
