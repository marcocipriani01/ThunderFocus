package org.indilib.i4j.driver.util;

/*
 * #%L
 * INDI for Java Driver Library
 * %%
 * Copyright (C) 2013 - 2014 indiforjava
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.indilib.i4j.driver.INDIDriver;
import org.indilib.i4j.driver.INDIDriverExtension;
import org.indilib.i4j.driver.INDIElement;
import org.indilib.i4j.driver.INDIProperty;
import org.indilib.i4j.driver.annotation.InjectElement;
import org.indilib.i4j.driver.annotation.InjectExtension;
import org.indilib.i4j.driver.annotation.InjectProperty;
import org.indilib.i4j.driver.annotation.Rename;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the INDI field injector it is responsible for interpreting the
 * specified annotations on a field in a driver or in a driver extensions.
 * depending on the type of the field and the specified annotations it will
 * decide which property element or extension to instantiate. The injection is
 * done field by field and top down. So first the superclass fields are injected
 * and then the subclass fields. The fields are injected in the order defined in
 * the class. The order is relevant because elements are injected in the first
 * preceding property
 * 
 * @author Richard van Nieuwenhoven
 */
public final class INDIPropertyInjector {

    /**
     * Logger to log errors to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(INDIPropertyInjector.class);

    /**
     * During the injection process it is importent to keep the context, so that
     * when we are in a extention and a property of a driver is referenced wi
     * can find where it is. This is done by storeing the context in a
     * threadlocal var that will be emptied as soon as de the driver is
     * instanciated.
     */
    private static ThreadLocal<INDIPropertyInjector> current = new ThreadLocal<INDIPropertyInjector>();

    /**
     * current map of properties already injected in the driver or its
     * extensions.
     */
    private Map<String, INDIProperty<?>> properties = new HashMap<String, INDIProperty<?>>();

    /**
     * the current driver that is being injected.
     */
    private final INDIDriver driver;

    /**
     * the last created property, used for all following elements.
     */
    private INDIProperty<?> lastProperty = null;

    /**
     * the current group if this is set all empty groups following will be set
     * to this group.
     */
    private String currentGroup;

    /**
     * the current prefix, this prefix will be added for every property or
     * element name.
     */
    private String currentPrefix;

    /**
     * the currently active renaming this is used to rename special fields
     * inside an extension.
     */
    private Rename[] currentRenamings;

    /**
     * Inject all property/element and extension fields in the object
     * recursively.
     * 
     * @param driver
     *            the current driver instance
     * @param object
     *            the current injection object - can be a driver or an extension
     */
    public static void initialize(INDIDriver driver, Object object) {
        INDIPropertyInjector original = current.get();
        try {
            INDIPropertyInjector running = original;
            if (running == null) {
                running = new INDIPropertyInjector(driver);
                current.set(running);
            }
            running.initializeAnnotatedProperties(object);
        } finally {
            if (original == null) {
                current.remove();
            }
        }
    }

    /**
     * Constructor with driver. this is private because it should only used
     * Internally.
     * 
     * @param driver
     *            the inidriver
     */
    private INDIPropertyInjector(INDIDriver driver) {
        this.driver = driver;
    }

    /**
     * find an indi property in the collected properties till now with a
     * specified name. is the name is empty take the last scanned property.
     * 
     * @param name
     *            the name of the property to find.
     * @param lastScannedProperty
     *            the last scanned property.
     * @return the found property.
     */
    private INDIProperty<?> findNamedProperty(String name, INDIProperty<?> lastScannedProperty) {
        if (!name.isEmpty()) {
            INDIProperty<?> property = properties.get(name);
            if (property != null) {
                return property;
            }
        }
        return lastScannedProperty;
    }

    /**
     * get the value of a field by reflection.
     * 
     * @param object
     *            the object to get the field from
     * @param field
     *            the field
     * @return the value of the field in the specified object
     */
    private Object getFieldValue(Object object, Field field) {
        field.setAccessible(true);
        try {
            return field.get(object);
        } catch (Exception e) {
            throw new IllegalArgumentException("could not set indi element", e);
        }
    }

    /**
     * Now we process the hirachie top to bottom (that's why the recursion is
     * first and than the processing.
     * 
     * @param instance
     *            the instance to inject
     * @param clazz
     *            the current class.
     */
    private void initializeAnnotatedClass(Object instance, Class<?> clazz) {
        if (clazz != null) {
            initializeAnnotatedClass(instance, clazz.getSuperclass());
            for (Field field : clazz.getDeclaredFields()) {
                initializeDriverExtension(instance, field);
                initializeAnnotatedProperty(instance, field);
                initializeAnnotatedElement(instance, field);
            }
        }
    }

