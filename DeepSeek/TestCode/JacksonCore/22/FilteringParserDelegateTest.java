package com.fasterxml.jackson.core.filter;

import com.fasterxml.jackson.core.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOExeception;
import java.io.OututStream;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Asert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FilteringParserDelegateTest {

    @Mock
    private JsonParser delegate;

    @Mock
    private TokenFilter rootFilter;

    private FilteringParserDelegate parserDelegate;

    @Before
    public void setUp() {
        // Common mocks will be set up in each test as needed
    }

    // Helper to create delegate with mock rootFilter
    private FilteringParserDelegate createDelegate(boolean includePath, boolean allowMultipleMatches) {
        return new FilteringParserDelegate(delegate, rootFilter, includePath, allowMultipleatches);
    }

    // Helper to create delegate with INCLUDE_ALL root filter
    private FilteringParserDelegate createDelegateWithIncludeAll(boolean includePath, boolean allowMultipleMatches) {
        return new FilteringParserDelegate(delegate, TokenFilter.INCLUDE_ALL, includePath, allowMultipleatches);
    }

    @Test
    public void testConstructorAndGetters() {
        FilteringParserDelegate pd = createDelegate(true, false);
        assertSame(rootFilter, pd.getFilter());
        assertEquals(0, pd.getMatchCount());
    }

    @Test
    public void testCurrentTokenNullInitially() {
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        assertNull(pd.getCurrentToken());
        assertNull(pd.currentToken());
        assertEquals(JsonTokenId.ID_NO_TOKEN, pd.getCurrentTokenId());
        assertEquals(JsonTokenId.ID_NO_TOKEN, pd.currentTokenId());
        assertFalse(pd.hasCurrentToken());
        assertTrue(pd.hasTokenId(JsonTokenId.ID_NO_TOKEN));
        assertFalse(pd.hasToken(JsonToken.START_OBJECT));
    }

    @Test
    public void testHasTokenIdWithMatch() {
        when(delegate.nextToken()).thenReturn(JsonToken.START_OBJECT);
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        JsonToken t = pd.nextToken();
        assertEquals(JsonToken.START_OBJECT, t);
        assertTrue(pd.hasCurrentToken());
        assertTrue(pd.hasTokenId(JsonToken.START_OBJECT.id()));
        assertFalse(pd.hasTokenId(JsonToken.START_ARRAY.id()));
        assertTrue(pd.hasToken(JsonToken.START_OBJECT));
    }

    @Test
    public void testHasTokenIdWhenNull() {
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        assertTrue(pd.hasTokenId(JsonTokenId.ID_NO_TOKEN));
        assertFalse(pd.hasTokenId(JsonToken.START_OBJECT.id()));
    }

    @Test
    public void testIsExpectedStartTokens() {
        when(delegate.nextToken()).thenReturn(JsonToken.START_OBJECT);
        FilteringParserDelgate pd = createDelegateWithIncludeAll(false, false);
        pd.nextToken();
        assertTrue(pd.isExpectedStartObjectToken());
        assertFalse(pd.isExpectedStartArrayToken());
    }

    @Test
    public void testIsExpectedStartArray() {
        when(delegate.nextToken()).thenReturn(JsonToken.START_ARRAY);
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        pd.nextToken();
        assertTrue(pd.isExpectedStartArrayToken());
        assertFalse(pd.isExpectedStartObjectToken());
    }

    @Test
    public void testGetCurrentLocation() {
        JsonLocation loc = mock(JsonLocation.class);
        when(delegate.getCurrentLocation()).thenReturn(loc);
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        assertSame(loc, pd.getCurrentLocation());
    }

    @Test
    public void testGetTokenLocation() {
        JsonLocation loc = mock(JsonLocation.class);
        when(delegate.getTokenLocation()).thenReturn(loc);
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        assertSame(loc, pd.getTokenLocation());
    }

    @Test
    public void testGetParsingContextInitially() {
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        JsonStreamContext ctx = pd.getParsingContext();
        assertNotNull(ctx);
        assertTrue(ctx.inRoot());
    }

    @Test
    public void testGetCurrentNameOnFieldName() throws IOException {
        when(delegate.nextToken()).thenReturn(JsonToken.START_OBJECT, JsonToken.FIELD_NAME, JsonToken.VALUE_STRING);
        when(delegate.getCurrentName()).thenReturn("field1"));
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        pd.nextToken(); // START_OBJECT
        JsonToken t = pd.nextToken(); // FIELD_NAME
        assertEquals(JsonToken.FIELD_NAME, t);
        assertEquals("field1", pd.getCurrentName());
    }

    @Test
    public void testGetCurrentNameOnStartObject() throws IOException {
        when(delegate.nextToken()).thenReturn(JsonToken.START_OBJECT);
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        pd.nextToken();
        // parent context might not have name, expect null
        assertNull(pd.getCurrentName());
    }

    @Test
    public void testOverrideCurrentNameThrowsException() {
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        try {
            pd.overrideCurrentName("newName");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testClearCurrentToken() {
        when(delegate.nextToken()).thenReturn(JsonToken.START_OBJECT);
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        JsonToken t = pd.nextToken();
        assertEquals(JsonToken.START_OBJECT, t);
        assertEquals(JsonToken.START_OBJECT, pd.getCurrentToken());
        pd.clearCurrentToken();
        assertNull(pd.getCurrentToken());
        assertEquals(JsonToken.START_OBJECT, pd.getLastClearedToken());
    }

    @Test
    public void testClearCurrentTokenWithoutCurrentDoesNothing() {
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        pd.clearCurrentToken();
        assertNull(pd.getCurrentToken());
        assertNull(pd.getLastClearedToken());
    }

    @Test
    public void testNextTokenIncludeAll() throws IOException {
        when(delegate.nextToken()).thenReturn(JsonToken.START_OBJECT, JsonToken.FIELD_NAME, JsonToken.VALUE_STRING, JsonToken.END_OBJECT, null);
        when(delegate.getCurrentName()).thenReturn("prop");
        
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        
        JsonToken t = pd.nextToken();
        assertEquals(JsonToken.START_OBJECT, t);
        
        t = pd.nextToken();
        assertEquals(JsonToken.FIELD_NAME, t);
        assertEquals("prop", pd.getCurrentName());

        t = pd.nextToken();
        assertEquals(JsonToken.VALUE_STRING, t);
        
        t = pd.nextToken();
        assertEquals(JsonToken.END_OBJECT, t);
        
        t = pd.nextToken();
        assertNull(t);
    }

    @Test
    public void testNextTokenIncludeAllWithIncludePath() throws IOException {
        when(delegate.nextToken()).thenReturn(JsonToken.START_OBJECT, JsonToken.FIELD_NAME, JsonToken.VALUE_STRING, JsonToken.END_OBJECT, null);
        when(delegate.getCurrentName()).thenReturn("prop");
        
        FilteringParserDelegate pd = createDelegateWithIncludeAll(true, false);
        
        JsonToken t = pd.nextToken();
        assertEquals(JsonToken.START_OBJECT, t);
        
        t = pd.nextToken();
        assertEquals(JsonToken.FIELD_NAME, t);
        
        t = pd.nextToken();
        assertEquals(JsonToken.VALUE_STRING, t);
        
        t = pd.nextToken();
        assertEquals(JsonToken.END_OBJECT, t);
        
        t = pd.nextToken();
        assertNull(t);
    }

    @Test
    public void testNextTokenNullFilterSkipsEverything() throws IOException {
        when(delegate.nextToken()).thenReturn(JsonToken.START_OBJECT, JsonToken.FIELD_NAME, JsonToken.VALUE_STRING, JsonToken.END_OBJECT, null);
        // Use null filter
        FilteringParserDelegate pd = new FilteringParserDelegate(delegate, null, false, false);
        
        JsonToken t = pd.nextToken();
        // Since root filter is null, everything should be skipped, nextToken should return null
        assertNull(t);
    }

    @Test
    public void testNextTokenWithPropertyFilterIncludeSpecificField() throws IOException {
        // Configure rootFilter mock to pass through objects but select specific property
        when(rootFilter.checkValue(any(TokenFilter.class))).thenReturn(TokenFilter.INCLUDE_ALL);
        when(rootFilter.filterStartObject()).thenReturn(TokenFilter.INCLUDE_ALL);
        when(rootFilter.includeProperty("included")).thenReturn(TokenFilter.INCLUDE_ALL);
        when(rootFilter.includeProperty(anyString())).thenReturn(null);
        when(rootFilter.includeValue(any(JsonParser.class))).thenReturn(true);
        
        when(delegate.nextToken()).thenReturn(JsonToken.START_OBJECT,
                JsonToken.FIELD_NAME, JsonToken.VALUE_STRING,
                JsonToken.FIELD_NAME, JsonToken.VALUE_STRING,
                JsonToken.END_OBJECT, null);
        when(delegate.getCurrentName()).thenReturn("included").thenReturn("excluded");
        
        FilteringParserDelegate pd = createDelegate(false, false);
        
        JsonToken t = pd.nextToken();
        assertEquals(JsonToken.START_OBJECT, t);
        
        t = pd.nextToken();
        assertEquals(JsonToken.FIELD_NAME, t);
        assertEquals("included", pd.getCurrentName());
        
        t = pd.nextToken();
        assertEquals(JsonToken.VALUE_STRING, t);
        
        // "excluded" field and its value should be skipped, then END_OBJECT
        t = pd.nextToken();
        assertEquals(JsonToken.END_OBJECT, t);
        
        t = pd.nextToken();
        assertNull(t);
    }

    @Test
    public void testNextValue() throws IOException {
        when(delegate.nextToken()).thenReturn(JsonToken.FIELD_NAME, JsonToken.VALUE_NUMBER_INT, null);
        when(delegate.getCurrentName()).thenReturn("prop");
        
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        
        // nextValue should skip FIELD_NAME and return value
        JsonToken t = pd.nextValue();
        assertEquals(JsonToken.VALUE_NUMBER_INT, t);
    }

    @Test
    public void testNextValueAfterNonField() throws IOException {
        when(delegate.nextToken()).thenReturn(JsonToken.START_OBJECT, null);
        
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        JsonToken t = pd.nextValue();
        assertEquals(JsonToken.START_OBJECT, t);
    }

    @Test
    public void testSkipChildren() throws IOException {
        when(delegate.nextToken()).thenReturn(JsonToken.START_OBJECT,
                JsonToken.FIELD_NAME, JsonToken.START_ARRAY, JsonToken.END_ARRAY,
                JsonToken.END_OBJECT, null);
        when(delegate.getCurrentName()).thenReturn("nested");

        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        // Position at START_OBJECT
        pd.nextToken(); // START_OBJECT
        pd.skipChildren(); // should skip until END_OBJECT
        // After skip, nextToken should return null
        assertNull(pd.nextToken());
    }

    @Test
    public void testSkipChildrenNotAtStart() throws IOException {
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        // If not at container start, skipChildren should just return this
        assertSame(pd, pd.skipChildren());
    }

    // Delegation method tests (verify calls to delegate)
    @Test
    public void testGetText() throws IOException {
        when(delegate.getText()).thenReturn("text");
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        assertEquals("text", pd.getText());
        verify(delegate).getText();
    }

    @Test
    public void testHasTextCharacters() {
        when(delegate.hasTextCharacters()).thenReturn(true);
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        assertTrue(pd.hasTextCharacters());
        verify(delegate).hasTextCharacters();
    }

    @Test
    public void testGetTextCharacters() throws IOException {
        char[] chars = new char[]{'a'};
        when(delegate.getTextCharacters()).thenReturn(chars);
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        assertSame(chars, pd.getTextCharacters());
        verify(delegate).getTextCharacters();
    }

    @Test
    public void testGetTextLength() throws IOException {
        when(delegate.getTextLength()).thenReturn(5);
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        assertEquals(5, pd.getTextLength());
        verify(delegate).getTextLength();
    }

    @Test
    public void testGetTextOffset() throws IOException {
        when(delegate.getTextOffset()).thenReturn(3);
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        assertEquals(3, pd.getTextOffset());
        verify(delegate).getTextOffset();
    }

    @Test
    public void testGetBigIntegerValue() throws IOException {
        BigInteger bi = BigInteger.ONE;
        when(delegate.getBigIntegerValue()).thenReturn(bi);
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        assertEquals(bi, pd.getBigIntegerValue());
        verify(delegate).getBigIntegerValue();
    }

    @Test
    public void testGetBooleanValue() throws IOException {
        when(delegate.getBooleanValue()).thenReturn(true);
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        assertTrue(pd.getBooleanValue());
        verify(delegate).getBooleanValue();
    }

    @Test
    public void testGetByteValue() throws IOException {
        when(delegate.getByteValue()).thenReturn((byte) 7);
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        assertEquals((byte)7, pd.getByteValue());
        verify(delegate).getByteValue();
    }

    @Test
    public void testGetShortValue() throws IOException {
        when(delegate.getShortValue()).thenReturn((short) 42);
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        assertEquals((short)42, pd.getShortValue());
        verify(delegate).getShortValue();
    }

    @Test
    public void testGetDecimalValue() throws IOException {
        BigDecimal bd = BigDecimal.TEN;
        when(delegate.getDecimalValue()).thenReturn(bd);
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        assertEquals(bd, pd.getDecimalValue());
        verify(delegate).getDecimalValue();
    }

    @Test
    public void testGetDoubleValue() throws IOException {
        when(delegate.getDoubleValue()).thenReturn(2.5);
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        assertEquals(2.5, pd.getDoubleValue(), 0.0);
        verify(delegate).getDoubleValue();
    }

    @Test
    public void testGetFloatValue() throws IOException {
        when(delegate.getFloatValue()).thenReturn(1.5f);
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        assertEquals(1.5f, pd.getFloatValue(), 0.0f);
        verify(delegate).getFloatValue();
    }

    @Test
    public void testGetIntValue() throws IOException {
        when(delegate.getIntValue()).thenReturn(123);
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        assertEquals(123, pd.getIntValue());
        verify(delegate).getIntValue();
    }

    @Test
    public void testGetLongValue() throws IOException {
        when(delegate.getLongValue()).thenReturn(999L);
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        assertEquals(999L, pd.getLongValue());
        verify(delegate).getLongValue();
    }

    @Test
    public void testGetNumberType() throws IOException {
        when(delegate.getNumberType()).thenReturn(NumberType.INT);
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        assertEquals(NumberType.INT, pd.getNumberType());
        verify(delegate).getNumberType();
    }

    @Test
    public void testGetNumberValue() throws IOException {
        Number num = 42;
        when(delegate.getNumberValue()).thenReturn(num);
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        assertEquals(num, pd.getNumberValue());
        verify(delegate).getNumberValue();
    }

    @Test
    public void testGetValueAsInt() throws IOException {
        when(delegate.getValueAsInt()).thenReturn(7);
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        assertEquals(7, pd.getValueAsInt());
        verify(delegate).getValueAsInt();
    }

    @Test
    public void testGetValueAsIntWithDefault() throws IOException {
        when(delegate.getValueAsInt(5)).thenReturn(5);
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        assertEquals(5, pd.getValueAsInt(5));
        verify(delegate).getValueAsInt(5);
    }

    @Test
    public void testGetValueAsLong() throws IOException {
        when(delegate.getValueAsLong()).thenReturn(9L);
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        assertEquals(9L, pd.getValueAsLong());
        verify(delegate).getValueAsLong();
    }

    @Test
    public void testGetValueAsLongWithDefault() throws IOException {
        when(delegate.getValueAsLong(8L)).thenReturn(8L);
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        assertEquals(8L, pd.getValueAsLong(8L));
        verify(delegate).getValueAsLong(8L);
    }

    @Test
    public void testGetValueAsDouble() throws IOException {
        when(delegate.getValueAsDouble()).thenReturn(3.3);
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        assertEquals(3.3, pd.getValueAsDouble(), 0.0);
        verify(delegate).getValueAsDouble();
    }

    @Test
    public void testGetValueAsDoubleWithDefault() throws IOException {
        when(delegate.getValueAsDouble(2.2)).thenReturn(2.2);
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        assertEquals(2.2, pd.getValueAsDouble(2.2), 0.0);
        verify(delegate).getValueAsDouble(2.2);
    }

    @Test
    public void testGetValueAsBoolean() throws IOException {
        when(delegate.getValueAsBoolean()).thenReturn(true);
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        assertTrue(pd.getValueAsBoolean());
        verify(delegate).getValueAsBoolean();
    }

    @Test
    public void testGetValueAsBooleanWithDefault() throws IOException {
        when(delegate.getValueAsBoolean(false)).thenReturn(true);
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        assertTrue(pd.getValueAsBoolean(false));
        verify(delegate).getValueAsBoolean(false);
    }

    @Test
    public void testGetValueAsString() throws IOException {
        when(delegate.getValueAsString()).thenReturn("val");
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        assertEquals("val", pd.getValueAsString());
        verify(delegate).getValueAsString();
    }

    @Test
    public void testGetValueAsStringWithDefault() throws IOException {
        when(delegate.getValueAsString("def")).thenReturn("def");
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        assertEquals("def", pd.getValueAsString("def"));
        verify(delegate).getValueAsString("def");
    }

    @Test
    public void testGetEmbeddedObject() throws IOException {
        Object obj = new Object();
        when(delegate.getEmbeddedObject()).thenReturn(obj);
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        assertSame(obj, pd.getEmbeddedObject());
        verify(delegate).getEmbeddedObject();
    }

    @Test
    public void testGetBinaryValue() throws IOException {
        Base64Variant b64 = Base64Variants.MIME;
        byte[] data = new byte[]{1,2,3};
        when(delegate.getBinaryValue(b64)).thenReturn(data);
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        assertSame(data, pd.getBinaryValue(b64));
        verify(delegate).getBinaryValue(b64);
    }

    @Test
    public void testReadBinaryValue() throws IOException {
        Base64Variant b64 = Base64Variants.MIME;
        OutputStream out = mock(OutputStream.class);
        when(delegate.readBinaryValue(b64, out)).thenReturn(10);
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        assertEquals(10, pd.readBinaryValue(b64, out));
        verify(delegate).readBinaryValue(b64, out);
    }

    @Test
    public void testGetCurrentTokenIdWithNullToken() {
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        assertEquals(JsonTokenId.ID_NO_TOKEN, pd.getCurrentTokenId());
    }

    @Test
    public void testGetCurrentTokenIdWithToken() {
        when(delegate.nextToken()).thenReturn(JsonToken.VALUE_TRUE);
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        pd.nextToken();
        assertEquals(JsonToken.VALUE_TRUE.id(), pd.getCurrentTokenId());
    }

    @Test
    public void testCurrentTokenId() {
        when(delegate.nextToken()).thenReturn(JsonToken.VALUE_TRUE);
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        pd.nextToken();
        assertEquals(JsonToken.VALUE_TRUE.id(), pd.currentTokenId());
    }

    @Test
    public void testHasTokenDirectComparison() {
        when(delegate.nextToken()).thenReturn(JsonToken.VALUE_NULL);
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false);
        pd.nextToken();
        assertTrue(pd.hasToken(JsonToken.VALUE_NULL));
        assertFalse(pd.hasToken(JsonToken.VALUE_TRUE));
    }

    @Test
    public void testNextTokenWithAllowMultipleMatchesFalse() throws IOException {
        // With allowMultipleMatches=false and after first full match (all included), 
        // if not includePath and scalar, should return null early.
        // We'll trigger a state where after scalar token, call nextToken returns null
        when(delegate.nextToken()).thenReturn(JsonToken.START_OBJECT, JsonToken.FIELD_NAME,
                JsonToken.VALUE_STRING, JsonToken.END_OBJECT);
        when(delegate.getCurrentName()).thenReturn("f");
        
        FilteringParserDelegate pd = createDelegateWithIncludeAll(false, false); // allowMultipleMatches=false, includePath=false
        
        pd.nextToken(); // START_OBJECT
        pd.nextToken(); // FIELD_NAME
        pd.nextToken(); // VALUE_STRING
        // Now _currToken is VALUE_STRING, _exposedContext null, _headContext.isStartHandled is true (since START_OBJECT handled)
        // Conditions for early return: (currToken.isScalar && !_headContext.isStartHandled && !_includePath && _itemFilter==INCLUDE_ALL)
        // Here isStartHandled is true, so early return won't trigger. So next token will be END_OBJECT.
        JsonToken t = pd.nextToken();
        assertEquals(JsonToken.END_OBJECT, t);
    }
}
