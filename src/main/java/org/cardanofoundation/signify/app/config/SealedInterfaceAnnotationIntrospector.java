package org.cardanofoundation.signify.app.config;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.util.VersionUtil;

/**
 * Custom AnnotationIntrospector that removes @JsonTypeInfo from sealed interfaces.
 * This allows our custom deserializers to handle type resolution.
 */
public class SealedInterfaceAnnotationIntrospector extends AnnotationIntrospector {

    public TypeResolverBuilder<?> findTypeResolver(Object context, Annotated ann) {
        // Return null to skip type info processing for sealed interfaces
        // This allows custom deserializers to handle type resolution
        return null;
    }

    @Override
    public Version version() {
        return VersionUtil.parseVersion("1.0.0", "org.cardanofoundation", "signify");
    }
}
