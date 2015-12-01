/*
 * polymap.org Copyright (C) 2015 individual contributors as indicated by the
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
package org.polymap.p4.style.sld.to;

import org.geotools.styling.builder.FeatureTypeStyleBuilder;
import org.polymap.p4.style.SLDBuilder;
import org.polymap.p4.style.entities.StyleFeature;
import org.polymap.p4.style.sld.to.helper.StyleCompositeToSLDHelper;
import org.polymap.p4.style.sld.to.helper.StyleConfigurationToSLDHelper;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StyleFeatureToSLDVisitor
        extends AbstractStyleToSLDVisitor {

    private final StyleFeature styleFeature;


    public StyleFeatureToSLDVisitor( StyleFeature styleFeature ) {
        this.styleFeature = styleFeature;
    }


    @Override
    public void fillSLD( SLDBuilder builder ) {
        FeatureTypeStyleBuilder featureTypeStyleBuilder = singletonFeatureTypeStyle( builder );
        styleFeature.styleConfigurations.forEach( styleConfiguration -> new StyleConfigurationToSLDHelper()
                .handleStyleConfiguration( styleConfiguration, builder, featureTypeStyleBuilder ) );
        new StyleCompositeToSLDHelper( styleFeature.styleComposite.get() ).fillSLD( builder,
                ( ) -> featureTypeStyleBuilder.rule() );
    }
}
