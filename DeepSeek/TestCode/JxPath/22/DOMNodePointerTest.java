package org.apache.commons.jxpath.ri.model.dom;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Locale;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.jxpath.JXPathAbstractFactoryException;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.compiler.ProcessingInstructionTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

@RunWith(MockitoJUnitRunner.class)
public class DOMNodePointerTest {

    private Document doc;
    private Element root;
    private Element child;
    private Text textNode;
    private Comment commentNode;
    private ProcessingInstruction piNode;

    @Mock
    private JXPathContext mockContext;
    @Mock
    private NodePointer mockParent;
    @Mock
    private NodeIterator mockNodeIterator;
    @Mock
    private NodePointer mockNodePointer;

    @Before
    public void setUp() throws Exception {
        doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        root = doc.createElement("root");
        doc.appendChild(root);
        child = doc.createElement("child");
        root.appendChild(child);
        textNode = doc.createTextNode("some text");
        root.appendChild(textNode);
        commentNode = doc.createComment("comment");
        root.appendChild(commentNode);
        piNode = doc.createProcessingInstruction("target", "data");
        root.appendChild(piNode);
    }

    private DOMNodePointer pointerFor(Node node) {
        return new DOMNodePointer(node, Locale.US);
    }

    private DOMNodePointer pointerFor(Node node, String id) {
        return new DOMNodePointer(node, Locale.US, id);
    }

    private DOMNodePointer pointerWithParent(NodePointer parent, Node node) {
        return new DOMNodePointer(parent, node);
    }

    @Test
    public void testConstructorNodeLocale() {
        DOMNodePointer p = new DOMNodePointer(root, Locale.US);
        assertEquals(root, p.getNode());
    }

    @Test
    public void testConstructorNodeLocaleId() {
        DOMNodePointer p = new DOMNodePointer(root, Locale.US, "myId");
        assertEquals(root, p.getNode());
        assertEquals("myId", p.asPath());
    }

    @Test
    public void testConstructorParentNode() {
        when(mockParent.asPath()).thenReturn("/root");
        DOMNodePointer p = new DOMNodePointer(mockParent, child);
        assertEquals(child, p.getNode());
        assertEquals(mockParent, p.getParent());
    }

    @Test
    public void testTestNodeNullTestReturnsTrue() {
        assertTrue(DOMNodePointer.testNode(root, null));
    }

    @Test
    public void testTestNodeNodeNameTestElementWildcardTrue() {
        NodeNameTest test = new NodeNameTest(new QName("*"), null);
        assertTrue(DOMNodePointer.testNode(root, test));
    }

    @Test
    public void testTestNodeNodeNameTestNonElementReturnsFalse() {
        NodeNameTest test = new NodeNameTest(new QName("text"), null);
        assertFalse(DOMNodePointer.testNode(textNode, test));
    }

    @Test
    public void testTestNodeNodeNameTestMatchingLocalNameAndNS() {
        root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:ns", "http://example.com");
        child.setAttribute("xmlns:ns", "http://example.com");
        QName name = new QName("ns", "child");
        NodeNameTest test = new NodeNameTest(name, "http://example.com");
        assertTrue(DOMNodePointer.testNode(child, test));
    }

    @Test
    public void testTestNodeNodeNameTestMatchingLocalNameNullNS() {
        QName name = new QName(null, "child");
        NodeNameTest test = new NodeNameTest(name, null);
        assertTrue(DOMNodePointer.testNode(child, test));
    }

    @Test
    public void testTestNodeNodeNameTestMatchingWildcardAndPrefix() {
        QName name = new QName("ns", "*");
        NodeNameTest test = new NodeNameTest(name, "http://example.com");
        root.setAttribute("xmlns:ns", "http://example.com");
        assertTrue(DOMNodePointer.testNode(child, test));
    }

    @Test
    public void testTestNodeNodeTypeTestNodeTypeNodeReturnsTrue() {
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        assertTrue(DOMNodePointer.testNode(root, test));
    }

