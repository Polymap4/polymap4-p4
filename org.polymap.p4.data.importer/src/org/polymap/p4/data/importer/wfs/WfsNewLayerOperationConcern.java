/* 
 * polymap.org
 * Copyright (C) 2018, the @authors. All rights reserved.
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
package org.polymap.p4.data.importer.wfs;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Label;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import org.polymap.core.data.feature.storecache.StoreCacheProcessor;
import org.polymap.core.data.pipeline.ProcessorExtension;
import org.polymap.core.data.wfs.catalog.WfsServiceInfo;
import org.polymap.core.operation.IOperationConcernFactory;
import org.polymap.core.operation.OperationConcernAdapter;
import org.polymap.core.operation.OperationInfo;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.ILayer.ProcessorConfig;
import org.polymap.core.runtime.UIThreadExecutor;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.StatusDispatcher;

import org.polymap.rhei.batik.toolkit.DefaultToolkit;
import org.polymap.rhei.batik.toolkit.SimpleDialog;

import org.polymap.model2.runtime.ValueInitializer;
import org.polymap.p4.layer.NewLayerOperation;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class WfsNewLayerOperationConcern
        extends IOperationConcernFactory {

    private static final Log log = LogFactory.getLog( WfsNewLayerOperationConcern.class );

    @Override
    public IUndoableOperation newInstance( IUndoableOperation op, OperationInfo info ) {
        if (op instanceof NewLayerOperation &&
                ((NewLayerOperation)op).res.get().getServiceInfo() instanceof WfsServiceInfo ) {
            
            // Concern
            return new OperationConcernAdapter() {
                @Override
                public IStatus execute( IProgressMonitor monitor, IAdaptable a ) throws ExecutionException {
                    AtomicBoolean yes = new AtomicBoolean( false );
                    // wait until the user completed the dialog; this blocks the concern
                    // and the entire operation, so that subsequent operations actually
                    // work on our results
                    UIThreadExecutor.sync( () -> new SimpleDialog()
                        .title.put( "New WFS layer" )
                        .setContents( parent -> {
                            parent.setLayout( FormLayoutFactory.defaults().create() );
                            Label msg = DefaultToolkit.of( parent ).createFlowText( parent,
                                    "Do you want to add a **local cache** for this WFS?"
                                    + "<br/><br/>"
                                    + "The cache is an additional processor in the **render pipeline** of the layer. "
                                    + "You can change the configuration or remove the processor later whenever "
                                    + "you want."
                                    + "<br/><br/>"
                                    + "The minimum time between subsequent updates of the cache **defaults to "
                                    + StoreCacheProcessor.MIN_UPDATE_TIMEOUT.defaultValue().get().toHours() + "hour(s)**" );
                            FormDataFactory.on( msg ).fill().width( 300 ).height( 180 );
                        })
                        .addNoAction()
                        .addYesAction( action -> { yes.set( true ); } ) 
                        .openAndBlock()
                    );
                    // add cache                    
                    if (yes.get()) {
                        NewLayerOperation nlop = (NewLayerOperation)op;
                        ValueInitializer<ILayer> chained = nlop.initializer.get().and( (ILayer proto) -> {
                            ProcessorExtension ext = ProcessorExtension.forType( StoreCacheProcessor.class.getName() )
                                    .orElseThrow( () -> new RuntimeException( "StoreCacheProcessor is not available. Is the plugin installed?" ) );
                            proto.processorConfigs.createElement( ProcessorConfig.init( ext ) );
                            return proto;
                        });
                        nlop.initializer.set( chained );
                    }
                    // execute delegate
                    return super.execute( monitor, a );
                }

                @Override
                protected OperationInfo getInfo() {
                    return info;
                }
            };
        }
        return null;
    }
    
    
    protected void createCache( NewLayerOperation op ) {
        try {
            ProcessorExtension ext = ProcessorExtension.forType( StoreCacheProcessor.class.getName() )
                    .orElseThrow( () -> new RuntimeException( "StoreCacheProcessor is not available. Is the plugin installed?" ) );

            op.layer.get().processorConfigs.createElement( ProcessorConfig.init( ext ) );
        }
        catch (Exception e) {
            StatusDispatcher.handleError( "Unable to add/configure the cache.", e );
        }
    }

}
