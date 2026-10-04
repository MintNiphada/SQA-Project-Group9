package org.mockito.internal.configuration.injection.filter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NameBasedCandidateFilterTest {

    private MockCandidateFilter nextFilter;
    private NameBasedCandidateFilter filter;
    private OngoingInjecter expectedInjecter;

    public String testField;
    public String anotherField;

    @Before
    public void setUp() {
        nextFilter = mock(MockCandidateFilter.class);
        filter = new NameBasedCandidateFilter(nextFilter);
        expectedInjecter = mock(OngoingInjecter.class);
    }

    @Test
    public void should_delegate_directly_when_mocks_collection_is_empty() throws Exception {
        Field field = getClass().getField("testField");
        List<Object> mocks = Collections.emptyList();
        Object fieldInstance = new Object();

        when(nextFilter.filterCandidate(mocks, field, fieldInstance)).thenReturn(expectedInjecter);

        OngoingInjecter actualInjecter = filter.filterCandidate(mocks, field, fieldInstance);

        assertSame(expectedInjecter, actualInjecter);
        verify(nextFilter).filterCandidate(mocks, field, fieldInstance);
    }

    @Test
    public void should_delegate_directly_when_single_mock_present() throws Exception {
        Field field = getClass().getField("testField");
        Object mockObject = Mockito.mock(List.class, "differentName");
        List<Object> mocks = Collections.singletonList(mockObject);
        Object fieldInstance = new Object();

        when(nextFilter.filterCandidate(mocks, field, fieldInstance)).thenReturn(expectedInjecter);

        OngoingInjecter actualInjecter = filter.filterCandidate(mocks, field, fieldInstance);

        assertSame(expectedInjecter, actualInjecter);
        verify(nextFilter).filterCandidate(mocks, field, fieldInstance);
    }

    @Test
    public void should_filter_mock_matching_field_name_when_multiple_mocks_present() throws Exception {
        Field field = getClass().getField("testField");
        Object matchingMock = Mockito.mock(List.class, "testField");
        Object nonMatchingMock = Mockito.mock(List.class, "unrelatedName");
        List<Object> mocks = Arrays.asList(nonMatchingMock, matchingMock);
        Object fieldInstance = new Object();

        List<Object> expectedMatchingList = Collections.singletonList(matchingMock);
        when(nextFilter.filterCandidate(expectedMatchingList, field, fieldInstance)).thenReturn(expectedInjecter);

        OngoingInjecter actualInjecter = filter.filterCandidate(mocks, field, fieldInstance);

        assertSame(expectedInjecter, actualInjecter);
        verify(nextFilter).filterCandidate(expectedMatchingList, field, fieldInstance);
    }

    @Test
    public void should_pass_empty_list_when_multiple_mocks_and_none_match_field_name() throws Exception {
        Field field = getClass().getField("testField");
        Object mock1 = Mockito.mock(List.class, "name1");
        Object mock2 = Mockito.mock(List.class, "name2");
        List<Object> mocks = Arrays.asList(mock1, mock2);
        Object fieldInstance = new Object();

        List<Object> emptyMatches = Collections.emptyList();
        when(nextFilter.filterCandidate(emptyMatches, field, fieldInstance)).thenReturn(expectedInjecter);

        OngoingInjecter actualInjecter = filter.filterCandidate(mocks, field, fieldInstance);

        assertSame(expectedInjecter, actualInjecter);
        verify(nextFilter).filterCandidate(emptyMatches, field, fieldInstance);
    }

    @Test
    public void should_filter_all_matching_mocks_when_multiple_mocks_have_matching_name() throws Exception {
        Field field = getClass().getField("anotherField");
        Object mock1 = Mockito.mock(List.class, "anotherField");
        Object mock2 = Mockito.mock(List.class, "differentName");
        Object mock3 = Mockito.mock(Runnable.class, "anotherField");
        List<Object> mocks = Arrays.asList(mock1, mock2, mock3);
        Object fieldInstance = new Object();

        List<Object> expectedMatches = Arrays.asList(mock1, mock3);
        when(nextFilter.filterCandidate(expectedMatches, field, fieldInstance)).thenReturn(expectedInjecter);

        OngoingInjecter actualInjecter = filter.filterCandidate(mocks, field, fieldInstance);

        assertSame(expectedInjecter, actualInjecter);
        verify(nextFilter).filterCandidate(expectedMatches, field, fieldInstance);
    }
}
