/* 
 * polymap.org
 * Copyright 2011-2018, Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.p4.data.importer.image;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.widgets.Label;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import org.polymap.core.data.image.cache304.ImageCacheProcessor;
import org.polymap.core.data.pipeline.ProcessorExtension;
import org.polymap.core.operation.IOperationConcernFactory;
import org.polymap.core.operation.OperationConcernAdapter;
import org.polymap.core.operation.OperationInfo;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.ILayer.ProcessorConfig;
import org.polymap.core.runtime.UIThreadExecutor;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.rhei.batik.toolkit.DefaultToolkit;
import org.polymap.rhei.batik.toolkit.SimpleDialog;

import org.polymap.model2.runtime.ValueInitializer;
import org.polymap.p4.layer.NewLayerOperation;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CacheNewLayerOperationConcern
        extends IOperationConcernFactory {

    public IUndoableOperation newInstance( final IUndoableOperation op, final OperationInfo info ) {
        if (op instanceof NewLayerOperation) {
            NewLayerOperation nlop = (NewLayerOperation)op;
            
            return new OperationConcernAdapter() {
                @Override
                public IStatus execute( IProgressMonitor monitor, IAdaptable a ) throws ExecutionException {
                    // dialog
                    AtomicBoolean yes = new AtomicBoolean( true );
                    UIThreadExecutor.sync( () -> {
                        return new SimpleDialog()
                            .title.put( "Image Tile Cache" )
                            .setContents( parent -> {
                                parent.setLayout( FormLayoutFactory.defaults().create() );
                                Label msg = DefaultToolkit.of( parent ).createFlowText( parent,
                                        "An **image tile cache** will be add to the new layer. "
                                        + "This greatly **improves render performance** of the layer by storing "
                                        + "already rendered tiles."
                                        + "<br/><br/>"
                                        + "The cache is an additional processor in the **render pipeline** of the layer. "
                                        + "You can change the configuration or remove the processor later whenever "
                                        + "you want."
                                        + "<br/><br/>"
                                        + "The minimum time between subsequent updates of a cached tile **defaults to "
                                        + ImageCacheProcessor.MIN_UPDATE_TIMEOUT.defaultValue().get().toHours() + "hour(s)**" );
                                FormDataFactory.on( msg ).fill().width( 300 ).height( 230 );
                            })
                            //.addYesAction( action -> yes.set( true ) )
                            .addOkAction( () -> { yes.set( true ); return true; } )
                            .openAndBlock();
                    });

                    // add initializer
                    if (yes.get()) {
                        ValueInitializer<ILayer> chained = nlop.initializer.get().and( (ILayer proto) -> {
                            ProcessorExtension ext = ProcessorExtension.forType( ImageCacheProcessor.class.getName() )
                                    .orElseThrow( () -> new RuntimeException( "ImageCacheProcessor is not available. Is the plugin installed?" ) );
                            proto.processorConfigs.createElement( ProcessorConfig.init( ext ) );
                            return proto;
                        });
                        nlop.initializer.set( chained );
                    }

                    // upstream operation
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
    
}
