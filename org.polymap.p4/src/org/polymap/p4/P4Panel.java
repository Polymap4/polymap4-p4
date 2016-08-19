/* 
 * polymap.org
 * Copyright (C) 2016, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.p4;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.Scope;
import org.polymap.rhei.batik.toolkit.md.MdToolkit;

import org.polymap.p4.layer.FeaturePanel;
import org.polymap.p4.layer.FeatureSelection;
import org.polymap.p4.layer.FeatureSelectionTableContrib;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class P4Panel
        extends DefaultPanel {

    private static Log log = LogFactory.getLog( FeaturePanel.class );
    
    public static final int                 SIDE_PANEL_WIDTH = 380;
    
    public static final int                 SIDE_PANEL_WIDTH2 = 420;
    
    /**
     * The <b>active</b> layer and the <b>selected</b> features from this layer.
     * There is just one active layer at a given time. This layer is selected by
     * choosing the feature table to open ({@link FeatureSelectionTableContrib}).
     */
    @Scope( P4Plugin.Scope )
    protected Context<FeatureSelection>     featureSelection;
    
    /**
     * Sets size to: 
     * <pre>
     * SIDE_PANEL_WIDTH, SIDE_PANEL_WIDTH, Integer.MAX_VALUE
     * </pre>
     */
    @Override
    public void init() {
        site().setSize( SIDE_PANEL_WIDTH, SIDE_PANEL_WIDTH, Integer.MAX_VALUE );
    }

    
    protected MdToolkit tk() {
        return (MdToolkit)site().toolkit();
    }
    
    
    protected void createErrorContents( Composite parent, String msg, Throwable cause ) {
        log.warn( msg, cause );
        tk().createFlowText( parent, msg );
    }
    
    
//    protected void openErrorDialog( String msg, Throwable cause ) {
//        tk().createSimpleDialog( "" )
//    }
    
}
