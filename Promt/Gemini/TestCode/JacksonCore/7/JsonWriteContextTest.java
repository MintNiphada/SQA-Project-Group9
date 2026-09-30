package com.fasterxml.jackson.core.json;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Assert;
import org.junit.Test;

public class JsonWriteContextTest {

    @Test
    @SuppressWarnings("deprecation")
    public void testDeprecatedCreateRootContext() {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        Assert.assertNotNull(root);
        Assert.assertTrue(root.inRoot());
        Assert.assertNull(root.getParent());
        Assert.assertNull(root.getDupDetector());
        Assert.assertEquals("/", root.toString());
    }

    @Test
    public void testRootContextBasic() {
        JsonWriteContext root = JsonWriteContext.createRootContext(null);
        Assert.assertNotNull(root);
        Assert.assertTrue(root.inRoot());
        Assert.assertFalse(root.inArray());
        Assert.assertFalse(root.inObject());
        Assert.assertNull(root.getParent());
        Assert.assertNull(root.getCurrentName());
        Assert.assertNull(root.getCurrentValue());
        Assert.assertNull(root.getDupDetector());
        Assert.assertEquals(0, root.getCurrentIndex());
        Assert.assertEquals(0, root.getEntryCount());
        Assert.assertEquals("/", root.toString());

        // First writeValue in root context
        int status1 = root.writeValue();
        Assert.assertEquals(JsonWriteContext.STATUS_OK_AS_IS, status1);
        Assert.assertEquals(0, root.getCurrentIndex());
        Assert.assertEquals(1, root.getEntryCount());

        // Second writeValue in root context
        int status2 = root.writeValue();
        Assert.assertEquals(JsonWriteContext.STATUS_OK_AFTER_SPACE, status2);
        Assert.assertEquals(1, root.getCurrentIndex());
        Assert.assertEquals(2, root.getEntryCount());

        // Third writeValue in root context
        int status3 = root.writeValue();
        Assert.assertEquals(JsonWriteContext.STATUS_OK_AFTER_SPACE, status3);
        Assert.assertEquals(2, root.getCurrentIndex());
        Assert.assertEquals(3, root.getEntryCount());
    }

    @Test
    public void testCurrentValue() {
        JsonWriteContext root = JsonWriteContext.createRootContext(null);
        Assert.assertNull(root.getCurrentValue());

        Object val = new Object();
        root.setCurrentValue(val);
        Assert.assertSame(val, root.getCurrentValue());

        root.setCurrentValue(null);
        Assert.assertNull(root.getCurrentValue());
    }

    @Test
    public void testArrayContext() {
        JsonWriteContext root = JsonWriteContext.createRootContext(null);
        JsonWriteContext array = root.createChildArrayContext();

        Assert.assertNotNull(array);
        Assert.assertSame(root, array.getParent());
        Assert.assertTrue(array.inArray());
        Assert.assertFalse(array.inRoot());
        Assert.assertFalse(array.inObject());
        Assert.assertEquals(0, array.getCurrentIndex());
        Assert.assertEquals("[0]", array.toString());

        // First item in array
        int status1 = array.writeValue();
        Assert.assertEquals(JsonWriteContext.STATUS_OK_AS_IS, status1);
        Assert.assertEquals(0, array.getCurrentIndex());
        Assert.assertEquals(1, array.getEntryCount());
        Assert.assertEquals("[0]", array.toString());

        // Second item in array
        int status2 = array.writeValue();
        Assert.assertEquals(JsonWriteContext.STATUS_OK_AFTER_COMMA, status2);
        Assert.assertEquals(1, array.getCurrentIndex());
        Assert.assertEquals(2, array.getEntryCount());
        Assert.assertEquals("[1]", array.toString());

        // Third item in array
        int status3 = array.writeValue();
        Assert.assertEquals(JsonWriteContext.STATUS_OK_AFTER_COMMA, status3);
        Assert.assertEquals(2, array.getCurrentIndex());
        Assert.assertEquals(3, array.getEntryCount());
        Assert.assertEquals("[2]", array.toString());
    }

    @Test
    public void testObjectContextFlow() throws JsonProcessingException {
        JsonWriteContext root = JsonWriteContext.createRootContext(null);
        JsonWriteContext obj = root.createChildObjectContext();

        Assert.assertNotNull(obj);
        Assert.assertSame(root, obj.getParent());
        Assert.assertTrue(obj.inObject());
        Assert.assertFalse(obj.inRoot());
        Assert.assertFalse(obj.inArray());
        Assert.assertEquals("{?}", obj.toString());

        // Write first field name
        int nameStatus1 = obj.writeFieldName("prop1");
        Assert.assertEquals(JsonWriteContext.STATUS_OK_AS_IS, nameStatus1);
        Assert.assertEquals("prop1", obj.getCurrentName());
        Assert.assertEquals("{\"prop1\"}", obj.toString());

        // Expect value next: trying to write another field name immediately returns STATUS_EXPECT_VALUE
        int expectValueStatus = obj.writeFieldName("prop2");
        Assert.assertEquals(JsonWriteContext.STATUS_EXPECT_VALUE, expectValueStatus);

        // Write value for first field
        int valStatus1 = obj.writeValue();
        Assert.assertEquals(JsonWriteContext.STATUS_OK_AFTER_COLON, valStatus1);
        Assert.assertEquals(0, obj.getCurrentIndex());
        Assert.assertEquals(1, obj.getEntryCount());

        // Write second field name
        int nameStatus2 = obj.writeFieldName("prop2");
        Assert.assertEquals(JsonWriteContext.STATUS_OK_AFTER_COMMA, nameStatus2);
        Assert.assertEquals("prop2", obj.getCurrentName());
        Assert.assertEquals("{\"prop2\"}", obj.toString());

        // Write value for second field
        int valStatus2 = obj.writeValue();
        Assert.assertEquals(JsonWriteContext.STATUS_OK_AFTER_COLON, valStatus2);
        Assert.assertEquals(1, obj.getCurrentIndex());
        Assert.assertEquals(2, obj.getEntryCount());
    }

