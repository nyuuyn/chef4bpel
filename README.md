chef4bpel
=========

BPEL Extension to execute Chef scripts

Chef4BPEL Extension Info:
=========
Build:

- run 'mvn clean package' in Chef4BPEL folder.

Run:
- copy the chef4bpel-*.jar (pre-build under build/ or own build under target/) into the WSO2 BPS folders under {bps-root}/repository/components/lib 
- add to or adjust {bps-root}/repository/conf/bps.xml with following lines:
```xml
    <tns:ExtensionBundles xmlns:tns="http://wso2.org/bps/config">
        <tns:runtimes>
            <tns:runtime class="org.opentosca.chef4bpel.extension.Chef4BpelExtensionBundle"/>
        </tns:runtimes>
    </tns:ExtensionBundles> 
```
Chef4BPEL Test Process:
=========
- Deploy the provided .zip file under build/ unto you WSO2 BPS
- Test with TryIt or some other soap tool

Tested with Java 1.6 and WSO2 BPS 2.1.2
