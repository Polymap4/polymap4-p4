/* 
 * polymap.org
 * Copyright (C) 2015-2016, Falko Bräutigam. All rights reserved.
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
package org.polymap.p4.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.polymap.core.data.feature.DataSourceProcessor;
import org.polymap.core.data.feature.FeatureRenderProcessor2;
import org.polymap.core.data.image.ImageDecodeProcessor;
import org.polymap.core.data.image.ImageEncodeProcessor;
import org.polymap.core.data.pipeline.AutoWirePipelineBuilder;
import org.polymap.core.data.pipeline.DataSourceDescriptor;
import org.polymap.core.data.pipeline.Param.ParamsHolder;
import org.polymap.core.data.pipeline.Pipeline;
import org.polymap.core.data.pipeline.PipelineBuilderException;
import org.polymap.core.data.pipeline.PipelineProcessor;
import org.polymap.core.data.pipeline.PipelineProcessorSite;
import org.polymap.core.data.pipeline.PipelineProcessorSite.Params;
import org.polymap.core.data.pipeline.ProcessorDescriptor;
import org.polymap.core.data.pipeline.ProcessorExtension;
import org.polymap.core.data.raster.RasterRenderProcessor;
import org.polymap.core.data.wms.WmsRenderProcessor;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.ILayer.ProcessorConfig;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class P4PipelineBuilder
        extends AutoWirePipelineBuilder
        implements ParamsHolder {

    /** Terminal and transformer processors. */
    private static final Class<PipelineProcessor>[] DEFAULT_PIPELINE_PROCESSORS = new Class[] {
        ImageEncodeProcessor.class,
        ImageDecodeProcessor.class,
        FeatureRenderProcessor2.class,
        DataSourceProcessor.class,
        WmsRenderProcessor.class,
        RasterRenderProcessor.class,
    };

    
    public static P4PipelineBuilder forLayer( ILayer layer ) {
        List<ProcessorDescriptor> procs = new ArrayList();
        for (ProcessorConfig config : layer.processorConfigs) {
            ProcessorExtension ext = config.ext.get()
                    .orElseThrow( () -> new RuntimeException( "Plugin of processor is not installed: " + config.type.get() ) );
            procs.add( new ProcessorDescriptor( ext.getProcessorType(), config.params() ) );
        }
        return new P4PipelineBuilder( (String)layer.id(), procs );
    }
    
    
    // instance *******************************************
    
    private Params                      params = new Params();
    
    private List<ProcessorDescriptor>   procs;

    private String                      layerId;
    

    public P4PipelineBuilder( String layerId ) {
        this( layerId, Collections.EMPTY_LIST );
    }
    
    public P4PipelineBuilder( String layerId, List<ProcessorDescriptor> procs ) {
        super( DEFAULT_PIPELINE_PROCESSORS );
        this.procs = procs;
        this.layerId = layerId;
    }
    
    @Override
    public Params params() {
        return params;
    }
    
    public Optional<Pipeline> createPipeline( Class<? extends PipelineProcessor> usecase, DataSourceDescriptor dsd )
            throws PipelineBuilderException {
        return Optional.ofNullable( createPipeline( layerId, usecase, dsd, procs ) );
    }

    @Override
    @SuppressWarnings( "hiding" )
    public Optional<Pipeline> createPipeline( String layerId, Class<? extends PipelineProcessor> usecase, 
            DataSourceDescriptor dsd ) throws PipelineBuilderException {
        assert this.layerId.equals( layerId );
        return createPipeline( usecase, dsd );
    }

    @Override
    protected PipelineProcessorSite createProcessorSite( ProcessorDescriptor procDesc ) {
        Params all = new Params();
        all.putAll( params );
        all.putAll( procDesc.params() );
        return new PipelineProcessorSite( all );
    }
    
}