    @Test
    public void testTestNodeNodeTypeTestNodeTypeText() {
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        assertTrue(DOMNodePointer.testNode(textNode, test));
        assertTrue(DOMNodePointer.testNode(doc.createCDATASection("cdata"), test));
    }

    @Test
    public void testTestNodeNodeTypeTestNodeTypeComment() {
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_COMMENT);
        assertTrue(DOMNodePointer.testNode(commentNode, test));
    }

    @Test
    public void testTestNodeNodeTypeTestNodeTypePI() {
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_PI);
        assertTrue(DOMNodePointer.testNode(piNode, test));
    }

    @Test
    public void testTestNodeNodeTypeTestUnknownTypeReturnsFalse() {
        NodeTypeTest test = new NodeTypeTest(999);
        assertFalse(DOMNodePointer.testNode(root, test));
    }

    @Test
    public void testTestNodeProcessingInstructionTestMatchingTarget() {
        ProcessingInstructionTest test = new ProcessingInstructionTest("target");
        assertTrue(DOMNodePointer.testNode(piNode, test));
    }

    @Test
    public void testTestNodeProcessingInstructionTestNonMatchingTarget() {
        ProcessingInstructionTest test = new ProcessingInstructionTest("other");
        assertFalse(DOMNodePointer.testNode(piNode, test));
    }

    @Test
    public void testTestNodeProcessingInstructionTestNonPINode() {
        ProcessingInstructionTest test = new ProcessingInstructionTest("target");
        assertFalse(DOMNodePointer.testNode(textNode, test));
    }

    @Test
    public void testGetNameElementWithPrefix() {
        root.setAttribute("xmlns:pre", "http://ns");
        child.setPrefix("pre");
        QName name = pointerFor(child).getName();
        assertEquals("child", name.getName());
        assertEquals("pre", name.getPrefix());
    }

    @Test
    public void testGetNameProcessingInstruction() {
        QName name = pointerFor(piNode).getName();
        assertEquals("target", name.getName());
    }

    @Test
    public void testGetNameOtherNodeType() {
        QName name = pointerFor(textNode).getName();
        assertNull(name.getName());
        assertNull(name.getPrefix());
    }

    @Test
    public void testGetNamespaceURIElement() {
        root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:ns", "http://example.com");
        child.setAttribute("xmlns:ns", "http://example.com");
        child.setPrefix("ns");
        assertEquals("http://example.com", pointerFor(child).getNamespaceURI());
    }

    @Test
    public void testGetNamespaceURIXml() {
        assertEquals("http://www.w3.org/XML/1998/namespace", pointerFor(root).getNamespaceURI("xml"));
    }

    @Test
    public void testGetNamespaceURIXmlns() {
        assertEquals("http://www.w3.org/2000/xmlns/", pointerFor(root).getNamespaceURI("xmlns"));
    }

    @Test
    public void testGetNamespaceURIUnknownPrefix() {
        assertNull(pointerFor(root).getNamespaceURI("bogus"));
    }

    @Test
    public void testGetNamespaceURICached() {
        root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:pre", "http://example.com");
        DOMNodePointer p = pointerFor(root);
        assertEquals("http://example.com", p.getNamespaceURI("pre"));
        assertEquals("http://example.com", p.getNamespaceURI("pre"));
    }

    @Test
    public void testGetDefaultNamespaceURIDefined() {
        root.setAttribute("xmlns", "http://default");
        assertEquals("http://default", pointerFor(root).getDefaultNamespaceURI());
    }

    @Test
    public void testGetDefaultNamespaceURINotDefined() {
        assertNull(pointerFor(root).getDefaultNamespaceURI());
    }

    @Test
    public void testChildIterator() {
        DOMNodePointer p = pointerFor(root);
        NodeIterator it = p.childIterator(null, false, null);
        assertNotNull(it);
    }

    @Test
    public void testAttributeIterator() {
        DOMNodePointer p = pointerFor(root);
        NodeIterator it = p.attributeIterator(new QName("attr"));
        assertNotNull(it);
    }

    @Test
    public void testNamespacePointer() {
        DOMNodePointer p = pointerFor(root);
        Pointer ptr = p.namespacePointer("pre");
        assertNotNull(ptr);
    }

    @Test
    public void testNamespaceIterator() {
        DOMNodePointer p = pointerFor(root);
        NodeIterator it = p.namespaceIterator();
        assertNotNull(it);
    }

    @Test
    public void testGetNamespaceResolver() {
        DOMNodePointer p = pointerFor(root);
        assertNotNull(p.getNamespaceResolver());
        assertSame(p.getNamespaceResolver(), p.getNamespaceResolver());
    }

    @Test
    public void testGetBaseValue() {
        assertEquals(root, pointerFor(root).getBaseValue());
    }

    @Test
    public void testGetImmediateNode() {
        assertEquals(root, pointerFor(root).getImmediateNode());
    }

    @Test
    public void testIsActual() {
        assertTrue(pointerFor(root).isActual());
    }

    @Test
    public void testIsCollection() {
        assertFalse(pointerFor(root).isCollection());
    }

    @Test
    public void testGetLength() {
        assertEquals(1, pointerFor(root).getLength());
    }

    @Test
    public void testIsLeafNoChildren() {
        Element empty = doc.createElement("empty");
        assertTrue(pointerFor(empty).isLeaf());
    }

    @Test
    public void testIsLeafWithChildren() {
        assertFalse(pointerFor(root).isLeaf());
    }

    @Test
    public void testIsLanguageMatch() {
        child.setAttribute("xml:lang", "en-US");
        assertTrue(pointerFor(child).isLanguage("en"));
    }

    @Test
    public void testIsLanguageNoLanguageFallsToSuper() {
        DOMNodePointer p = spy(pointerFor(child));
        assertTrue(p.isLanguage("en"));
        verify(p).superCallIsLanguage(anyString());
    }

    private boolean superCallIsLanguage(String lang) {
        return true;
    }

    @Test
    public void testFindEnclosingAttributeFound() {
        child.setAttribute("xml:lang", "fr");
        assertEquals("fr", DOMNodePointer.findEnclosingAttribute(child, "xml:lang"));
    }

    @Test
    public void testFindEnclosingAttributeNotFound() {
        assertNull(DOMNodePointer.findEnclosingAttribute(child, "xml:lang"));
    }

    @Test
    public void testGetLanguage() {
        child.setAttribute("xml:lang", "de");
        assertEquals("de", pointerFor(child).getLanguage());
    }

    @Test
    public void testSetValueTextNode() {
        Text text = doc.createTextNode("old");
        DOMNodePointer p = pointerFor(text);
        p.setValue("new");
        assertEquals("new", text.getNodeValue());
    }

    @Test
    public void testSetValueTextNodeEmptyStringRemovesNode() {
        Text text = doc.createTextNode("old");
        root.appendChild(text);
        DOMNodePointer p = pointerFor(text);
        p.setValue("");
        assertNull(text.getParentNode());
    }

    @Test
    public void testSetValueElementWithNodeValue() {
        Element newChild = doc.createElement("newchild");
        DOMNodePointer p = pointerFor(root);
        p.setValue(newChild);
        assertEquals(1, root.getChildNodes().getLength());
        assertEquals("newchild", root.getFirstChild().getNodeName());
    }

    @Test
    public void testSetValueElementWithDocumentValue() {
        Document newDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element newRoot = newDoc.createElement("newroot");
        newDoc.appendChild(newRoot);
        newRoot.appendChild(newDoc.createElement("sub"));
        DOMNodePointer p = pointerFor(root);
        p.setValue(newDoc);
        assertEquals(1, root.getChildNodes().getLength());
        assertEquals("sub", root.getFirstChild().getNodeName());
    }

    @Test
    public void testSetValueElementWithString() {
        DOMNodePointer p = pointerFor(root);
        p.setValue("text only");
        assertEquals(1, root.getChildNodes().getLength());
        assertEquals("text only", root.getTextContent());
    }

    @Test
    public void testSetValueElementWithEmptyString() {
        DOMNodePointer p = pointerFor(root);
        p.setValue("");
        assertEquals(0, root.getChildNodes().getLength());
    }

    @Test(expected = JXPathAbstractFactoryException.class)
    public void testCreateChildFactoryFails() {
        DOMNodePointer p = pointerFor(root);
        when(mockContext.getAbstractFactory(any())).thenReturn(null);
        p.createChild(mockContext, new QName("newchild"), 0);
    }

    @Test
    public void testCreateChildFactorySuccessButNotFound() {
        Document otherDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element factoryNode = otherDoc.createElement("factorychild");
        DOMNodePointer p = pointerFor(root);
        when(mockContext.getAbstractFactory(any())).thenReturn(new TestFactory(true));
        try {
            p.createChild(mockContext, new QName("newchild"), 0);
            fail("Expected JXPathAbstractFactoryException");
        } catch (JXPathAbstractFactoryException e) {
        }
    }

    private static class TestFactory implements org.apache.commons.jxpath.AbstractFactory {
        boolean createSuccess;
        TestFactory(boolean success) { createSuccess = success; }
        public boolean createObject(JXPathContext context, Pointer pointer, Object parent, String name, int index) {
            return createSuccess;
        }
        public boolean declareVariable(JXPathContext context, String name) { return false; }
    }

    @Test
    public void testCreateChildWithValue() {
        DOMNodePointer p = spy(pointerFor(root));
        doReturn(mockNodePointer).when(p).createChild(mockContext, new QName("c"), 0);
        NodePointer result = p.createChild(mockContext, new QName("c"), 0, "value");
        verify(mockNodePointer).setValue("value");
        assertEquals(mockNodePointer, result);
    }

    @Test
    public void testCreateAttributeElementNullContext() {
        DOMNodePointer p = pointerFor(root);
        assertNotNull(p.createAttribute(null, new QName("attr")));
    }

    @Test(expected = JXPathException.class)
    public void testCreateAttributeUnknownPrefix() {
        DOMNodePointer p = pointerFor(root);
        p.createAttribute(mockContext, new QName("unknown", "attr"));
    }

    @Test
    public void testRemoveNodeHasParent() {
        parentHasChildRemove();
    }

    private void parentHasChildRemove() {
        DOMNodePointer p = pointerFor(child);
        p.remove();
        assertNull(child.getParentNode());
    }

    @Test(expected = JXPathException.class)
    public void testRemoveRootNodeThrows() {
        pointerFor(doc).remove();
    }

    @Test
    public void testAsPathWithId() {
        DOMNodePointer p = pointerFor(root, "testId");
        assertEquals("id('testId')", p.asPath());
    }

    @Test
    public void testAsPathElementNoNamespace() {
        DOMNodePointer p = pointerWithParent(mockParent, child);
        when(mockParent.asPath()).thenReturn("/root");
        String path = p.asPath();
        assertTrue(path.startsWith("/root/child["));
    }

    @Test
    public void testAsPathTextNode() {
        DOMNodePointer p = pointerWithParent(mockParent, textNode);
        when(mockParent.asPath()).thenReturn("/root");
        assertTrue(p.asPath().startsWith("/root/text()["));
    }

    @Test
    public void testAsPathPINode() {
        DOMNodePointer p = pointerWithParent(mockParent, piNode);
        when(mockParent.asPath()).thenReturn("/root");
        assertTrue(p.asPath().startsWith("/root/processing-instruction('target')["));
    }

    @Test
    public void testAsPathDocumentNode() {
        DOMNodePointer p = pointerFor(doc);
        assertEquals("", p.asPath());
    }

    @Test
    public void testGetPointerByID() {
        child.setIdAttribute("id", true);
        child.setAttribute("id", "childId");
        DOMNodePointer p = pointerFor(root);
        Pointer result = p.getPointerByID(mockContext, "childId");
        assertEquals(child, result.getNode());
    }

    @Test
    public void testGetPointerByIDNotFound() {
        DOMNodePointer p = pointerFor(root);
        Pointer result = p.getPointerByID(mockContext, "nonexistent");
        assertTrue(result instanceof NullPointer);
    }

    @Test
    public void testCompareChildNodePointersSameNode() {
        DOMNodePointer p = pointerFor(root);
        assertEquals(0, p.compareChildNodePointers(p, p));
    }

    @Test
    public void testCompareChildNodePointersAttributeOtherOrder() {
        child.setAttribute("attr", "val");
        Attr attr = child.getAttributeNode("attr");
        NodePointer attrPtr = pointerFor(attr);
        NodePointer elemPtr = pointerFor(child);
        DOMNodePointer p = pointerFor(child);
        assertEquals(-1, p.compareChildNodePointers(attrPtr, elemPtr));
        assertEquals(1, p.compareChildNodePointers(elemPtr, attrPtr));
    }

    @Test
    public void testCompareChildNodePointersBothAttributes() {
        child.setAttribute("a", "1");
        child.setAttribute("b", "2");
        Attr attrA = child.getAttributeNode("a");
        Attr attrB = child.getAttributeNode("b");
        NodePointer pA = pointerFor(attrA);
        NodePointer pB = pointerFor(attrB);
        DOMNodePointer p = pointerFor(child);
        assertEquals(-1, p.compareChildNodePointers(pA, pB));
        assertEquals(1, p.compareChildNodePointers(pB, pA));
    }

    @Test
    public void testCompareChildNodePointersSiblingOrder() {
        Element elem1 = doc.createElement("e1");
        Element elem2 = doc.createElement("e2");
        root.appendChild(elem1);
        root.appendChild(elem2);
        NodePointer p1 = pointerFor(elem1);
        NodePointer p2 = pointerFor(elem2);
        DOMNodePointer p = pointerFor(root);
        assertEquals(-1, p.compareChildNodePointers(p1, p2));
        assertEquals(1, p.compareChildNodePointers(p2, p1));
    }

    @Test
    public void testGetPrefix() {
        assertNull(DOMNodePointer.getPrefix(root));
        root.setPrefix("pre");
        assertEquals("pre", DOMNodePointer.getPrefix(root));
    }

    @Test
    public void testGetLocalName() {
        assertEquals("root", DOMNodePointer.getLocalName(root));
        root.setPrefix("pre");
        assertEquals("root", DOMNodePointer.getLocalName(root));
    }

    @Test
    public void testGetNamespaceURIStaticMethod() {
        root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:ns", "http://example.com");
        child.setPrefix("ns");
        assertEquals("http://example.com", DOMNodePointer.getNamespaceURI(child));
    }

    @Test
    public void testHashCode() {
        assertEquals(root.hashCode(), pointerFor(root).hashCode());
    }

    @Test
    public void testEqualsSameObject() {
        DOMNodePointer p = pointerFor(root);
        assertTrue(p.equals(p));
    }

    @Test
    public void testEqualsSameNode() {
        DOMNodePointer p1 = pointerFor(root);
        DOMNodePointer p2 = pointerFor(root);
        assertTrue(p1.equals(p2));
    }

    @Test
    public void testEqualsDifferentNode() {
        DOMNodePointer p1 = pointerFor(root);
        DOMNodePointer p2 = pointerFor(child);
        assertFalse(p1.equals(p2));
    }

    @Test
    public void testGetValueComment() {
        Comment cmt = doc.createComment(" data ");
        DOMNodePointer p = pointerFor(cmt);
        assertEquals("data", p.getValue());
    }

    @Test
    public void testGetValueText() {
        assertEquals("some text", pointerFor(textNode).getValue());
    }

    @Test
    public void testGetValueElementWithChildren() {
        assertEquals("some textcomment", pointerFor(root).getValue());
    }

    @Test
    public void testStringValueCommentReturnsEmpty() {
        assertEquals("", pointerFor(commentNode).getValue());
    }
}
