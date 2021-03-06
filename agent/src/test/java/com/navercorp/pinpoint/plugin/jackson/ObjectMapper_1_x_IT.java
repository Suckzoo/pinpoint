/**
 * Copyright 2014 NAVER Corp.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.jackson;

import static com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier.ExpectedAnnotation.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.DeserializerProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializerProvider;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier.BlockType;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier.ExpectedAnnotation;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;

/**
 * @see JacksonPlugin#intercept_ObjectMapper(com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginContext)
 * @author Sungkook Kim
 */
@RunWith(PinpointPluginTestSuite.class)
@Dependency({"org.codehaus.jackson:jackson-mapper-asl:[1.0.0,)"})
public class ObjectMapper_1_x_IT {
    private final ObjectMapper mapper = new ObjectMapper();
    

    @Test
    public void testConstructor() throws Exception {
        ObjectMapper mapper1 = new ObjectMapper();
        ObjectMapper mapper2 = new ObjectMapper(new JsonFactory());

        
        
        
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache(System.out);
        verifier.printBlocks(System.out);
                
        Constructor<?> omConstructor1 = ObjectMapper.class.getConstructor();
        Constructor<?> omConstructor2 = ObjectMapper.class.getConstructor(JsonFactory.class);
        Constructor<?> omConstructor3 = ObjectMapper.class.getConstructor(JsonFactory.class, SerializerProvider.class, DeserializerProvider.class);
        
        
        Class<?> serializationConfig = null;
        Class<?> deserializationConfig = null;
        
        try {
            serializationConfig = Class.forName("org.codehaus.jackson.map.SerializationConfig"); 
            deserializationConfig = Class.forName("org.codehaus.jackson.map.DeserializationConfig");
        } catch (ClassNotFoundException e) {
            
        }
        
        Constructor<?> omConstructor4 = null;
        
        if (serializationConfig != null && deserializationConfig != null) {
            omConstructor4 = ObjectMapper.class.getConstructor(JsonFactory.class, SerializerProvider.class, DeserializerProvider.class, serializationConfig, deserializationConfig);
        }
        
        
        
        
        
        
        if (omConstructor4 != null) {
            verifier.verifyApi("JACKSON", omConstructor4);
        }
        
        verifier.verifyApi("JACKSON", omConstructor3);
        verifier.verifyApi("JACKSON", omConstructor1);
        
        if (omConstructor4 != null) {
            verifier.verifyApi("JACKSON", omConstructor4);
        }
        
        verifier.verifyApi("JACKSON", omConstructor3);
        verifier.verifyApi("JACKSON", omConstructor2);

        verifier.verifyTraceBlockCount(0);
    }

    @Test()
    public void testWriteValue() throws Exception {
        __POJO pojo = new __POJO();
        pojo.setName("Jackson");

        Method writeValueAsString = null;
        try {
            writeValueAsString = ObjectMapper.class.getMethod("writeValueAsString", Object.class);
        } catch (NoSuchMethodException e) {
            
        }
        
        Method writeValueAsBytes = null;
        try {
            writeValueAsBytes = ObjectMapper.class.getMethod("writeValueAsBytes", Object.class);
        } catch (NoSuchMethodException e) {
            
        }
        
        Method writeValue = ObjectMapper.class.getMethod("writeValue", Writer.class, Object.class);

        
        
        mapper.writeValue(new OutputStreamWriter(new ByteArrayOutputStream()), pojo);
        
        if (writeValueAsString != null) {
            writeValueAsString.invoke(mapper, pojo);
        }
        
        if (writeValueAsBytes != null) {
            writeValueAsBytes.invoke(mapper, pojo);
        }
        
        

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache(System.out);
        verifier.printBlocks(System.out);

        ExpectedAnnotation length = annotation("jackson.json.length", 18);

        verifier.verifyApi("JACKSON", writeValue);
        
        if (writeValueAsString != null) {
            verifier.verifyTraceBlock(BlockType.EVENT, "JACKSON", writeValueAsString, null, null, null, null, length);
        }
        
        if (writeValueAsBytes != null) {
            verifier.verifyTraceBlock(BlockType.EVENT, "JACKSON", writeValueAsBytes, null, null, null, null, length);
        }

        verifier.verifyTraceBlockCount(0);
    }

    @Test
    public void testReadValue() throws Exception {
        String json_str = "{\"name\" : \"Jackson\"}";
        byte[] json_b = json_str.getBytes("UTF-8");
        
        Method readValueString = ObjectMapper.class.getMethod("readValue", String.class, Class.class);
        Method readValueBytes = null;
        try {
            readValueBytes = ObjectMapper.class.getMethod("readValue", byte[].class, Class.class);
        } catch (NoSuchMethodException e) {
            
        }
                
        
        
        
        __POJO pojo = mapper.readValue(json_str, __POJO.class);
        
        if (readValueBytes != null) {
            readValueBytes.invoke(mapper, json_b, __POJO.class);
        }

        
        
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache(System.out);
        verifier.printBlocks(System.out);

        ExpectedAnnotation length = annotation("jackson.json.length", 20);

        verifier.verifyTraceBlock(BlockType.EVENT, "JACKSON", readValueString, null, null, null, null, length);
        
        if (readValueBytes != null) {
            verifier.verifyTraceBlock(BlockType.EVENT, "JACKSON", readValueBytes, null, null, null, null, length);
        }

        verifier.verifyTraceBlockCount(0);
    }

    public static class __POJO {
        public String name;
        
        public String getName() { return name; }
        public void setName(String str) { name = str; }
    }
}

