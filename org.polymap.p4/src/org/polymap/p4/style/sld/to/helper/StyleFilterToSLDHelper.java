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
package org.polymap.p4.style.sld.to.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.geotools.filter.FilterFactoryImpl;
import org.geotools.styling.builder.RuleBuilder;
import org.mockito.internal.matchers.And;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.polymap.p4.style.entities.StyleCompositeFilter;
import org.polymap.p4.style.entities.StyleFilter;
import org.polymap.p4.style.entities.StyleFilterConfiguration;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StyleFilterToSLDHelper {

    public void handleFilterConfiguration( StyleFilterConfiguration filterConfiguration, RuleBuilder ruleBuilder ) {
        if (filterConfiguration.simpleFilter.get() != null) {
            StyleFilter simpleFilter = filterConfiguration.simpleFilter.get();
            FilterFactory filterFactory = new FilterFactoryImpl();
            Filter filter = getFilter( simpleFilter, filterFactory );
            if (filter != null) {
                ruleBuilder.filter( filter );
            }
        }
        else if (filterConfiguration.complexFilter.get() != null) {
            StyleCompositeFilter complexFilter = filterConfiguration.complexFilter.get();
            FilterFactory filterFactory = new FilterFactoryImpl();
            Filter filter = getFilter( complexFilter, filterFactory );
            if (filter != null) {
                ruleBuilder.filter( filter );
            }
        }
    }


    private Filter getFilter( StyleCompositeFilter complexFilter, FilterFactory filterFactory ) {
        Filter filter = null;
        List<Filter> simpleChildren = complexFilter.simpleFilters.stream().map( f -> getFilter( f, filterFactory ) )
                .collect( Collectors.toList() );
        List<Filter> complexChildren = complexFilter.complexFilters.stream().map( f -> getFilter( f, filterFactory ) )
                .collect( Collectors.toList() );
        List<Filter> children = new ArrayList<Filter>();
        children.addAll( simpleChildren );
        children.addAll( complexChildren );
        if (And.class.getSimpleName().equals( complexFilter.predicate.get() )) {
            filter = filterFactory.and( children );
        }
        else if (Or.class.getSimpleName().equals( complexFilter.predicate.get() )) {
            filter = filterFactory.or( children );
        }
        return filter;
    }


    private Filter getFilter( StyleFilter simpleFilter, FilterFactory filterFactory ) {
        Filter filter = null;
        if (PropertyIsLessThan.NAME.equals( simpleFilter.predicate.get() )) {
            filter = filterFactory.less( filterFactory.property( simpleFilter.propertyName.get() ),
                    filterFactory.literal( simpleFilter.value.get() ) );
        }
        else if (PropertyIsLessThanOrEqualTo.NAME.equals( simpleFilter.predicate.get() )) {
            filter = filterFactory.lessOrEqual( filterFactory.property( simpleFilter.propertyName.get() ),
                    filterFactory.literal( simpleFilter.value.get() ) );
        }
        else if (PropertyIsGreaterThanOrEqualTo.NAME.equals( simpleFilter.predicate.get() )) {
            filter = filterFactory.greaterOrEqual( filterFactory.property( simpleFilter.propertyName.get() ),
                    filterFactory.literal( simpleFilter.value.get() ) );
        }
        else if (PropertyIsGreaterThan.NAME.equals( simpleFilter.predicate.get() )) {
            filter = filterFactory.greater( filterFactory.property( simpleFilter.propertyName.get() ),
                    filterFactory.literal( simpleFilter.value.get() ) );
        }
        else if (PropertyIsEqualTo.NAME.equals( simpleFilter.predicate.get() )) {
            filter = filterFactory.equals( filterFactory.property( simpleFilter.propertyName.get() ),
                    filterFactory.literal( simpleFilter.value.get() ) );
        }
        return filter;
    }
}
