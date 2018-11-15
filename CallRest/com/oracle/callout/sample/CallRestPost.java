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

import oracle.tip.mft.bean.Instance;
import oracle.tip.mft.bean.MFTMessage;
import oracle.tip.mft.bean.TargetMessage;
import oracle.tip.mft.bean.SourceMessage;
import oracle.tip.mft.engine.processsor.plugin.PluginContext;
import oracle.tip.mft.engine.processsor.plugin.PluginOutput;
import oracle.tip.mft.engine.processsor.plugin.PreCalloutPlugin;
import oracle.tip.mft.engine.processsor.plugin.PostCalloutPlugin;
import oracle.tip.mft.bean.DataStorage;

import java.lang.reflect.Method;
import java.lang.reflect.Field;

import java.util.Properties;

public class CallRestPost implements PostCalloutPlugin {

  public void process(PluginContext context, InputStream input, Map<String, String> calloutParams) throws Exception {

    //  
    // Get OIC details from properties file 
    //  
    Properties prop = new Properties();
    InputStream propertiesInput = null;
    try {
      propertiesInput = new FileInputStream(System.getProperty("user.dir")+"/oic/oic.properties");
      if(propertiesInput == null) {
        PluginContext.getLogger().info( "CallRest","Missing oic.properties file '" +System.getProperty("user.dir")+"/oic/oic.properties'");
        throw(new Exception("Missing oic.properties file '" +System.getProperty("user.dir")+"/oic/oic.properties'"));
      }
      prop.load(propertiesInput);

    } catch (IOException ex) {
      PluginContext.getLogger().info( "CallRest","Exception in trying to read properties file: '" +System.getProperty("user.dir")+"/oic/oic.properties'");
      ex.printStackTrace();
      throw(ex);
    } finally {
      if(propertiesInput != null) {
        try {
          propertiesInput.close();
        } catch (IOException e) {
          e.printStackTrace();
          throw(e);
        }
      }
    }

    //
    // Get Auth details
    //
    String authorizationHeaderValue=prop.getProperty("authorizationHeaderValue");
    if(authorizationHeaderValue == null || authorizationHeaderValue == "") {
      PluginOutput pOutput = new PluginOutput();
      throw(new Exception("Missing authorizationHeaderValue in oic.properties file '" +System.getProperty("user.dir")+"/oic/oic.properties'"));
    }

    //
    // Get REST url
    //
    String restUrl = prop.getProperty("targetPostUrl");
    if(restUrl == null || restUrl == "") {
      throw(new Exception("Missing targetPostUrl in oic.properties file '" +System.getProperty("user.dir")+"/oic/oic.properties'"));
    }
System.out.println("Calling: "+restUrl);

    MFTMessage mftMessage = context.getMessage();
    PluginContext.getLogger().info( "CallRest","Process: " +calloutParams);
    PluginContext.getLogger().info( "CallRest","Sending To: " +restUrl);
    String action = calloutParams.get("Action");
    String description = calloutParams.get("Description");
    try {
      // Setup User/Pass
      //
      Client client = ClientBuilder.newClient();
      WebTarget target = client.target(restUrl);
      WebTarget resourceWebTarget;
      resourceWebTarget = target.path("");
      Invocation.Builder invocationBuilder;
      invocationBuilder = resourceWebTarget.request(MediaType.APPLICATION_JSON_TYPE).header("Authorization", authorizationHeaderValue).header("MFT-Action",action).header("MFT-Description",description);

      //Build message
      Response response = invocationBuilder.post(Entity.entity(mftMessage, MediaType.APPLICATION_JSON));
    } catch (Exception e) {
      throw(e);
    }
  }
}
