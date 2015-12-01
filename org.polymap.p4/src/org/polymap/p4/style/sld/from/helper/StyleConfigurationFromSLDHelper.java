/*
 * polymap.org Copyright (C) 2015 individual contributors as indicated by the
 * 
 * @authors tag. All rights reserved.
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
import org.polymap.p4.style.entities.StyleConfiguration;
import org.polymap.p4.style.entities.StyleFilterConfiguration;
import org.polymap.p4.style.entities.StyleZoomConfiguration;
import org.polymap.p4.style.sld.from.AbstractStyleFromSLDVisitor;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StyleConfigurationFromSLDHelper
        extends AbstractStyleFromSLDVisitor {

    private final Function<String,StyleConfiguration> styleConfigurationInit;

    private StyleConfiguration                        styleConfiguration = null;


    public StyleConfigurationFromSLDHelper( Function<String,StyleConfiguration> styleConfigurationInit ) {
        this.styleConfigurationInit = styleConfigurationInit;
    }


    @Override
    public void visit( Rule rule ) {
        if (hasZoomAttributes( rule ) | hasFilterAttribute( rule )) {
            styleConfiguration = styleConfigurationInit.apply( rule.getName() );
            if (rule.getDescription() != null && rule.getDescription().getTitle() != null) {
                styleConfiguration.configurationTitle.set( rule.getDescription().getTitle().toString() );
            }
            fillSymbolizers( rule );
            if (hasZoomAttributes( rule )) {
                Function<String,StyleZoomConfiguration> fun = ( String label ) -> styleConfiguration.styleZoomConfiguration
                        .createValue( null );
                new StyleZoomFromSLDHelper( fun ).visit( rule );
            }
            if (hasFilterAttribute( rule )) {
                Function<String,StyleFilterConfiguration> fun = ( String label ) -> styleConfiguration.styleFilterConfiguration
                        .createValue( null );
                new StyleFilterFromSLDHelper( fun ).visit( rule );
            }
        }
    }


    public boolean hasZoomAttributes( Rule rule ) {
        return rule.getMinScaleDenominator() != 0 || rule.getMaxScaleDenominator() != Double.POSITIVE_INFINITY;
    }


    public boolean hasFilterAttribute( Rule rule ) {
        return rule.getFilter() != null;
    }


    private void fillSymbolizers( Rule rule ) {
        new StyleCompositeFromSLDHelper( styleConfiguration.styleComposite.get() ).visit( rule );
    }
}
