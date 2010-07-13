<? header ("Content-Type: application/x-java-jnlp-file"); ?>
<jnlp
  spec="1.0+"
  codebase="http://jakubadamek.me.cz/trickbenchmark"
  href="perla/perla.php">
  <information>
    <title>Robot Emil</title>
    <vendor>Jakub Adámek</vendor>
    <!--homepage href="docs/help.html"/-->
    <description>Benchmark cen hotelových pokojů</description>
    <icon href="robot.jpg"/>
    <offline-allowed/>
  </information>
  <security>
      <all-permissions/>
  </security>
  <resources>
    <j2se version="1.5+" max-heap-size="512M"/>
    <property name="customer" value="PERLA"/>
    <jar href="trickbenchmark.jar"/>
    <jar href="lib/commons-codec-1.4.jar"/>
    <jar href="lib/commons-collections-3.2.1.jar"/>
    <jar href="lib/commons-httpclient-3.1.jar"/>
    <jar href="lib/commons-io-1.4.jar"/>
    <jar href="lib/commons-lang-2.4.jar"/>
    <jar href="lib/commons-logging-1.1.1.jar"/>
    <jar href="lib/cssparser-0.9.5.jar"/>
    <jar href="lib/dll-loader-1.2.1.jar"/>
    <jar href="lib/htmlparser-1.6.jar"/>
    <jar href="lib/htmlunit-2.6.jar"/>
    <jar href="lib/htmlunit-core-js-2.6.jar"/>
    <jar href="lib/jxl-2.6.jar"/>
    <jar href="lib/nekohtml-1.9.13.jar"/>
    <jar href="lib/sac-1.3.jar"/>
    <jar href="lib/serializer-2.7.1.jar"/>
    <jar href="lib/sqlite-werner-1.6.3.jar"/>
    <jar href="lib/swt-3.3-win32-win32-x86.jar"/>
    <jar href="lib/xalan-2.7.1.jar"/>
    <jar href="lib/xercesImpl-2.9.1.jar"/>
    <jar href="lib/xml-apis-1.3.04.jar"/>
  </resources>
  <application-desc main-class="com.jakubadamek.robotemil.App"/>
</jnlp>

