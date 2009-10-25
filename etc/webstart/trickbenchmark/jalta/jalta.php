<? header ("Content-Type: application/x-java-jnlp-file"); ?>
<jnlp
  spec="1.0+"
  codebase="http://jakubadamek.me.cz/trickbenchmark/jalta"
  href="jalta.php">
  <information>
    <title>Robot Emil</title>
    <vendor>Jakub Adámek</vendor>
    <!--homepage href="docs/help.html"/-->
    <description>Robot, který načte ceny hotelových pokojů v daném období</description>
    <icon href="robot.jpg"/>
    <offline-allowed/>
  </information>
  <security>
      <all-permissions/>
  </security>
  <resources>
    <j2se version="1.5+" max-heap-size="512M"/>
    <jar href="jalta.jar"/>
  </resources>
  <application-desc main-class="netx.jnlp.runtime.Boot13"/>
</jnlp>
