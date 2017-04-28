package com.dtstack.rdos.engine.execution.base;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

/**
 * Reason:
 * Date: 2017/2/20
 * Company: www.dtstack.com
 *
 * @ahthor xuchao
 */

public class ClientFactory {

    private static final Logger logger = LoggerFactory.getLogger(ClientFactory.class);
        
    private static Map<String,IClient> pluginIClient = Maps.newConcurrentMap();
    
    private static Map<String,ClassLoader> pluginClassLoader = Maps.newConcurrentMap();


    public static IClient getClient(String type) throws Exception{
    	type = type.toLowerCase();
    	IClient iClient = pluginIClient.get(type);
    	while(iClient == null){
    		initPluginClass(type,pluginClassLoader.get(type));
    		Thread.sleep(1000);
    		iClient = pluginIClient.get(type);
    	}
        return iClient;
    }
    
    public static void initPluginClass(final String pluginType,ClassLoader classLoader){
    	pluginClassLoader.put(pluginType, classLoader);
        new Thread(new Runnable(){
			@Override
			public void run() {
				try{
					// TODO Auto-generated method stub
					Thread.currentThread().setContextClassLoader(classLoader);
			        switch (pluginType){
			            case "flink":
			            	pluginIClient.put(pluginType, (IClient) classLoader.loadClass("com.dtstack.rdos.engine.execution.flink120.FlinkClient").newInstance());
			            	break;
			            case "spark":
			            	pluginIClient.put(pluginType, (IClient)classLoader.loadClass("com.dtstack.rdos.engine.execution.spark210.SparkClient").newInstance());
			                break;
			                
			                default:
			                    logger.error("not support for engine type " + pluginType);
			                    break;
			        }
				}catch(Exception e){
					System.exit(-1);
					logger.error("init engine type:{} error:",pluginType,e);
				}
			}
        }).start();
    }
}
