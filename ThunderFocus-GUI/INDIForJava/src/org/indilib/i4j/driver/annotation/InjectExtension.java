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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Inject an extension class in the current field and change the names of them
 * in a generic way.
 * 
 * @author Richard van Nieuwenhoven
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({
    ElementType.FIELD
})
public @interface InjectExtension {

    /**
     * @return a prefix that will be inserted before all fields in this
     *         extension (except the renamed ones). default is that the names
     *         stay as they are. This can be necessary if the extension in
     *         included multiple times in one driver.
     */
    String prefix() default "";

    /**
     * @return if in an extension any property has no group the here specified
     *         group will be used.
     */
    String group() default "";

    /**
     * @return is specific fields in the extension has to have specific names it
     *         can be done with these renamings.
     */
    Rename[] rename() default {
    // default no renaming
    };
}
