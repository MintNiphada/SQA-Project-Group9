package org.apache.commons.cli2.builder;

import org.apache.commons.cli2.Argument;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.validation.ClassValidator;
import org.apache.commons.cli2.validation.DateValidator;
import org.apache.commons.cli2.validation.FileValidator;
import org.apache.commons.cli2.validation.NumberValidator;
import org.apache.commons.cli2.validation.UrlValidator;
import org.apache.commons.cli2.validation.Validator;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

public class PatternBuilderTest {

    @Test
    public void testDefaultConstructor() {
        PatternBuilder builder = new PatternBuilder();
        assertNotNull(builder);
    }

    @Test
    public void testParameterizedConstructor() {
        GroupBuilder gbuilder = new GroupBuilder();
        DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        ArgumentBuilder abuilder = new ArgumentBuilder();
        PatternBuilder builder = new PatternBuilder(gbuilder, obuilder, abuilder);
        assertNotNull(builder);
    }

    @Test
    public void testCreateWithNoPattern() {
        PatternBuilder builder = new PatternBuilder();
        Option option = builder.create();
        // With no options added, gbuilder.create() probably returns an empty Group
        assertTrue(option instanceof Group);
        Group group = (Group) option;
        assertTrue(group.getOptions().isEmpty());
    }

    @Test
    public void testCreateWithSingleOptionNoArgumentNotRequired() {
        PatternBuilder builder = new PatternBuilder();
        builder.withPattern("f");
        Option option = builder.create();
        assertNotNull(option);
        assertFalse(option.isRequired());
        assertEquals("f", option.getPreferredName());
        assertNull(option.getArgument());
    }

    @Test
    public void testCreateWithSingleOptionRequiredNoArgument() {
        PatternBuilder builder = new PatternBuilder();
        builder.withPattern("!f");
        Option option = builder.create();
        assertTrue(option.isRequired());
        assertEquals("f", option.getPreferredName());
        assertNull(option.getArgument());
    }

    @Test
    public void testCreateWithSingleOptionNumberArgument() {
        PatternBuilder builder = new PatternBuilder();
        builder.withPattern("f%");
        Option option = builder.create();
        assertNotNull(option.getArgument());
        List<Validator> validators = option.getArgument().getValidators();
        assertEquals(1, validators.size());
        Validator v = validators.get(0);
        assertTrue(v instanceof NumberValidator);
    }

    @Test
    public void testCreateWithSingleOptionRequiredNumberArgument() {
        PatternBuilder builder = new PatternBuilder();
        builder.withPattern("!f%");
        Option option = builder.create();
        assertTrue(option.isRequired());
        assertNotNull(option.getArgument());
        List<Validator> validators = option.getArgument().getValidators();
        assertEquals(1, validators.size());
        assertTrue(validators.get(0) instanceof NumberValidator);
    }

    @Test
    public void testCreateWithMultipleOptions() {
        PatternBuilder builder = new PatternBuilder();
        builder.withPattern("ab");
        Option result = builder.create();
        assertTrue(result instanceof Group);
        Group group = (Group) result;
        Collection<Option> options = group.getOptions();
        assertEquals(2, options.size());
        for (Option opt : options) {
            assertFalse(opt.isRequired());
            assertNull(opt.getArgument());
            assertTrue("a".equals(opt.getPreferredName()) || "b".equals(opt.getPreferredName()));
        }
    }

    @Test
    public void testCreateWithMultipleOptionsAndValidators() {
        PatternBuilder builder = new PatternBuilder();
        builder.withPattern("a%b#");
        Option result = builder.create();
        assertTrue(result instanceof Group);
        Group group = (Group) result;
        Collection<Option> options = group.getOptions();
        assertEquals(2, options.size());
        for (Option opt : options) {
            if ("a".equals(opt.getPreferredName())) {
                assertNotNull(opt.getArgument());
                List<Validator> validators = opt.getArgument().getValidators();
                assertEquals(1, validators.size());
                assertTrue(validators.get(0) instanceof NumberValidator);
            } else if ("b".equals(opt.getPreferredName())) {
                assertNotNull(opt.getArgument());
                List<Validator> validators = opt.getArgument().getValidators();
                assertEquals(1, validators.size());
                assertTrue(validators.get(0) instanceof DateValidator);
            } else {
                fail("Unexpected option: " + opt.getPreferredName());
            }
        }
    }

