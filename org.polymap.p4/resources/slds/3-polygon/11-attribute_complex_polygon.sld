<?xml version="1.0" encoding="ISO-8859-1"?>
<StyledLayerDescriptor version="1.0.0"
   xsi:schemaLocation="http://www.opengis.net/sld StyledLayerDescriptor.xsd" 
   xmlns="http://www.opengis.net/sld" 
   xmlns:ogc="http://www.opengis.net/ogc" 
  xmlns:xlink="http://www.w3.org/1999/xlink" 
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
<NamedLayer>
    <Name>MyLayer</Name>
    <UserStyle>
      <Title>MyStyle</Title>
      <FeatureTypeStyle>
        <Rule>
          <Name>Other</Name>
          <ogc:Filter>
            <ogc:Or>
              <ogc:PropertyIsEqualTo>
                <ogc:PropertyName>PROP_TYPE_ID</ogc:PropertyName>
                <ogc:Literal>103</ogc:Literal>
              </ogc:PropertyIsEqualTo>
              <ogc:PropertyIsGreaterThanOrEqualTo>
                <ogc:PropertyName>PROP_TYPE_ID</ogc:PropertyName>
                <ogc:Literal>500</ogc:Literal>
              </ogc:PropertyIsGreaterThanOrEqualTo>
              <ogc:And>
                <ogc:PropertyIsGreaterThanOrEqualTo>
                  <ogc:PropertyName>PROP_TYPE_ID</ogc:PropertyName>
                  <ogc:Literal>0</ogc:Literal>
                </ogc:PropertyIsGreaterThanOrEqualTo>
                <ogc:PropertyIsLessThanOrEqualTo>
                  <ogc:PropertyName>PROP_TYPE_ID</ogc:PropertyName>
                  <ogc:Literal>100</ogc:Literal>
                </ogc:PropertyIsLessThanOrEqualTo>
              </ogc:And>
              <ogc:And>
                <ogc:PropertyIsGreaterThanOrEqualTo>
                  <ogc:PropertyName>PROP_TYPE_ID</ogc:PropertyName>
                  <ogc:Literal>106</ogc:Literal>
                </ogc:PropertyIsGreaterThanOrEqualTo>
                <ogc:PropertyIsLessThanOrEqualTo>
                  <ogc:PropertyName>PROP_TYPE_ID</ogc:PropertyName>
                  <ogc:Literal>129</ogc:Literal>
                </ogc:PropertyIsLessThanOrEqualTo>
              </ogc:And>
              <ogc:And>
                <ogc:PropertyIsGreaterThanOrEqualTo>
                  <ogc:PropertyName>PROP_TYPE_ID</ogc:PropertyName>
                  <ogc:Literal>133</ogc:Literal>
                </ogc:PropertyIsGreaterThanOrEqualTo>
                <ogc:PropertyIsLessThanOrEqualTo>
                  <ogc:PropertyName>PROP_TYPE_ID</ogc:PropertyName>
                  <ogc:Literal>299</ogc:Literal>
                </ogc:PropertyIsLessThanOrEqualTo>
              </ogc:And>
            </ogc:Or>
          </ogc:Filter>
          <PolygonSymbolizer>
            <Fill>
              <CssParameter name="fill">#1A9641</CssParameter>
              <CssParameter name="fill-opacity">0.4</CssParameter>
            </Fill>
          </PolygonSymbolizer>
        </Rule>
      </FeatureTypeStyle>
    </UserStyle>
  </NamedLayer>
</StyledLayerDescriptor>

