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

package org.wildfly.microprofile.config;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.config.spi.Converter;

/**
 * @author <a href="http://jmesnil.net/">Jeff Mesnil</a> (c) 2017 Red Hat inc.
 */
class Converters {

    @SuppressWarnings("unchecked")
    static final Converter<String> STRING_CONVERTER = (Converter & Serializable) value -> value;

    @SuppressWarnings("unchecked")
    static final Converter<Boolean> BOOLEAN_CONVERTER = (Converter & Serializable) value -> {
        if (value != null) {
            return "TRUE".equalsIgnoreCase(value)
                    || "1".equalsIgnoreCase(value)
                    || "YES".equalsIgnoreCase(value)
                    || "Y".equalsIgnoreCase(value)
                    || "ON".equalsIgnoreCase(value)
                    || "JA".equalsIgnoreCase(value)
                    || "J".equalsIgnoreCase(value)
                    || "OUI".equalsIgnoreCase(value);
        }
        return null;
    };

    @SuppressWarnings("unchecked")
    static final Converter<Double> DOUBLE_CONVERTER = (Converter & Serializable) value -> value != null ? Double.valueOf(value) : null;

    @SuppressWarnings("unchecked")
    static final Converter<Float> FLOAT_CONVERTER = (Converter & Serializable) value -> value != null ? Float.valueOf(value) : null;

    @SuppressWarnings("unchecked")
    static final Converter<Long> LONG_CONVERTER = (Converter & Serializable) value -> value != null ? Long.valueOf(value) : null;

    @SuppressWarnings("unchecked")
    static final Converter<Integer> INTEGER_CONVERTER = (Converter & Serializable) value -> value != null ? Integer.valueOf(value) : null;

    @SuppressWarnings("unchecked")
    static final Converter<Class<?>> CLASS_CONVERTER = (Converter & Serializable) value -> {
        try {
            return value != null ? Class.forName(value) : null;
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    };

    public static final Map<Type, Converter> ALL_CONVERTERS = new HashMap<>();

    static {
        ALL_CONVERTERS.put(String.class, STRING_CONVERTER);

        ALL_CONVERTERS.put(Boolean.class, BOOLEAN_CONVERTER);
        ALL_CONVERTERS.put(Boolean.TYPE, BOOLEAN_CONVERTER);

        ALL_CONVERTERS.put(Double.class, DOUBLE_CONVERTER);
        ALL_CONVERTERS.put(Double.TYPE, DOUBLE_CONVERTER);

        ALL_CONVERTERS.put(Float.class, FLOAT_CONVERTER);
        ALL_CONVERTERS.put(Float.TYPE, FLOAT_CONVERTER);

        ALL_CONVERTERS.put(Long.class, LONG_CONVERTER);
        ALL_CONVERTERS.put(Long.TYPE, LONG_CONVERTER);

        ALL_CONVERTERS.put(Integer.class, INTEGER_CONVERTER);
        ALL_CONVERTERS.put(Integer.TYPE, INTEGER_CONVERTER);

        ALL_CONVERTERS.put(Class.class, CLASS_CONVERTER);
    }
}
