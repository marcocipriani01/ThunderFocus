/**
 * Provides the classes necessary to create INDI Servers and a basic but
 * functional Server.
 * Usually a INDI Server inherits from <code>INDIServer</code> and implements
 * its abstract methods. In those methods additional filtering can be done to
 * avoid some messages arriving to some Devices or Clients. Thus, it is possible
 * to create filering and security mechanisms.
 * Please check <code>laazotea.indi.server.INDIBasicServer</code> for a
 * functional example of an INDI Server.
 */

package org.indilib.i4j.server;

/*
 * #%L
 * INDI for Java Server Library
 * %%
 * Copyright (C) 2012 - 2014 indiforjava
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