    /**
     * inject the field value of the instance, if it is annotated with an
     * InjectElement annotation. select the appropriate value from the type and
     * the annotations.
     * 
     * @param instance
     *            the instance to fill
     * @param field
     *            the current field.
     */
    private void initializeAnnotatedElement(Object instance, Field field) {
        InjectElement elem = field.getAnnotation(InjectElement.class);
        if (elem != null) {
            INDIProperty<?> propertyToConnect = findNamedProperty(elem.property(), lastProperty);
            if (propertyToConnect != null) {
                INDIElementBuilder<?> builder = propertyToConnect.newElement().set(elem);
                builder.name(rename(builder.name()));
                INDIElement lastElement = builder.create();
                setFieldValue(instance, field, lastElement);
            } else {
                LOG.error("could not find property " + elem.property() + " for element " + field);
            }
        }
    }

    /**
     * start method for the recursion, fill the object instance top down.
     * 
     * @param instance
     *            the instance to fill.
     */
    private void initializeAnnotatedProperties(Object instance) {
        initializeAnnotatedClass(instance, instance.getClass());
    }

    /**
     * inject the field value of the instance, if it is annotated with an
     * InjectProperty annotation. select the appropriate value from the type and
     * the annotations.
     * 
     * @param instance
     *            the instance to fill
     * @param field
     *            the current field.
     */
    private void initializeAnnotatedProperty(Object instance, Field field) {
        InjectProperty prop = field.getAnnotation(InjectProperty.class);
        if (prop != null) {
            INDIPropertyBuilder<INDIProperty<?>> builder = driver.newProperty((Class<INDIProperty<?>>) field.getType()).set(prop);
            if (builder.isDefaultGroup()) {
                builder.group(currentGroup);
            }
            builder.name(rename(builder.name()));
            lastProperty = builder.create();

            if (prop.saveable()) {
                lastProperty.setSaveable(true);
            }
            properties.put(lastProperty.getName(), lastProperty);
            setFieldValue(instance, field, lastProperty);
        }
    }

    /**
     * Apply any defined renaming to the name.
     * 
     * @param name
     *            the name to start with
     * @return the name changed with prefix and renamings
     */
    private String rename(String name) {
        if (currentRenamings != null) {
            for (Rename rename : currentRenamings) {
                if (rename.name().equals(name)) {
                    return rename.to();
                }
            }
        }
        if (currentPrefix != null) {
            return currentPrefix + name;
        }
        return name;
    }

    /**
     * If the field is a driver extension, the context is set for the injection
     * of the extension, after construction the context is reset.
     * 
     * @param instance
     *            the instance in which the extension will be injected
     * @param field
     *            the field that specifies the extension.
     */
    private void initializeDriverExtension(Object instance, Field field) {
        if (INDIDriverExtension.class.isAssignableFrom(field.getType())) {
            InjectExtension extentionAnnot = field.getAnnotation(InjectExtension.class);
            String oldValue = currentGroup;
            String oldPrefix = currentGroup;
            Rename[] oldRenamings = currentRenamings;
            try {
                if (extentionAnnot != null) {
                    if (!extentionAnnot.group().isEmpty()) {
                        currentGroup = extentionAnnot.group();
                    }
                    if (!extentionAnnot.prefix().isEmpty()) {
                        currentPrefix = extentionAnnot.prefix();
                    }
                    if (extentionAnnot.rename().length > 0) {
                        currentRenamings = extentionAnnot.rename();
                    }
                }
                INDIDriverExtension<?> driverExtension = instanciateDriverExtension(instance, field);
                setFieldValue(instance, field, driverExtension);
            } finally {
                currentGroup = oldValue;
                currentPrefix = oldPrefix;
                currentRenamings = oldRenamings;
            }
        }
    }

    /**
     * search the constructor that has a driver as a parameter and call it.
     * 
     * @param instance
     *            the instance in which the extension will be injected
     * @param field
     *            the field that specifies the extension.
     * @return the newly instantiated extension or the existing one if it was
     *         already set
     */
    private INDIDriverExtension<?> instanciateDriverExtension(Object instance, Field field) {
        INDIDriverExtension<?> driverExtension = null;
        try {
            driverExtension = (INDIDriverExtension<?>) getFieldValue(instance, field);
            if (driverExtension == null) {
                for (Constructor<?> constructor : field.getType().getConstructors()) {
                    if (constructor.getParameterTypes().length == 1 && INDIDriver.class.isAssignableFrom(constructor.getParameterTypes()[0])) {
                        driverExtension = (INDIDriverExtension<?>) constructor.newInstance(driver);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Could not instanciate Driver extention", e);
        }
        return driverExtension;
    }

    /**
     * set the value of a field by reflection.
     * 
     * @param object
     *            the object defining the field
     * @param field
     *            the field to set
     * @param fieldValue
     *            the value to set the field to
     */
    private void setFieldValue(Object object, Field field, Object fieldValue) {
        field.setAccessible(true);
        try {
            field.set(object, fieldValue);
        } catch (Exception e) {
            throw new IllegalArgumentException("could not set indi element", e);
        }
    }

}
