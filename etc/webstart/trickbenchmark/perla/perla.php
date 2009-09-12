<? header ("Content-Type: application/x-java-jnlp-file"); ?>
<jnlp
  spec="1.0+"
  codebase="http://jakubadamek.me.cz/trickbenchmark/perla"
  href="perla.php">
  <information>
    <title>Trick Benchmark</title>
    <vendor>Jakub Adámek</vendor>
    <!--homepage href="docs/help.html"/-->
    <description>Benchmark cen hotelových pokojů</description>
    <!--description kind="short">A demo of the capabilities of the Swing Graphical User Interface.</description-->
    <icon href="robot.jpg"/>
    <icon kind="splash" href="robot.jpg"/>
    <offline-allowed/>
  </information>
  <security>
      <all-permissions/>
  </security>
  <resources>
    <j2se version="1.5+" max-heap-size="512M"/>
    <property name="customer" value="PERLA"/>
    <jar href="robotemil.jar"/>
    <jar href="lib/commons-codec-1.3.jar"/>
    <jar href="lib/commons-collections-3.2.jar"/>
    <jar href="lib/commons-httpclient-3.1.jar"/>
    <jar href="lib/commons-io-1.4.jar"/>
    <jar href="lib/commons-lang-2.4.jar"/>
    <jar href="lib/commons-logging-1.1.1.jar"/>
    <jar href="lib/cssparser-0.9.5.jar"/>
    <jar href="lib/htmlparser-1.6.jar"/>
    <jar href="lib/htmlunit-2.2.jar"/>
    <jar href="lib/htmlunit-core-js-2.2.jar"/>
    <jar href="lib/jxl-2.6.jar"/>
    <jar href="lib/nekohtml-1.9.8.jar"/>
    <jar href="lib/sac-1.3.jar"/>
    <jar href="lib/swt-3.3-win32-win32-x86.jar"/>
    <jar href="lib/xalan-2.7.0.jar"/>
    <jar href="lib/xercesImpl-2.8.1.jar"/>
    <jar href="lib/xml-apis-1.0.b2.jar"/>
  </resources>
  <application-desc main-class="com.jakubadamek.robotemil.App"/>
</jnlp>
