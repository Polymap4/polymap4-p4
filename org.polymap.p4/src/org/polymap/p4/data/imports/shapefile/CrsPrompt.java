/*
 * polymap.org Copyright (C) 2015, Falko Bräutigam. All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3.0 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.p4.data.imports.shapefile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.polymap.core.runtime.Streams;
import org.polymap.core.runtime.Streams.ExceptionCollector;
import org.polymap.p4.data.imports.ImporterPrompt;
import org.polymap.p4.data.imports.ImporterPrompt.Severity;
import org.polymap.p4.data.imports.ImporterSite;
import org.polymap.p4.data.imports.utils.FilteredListPromptUIBuilder;

/**
 * 
 *
 * @author @author Joerg Reichert <joerg@mapzone.io>
 */
public class CrsPrompt {

    private static Log                log       = LogFactory.getLog( CrsPrompt.class );

    private static String             DEFAULT   = "EPSG:4326";

    private ImporterSite              site;

    private CoordinateReferenceSystem selection = null;


    public CrsPrompt( ImporterSite site, List<File> files ) {
        this.site = site;

        final List<String> selectable = new ArrayList<String>();

        try (ExceptionCollector<RuntimeException> exc = Streams.exceptions()) {
            // TODO: for later, when we want to list all possible CRSs
//            List<String> allSelectable = ReferencingFactoryFinder
//                    .getCRSAuthorityFactories( null )
//                    .stream()
//                    .flatMap(
//                            factory -> exc.check( ( ) -> factory.getAuthorityCodes( CoordinateReferenceSystem.class ) )
//                                    .stream() )
//                    .collect( Collectors.toSet() ).stream().sorted( ( s1, s2 ) -> s1.compareTo( s2 ) )
//                    .collect( Collectors.toList() );
            List<String> allSelectable = new ArrayList<String>();
            allSelectable.add( "EPSG:3857" ); // Mercator
            allSelectable.add( "EPSG:4326" ); // WGS-84
            selectable.addAll(allSelectable);

            selection = files.stream()
                    .filter( file -> "prj".equalsIgnoreCase( FilenameUtils.getExtension( file.getName() ) ) )
                    .findAny().map( f -> exc.check( ( ) -> parseCoordinateReferenceSystem( f ) ) )
                    .orElse( exc.check( ( ) -> CRS.decode( DEFAULT ) ) );
        }

        site.newPrompt( "crs" ).summary.put( "Coordinate reference system" ).description
                .put( "The coordinate reference system for projecting the feature content. If unsure use EPSG:4326 (= WGS 84)." ).value
                .put( selection.getName().toString() ).severity.put( Severity.VERIFY ).extendedUI
                .put( new FilteredListPromptUIBuilder() {

                    @Override
                    public void submit( ImporterPrompt prompt ) {
                        prompt.ok.set( true );
                        prompt.value.put( selection.getName().toString() );
                    }


                    @Override
                    protected String[] listItems() {
                        return selectable.toArray( new String[selectable.size()] );
                    }


                    @Override
                    protected String initiallySelectedItem() {
                        return selection.getName().toString();
                    }

                    @Override
                    protected String getTooltipText() {
                        return "EPSG Code or part thereof";
                    }

                    @Override
                    protected void handleSelection( String selectedCrs ) {
                        try {
                            selection = CRS.decode( selectedCrs );
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        assert selection != null;
                    }
                } );
    }


    private CoordinateReferenceSystem parseCoordinateReferenceSystem( File f ) {
        return ShapeFileParserUtil.parsePrjFile( f, file -> file.getCoordinateReferenceSystem() );
    }


    /**
     * The selected {@link CoordinateReferenceSystem}.
     */
    public CoordinateReferenceSystem selection() {
        return selection;
    }
}
