<jmeterTestPlan version="1.2" properties="1.8">
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testclass="TestPlan" testname="${testname}" enabled="true">
      <boolProp name="TestPlan.functional_mode">false</boolProp>
      <stringProp name="TestPlan.comments"></stringProp>
      <stringProp name="TestPlan.user_define_classpath"></stringProp>
      <boolProp name="TestPlan.serialize_threadgroups">false</boolProp>
      <elementProp name="TestPlan.user_defined_variables" elementType="Arguments" guiclass="ArgumentsPanel" testclass="Arguments" testname="User Defined Variables" enabled="true">
        <collectionProp name="Arguments.arguments"/>
      </elementProp>
    </TestPlan>
    <hashTree>
      <ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="Thread Group" enabled="true">
        <boolProp name="ThreadGroup.scheduler">false</boolProp>
        <stringProp name="ThreadGroup.duration"></stringProp>
        <stringProp name="ThreadGroup.num_threads">1</stringProp>
        <stringProp name="ThreadGroup.delay"></stringProp>
        <longProp name="ThreadGroup.start_time">1175121299000</longProp>
        <stringProp name="ThreadGroup.on_sample_error">continue</stringProp>
        <stringProp name="ThreadGroup.ramp_time">1</stringProp>
        <elementProp name="ThreadGroup.main_controller" elementType="LoopController" guiclass="LoopControlPanel" testclass="LoopController" testname="Loop Controller" enabled="true">
          <stringProp name="LoopController.loops">1</stringProp>
          <boolProp name="LoopController.continue_forever">false</boolProp>
        </elementProp>
        <longProp name="ThreadGroup.end_time">1175121299000</longProp>
      </ThreadGroup>
      <hashTree>
        <ConfigTestElement guiclass="HttpDefaultsGui" testclass="ConfigTestElement" testname="HTTP Request Defaults" enabled="true">
          <stringProp name="HTTPSampler.domain">${host}</stringProp>
          <stringProp name="HTTPSampler.path">/${geoserver}/wms</stringProp>
          <stringProp name="HTTPSampler.port">${port}</stringProp>
          <elementProp name="HTTPsampler.Arguments" elementType="Arguments" guiclass="HTTPArgumentsPanel" testclass="Arguments" testname="User Defined Variables" enabled="true">
            <collectionProp name="Arguments.arguments">
              <elementProp name="" elementType="HTTPArgument">
                <boolProp name="HTTPArgument.use_equals">true</boolProp>
                <boolProp name="HTTPArgument.always_encode">false</boolProp>
                <stringProp name="Argument.name">service</stringProp>
                <stringProp name="Argument.value">wms</stringProp>
                <stringProp name="Argument.metadata">=</stringProp>
              </elementProp>
              <elementProp name="" elementType="HTTPArgument">
                <boolProp name="HTTPArgument.use_equals">true</boolProp>
                <boolProp name="HTTPArgument.always_encode">false</boolProp>
                <stringProp name="Argument.name">request</stringProp>
                <stringProp name="Argument.value">getmap</stringProp>
                <stringProp name="Argument.metadata">=</stringProp>
              </elementProp>
              <elementProp name="" elementType="HTTPArgument">
                <boolProp name="HTTPArgument.use_equals">true</boolProp>
                <boolProp name="HTTPArgument.always_encode">false</boolProp>
                <stringProp name="Argument.name">version</stringProp>
                <stringProp name="Argument.value">1.1.1</stringProp>
                <stringProp name="Argument.metadata">=</stringProp>
              </elementProp>
            </collectionProp>
          </elementProp>
          <stringProp name="HTTPSampler.protocol"></stringProp>
        </ConfigTestElement>
        <hashTree/>
        <HTTPSampler guiclass="HttpTestSampleGui" testclass="HTTPSampler" testname="HTTP Request" enabled="true">
          <stringProp name="HTTPSampler.domain"></stringProp>
          <stringProp name="HTTPSampler.FILE_NAME"></stringProp>
          <stringProp name="HTTPSampler.path"></stringProp>
          <stringProp name="HTTPSampler.method">GET</stringProp>
          <elementProp name="HTTPsampler.Arguments" elementType="Arguments" guiclass="HTTPArgumentsPanel" testclass="Arguments" testname="User Defined Variables" enabled="true">
            <collectionProp name="Arguments.arguments">
              <elementProp name="" elementType="HTTPArgument">
                <boolProp name="HTTPArgument.use_equals">true</boolProp>
                <boolProp name="HTTPArgument.always_encode">false</boolProp>
                <stringProp name="Argument.name">srs</stringProp>
                <stringProp name="Argument.value">epsg:4326</stringProp>
                <stringProp name="Argument.metadata">=</stringProp>
              </elementProp>
              <elementProp name="" elementType="HTTPArgument">
                <boolProp name="HTTPArgument.use_equals">true</boolProp>
                <boolProp name="HTTPArgument.always_encode">false</boolProp>
                <stringProp name="Argument.name">format</stringProp>
                <stringProp name="Argument.value">image/png</stringProp>
                <stringProp name="Argument.metadata">=</stringProp>
              </elementProp>
              <elementProp name="" elementType="HTTPArgument">
                <boolProp name="HTTPArgument.use_equals">true</boolProp>
                <boolProp name="HTTPArgument.always_encode">false</boolProp>
                <stringProp name="Argument.name">layers</stringProp>
                <stringProp name="Argument.value">nurc:~gtiff172030523_192_106_197_2_0</stringProp>
                <stringProp name="Argument.metadata">=</stringProp>
              </elementProp>
              <elementProp name="" elementType="HTTPArgument">
                <boolProp name="HTTPArgument.use_equals">true</boolProp>
                <boolProp name="HTTPArgument.always_encode">false</boolProp>
                <stringProp name="Argument.name">styles</stringProp>
                <stringProp name="Argument.value">raster</stringProp>
                <stringProp name="Argument.metadata">=</stringProp>
              </elementProp>
              <elementProp name="" elementType="HTTPArgument">
                <boolProp name="HTTPArgument.use_equals">true</boolProp>
                <boolProp name="HTTPArgument.always_encode">false</boolProp>
                <stringProp name="Argument.name">bbox</stringProp>
                <stringProp name="Argument.value">-68.7960467872588,37.7613137356769,-58.6506290907908,42.1742707652839</stringProp>
                <stringProp name="Argument.metadata">=</stringProp>
              </elementProp>
              <elementProp name="" elementType="HTTPArgument">
                <boolProp name="HTTPArgument.use_equals">true</boolProp>
                <boolProp name="HTTPArgument.always_encode">false</boolProp>
                <stringProp name="Argument.name">height</stringProp>
                <stringProp name="Argument.value">218</stringProp>
                <stringProp name="Argument.metadata">=</stringProp>
              </elementProp>
              <elementProp name="" elementType="HTTPArgument">
                <boolProp name="HTTPArgument.use_equals">true</boolProp>
                <boolProp name="HTTPArgument.always_encode">false</boolProp>
                <stringProp name="Argument.name">width</stringProp>
                <stringProp name="Argument.value">500</stringProp>
                <stringProp name="Argument.metadata">=</stringProp>
              </elementProp>
            </collectionProp>
          </elementProp>
          <stringProp name="HTTPSampler.FILE_FIELD"></stringProp>
          <stringProp name="HTTPSampler.mimetype"></stringProp>
          <boolProp name="HTTPSampler.auto_redirects">false</boolProp>
          <boolProp name="HTTPSampler.follow_redirects">true</boolProp>
          <stringProp name="HTTPSampler.port"></stringProp>
          <boolProp name="HTTPSampler.use_keepalive">true</boolProp>
          <stringProp name="HTTPSampler.monitor">false</stringProp>
          <stringProp name="HTTPSampler.protocol"></stringProp>
        </HTTPSampler>
        <hashTree/>
        <Summariser guiclass="SummariserGui" testclass="Summariser" testname="Generate Summary Results" enabled="true"/>
        <hashTree/>
        <ResultCollector guiclass="ViewResultsFullVisualizer" testclass="ResultCollector" testname="View Results Tree" enabled="true">
          <objProp>
            <value class="SampleSaveConfiguration">
              <time>true</time>
              <latency>true</latency>
              <timestamp>true</timestamp>
              <success>true</success>
              <label>true</label>
              <code>true</code>
              <message>true</message>
              <threadName>true</threadName>
              <dataType>true</dataType>
              <encoding>false</encoding>
              <assertions>true</assertions>
              <subresults>true</subresults>
              <responseData>false</responseData>
              <samplerData>false</samplerData>
              <xml>false</xml>
              <fieldNames>false</fieldNames>
              <responseHeaders>false</responseHeaders>
              <requestHeaders>false</requestHeaders>
              <responseDataOnError>false</responseDataOnError>
              <saveAssertionResultsFailureMessage>false</saveAssertionResultsFailureMessage>
              <assertionsResultsToSave>0</assertionsResultsToSave>
            </value>
            <name>saveConfig</name>
          </objProp>
          <stringProp name="filename"></stringProp>
          <boolProp name="ResultCollector.error_logging">false</boolProp>
        </ResultCollector>
        <hashTree/>
      </hashTree>
    </hashTree>
  </hashTree>
</jmeterTestPlan>