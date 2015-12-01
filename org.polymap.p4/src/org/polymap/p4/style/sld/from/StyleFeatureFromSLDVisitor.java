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
package org.polymap.p4.style.sld.from;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.polymap.p4.style.entities.StyleConfiguration;
import org.polymap.p4.style.entities.StyleFeature;
import org.polymap.p4.style.sld.from.helper.StyleCompositeFromSLDHelper;
import org.polymap.p4.style.sld.from.helper.StyleConfigurationFromSLDHelper;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StyleFeatureFromSLDVisitor
        extends AbstractStyleFromSLDVisitor {

    private final StyleFeature styleFeature;


    public StyleFeatureFromSLDVisitor( StyleFeature styleFeature ) {
        this.styleFeature = styleFeature;
    }


    @Override
    public void visit( Style style ) {
        long NonConfiguredFeatureCount = getNonConfiguredFeatureCount( style );
        if (NonConfiguredFeatureCount > 0) {
            new StyleCompositeFromSLDHelper( styleFeature.styleComposite.get() ).visit( style );
        }
        super.visit( style );
    }


    private long getNonConfiguredFeatureCount( Style style ) {
        return style.featureTypeStyles().stream()
                .flatMap( featureTypeStyle -> featureTypeStyle.rules().stream() )
                .filter( rule -> isNonConfiguredRule( rule ) ).count();
    }


    public long getNonConfiguredRulesCount( FeatureTypeStyle featureTypeStyle ) {
        return featureTypeStyle.rules().stream().filter( rule -> isNonConfiguredRule( rule ) ).count();
    }


    boolean isNonConfiguredRule( Rule rule ) {
        StyleConfigurationFromSLDHelper helper = new StyleConfigurationFromSLDHelper( null );
        return !helper.hasZoomAttributes( rule ) && !helper.hasFilterAttribute( rule );
    }


    public void visit( FeatureTypeStyle featureTypeStyle ) {
        List<Rule> configuredFeatures = featureTypeStyle.rules().stream().filter( rule -> !isNonConfiguredRule( rule ) )
                .collect( Collectors.toList() );
        Function<String,StyleConfiguration> fun = ( String label ) -> styleFeature.styleConfigurations
                .createElement( styleConfiguration -> {
                    styleConfiguration.configurationName.set( label );
                    styleConfiguration.styleComposite.createValue( null );
                    return styleConfiguration;
                } );
        configuredFeatures.stream().forEach( rule -> new StyleConfigurationFromSLDHelper( fun ).visit( rule ) );
    }
}
