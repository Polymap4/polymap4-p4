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
package org.polymap.p4.style.sld.from.helper;

import java.util.function.Function;

import org.geotools.styling.Rule;
import org.polymap.p4.style.entities.StyleZoomConfiguration;
import org.polymap.p4.style.sld.from.AbstractStyleFromSLDVisitor;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StyleZoomFromSLDHelper
        extends AbstractStyleFromSLDVisitor {

    private final Function<String,StyleZoomConfiguration> styleZoomConfigurationInit;

    private StyleZoomConfiguration                        styleZoomConfiguration = null;


    public StyleZoomFromSLDHelper( Function<String,StyleZoomConfiguration> styleZoomConfigurationInit ) {
        this.styleZoomConfigurationInit = styleZoomConfigurationInit;
    }


    @Override
    public void visit( Rule rule ) {
        if (rule.getMinScaleDenominator() != 0 || rule.getMaxScaleDenominator() != Double.POSITIVE_INFINITY) {
            styleZoomConfiguration = styleZoomConfigurationInit.apply( rule.getName() );
            styleZoomConfiguration.minScaleDenominator.set( rule.getMinScaleDenominator() );
            styleZoomConfiguration.maxScaleDenominator.set( rule.getMaxScaleDenominator() );
        }
    }
}
