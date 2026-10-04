package com.fasterxml.jackson.databind.jsontype.impl;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Assert;
import org.junit.Test;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.FileHandler;

public class SubTypeValidatorTest {

    @Test
    public void testSingletonInstance() {
        SubTypeValidator v1 = SubTypeValidator.instance();
        SubTypeValidator v2 = SubTypeValidator.instance();
        Assert.assertNotNull(v1);
        Assert.assertSame(v1, v2);
    }

    @Test
    public void testProtectedConstructor() {
        SubTypeValidator validator = new SubTypeValidator();
        Assert.assertNotNull(validator);
        Assert.assertNotNull(validator._cfgIllegalClassNames);
        Assert.assertTrue(validator._cfgIllegalClassNames.contains("java.util.logging.FileHandler"));
    }

    @Test
    public void testValidTypes() throws Exception {
        SubTypeValidator validator = SubTypeValidator.instance();
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        JavaType stringType = TypeFactory.defaultInstance().constructType(String.class);
        validator.validateSubType(ctxt, stringType);

        JavaType listType = TypeFactory.defaultInstance().constructType(ArrayList.class);
        validator.validateSubType(ctxt, listType);

        JavaType mapType = TypeFactory.defaultInstance().constructType(HashMap.class);
        validator.validateSubType(ctxt, mapType);

        JavaType objectType = TypeFactory.defaultInstance().constructType(Object.class);
        validator.validateSubType(ctxt, objectType);
    }

    @Test
    public void testIllegalJdkTypesFileHandler() {
        SubTypeValidator validator = SubTypeValidator.instance();
        JavaType type = TypeFactory.defaultInstance().constructType(FileHandler.class);
        try {
            validator.validateSubType(null, type);
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Illegal type (java.util.logging.FileHandler) to deserialize"));
        }
    }

    @Test
    public void testIllegalJdkTypesUnicastRemoteObject() {
        SubTypeValidator validator = SubTypeValidator.instance();
        JavaType type = TypeFactory.defaultInstance().constructType(UnicastRemoteObject.class);
        try {
            validator.validateSubType(null, type);
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Illegal type (java.rmi.server.UnicastRemoteObject) to deserialize"));
        }
    }

    @Test
    public void testCustomIllegalClassNamesSet() throws Exception {
        SubTypeValidator validator = new SubTypeValidator();
        Set<String> custom = new HashSet<String>();
        custom.add(String.class.getName());
        validator._cfgIllegalClassNames = custom;

        JavaType stringType = TypeFactory.defaultInstance().constructType(String.class);
        try {
            validator.validateSubType(null, stringType);
            Assert.fail("Expected JsonMappingException for custom blocked String type");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Illegal type (java.lang.String) to deserialize"));
        }

        JavaType intType = TypeFactory.defaultInstance().constructType(Integer.class);
        validator.validateSubType(null, intType);
    }

    @Test
    public void testSpringFrameworkValidation() throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            return;
        }

        Map<String, String> sources = new HashMap<String, String>();
        sources.put("org.springframework.aop.AbstractPointcutAdvisor",
                "package org.springframework.aop; public class AbstractPointcutAdvisor {}");
        sources.put("org.springframework.aop.MyAdvisor",
                "package org.springframework.aop; public class MyAdvisor extends AbstractPointcutAdvisor {}");
        sources.put("org.springframework.context.AbstractApplicationContext",
                "package org.springframework.context; public class AbstractApplicationContext {}");
        sources.put("org.springframework.context.MyAppContext",
                "package org.springframework.context; public class MyAppContext extends AbstractApplicationContext {}");
        sources.put("org.springframework.safe.SafeSpringBean",
                "package org.springframework.safe; public class SafeSpringBean {}");

        ClassLoader cl = compileInMemory(compiler, sources);

        SubTypeValidator validator = SubTypeValidator.instance();

        Class<?> safeClass = cl.loadClass("org.springframework.safe.SafeSpringBean");
        validator.validateSubType(null, TypeFactory.defaultInstance().constructType(safeClass));

        Class<?> advisorClass = cl.loadClass("org.springframework.aop.MyAdvisor");
        try {
            validator.validateSubType(null, TypeFactory.defaultInstance().constructType(advisorClass));
            Assert.fail("Expected JsonMappingException for MyAdvisor");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Illegal type (org.springframework.aop.MyAdvisor) to deserialize"));
        }

        Class<?> appContextClass = cl.loadClass("org.springframework.context.MyAppContext");
        try {
            validator.validateSubType(null, TypeFactory.defaultInstance().constructType(appContextClass));
            Assert.fail("Expected JsonMappingException for MyAppContext");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Illegal type (org.springframework.context.MyAppContext) to deserialize"));
        }

        Class<?> baseAdvisorClass = cl.loadClass("org.springframework.aop.AbstractPointcutAdvisor");
        try {
            validator.validateSubType(null, TypeFactory.defaultInstance().constructType(baseAdvisorClass));
            Assert.fail("Expected JsonMappingException for AbstractPointcutAdvisor");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Illegal type (org.springframework.aop.AbstractPointcutAdvisor) to deserialize"));
        }

        Class<?> baseAppContextClass = cl.loadClass("org.springframework.context.AbstractApplicationContext");
        try {
            validator.validateSubType(null, TypeFactory.defaultInstance().constructType(baseAppContextClass));
            Assert.fail("Expected JsonMappingException for AbstractApplicationContext");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Illegal type (org.springframework.context.AbstractApplicationContext) to deserialize"));
        }
    }

    private ClassLoader compileInMemory(JavaCompiler compiler, Map<String, String> sources) {
        final Map<String, ByteArrayOutputStream> byteCodes = new HashMap<String, ByteArrayOutputStream>();
        JavaFileManager standardFileManager = compiler.getStandardFileManager(null, null, null);
        JavaFileManager customFileManager = new ForwardingJavaFileManager<JavaFileManager>(standardFileManager) {
            @Override
            public JavaFileObject getJavaFileForOutput(Location location, final String className,
                                                       JavaFileObject.Kind kind, FileObject sibling) {
                return new SimpleJavaFileObject(URI.create("string:///" + className.replace('.', '/') + kind.extension), kind) {
                    @Override
                    public OutputStream openOutputStream() {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byteCodes.put(className, baos);
                        return baos;
                    }
                };
            }
        };

        List<JavaFileObject> compilationUnits = new ArrayList<JavaFileObject>();
        for (Map.Entry<String, String> entry : sources.entrySet()) {
            final String name = entry.getKey();
            final String code = entry.getValue();
            compilationUnits.add(new SimpleJavaFileObject(URI.create("string:///" + name.replace('.', '/') + JavaFileObject.Kind.SOURCE.extension), JavaFileObject.Kind.SOURCE) {
                @Override
                public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                    return code;
                }
            });
        }

        JavaCompiler.CompilationTask task = compiler.getTask(null, customFileManager, null, null, null, compilationUnits);
        Assert.assertTrue(task.call());

        return new ClassLoader(SubTypeValidatorTest.class.getClassLoader()) {
            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                ByteArrayOutputStream baos = byteCodes.get(name);
                if (baos != null) {
                    byte[] bytes = baos.toByteArray();
                    return defineClass(name, bytes, 0, bytes.length);
                }
                return super.findClass(name);
            }
        };
    }
}
