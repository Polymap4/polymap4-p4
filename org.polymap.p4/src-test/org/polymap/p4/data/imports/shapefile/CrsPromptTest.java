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
package org.polymap.p4.data.imports.shapefile;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.geotools.feature.FeatureCollection;
import org.geotools.referencing.CRS;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.polymap.core.runtime.config.Config2;
import org.polymap.p4.data.imports.ImporterSite;
import org.polymap.p4.imports.FileImporter;

import com.google.common.base.Charsets;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class CrsPromptTest {

    private class TestResult {

        FeatureCollection featureCollection = null;

        Exception         exception         = null;
    }


    @Test
    public void testNycRoadsImport() throws Exception {
        TestResult testResult = new TestResult();
        Charset charsetToSelect = Charsets.ISO_8859_1;
        CoordinateReferenceSystem crsToSelect = CRS.decode( "EPSG:4326" );
        executeTest( "nyc_roads.zip", charsetToSelect, crsToSelect, testResult );
        Assert.assertNotNull( testResult.featureCollection );
        Assert.assertFalse( testResult.featureCollection.isEmpty() );
        Assert.assertNull( testResult.exception );
    }


    private void executeTest( String testFile, Charset charsetToSelect, CoordinateReferenceSystem crsToSelect,
            TestResult testResult ) throws Exception {
        File zipFile = new File( "resources-test/" + testFile );
        FileInputStream in = new FileInputStream( zipFile );
        List<File> outerFiles = new FileImporter().run( testFile, "application/zip", in );
        File outerShp = outerFiles.stream()
                .filter( file -> FilenameUtils.getExtension( file.getName() ).equals( "shp" ) )
                .findAny().get();
        ImporterSite outerSite = Mockito.mock( ImporterSite.class );
        Config2 config2 = Mockito.mock( Config2.class );
        outerSite.ok = config2;

        ShpImporter importer = new ShpImporter() {

            public void verify( org.eclipse.core.runtime.IProgressMonitor monitor ) {
                this.files = outerFiles;
                this.shp = outerShp;
                super.verify( monitor );
            }


            public ImporterSite site() {
                return outerSite;
            }


            protected CharsetPrompt createCharsetPrompt() {
                CharsetPrompt prompt = Mockito.mock( CharsetPrompt.class );
                try {
                    Mockito.when( prompt.selection() ).thenReturn( charsetToSelect );
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                return prompt;
            }


            protected CrsPrompt createCrsPrompt() {
                CrsPrompt prompt = Mockito.mock( CrsPrompt.class );
                try {
                    Mockito.when( prompt.selection() ).thenReturn( crsToSelect );
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                return prompt;
            }


            public void execute( org.eclipse.core.runtime.IProgressMonitor monitor ) throws Exception {
                super.execute( monitor );
                testResult.featureCollection = super.features;
                testResult.exception = super.exception;
            }
        };
        importer.createPrompts( new NullProgressMonitor() );
        importer.verify( new NullProgressMonitor() );
        importer.execute( new NullProgressMonitor() );
    }
}
