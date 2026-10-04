package com.fasterxml.jackson.databind.jsontype.impl;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.Serializable;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.FileHandler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class SubTypeValidatorTest {

    private SubTypeValidator validator;
    private DeserializationContext ctxt;
    private TypeFactory typeFactory;

    @Before
    public void setUp() {
        validator = SubTypeValidator.instance();
        ObjectMapper mapper = new ObjectMapper();
        ctxt = mapper.getDeserializationContext();
        typeFactory = mapper.getTypeFactory();
    }

    @Test
    public void testInstanceNotNull() {
        SubTypeValidator inst1 = SubTypeValidator.instance();
        SubTypeValidator inst2 = SubTypeValidator.instance();
        Assert.assertNotNull(inst1);
        Assert.assertSame(inst1, inst2);
    }

    @Test
    public void testDefaultConstructor() {
        SubTypeValidator custom = new SubTypeValidator();
        Assert.assertNotNull(custom);
        Assert.assertNotNull(custom._cfgIllegalClassNames);
        Assert.assertEquals(SubTypeValidator.DEFAULT_NO_DESER_CLASS_NAMES, custom._cfgIllegalClassNames);
    }

    @Test
    public void testValidateSafeTypes() throws Exception {
        JavaType stringType = typeFactory.constructType(String.class);
        validator.validateSubType(ctxt, stringType);

        JavaType objectType = typeFactory.constructType(Object.class);
        validator.validateSubType(ctxt, objectType);

        JavaType listType = typeFactory.constructType(ArrayList.class);
        validator.validateSubType(ctxt, listType);

        JavaType intType = typeFactory.constructType(Integer.TYPE);
        validator.validateSubType(ctxt, intType);
    }

    @Test
    public void testValidateInterfacesAllowed() throws Exception {
        JavaType ifaceType = typeFactory.constructType(List.class);
        validator.validateSubType(ctxt, ifaceType);

        JavaType serializableType = typeFactory.constructType(Serializable.class);
        validator.validateSubType(ctxt, serializableType);

        JavaType runnableType = typeFactory.constructType(Runnable.class);
        validator.validateSubType(ctxt, runnableType);
    }

    @Test
    public void testValidateBlockedFileHandler() {
        JavaType type = typeFactory.constructType(FileHandler.class);
        try {
            validator.validateSubType(ctxt, type);
            Assert.fail("Expected JsonMappingException for FileHandler");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Illegal type"));
            Assert.assertTrue(e.getMessage().contains("prevented for security reasons"));
            Assert.assertTrue(e.getMessage().contains("java.util.logging.FileHandler"));
        }
    }

    @Test
    public void testValidateBlockedUnicastRemoteObject() {
        JavaType type = typeFactory.constructType(UnicastRemoteObject.class);
        try {
            validator.validateSubType(ctxt, type);
            Assert.fail("Expected JsonMappingException for UnicastRemoteObject");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Illegal type"));
            Assert.assertTrue(e.getMessage().contains("prevented for security reasons"));
            Assert.assertTrue(e.getMessage().contains("java.rmi.server.UnicastRemoteObject"));
        }
    }

    @Test
    public void testValidateWithNullContext() {
        JavaType type = typeFactory.constructType(FileHandler.class);
        try {
            validator.validateSubType(null, type);
            Assert.fail("Expected JsonMappingException even with null DeserializationContext");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Illegal type"));
            Assert.assertTrue(e.getMessage().contains("java.util.logging.FileHandler"));
        }
    }

    @Test
    public void testCustomIllegalClassNamesSet() throws Exception {
        SubTypeValidator custom = new SubTypeValidator();
        Set<String> customIllegal = new HashSet<String>();
        customIllegal.add(String.class.getName());
        custom._cfgIllegalClassNames = customIllegal;

        JavaType stringType = typeFactory.constructType(String.class);
        try {
            custom.validateSubType(ctxt, stringType);
            Assert.fail("Expected JsonMappingException for customized illegal class name");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("java.lang.String"));
        }

        JavaType fileHandlerType = typeFactory.constructType(FileHandler.class);
        custom.validateSubType(ctxt, fileHandlerType);
    }

    @Test
    public void testSpringClassesValidationViaDynamicLoading() throws Exception {
        DynamicSpringClassLoader loader = new DynamicSpringClassLoader(getClass().getClassLoader());

        Class<?> springAdvisorClass = loader.createClass("org.springframework.aop.support.AbstractPointcutAdvisor", "java.lang.Object", false);
        Class<?> subAdvisorClass = loader.createClass("org.springframework.aop.support.MyAdvisor", "org.springframework.aop.support.AbstractPointcutAdvisor", false);
        JavaType advisorType = typeFactory.constructType(subAdvisorClass);

        try {
            validator.validateSubType(ctxt, advisorType);
            Assert.fail("Expected JsonMappingException for Spring AbstractPointcutAdvisor subtype");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Illegal type"));
            Assert.assertTrue(e.getMessage().contains("org.springframework.aop.support.MyAdvisor"));
        }

        Class<?> springAppContextClass = loader.createClass("org.springframework.context.support.AbstractApplicationContext", "java.lang.Object", false);
        Class<?> subAppContextClass = loader.createClass("org.springframework.context.support.MyApplicationContext", "org.springframework.context.support.AbstractApplicationContext", false);
        JavaType appContextType = typeFactory.constructType(subAppContextClass);

        try {
            validator.validateSubType(ctxt, appContextType);
            Assert.fail("Expected JsonMappingException for Spring AbstractApplicationContext subtype");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Illegal type"));
            Assert.assertTrue(e.getMessage().contains("org.springframework.context.support.MyApplicationContext"));
        }

        Class<?> springSafeClass = loader.createClass("org.springframework.core.SafeSpringBean", "java.lang.Object", false);
        JavaType safeSpringType = typeFactory.constructType(springSafeClass);
        validator.validateSubType(ctxt, safeSpringType);

        Class<?> springInterface = loader.createClass("org.springframework.context.ApplicationContextInterface", "java.lang.Object", true);
        JavaType springInterfaceType = typeFactory.constructType(springInterface);
        validator.validateSubType(ctxt, springInterfaceType);
    }

    @Test
    public void testDefaultNoDeserClassNamesImmutability() {
        Set<String> set = SubTypeValidator.DEFAULT_NO_DESER_CLASS_NAMES;
        Assert.assertNotNull(set);
        Assert.assertFalse(set.isEmpty());
        try {
            set.add("dummy.Class");
            Assert.fail("Expected UnsupportedOperationException on unmodifiable set");
        } catch (UnsupportedOperationException expected) {
        }
    }

    private static class DynamicSpringClassLoader extends ClassLoader {
        public DynamicSpringClassLoader(ClassLoader parent) {
            super(parent);
        }

        public Class<?> createClass(String className, String superClassName, boolean isInterface) throws Exception {
            byte[] bytes = generateClassBytes(className.replace('.', '/'), superClassName.replace('.', '/'), isInterface);
            return defineClass(className, bytes, 0, bytes.length);
        }

        private byte[] generateClassBytes(String internalName, String superInternalName, boolean isInterface) throws Exception {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(0xCAFEBABE);
            dos.writeShort(0);
            dos.writeShort(49);

            dos.writeShort(5);

            dos.writeByte(7);
            dos.writeShort(2);

            dos.writeByte(1);
            dos.writeUTF(internalName);

            dos.writeByte(7);
            dos.writeShort(4);

            dos.writeByte(1);
            dos.writeUTF(superInternalName);

            int accessFlags = 0x0001 | 0x0020;
            if (isInterface) {
                accessFlags = 0x0200 | 0x0600;
            }
            dos.writeShort(accessFlags);

            dos.writeShort(1);
            dos.writeShort(3);

            dos.writeShort(0);
            dos.writeShort(0);
            dos.writeShort(0);
            dos.writeShort(0);

            dos.flush();
            return baos.toByteArray();
        }
    }
}
