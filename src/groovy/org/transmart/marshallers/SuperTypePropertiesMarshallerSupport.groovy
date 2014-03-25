package org.transmart.marshallers

/**
 * Support class for exposing only the properties of some object that are
 * defined on a subtype of that object.
 */
abstract class SuperTypePropertiesMarshallerSupport {

    protected getPropertySubsetForSuperType(Object o, Class superType) {
        if (!superType.isAssignableFrom(o.getClass())) {
            throw new IllegalArgumentException("Object '$o' is not of type " +
                    "$superType")
        }

        superType.metaClass.properties.collectEntries { MetaBeanProperty prop ->
            [prop.name, prop.getProperty(o)]
        }
    }
}