    @Test
    public void testChildContextReuse() throws Exception {
        JsonWriteContext root = JsonWriteContext.createRootContext(null);
        root.setCurrentValue("rootVal");

        // First child creation (allocates new instance)
        JsonWriteContext child1 = root.createChildArrayContext();
        child1.setCurrentValue("arrayVal");
        child1.writeValue();
        Assert.assertEquals(1, child1.getEntryCount());

        // Reusing child for object
        JsonWriteContext child2 = root.createChildObjectContext();
        Assert.assertSame(child1, child2);
        Assert.assertTrue(child2.inObject());
        Assert.assertEquals(0, child2.getEntryCount());
        Assert.assertNull(child2.getCurrentName());
        Assert.assertNull(child2.getCurrentValue());

        child2.writeFieldName("foo");
        child2.writeValue();

        // Reusing child for array again
        JsonWriteContext child3 = root.createChildArrayContext();
        Assert.assertSame(child1, child3);
        Assert.assertTrue(child3.inArray());
        Assert.assertEquals(0, child3.getEntryCount());
        Assert.assertNull(child3.getCurrentName());
        Assert.assertNull(child3.getCurrentValue());
    }

    @Test
    public void testDuplicateDetectionNoDuplicates() throws Exception {
        DupDetector dups = DupDetector.rootDetector((com.fasterxml.jackson.core.JsonGenerator) null);
        JsonWriteContext root = JsonWriteContext.createRootContext(dups);
        Assert.assertSame(dups, root.getDupDetector());

        JsonWriteContext obj = root.createChildObjectContext();
        Assert.assertNotNull(obj.getDupDetector());
        Assert.assertNotSame(dups, obj.getDupDetector());

        Assert.assertEquals(JsonWriteContext.STATUS_OK_AS_IS, obj.writeFieldName("field1"));
        Assert.assertEquals(JsonWriteContext.STATUS_OK_AFTER_COLON, obj.writeValue());

        Assert.assertEquals(JsonWriteContext.STATUS_OK_AFTER_COMMA, obj.writeFieldName("field2"));
        Assert.assertEquals(JsonWriteContext.STATUS_OK_AFTER_COLON, obj.writeValue());
    }

    @Test
    public void testDuplicateDetectionThrowsException() throws Exception {
        DupDetector dups = DupDetector.rootDetector((com.fasterxml.jackson.core.JsonGenerator) null);
        JsonWriteContext root = JsonWriteContext.createRootContext(dups);
        JsonWriteContext obj = root.createChildObjectContext();

        Assert.assertEquals(JsonWriteContext.STATUS_OK_AS_IS, obj.writeFieldName("duplicateField"));
        Assert.assertEquals(JsonWriteContext.STATUS_OK_AFTER_COLON, obj.writeValue());

        try {
            obj.writeFieldName("duplicateField");
            Assert.fail("Expected JsonGenerationException for duplicate field");
        } catch (JsonGenerationException e) {
            Assert.assertTrue(e.getMessage().contains("Duplicate field 'duplicateField'"));
        }
    }

    @Test
    public void testChildContextReuseWithDupDetectorReset() throws Exception {
        DupDetector dups = DupDetector.rootDetector((com.fasterxml.jackson.core.JsonGenerator) null);
        JsonWriteContext root = JsonWriteContext.createRootContext(dups);

        JsonWriteContext obj1 = root.createChildObjectContext();
        obj1.writeFieldName("test");
        obj1.writeValue();

        // Reuse child context -> reset() should reset dup detector
        JsonWriteContext obj2 = root.createChildObjectContext();
        Assert.assertSame(obj1, obj2);

        // Writing "test" again in the new object should succeed because dups are reset
        int status = obj2.writeFieldName("test");
        Assert.assertEquals(JsonWriteContext.STATUS_OK_AS_IS, status);
    }

    @Test
    public void testWithDupDetector() {
        JsonWriteContext root = JsonWriteContext.createRootContext(null);
        Assert.assertNull(root.getDupDetector());

        DupDetector dups = DupDetector.rootDetector((com.fasterxml.jackson.core.JsonGenerator) null);
        JsonWriteContext returned = root.withDupDetector(dups);
        Assert.assertSame(root, returned);
        Assert.assertSame(dups, root.getDupDetector());
    }

    @Test
    public void testProtectedConstructorDirectly() {
        JsonWriteContext custom = new JsonWriteContext(JsonWriteContext.TYPE_ROOT, null, null);
        Assert.assertTrue(custom.inRoot());
        Assert.assertNull(custom.getParent());
        Assert.assertNull(custom.getDupDetector());
        Assert.assertEquals(-1, custom._index);
    }
}
