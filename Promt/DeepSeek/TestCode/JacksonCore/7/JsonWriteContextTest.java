package com.fasterxml.jackson.core.json;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonStreamContext;

public class JsonWriteContextTest {

    @Test
    public void testCreateRootContextNoDups() {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        assertNotNull(root);
        assertEquals(JsonStreamContext.TYPE_ROOT, root.getType());
        assertNull(root.getParent());
        assertEquals(-1, root.getCurrentIndex());
        assertNull(root.getCurrentName());
        assertNull(root.getCurrentValue());
        assertNull(root.getDupDetector());
    }

    @Test
    public void testCreateRootContextWithDups() {
        DupDetector dd = DupDetector.rootDetector();
        JsonWriteContext root = JsonWriteContext.createRootContext(dd);
        assertNotNull(root);
        assertEquals(JsonStreamContext.TYPE_ROOT, root.getType());
        assertNull(root.getParent());
        assertEquals(-1, root.getCurrentIndex());
        assertNull(root.getCurrentName());
        assertNull(root.getCurrentValue());
        assertSame(dd, root.getDupDetector());
    }

    @Test
    public void testDeprecatedCreateRootContext() {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        assertNotNull(root);
        assertEquals(JsonStreamContext.TYPE_ROOT, root.getType());
        assertNull(root.getParent());
    }

    @Test
    public void testWriteValueRoot() {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        // First value
        assertEquals(JsonWriteContext.STATUS_OK_AS_IS, root.writeValue());
        assertEquals(0, root.getCurrentIndex());
        // Second value
        assertEquals(JsonWriteContext.STATUS_OK_AFTER_SPACE, root.writeValue());
        assertEquals(1, root.getCurrentIndex());
        // Third value
        assertEquals(JsonWriteContext.STATUS_OK_AFTER_SPACE, root.writeValue());
        assertEquals(2, root.getCurrentIndex());
    }

    @Test
    public void testWriteValueArray() {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        JsonWriteContext arrayCtx = root.createChildArrayContext();
        assertNotNull(arrayCtx);
        assertEquals(JsonStreamContext.TYPE_ARRAY, arrayCtx.getType());

        // First value
        assertEquals(JsonWriteContext.STATUS_OK_AS_IS, arrayCtx.writeValue());
        assertEquals(0, arrayCtx.getCurrentIndex());
        // Second value
        assertEquals(JsonWriteContext.STATUS_OK_AFTER_COMMA, arrayCtx.writeValue());
        assertEquals(1, arrayCtx.getCurrentIndex());
    }

    @Test
    public void testWriteValueObjectWithPriorFieldName() throws JsonProcessingException {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        JsonWriteContext objCtx = root.createChildObjectContext();
        assertNotNull(objCtx);
        assertEquals(JsonStreamContext.TYPE_OBJECT, objCtx.getType());

        // Write field name
        assertEquals(JsonWriteContext.STATUS_OK_AS_IS, objCtx.writeFieldName("field"));
        assertTrue(objCtx._gotName);
        assertEquals("field", objCtx.getCurrentName());

        // Write value
        assertEquals(JsonWriteContext.STATUS_OK_AFTER_COLON, objCtx.writeValue());
        assertFalse(objCtx._gotName);
        assertEquals(0, objCtx.getCurrentIndex());
    }

    @Test
    public void testWriteValueObjectWithoutFieldName() {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        JsonWriteContext objCtx = root.createChildObjectContext();

        // Attempt to write value without field name
        assertEquals(JsonWriteContext.STATUS_OK_AFTER_COLON, objCtx.writeValue());
        // Index should still increment
        assertEquals(0, objCtx.getCurrentIndex());
        // _gotName should remain false (it was false initially)
        assertFalse(objCtx._gotName);
    }

    @Test
    public void testWriteFieldNameBasic() throws JsonProcessingException {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        JsonWriteContext objCtx = root.createChildObjectContext();
        // First field (index -1)
        assertEquals(JsonWriteContext.STATUS_OK_AS_IS, objCtx.writeFieldName("a"));
        assertEquals("a", objCtx.getCurrentName());
        assertTrue(objCtx._gotName);
        // write value
        objCtx.writeValue();
        // Second field (index 0 now)
        assertEquals(JsonWriteContext.STATUS_OK_AFTER_COMMA, objCtx.writeFieldName("b"));
        assertEquals("b", objCtx.getCurrentName());
        assertTrue(objCtx._gotName);
    }

    @Test
    public void testWriteFieldNameWhenGotNameTrue() throws JsonProcessingException {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        JsonWriteContext objCtx = root.createChildObjectContext();
        // Set gotName true
        objCtx.writeFieldName("first");
        // Call again without writeValue
        int status = objCtx.writeFieldName("second");
        assertEquals(JsonWriteContext.STATUS_EXPECT_VALUE, status);
        // State should remain unchanged
        assertEquals("first", objCtx.getCurrentName());
        assertTrue(objCtx._gotName);
    }

    @Test(expected = JsonGenerationException.class)
    public void testWriteFieldNameDuplicateDetection() throws JsonProcessingException {
        DupDetector dd = DupDetector.rootDetector();
        JsonWriteContext root = JsonWriteContext.createRootContext(dd);
        JsonWriteContext objCtx = root.createChildObjectContext();

        objCtx.writeFieldName("dup");
        objCtx.writeValue(); // advance index, not affecting dup detector
        // Should throw because "dup" already seen
        objCtx.writeFieldName("dup");
    }

