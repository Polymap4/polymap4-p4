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
package org.polymap.p4.imports.utils.prompt;

import java.util.Optional;

import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.polymap.p4.data.imports.ImporterPrompt;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class CRSPromptBuilder
        extends AbstractPromptBuilder<CoordinateReferenceSystem,String> {

    public CRSPromptBuilder( CRSSelection crsAware ) {
        super( crsAware );
    }


    @Override
    public void submit( ImporterPrompt prompt ) {
        CoordinateReferenceSystem crs;
        try {
            crs = CRS.decode( getValue() );
            getProvider().setSelected( crs );
            prompt.value.put( getValue() );
            prompt.ok.set( true );
        }
        catch (FactoryException e) {
            e.printStackTrace();
            prompt.ok.set( false );
        }
    }


    @Override
    protected boolean getInitialSelection( String cs ) {
        boolean selected = false;
        if (getProvider().getSelected() == null) {
            selected = cs == getProvider().getDefault();
        }
        else {
            selected = cs == transformToValueObject( getProvider().getSelected() );
        }
        return selected;
    }


    @Override
    protected String transformFromDisplayValue( String listEntry ) {
        return listEntry;
    }


    @Override
    protected String transformToDisplayValue( String value ) {
        return value;
    }


    @Override
    protected String transformToValueObject( CoordinateReferenceSystem selected ) {
        String code = selected.getName().getCode();
        if (!code.contains( ":" )) {
            Optional<String> opt = selected.getIdentifiers().stream().map( ident -> ident.toString() )
                    .filter( cd -> cd.contains( ":" ) ).findFirst();
            if (opt.isPresent()) {
                return opt.get();
            }
        }
        return selected.getName().getCode();
    }
}
