/*
 * polymap.org 
 * Copyright (C) 2015 individual contributors as indicated by the @authors tag. 
 * All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.p4.style;

import java.util.List;

import org.geotools.styling.StyledLayerDescriptor;
import org.opengis.feature.type.FeatureType;
import org.polymap.model2.Entity;

/**
 * Styler is the high level API to provide simple and complex feature stylings. A
 * Styler provides the UI to manipulate its parameters and it provides the logic to
 * build SLD for it.
 * <p/>
 * A Styler serves the following purposes:
 * <ul>
 * <li>provides a UI to set parameters</li>
 * <li>loads/stores its state (via Model2)</li>
 * <li>build SLD</li>
 * </ul>
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class AbstractStyler
        extends Entity {

    /**
     * A styler always works on a concrete scheme.
     *
     * @param schema
     */
    public abstract void init( FeatureType schema );


    /**
     * Complex stylers expand / mix all combinations of their referenced / contained
     * stylers to single SLD rules.
     * 
     * @param children
     * @return
     */
    public abstract void fillSLD( SLDBuilder builder, List<AbstractStyler> children );


    public abstract void fromSLD( StyledLayerDescriptor sld );
}
