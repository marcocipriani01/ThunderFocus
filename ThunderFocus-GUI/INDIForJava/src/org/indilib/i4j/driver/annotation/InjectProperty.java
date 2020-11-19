package org.indilib.i4j.driver.annotation;

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

import org.indilib.i4j.Constants.PropertyPermissions;
import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.Constants.SwitchRules;
import org.indilib.i4j.properties.INDIStandardProperty;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation on a field in your driver oder extension and a element
 * will be injected in the dirver with the attributes specified.
 * 
 * @author Richard van Nieuwenhoven
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({
    ElementType.FIELD
})
public @interface InjectProperty {

    /**
     * the group for all the properties, that do not specify a group.
     */
    String UNSORTED_GROUP = "Unsorted";

    /**
     * One minute in seconds.
     */
    int ONE_MINUTE_SECONDS = 60;

    /**
     * @return the permissions for the property, defaults to RW.
     */
    PropertyPermissions permission() default PropertyPermissions.RW;

    /**
     * @return the timeout for the property defaults to 60.
     */
    int timeout() default ONE_MINUTE_SECONDS;

    /**
     * @return name of the property (mandatory ).
     */
    String name() default "";

    /**
     * @return the general property that this property represents.
     */
    INDIStandardProperty std() default INDIStandardProperty.NONE;

    /**
     * @return label of the property (mandatory).
     */
    String label();

    /**
     * @return the initial state of the property.
     */
    PropertyStates state() default PropertyStates.IDLE;

    /**
     * @return the tab group to use for this property (mandatory if it is not in
     *         a group).
     */
    String group() default UNSORTED_GROUP;

    /**
     * @return should the value of this property be saved in a property file?
     *         defaults to false.
     */
    boolean saveable() default false;

    /**
     * @return if this property is a switch property what rule should apply?
     *         defaults to ONE_OF_MANY.
     */
    SwitchRules switchRule() default SwitchRules.ONE_OF_MANY;

    /**
     * @return the index to use instead of the lowercase 'n' character.
     */
    int nIndex() default -1;

}
