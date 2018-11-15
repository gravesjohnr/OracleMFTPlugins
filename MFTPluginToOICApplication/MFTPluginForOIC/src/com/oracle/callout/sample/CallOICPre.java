package com.oracle.callout.sample;

import java.util.Map;
import java.util.HashMap;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.IOException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.client.Entity;

import oracle.tip.mft.bean.MFTMessage;
import oracle.tip.mft.engine.processsor.plugin.PluginContext;
import oracle.tip.mft.engine.processsor.plugin.PluginOutput;
import oracle.tip.mft.engine.processsor.plugin.PreCalloutPlugin;

import java.util.Properties;

//
// This expects a properties file locate in the domain's 'oic' directory:
// e.g. mydomain/oic/oic.properties.
// It should contain four fields: (examples.  These can be any rest service which conforms to the json payloads for each.)
// sourcePreUrl=https://<myinstance>.oraclecloud.com/ic/api/integration/v1/flows/rest/PROCESSMFTNOTIFICATIONSSOURCEPRE/1.0/notify
// targetPreUrl=https://<myinstance>.oraclecloud.com/ic/api/integration/v1/flows/rest/PROCESSMFTNOTIFICATIONSTARGETPRE/1.0/notify
// targetPostUrl=https://<myinstance>.oraclecloud.com/ic/api/integration/v1/flows/rest/PROCESSMFTNOTIFICATIONSTARGETPOS/1.0/notify
// authorizationHeaderValue=Basic ...=

public class CallOICPre implements PreCalloutPlugin {

  // determines which "process" method is called"
  public boolean isPayloadChangeRequired(PluginContext context, Map<String, String> calloutParams) {
      return false;
    }

  // Not used because the isPayloadChangedRequired is always false
  public PluginOutput process(PluginContext context, InputStream input, OutputStream out, Map<String, String> calloutParams) {
    return null;
  }

  public PluginOutput process(PluginContext context, InputStream input, Map<String, String> calloutParams) {
    //  
    // Get OIC details from properties file
    //  
    Properties prop = new Properties();
    InputStream propertiesInput = null;
    try {
      propertiesInput = new FileInputStream(System.getProperty("user.dir")+"/oic/oic.properties");
      if(propertiesInput == null) {
        PluginContext.getLogger().info( "CallRest","Missing oic.properties file '" +System.getProperty("user.dir")+"/oic/oic.properties'");
        PluginOutput pOutput = new PluginOutput();
        pOutput.setException(new Exception("Missing oic.properties file '" +System.getProperty("user.dir")+"/oic/oic.properties'"));
        return pOutput;
      }
      prop.load(propertiesInput);

    } catch (IOException ex) {
      PluginContext.getLogger().info( "CallRest","Exception in trying to read properties file: '" +System.getProperty("user.dir")+"/oic/oic.properties'");
      ex.printStackTrace();
      PluginOutput pOutput = new PluginOutput();
      pOutput.setException(ex);
      return pOutput;
    } finally {
      if(propertiesInput != null) {
        try {
          propertiesInput.close();
        } catch (IOException e) { 
          e.printStackTrace();
          PluginOutput pOutput = new PluginOutput();
          pOutput.setException(e);
          return pOutput;
        }
      } 
    }	

    //
    // Get Auth details
    //
    String authorizationHeaderValue=prop.getProperty("authorizationHeaderValue");
    if(authorizationHeaderValue == null || authorizationHeaderValue == "") {
      PluginOutput pOutput = new PluginOutput();
      pOutput.setException(new Exception("Missing authorizationHeaderValue in oic.properties file '" +System.getProperty("user.dir")+"/oic/oic.properties'"));
      return pOutput;
    }
 
    String restUrl = "";
    if(context.getMessage().getClass().getName() == "oracle.tip.mft.bean.SourceMessage") {
      restUrl = prop.getProperty("sourcePreUrl");
      if(restUrl == null || restUrl == "") {
        PluginOutput pOutput = new PluginOutput();
        pOutput.setException(new Exception("Missing sourcePreUrl in oic.properties file '" +System.getProperty("user.dir")+"/oic/oic.properties'"));
        return pOutput;
      }
    } else {
      restUrl = prop.getProperty("targetPreUrl");
      if(restUrl == null || restUrl == "") {
        PluginOutput pOutput = new PluginOutput();
        pOutput.setException(new Exception("Missing targetPreUrl in oic.properties file '" +System.getProperty("user.dir")+"/oic/oic.properties'"));
        return pOutput;
      }
    }
    
    MFTMessage mftMessage = context.getMessage();
    PluginContext.getLogger().info( "CallRest","Process: " +calloutParams);
    PluginContext.getLogger().info( "CallRest","Sending To: " +restUrl);
    String action = calloutParams.get("Action");
    String description = calloutParams.get("Description");
    try {
      Client client = ClientBuilder.newClient();
      WebTarget target = client.target(restUrl);
      WebTarget resourceWebTarget;
      resourceWebTarget = target.path("");
      Invocation.Builder invocationBuilder;
      invocationBuilder = resourceWebTarget.request(MediaType.APPLICATION_JSON_TYPE).header("Authorization", authorizationHeaderValue).header("MFT-Action",action).header("MFT-Description",description);

      //Build message
      Response response = invocationBuilder.post(Entity.entity(mftMessage, MediaType.APPLICATION_JSON));
    } catch (Exception e) {
      PluginOutput pOutput = new PluginOutput();
      pOutput.setException(e);
      return pOutput;
    }
    PluginOutput pOutput = new PluginOutput();
    return pOutput;
  }
}