    @Test
    public void testPatternNullThrowsNullPointerException() {
        PatternBuilder builder = new PatternBuilder();
        try {
            builder.withPattern(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testPatternEmptyStringCreatesNoOptions() {
        PatternBuilder builder = new PatternBuilder();
        builder.withPattern("");
        Option option = builder.create();
        assertTrue(option instanceof Group);
        Group group = (Group) option;
        assertTrue(group.getOptions().isEmpty());
    }

    @Test
    public void testPatternWithOnlyExclamationCreatesNoOptions() {
        PatternBuilder builder = new PatternBuilder();
        builder.withPattern("!");
        Option option = builder.create();
        assertTrue(option instanceof Group);
        Group group = (Group) option;
        assertTrue(group.getOptions().isEmpty());
    }

    @Test
    public void testCreateResetsBuilder() {
        PatternBuilder builder = new PatternBuilder();
        builder.withPattern("x");
        // first create
        Option first = builder.create();
        assertNotNull(first);
        // second create should now produce empty group because builder was reset
        Option second = builder.create();
        assertTrue(second instanceof Group);
        Group group = (Group) second;
        assertTrue(group.getOptions().isEmpty());
    }

    @Test
    public void testResetMethod() {
        PatternBuilder builder = new PatternBuilder();
        builder.withPattern("p");
        builder.reset();
        Option option = builder.create();
        assertTrue(option instanceof Group);
        Group group = (Group) option;
        assertTrue(group.getOptions().isEmpty());
    }

    // Validator tests using reflection to cover private method and all branches

    @Test
    public void testValidatorAtSign() throws Exception {
        Validator v = invokeValidator('@');
        assertNotNull(v);
        assertTrue(v instanceof ClassValidator);
        ClassValidator cv = (ClassValidator) v;
        assertTrue(cv.isInstance());
    }

    @Test
    public void testValidatorPlusSign() throws Exception {
        Validator v = invokeValidator('+');
        assertNotNull(v);
        assertTrue(v instanceof ClassValidator);
        ClassValidator cv = (ClassValidator) v;
        assertFalse(cv.isInstance());
    }

    @Test
    public void testValidatorColon() throws Exception {
        Validator v = invokeValidator(':');
        assertNull(v);
    }

    @Test
    public void testValidatorPercent() throws Exception {
        Validator v = invokeValidator('%');
        assertNotNull(v);
        assertTrue(v instanceof NumberValidator);
    }

    @Test
    public void testValidatorHash() throws Exception {
        Validator v = invokeValidator('#');
        assertNotNull(v);
        assertTrue(v instanceof DateValidator);
    }

    @Test
    public void testValidatorLessThan() throws Exception {
        Validator v = invokeValidator('<');
        assertNotNull(v);
        assertTrue(v instanceof FileValidator);
        FileValidator fv = (FileValidator) v;
        assertTrue(fv.isExisting());
        assertTrue(fv.isFile());
    }

    @Test
    public void testValidatorGreaterThan() throws Exception {
        Validator v = invokeValidator('>');
        assertNotNull(v);
        assertTrue(v instanceof FileValidator);
        FileValidator fv = (FileValidator) v;
        assertFalse(fv.isExisting());
        assertFalse(fv.isFile());
    }

    @Test
    public void testValidatorAsterisk() throws Exception {
        Validator v = invokeValidator('*');
        assertNotNull(v);
        assertTrue(v instanceof FileValidator);
        FileValidator fv = (FileValidator) v;
        assertFalse(fv.isExisting());
        assertFalse(fv.isFile());
    }

    @Test
    public void testValidatorSlash() throws Exception {
        Validator v = invokeValidator('/');
        assertNotNull(v);
        assertTrue(v instanceof UrlValidator);
    }

    @Test
    public void testValidatorDefault() throws Exception {
        Validator v = invokeValidator('?'); // arbitrary unsupported char
        assertNull(v);
    }

    private Validator invokeValidator(char c) throws Exception {
        Method method = PatternBuilder.class.getDeclaredMethod("validator", char.class);
        method.setAccessible(true);
        return (Validator) method.invoke(null, c);
    }
}
