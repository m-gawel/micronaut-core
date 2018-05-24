/*
 * Copyright 2017-2018 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.micronaut.inject.qualifiers;

import io.micronaut.context.Qualifier;
import io.micronaut.core.reflect.GenericTypeUtils;
import io.micronaut.inject.BeanType;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A {@link Qualifier} that qualifies beans by generic type arguments.
 *
 * @param <T> The type
 * @author Graeme Rocher
 * @since 1.0
 */
public class TypeArgumentQualifier<T> implements Qualifier<T> {

    private final Class[] typeArguments;

    /**
     * @param typeArguments The type arguments
     */
    public TypeArgumentQualifier(Class... typeArguments) {
        this.typeArguments = typeArguments;
    }

    @Override
    public <BT extends BeanType<T>> Stream<BT> reduce(Class<T> beanType, Stream<BT> candidates) {
        return candidates.filter(candidate -> {
            if (beanType.isInterface()) {
                Class[] classes = GenericTypeUtils.resolveInterfaceTypeArguments(candidate.getBeanType(), beanType);
                return areTypesCompatible(classes);
            } else {
                Class[] classes = GenericTypeUtils.resolveSuperTypeGenericArguments(candidate.getBeanType(), beanType);
                return areTypesCompatible(classes);
            }
        });
    }

    /**
     * @param classes An array of classes
     * @return Whether the types are compatible
     */
    protected boolean areTypesCompatible(Class[] classes) {
        if (classes.length == 0) {
            // in this case the type doesn't specify type arguments, so this is the equivalent of using Object
            return true;
        } else if (classes.length != typeArguments.length) {
            return false;
        } else {
            for (int i = 0; i < classes.length; i++) {
                Class left = classes[i];
                Class right = typeArguments[i];
                if (right == Object.class) {
                    continue;
                }
                if (left != right && !left.isAssignableFrom(right)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TypeArgumentQualifier<?> that = (TypeArgumentQualifier<?>) o;
        return Arrays.equals(typeArguments, that.typeArguments);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(typeArguments);
    }

    @Override
    public String toString() {
        return "<" + Arrays.stream(typeArguments).map(Class::getSimpleName).collect(Collectors.joining(",")) + ">";
    }
}
