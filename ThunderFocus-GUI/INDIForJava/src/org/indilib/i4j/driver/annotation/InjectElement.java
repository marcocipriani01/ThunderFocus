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

import org.indilib.i4j.Constants.LightStates;
import org.indilib.i4j.Constants.SwitchStatus;
import org.indilib.i4j.properties.INDIStandardElement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation on a field in your driver oder extension and a element
 * will be injected in the property with the attributes specified.
 * 
 * @author Richard van Nieuwenhoven
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({
    ElementType.FIELD
})
public @interface InjectElement {

    /**
     * @return the name of the element,defaults to the name of the property.
     */
    String name() default "";

    /**
     * @return name is set useing the general properties.
     */
    INDIStandardElement std() default INDIStandardElement.NONE;

    /**
     * @return the label of the element ,defaults to the name or the label of
     *         the property.
     */
    String label() default "";

    /**
     * @return the default value of the element when it is a number field,
     *         defaults to 0.
     */
    double numberValue() default 0d;

    /**
     * @return the default value of the element when it is a text field,
     *         defaults to an empty string.
     */
    String textValue() default "";

    /**
     * @return the minimal value of the element when it is a number field,
     *         defaults to 0.
     */
    double minimum() default 0d;

    /**
     * @return the maximal value of the element when it is a number field,
     *         defaults to Double.MAX_VALUE.
     */
    double maximum() default Double.MAX_VALUE;

    /**
     * @return the step value of the element when it is a number field, defaults
     *         to 0.
     */
    double step() default 0d;

    /**
     * @return the number format value of the element when it is a number field,
     *         defaults to %g.
     */
    String numberFormat() default "%g";

    /**
     * @return the default value of the element when it is a switch field,
     *         defaults to an empty string.
     */
    SwitchStatus switchValue() default SwitchStatus.OFF;

    /**
     * @return the property this element should be created in (defaults to the
     *         last defined property field in the current class. Only specify
     *         this field if the field is somewhere else.
     */
    String property() default "";

    /**
     * @return the default value of the element when it is a light field,
     *         defaults to an empty string.
     */
    LightStates state() default LightStates.IDLE;

    /**
     * @return the index to use instead of the lowercase 'n' character.
     */
    int nIndex() default -1;
}
