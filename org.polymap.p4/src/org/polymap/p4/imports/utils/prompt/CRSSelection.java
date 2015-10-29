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
package org.polymap.p4.imports.utils.prompt;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.geotools.referencing.ReferencingFactoryFinder;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.polymap.p4.imports.utils.ISelectionAware;

import com.google.common.collect.Sets;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class CRSSelection
        implements ISelectionAware<CoordinateReferenceSystem,String> {

    private CoordinateReferenceSystem selected = null;

    private final List<String>        selectable;


    public CRSSelection() {
        selectable = ReferencingFactoryFinder.getCRSAuthorityFactories( null ).stream()
                .flatMap( factory -> getAuthorityCodes( factory ).stream() )
                .collect( Collectors.toSet() ).stream().sorted( ( s1, s2 ) -> s1.compareTo( s2 ) )
                .collect( Collectors.toList() );
         
    }


    public List<String> getSelectable() {
        return selectable;
    }


    static Set<String> getAuthorityCodes( CRSAuthorityFactory factory ) {
        try {
            return factory.getAuthorityCodes( CoordinateReferenceSystem.class );
        }
        catch (FactoryException e) {
            e.printStackTrace();
            return Sets.newHashSet();
        }
    }


    public String getDefault() {
        return "EPSG:4326";
    }


    @Override
    public CoordinateReferenceSystem getSelected() {
        return selected;
    }


    @Override
    public void setSelected( CoordinateReferenceSystem selected ) {
        this.selected = selected;
    }
}
