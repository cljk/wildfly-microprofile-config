/*
 * Copyright 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.extension.microprofile.config;

import static org.jboss.as.controller.SimpleAttributeDefinitionBuilder.create;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MODULE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAME;
import static org.wildfly.extension.microprofile.config.MicroProfileConfigLogger.ROOT_LOGGER;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import io.smallrye.config.DirConfigSource;
import io.smallrye.config.PropertiesConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.AttributeMarshaller;
import org.jboss.as.controller.AttributeMarshallers;
import org.jboss.as.controller.AttributeParsers;
import org.jboss.as.controller.ObjectTypeAttributeDefinition;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PersistentResourceDefinition;
import org.jboss.as.controller.PropertiesAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;

/**
 * @author <a href="http://jmesnil.net/">Jeff Mesnil</a> (c) 2017 Red Hat inc.
 */
public class ConfigSourceDefinition extends PersistentResourceDefinition {

    static AttributeDefinition ORDINAL = SimpleAttributeDefinitionBuilder.create("ordinal", ModelType.INT)
            .setDefaultValue(new ModelNode(100))
            .setAllowNull(true)
            .setRestartAllServices()
            .build();
    static AttributeDefinition PROPERTIES = new PropertiesAttributeDefinition.Builder("properties", true)
            .setAttributeParser(new AttributeParsers.PropertiesParser(false))
            .setAttributeMarshaller(new AttributeMarshallers.PropertiesAttributeMarshaller(null, false))
            .setAlternatives("class", "dir")
            .setAllowNull(true)
            .setRestartAllServices()
            .build();
    static ObjectTypeAttributeDefinition CLASS = ObjectTypeAttributeDefinition.Builder.of("class",
            create(NAME, ModelType.STRING, false)
                    .setAllowExpression(false)
                    .build(),
            create(MODULE, ModelType.STRING, false)
                    .setAllowExpression(false)
                    .build())
            .setAlternatives("properties", "dir")
            .setAllowNull(true)
            .setAttributeMarshaller(AttributeMarshaller.ATTRIBUTE_OBJECT)
            .setRestartAllServices()
            .build();
    static AttributeDefinition DIR = SimpleAttributeDefinitionBuilder.create("dir", ModelType.STRING)
            .setAllowExpression(true)
            .setAlternatives("class", "properties")
            .setAllowNull(true)
            .setRestartAllServices()
            .build();

    static AttributeDefinition[] ATTRIBUTES = { ORDINAL, PROPERTIES, CLASS, DIR };

    protected ConfigSourceDefinition() {
        super(SubsystemExtension.CONFIG_SOURCE_PATH,
                SubsystemExtension.getResourceDescriptionResolver(SubsystemExtension.CONFIG_SOURCE_PATH.getKey()),
                new AbstractAddStepHandler(ATTRIBUTES) {
                    @Override
                    protected boolean requiresRuntime(OperationContext context) {
                        return true;
                    }

                    @Override
                    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
                        super.performRuntime(context, operation, model);
                        String name = context.getCurrentAddressValue();
                        int ordinal = ORDINAL.resolveModelAttribute(context, model).asInt();
                        ModelNode props = PROPERTIES.resolveModelAttribute(context, model);
                        ModelNode classModel = CLASS.resolveModelAttribute(context, model);
                        ModelNode dirModel = DIR.resolveModelAttribute(context, model);
                        final ConfigSource configSource;
                        if (classModel.isDefined()) {
                            Class configSourceClass = unwrapClass(classModel);
                            try {
                                configSource = ConfigSource.class.cast(configSourceClass.newInstance());
                            } catch (Exception e) {
                                throw new OperationFailedException(e);
                            }
                        } else if (dirModel.isDefined()) {
                            File dir = new File(dirModel.asString());
                            configSource = new DirConfigSource(dir, ordinal);
                        } else {
                            Map<String, String> properties = PropertiesAttributeDefinition.unwrapModel(context, props);
                            configSource = new PropertiesConfigSource(properties, name, ordinal);
                        }
                        MicroProfileConfigLogger.ROOT_LOGGER.info("Reading properties from " + configSource.getName());
                        ConfigSourceService.install(context, name, configSource);
                    }
                }, new AbstractRemoveStepHandler() {
                    @Override
                    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
                        String name = context.getCurrentAddressValue();
                        context.removeService(ServiceNames.CONFIG_SOURCE.append(name));
                    }
                });
    }

    @Override
    public Collection<AttributeDefinition> getAttributes() {
        return Arrays.asList(ATTRIBUTES);
    }

    private static Class unwrapClass(ModelNode classModel) throws OperationFailedException {
        String className = classModel.get(NAME).asString();
        String moduleName = classModel.get(MODULE).asString();
        try {
            ModuleIdentifier moduleID = ModuleIdentifier.fromString(moduleName);
            Module module = Module.getCallerModuleLoader().loadModule(moduleID);
            Class<?> clazz = module.getClassLoader().loadClass(className);
            return clazz;
        } catch (Exception e) {
            throw ROOT_LOGGER.unableToLoadClassFromModule(className, moduleName);
        }
    }
}
