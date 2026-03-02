package org.cardanofoundation.signify.app.config;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBuilder;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import org.cardanofoundation.signify.app.aiding.KeyStateRecordKtDeserializer;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecordKt;

import java.util.Iterator;

/**
 * Modifies bean deserializers to use custom deserializers for sealed interface fields.
 */
public class SealedInterfaceModifier extends BeanDeserializerModifier {
    
    private static final KeyStateRecordKtDeserializer ktDeserializer = new KeyStateRecordKtDeserializer();

    @Override
    public BeanDeserializerBuilder updateBuilder(DeserializationConfig config,
                                                 BeanDescription beanDesc,
                                                 BeanDeserializerBuilder builder) {
        // Get all properties as an iterator
        Iterator<SettableBeanProperty> properties = builder.getProperties();
        if (properties != null) {
            while (properties.hasNext()) {
                SettableBeanProperty prop = properties.next();
                // Check if this property's type is a sealed interface that needs custom handling
                if (prop.getType().getRawClass() == KeyStateRecordKt.class) {
                    // Replace the deserializer for this property
                    SettableBeanProperty newProp = prop.withValueDeserializer(ktDeserializer);
                    builder.addOrReplaceProperty(newProp, true);
                }
            }
        }
        return builder;
    }
}
