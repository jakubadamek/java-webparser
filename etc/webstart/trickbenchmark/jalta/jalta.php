﻿<? header ("Content-Type: application/x-java-jnlp-file"); ?>
<jnlp
  spec="1.0+"
  codebase="http://jakubadamek.me.cz/trickbenchmark"
  href="jalta/jalta.php">
  <information>
    <title>Trick Benchmark</title>
    <vendor>Jakub Adámek</vendor>
    <!--homepage href="docs/help.html"/-->
    <description>Benchmark cen hotelových pokojů</description>
    <icon href="splash.png" kind="splash"/>
    <offline-allowed/>
  </information>
  <security>
      <all-permissions/>
  </security>
  <resources>
    <j2se version="1.5+" max-heap-size="512M"/>
    <jar href="trickbenchmark.jar"/>
    <jar href="lib/aopalliance-1.0.jar"/>
    <jar href="lib/aspectjweaver-1.6.12.jar"/>
    <jar href="lib/commons-codec-1.4.jar"/>
    <jar href="lib/commons-collections-3.2.1.jar"/>
    <jar href="lib/commons-io-1.4.jar"/>
    <jar href="lib/commons-lang-2.4.jar"/>
    <jar href="lib/commons-lang3-3.1.jar"/>
    <jar href="lib/commons-logging-1.1.3.jar"/>
    <jar href="lib/cssparser-0.9.11.jar"/>
    <jar href="lib/h2-1.3.154.jar"/>
    <jar href="lib/htmllexer-2.1.jar"/>
    <jar href="lib/htmlparser-2.1.jar"/>
    <jar href="lib/htmlunit-2.13.jar"/>
    <jar href="lib/htmlunit-core-js-2.13.jar"/>
    <jar href="lib/httpclient-4.3.1.jar"/>
    <jar href="lib/httpcore-4.3.jar"/>
    <jar href="lib/httpmime-4.3.1.jar"/>
    <jar href="lib/jackson-annotations-2.1.0.jar"/>
    <jar href="lib/jackson-core-2.1.0.jar"/>
    <jar href="lib/jackson-databind-2.1.0.jar"/>
    <jar href="lib/jcl-over-slf4j-1.7.5.jar"/>
    <jar href="lib/jetty-http-8.1.12.v20130726.jar"/>
    <jar href="lib/jetty-io-8.1.12.v20130726.jar"/>
    <jar href="lib/jetty-util-8.1.12.v20130726.jar"/>
    <jar href="lib/jetty-websocket-8.1.12.v20130726.jar"/>
    <jar href="lib/joda-time-1.6.2.jar"/>
    <jar href="lib/jsoup-1.7.2.jar"/>
    <jar href="lib/jul-to-slf4j-1.7.5.jar"/>
    <jar href="lib/jxl-2.6.10.jar"/>
    <jar href="lib/logback-classic-1.0.13.jar"/>
    <jar href="lib/logback-core-1.0.13.jar"/>
    <jar href="lib/nekohtml-1.9.19.jar"/>
    <jar href="lib/sac-1.3.jar"/>
    <jar href="lib/serializer-2.7.1.jar"/>
    <jar href="lib/slf4j-api-1.7.5.jar"/>
    <jar href="lib/spring-aop-2.0.8.jar"/>
    <jar href="lib/spring-beans-2.0.8.jar"/>
    <jar href="lib/spring-context-2.0.8.jar"/>
    <jar href="lib/spring-core-2.0.8.jar"/>
    <jar href="lib/spring-dao-2.0.8.jar"/>
    <jar href="lib/spring-jdbc-2.0.8.jar"/>
    <jar href="lib/win32.x86-3.6.1.v3665c.jar"/>
    <jar href="lib/xalan-2.7.1.jar"/>
    <jar href="lib/xercesImpl-2.11.0.jar"/>
    <jar href="lib/xml-apis-1.4.01.jar"/>
</resources>
  <application-desc main-class="com.jakubadamek.robotemil.App">
    <argument>JALTA</argument>
  </application-desc>
</jnlp>

