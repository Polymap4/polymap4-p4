/*
 * polymap.org Copyright (C) 2016, the @authors. All rights reserved.
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
package org.polymap.p4.data.importer.kml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import org.geotools.referencing.CRS;
import org.junit.Test;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;

public class KMLFeatureIteratorTest {

    @Test
    public void normalFile() throws Exception {
        File file = new File( this.getClass().getResource( "doc.kml" ).getFile() );
        KMLFeatureIterator it = new KMLFeatureIterator( file, "foo" );
        SimpleFeatureType featureType = it.getFeatureType();
        int i = 0;
        while (it.hasNext()) {
            SimpleFeature feature = it.next();
            assertNull( feature.getIdentifier() );
            assertEquals( CRS.decode( "EPSG:4326" ), feature.getType().getCoordinateReferenceSystem() );
            Object defaultGeometry = feature.getDefaultGeometry();
            assertNotNull( defaultGeometry );
            GeometryAttribute defaultGeometryProperty = feature.getDefaultGeometryProperty();
            assertNotNull( defaultGeometryProperty );
            GeometryDescriptor geometryDescriptor = feature.getType().getGeometryDescriptor();
            assertNotNull( geometryDescriptor );
            GeometryDescriptor geometryDescriptor2 = featureType.getGeometryDescriptor();
            assertNotNull( geometryDescriptor2 );
            assertEquals( geometryDescriptor, geometryDescriptor2 );
            i++;
        }
        assertEquals( CRS.decode( "EPSG:4326" ), featureType.getCoordinateReferenceSystem() );
        it.close();
        assertEquals( 78, i );
        assertEquals( null, featureType.getName().getNamespaceURI() );
        assertEquals( "foo", featureType.getName().getLocalPart() );
    }
}
