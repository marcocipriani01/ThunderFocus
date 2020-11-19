/**
 * Provides the classes necessary to create INDI Drivers, their Properties and
 * Elements to which the INDI Clients may connect. Usually a INDI Driver will
 * extend the class <code>INDIDriver</code> and will add as many
 * <code>INDIProperty</code> as needed. Please check
 * <code>laazotea.indi.driver.examples.INDIElTiempoDriver</code>, for a simple
 * example on the use of the library. If you want to use it with the standard
 * not I4J <code>indiserver</code> application you will have tu use a shell
 * wrapper. A example one may be a <code>launch.sh</code> file with the
 * following contents:
 */
package org.indilib.i4j.driver;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