    @Test
    public void testWriteFieldNameDuplicateDetectionWithGotNameTrue() throws JsonProcessingException {
        DupDetector dd = DupDetector.rootDetector();
        JsonWriteContext root = JsonWriteContext.createRootContext(dd);
        JsonWriteContext objCtx = root.createChildObjectContext();

        objCtx.writeFieldName("dup");
        // Now _gotName is true; calling writeFieldName again should not check duplicate
        int status = objCtx.writeFieldName("dup");
        assertEquals(JsonWriteContext.STATUS_EXPECT_VALUE, status);
        // No exception thrown
    }

    @Test
    public void testWriteFieldNameNonDuplicate() throws JsonProcessingException {
        DupDetector dd = DupDetector.rootDetector();
        JsonWriteContext root = JsonWriteContext.createRootContext(dd);
        JsonWriteContext objCtx = root.createChildObjectContext();

        objCtx.writeFieldName("a");
        objCtx.writeValue();
        // Different name should be fine
        int status = objCtx.writeFieldName("b");
        assertEquals(JsonWriteContext.STATUS_OK_AFTER_COMMA, status);
        assertEquals("b", objCtx.getCurrentName());
    }

    @Test
    public void testGetParent() {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        assertNull(root.getParent());

        JsonWriteContext child = root.createChildArrayContext();
        assertSame(root, child.getParent());
    }

    @Test
    public void testGetCurrentName() {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        assertNull(root.getCurrentName());

        JsonWriteContext objCtx = root.createChildObjectContext();
        try {
            objCtx.writeFieldName("name");
        } catch (JsonProcessingException e) {
            fail("Unexpected exception");
        }
        assertEquals("name", objCtx.getCurrentName());
    }

    @Test
    public void testGetSetCurrentValue() {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        assertNull(root.getCurrentValue());
        Object value = new Object();
        root.setCurrentValue(value);
        assertSame(value, root.getCurrentValue());
    }

    @Test
    public void testWithDupDetector() {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        assertNull(root.getDupDetector());
        DupDetector dd = DupDetector.rootDetector();
        JsonWriteContext returned = root.withDupDetector(dd);
        assertSame(root, returned);
        assertSame(dd, root.getDupDetector());
    }

    @Test
    public void testCreateChildArrayContextReuse() {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        JsonWriteContext child1 = root.createChildArrayContext();
        assertNotNull(child1);
        assertEquals(JsonStreamContext.TYPE_ARRAY, child1.getType());

        // Modify child state
        child1.writeValue(); // index becomes 0
        assertEquals(0, child1.getCurrentIndex());

        // Request another array context (should reuse child1)
        JsonWriteContext child2 = root.createChildArrayContext();
        assertSame(child1, child2);
        // State should be reset
        assertEquals(-1, child2.getCurrentIndex());
        assertEquals(JsonStreamContext.TYPE_ARRAY, child2.getType());
        assertNull(child2.getCurrentName());
        assertFalse(child2._gotName);
        assertNull(child2.getCurrentValue());
    }

    @Test
    public void testCreateChildObjectContextReuse() throws JsonProcessingException {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        JsonWriteContext child1 = root.createChildObjectContext();
        assertNotNull(child1);
        assertEquals(JsonStreamContext.TYPE_OBJECT, child1.getType());

        // Modify state
        child1.writeFieldName("f");
        child1.writeValue();

        // Request another object context
        JsonWriteContext child2 = root.createChildObjectContext();
        assertSame(child1, child2);
        // State reset
        assertEquals(-1, child2.getCurrentIndex());
        assertNull(child2.getCurrentName());
        assertFalse(child2._gotName);
        assertNull(child2.getCurrentValue());
    }

    @Test
    public void testToStringRoot() {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        assertEquals("/", root.toString());
    }

    @Test
    public void testToStringArray() {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        JsonWriteContext arrayCtx = root.createChildArrayContext();
        // Initially index -1
        assertEquals("[-1]", arrayCtx.toString());
        // After writeValue
        arrayCtx.writeValue(); // index becomes 0
        assertEquals("[0]", arrayCtx.toString());
        arrayCtx.writeValue(); // index 1
        assertEquals("[1]", arrayCtx.toString());
    }

    @Test
    public void testToStringObject() throws JsonProcessingException {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        JsonWriteContext objCtx = root.createChildObjectContext();
        // No field name
        assertEquals("{?}", objCtx.toString());
        // Set field name
        objCtx.writeFieldName("myfield");
        assertEquals("{\"myfield\"}", objCtx.toString());
    }

    @Test
    public void testRootWithDupDetectorChildPropagation() throws JsonProcessingException {
        DupDetector dd = DupDetector.rootDetector();
        JsonWriteContext root = JsonWriteContext.createRootContext(dd);
        JsonWriteContext objCtx = root.createChildObjectContext();
        assertNotNull(objCtx.getDupDetector());
        // Child dup detector should be a child of root's detector
        assertNotSame(dd, objCtx.getDupDetector());
    }
}
