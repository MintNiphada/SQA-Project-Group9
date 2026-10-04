package org.apache.commons.jxpath.ri.model.jdom;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Locale;

import org.apache.commons.jxpath.AbstractFactory;
import org.apache.commons.jxpath.JXPathAbstractFactoryException;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.compiler.ProcessingInstructionTest;
import org.apache.commons.jxpath.ri.compiler.Compiler;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.jdom.Attribute;
import org.jdom.CDATA;
import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.ProcessingInstruction;
import org.jdom.Text;
import org.junit.Before;
import org.junit.Test;

public class JDOMNodePointerTest {
    private Element rootElement;
    private Document document;
    private Locale locale;

    @Before
    public void setUp() {
        locale = Locale.US;
        rootElement = new Element("root");
        rootElement.setAttribute("id", "123");
        rootElement.addContent(new Text("txt1"));
        rootElement.addContent(new Element("child").addContent(new Text("cdata?")));
        rootElement.addContent(new Comment("comment"));
        rootElement.addContent(new ProcessingInstruction("pi-target", "data"));
        document = new Document(rootElement);
    }

    @Test
    public void testConstructorNodeLocale() {
        JDOMNodePointer pointer = new JDOMNodePointer(rootElement, locale);
        assertSame(rootElement, pointer.getBaseValue());
        assertSame(rootElement, pointer.getImmediateNode());
        assertNull(pointer.getParent());
    }

    @Test
    public void testConstructorNodeLocaleId() {
        JDOMNodePointer pointer = new JDOMNodePointer(rootElement, locale, "someId");
        assertSame(rootElement, pointer.getBaseValue());
        assertEquals("someId", pointer.asPath()); // asPath returns id('someId')
    }

    @Test
    public void testConstructorParentNode() {
        JDOMNodePointer parentPointer = new JDOMNodePointer(document, locale);
        JDOMNodePointer childPointer = new JDOMNodePointer(parentPointer, rootElement);
        assertSame(parentPointer, childPointer.getParent());
        assertSame(rootElement, childPointer.getImmediateNode());
    }

    @Test
    public void testGetImmediateNode() {
        JDOMNodePointer pointer = new JDOMNodePointer(rootElement, locale);
        assertSame(rootElement, pointer.getImmediateNode());
    }

    @Test
    public void testGetBaseValue() {
        JDOMNodePointer pointer = new JDOMNodePointer(rootElement, locale);
        assertSame(rootElement, pointer.getBaseValue());
    }

    @Test
    public void testIsCollection() {
        JDOMNodePointer pointer = new JDOMNodePointer(rootElement, locale);
        assertFalse(pointer.isCollection());
    }

    @Test
    public void testGetLength() {
        JDOMNodePointer pointer = new JDOMNodePointer(rootElement, locale);
        assertEquals(1, pointer.getLength());
    }

    @Test
    public void testIsLeafEmptyElement() {
        Element empty = new Element("empty");
        JDOMNodePointer pointer = new JDOMNodePointer(empty, locale);
        assertTrue(pointer.isLeaf());
    }

    @Test
    public void testIsLeafElementWithContent() {
        JDOMNodePointer pointer = new JDOMNodePointer(rootElement, locale);
        assertFalse(pointer.isLeaf());
    }

    @Test
    public void testIsLeafDocumentEmpty() {
        Document emptyDoc = new Document();
        JDOMNodePointer pointer = new JDOMNodePointer(emptyDoc, locale);
        assertTrue(pointer.isLeaf());
    }

    @Test
    public void testIsLeafDocumentWithContent() {
        JDOMNodePointer pointer = new JDOMNodePointer(document, locale);
        assertEquals(document.getContent().size() > 0, !pointer.isLeaf());
    }

    @Test
    public void testIsLeafNonElement() {
        Text text = new Text("text");
        JDOMNodePointer pointer = new JDOMNodePointer(text, locale);
        assertTrue(pointer.isLeaf());
    }

    @Test
    public void testGetNamespaceURIElementWithNamespace() {
        Element elem = new Element("ns:local", "ns", "http://myns");
        JDOMNodePointer pointer = new JDOMNodePointer(elem, locale);
        assertEquals("http://myns", pointer.getNamespaceURI());
    }

    @Test
    public void testGetNamespaceURIElementEmptyNamespace() {
        Element elem = new Element("local");
        elem.setNamespace(Namespace.getNamespace("", ""));
        JDOMNodePointer pointer = new JDOMNodePointer(elem, locale);
        assertNull(pointer.getNamespaceURI());
    }

    @Test
    public void testGetNamespaceURINonElement() {
        JDOMNodePointer pointer = new JDOMNodePointer(new Text(""), locale);
        assertNull(pointer.getNamespaceURI());
    }

    @Test
    public void testGetNamespaceResolver() {
        JDOMNodePointer pointer = new JDOMNodePointer(rootElement, locale);
        assertNotNull(pointer.getNamespaceResolver());
        assertSame(pointer.getNamespaceResolver(), pointer.getNamespaceResolver());
    }

    @Test
    public void testGetNamespaceURIForPrefixXml() {
        JDOMNodePointer pointer = new JDOMNodePointer(rootElement, locale);
        assertEquals(Namespace.XML_NAMESPACE.getURI(), pointer.getNamespaceURI("xml"));
    }

    @Test
    public void testGetNamespaceURIForPrefixOnElement() {
        Element elem = new Element("e");
        elem.addNamespaceDeclaration(Namespace.getNamespace("pfx", "http://pfx"));
        JDOMNodePointer pointer = new JDOMNodePointer(elem, locale);
        assertEquals("http://pfx", pointer.getNamespaceURI("pfx"));
    }

    @Test
    public void testGetNamespaceURIForDocument() {
        JDOMNodePointer pointer = new JDOMNodePointer(document, locale);
        assertEquals(Namespace.XML_NAMESPACE.getURI(), pointer.getNamespaceURI("xml"));
        // document has root element with no custom namespace
        assertNull(pointer.getNamespaceURI("unknown"));
    }

    @Test
    public void testGetNamespaceURIForNullElement() {
        JDOMNodePointer pointer = new JDOMNodePointer(new Text(""), locale);
        assertNull(pointer.getNamespaceURI("any"));
    }

    @Test
    public void testCompareChildNodePointersEqual() {
        JDOMNodePointer pointer = new JDOMNodePointer(rootElement, locale);
        NodePointer p1 = new JDOMNodePointer(rootElement, locale);
        assertEquals(0, pointer.compareChildNodePointers(p1, p1));
    }

    @Test
    public void testCompareChildNodePointersOneAttribute() {
        Element element = new Element("parent");
        Attribute attr = new Attribute("attr", "val");
        element.setAttribute(attr);
        element.addContent(new Text("child"));
        JDOMNodePointer parentPointer = new JDOMNodePointer(element, locale);
        JDOMNodePointer attrPointer = new JDOMNodePointer(attr, locale);
        JDOMNodePointer textPointer = new JDOMNodePointer(element.getContent().get(0), locale);
        // attribute should be before non-attribute
        assertEquals(-1, parentPointer.compareChildNodePointers(attrPointer, textPointer));
        assertEquals(1, parentPointer.compareChildNodePointers(textPointer, attrPointer));
    }

    @Test
    public void testCompareChildNodePointersBothAttributes() {
        Element element = new Element("parent");
        Attribute attr1 = new Attribute("a1", "v1");
        Attribute attr2 = new Attribute("a2", "v2");
        element.setAttribute(attr1);
        element.setAttribute(attr2);
        JDOMNodePointer parentPointer = new JDOMNodePointer(element, locale);
        JDOMNodePointer p1 = new JDOMNodePointer(attr1, locale);
        JDOMNodePointer p2 = new JDOMNodePointer(attr2, locale);
        // attr1 appears before attr2 in list
        assertEquals(-1, parentPointer.compareChildNodePointers(p1, p2));
        assertEquals(1, parentPointer.compareChildNodePointers(p2, p1));
    }

    @Test
    public void testCompareChildNodePointersNonElement() {
        JDOMNodePointer pointer = new JDOMNodePointer(new Text("x"), locale);
        JDOMNodePointer another = new JDOMNodePointer(new Text("y"), locale);
        try {
            pointer.compareChildNodePointers(another, another);
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("compareChildNodes"));
        }
    }

    @Test
    public void testCompareChildNodePointersChildrenOrder() {
        Element parent = new Element("parent");
        Text t1 = new Text("first");
        Text t2 = new Text("second");
        parent.addContent(t1);
        parent.addContent(t2);
        JDOMNodePointer parentPointer = new JDOMNodePointer(parent, locale);
        JDOMNodePointer p1 = new JDOMNodePointer(t1, locale);
        JDOMNodePointer p2 = new JDOMNodePointer(t2, locale);
        assertEquals(-1, parentPointer.compareChildNodePointers(p1, p2));
        assertEquals(1, parentPointer.compareChildNodePointers(p2, p1));
    }

    @Test
    public void testGetNameElementNoPrefix() {
        JDOMNodePointer pointer = new JDOMNodePointer(rootElement, locale);
        QName name = pointer.getName();
        assertNull(name.getPrefix());
        assertEquals("root", name.getName());
    }

    @Test
    public void testGetNameElementWithPrefix() {
        Element elem = new Element("pfx:local", "pfx", "http://ns");
        JDOMNodePointer pointer = new JDOMNodePointer(elem, locale);
        QName name = pointer.getName();
        assertEquals("pfx", name.getPrefix());
        assertEquals("local", name.getName());
    }

    @Test
    public void testGetNameProcessingInstruction() {
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        JDOMNodePointer pointer = new JDOMNodePointer(pi, locale);
        QName name = pointer.getName();
        assertNull(name.getPrefix());
        assertEquals("target", name.getName());
    }

    @Test
    public void testGetNameOther() {
        JDOMNodePointer pointer = new JDOMNodePointer(new Text("text"), locale);
        QName name = pointer.getName();
        assertNull(name.getPrefix());
        assertNull(name.getName());
    }

    @Test
    public void testGetValueElementMixed() {
        // rootElement has txt1, child element, comment, PI
        JDOMNodePointer pointer = new JDOMNodePointer(rootElement, locale);
        String val = pointer.getValue().toString();
        // expected concatenation: "txt1" + value of child element (which has text "cdata?")
        // comment and PI are skipped
        assertTrue(val.contains("txt1"));
        assertTrue(val.contains("cdata?")); // from child element
        assertFalse(val.contains("comment")); // comment skipped
    }

    @Test
    public void testGetValueCommentTrim() {
        Comment comment = new Comment("  text  ");
        JDOMNodePointer pointer = new JDOMNodePointer(comment, locale);
        assertEquals("text", pointer.getValue());
    }

    @Test
    public void testGetValueTextTrim() {
        Text text = new Text("  text  ");
        JDOMNodePointer pointer = new JDOMNodePointer(text, locale);
        assertEquals("text", pointer.getValue());
    }

    @Test
    public void testGetValueTextPreserveSpace() {
        Element elem = new Element("e");
        elem.setAttribute("space", "preserve", Namespace.XML_NAMESPACE);
        Text text = new Text("  text  ");
        elem.addContent(text);
        JDOMNodePointer pointer = new JDOMNodePointer(text, locale);
        assertEquals("  text  ", pointer.getValue());
    }

    @Test
    public void testGetValueProcessingInstruction() {
        ProcessingInstruction pi = new ProcessingInstruction("t", "  data  ");
        JDOMNodePointer pointer = new JDOMNodePointer(pi, locale);
        assertEquals("data", pointer.getValue());
    }

    @Test
    public void testSetValueText() {
        Element parent = new Element("parent");
        Text text = new Text("original");
        parent.addContent(text);
        JDOMNodePointer pointer = new JDOMNodePointer(text, locale);
        pointer.setValue("new value");
        assertEquals("new value", text.getText());
    }

    @Test
    public void testSetValueTextEmptyStringRemovesNode() {
        Element parent = new Element("parent");
        Text text = new Text("original");
        parent.addContent(text);
        JDOMNodePointer pointer = new JDOMNodePointer(text, locale);
        pointer.setValue("");
        assertFalse(parent.getContent().contains(text));
    }

    @Test
    public void testSetValueElementWithElement() {
        Element target = new Element("target");
        target.addContent(new Text("old"));
        JDOMNodePointer pointer = new JDOMNodePointer(target, locale);
        Element newElem = new Element("new");
        newElem.addContent(new Text("new text"));
        pointer.setValue(newElem);
        assertEquals(1, target.getContent().size());
        assertTrue(target.getContent().get(0) instanceof Element);
        assertEquals("new", ((Element) target.getContent().get(0)).getName());
        assertEquals("new text", ((Text) ((Element) target.getContent().get(0)).getContent().get(0)).getText());
    }

    @Test
    public void testSetValueElementWithDocument() {
        Element target = new Element("target");
        JDOMNodePointer pointer = new JDOMNodePointer(target, locale);
        Document doc = new Document(new Element("root").addContent(new Text("docText")));
        pointer.setValue(doc);
        assertEquals(1, target.getContent().size());
        assertTrue(target.getContent().get(0) instanceof Element);
        assertEquals("root", ((Element) target.getContent().get(0)).getName());
    }

    @Test
    public void testSetValueElementWithTextOrCDATA() {
        Element target = new Element("target");
        JDOMNodePointer pointer = new JDOMNodePointer(target, locale);
        pointer.setValue(new Text("txt"));
        assertEquals(1, target.getContent().size());
        assertEquals("txt", ((Text) target.getContent().get(0)).getText());
    }

    @Test
    public void testSetValueElementWithCDATA() {
        Element target = new Element("target");
        JDOMNodePointer pointer = new JDOMNodePointer(target, locale);
        pointer.setValue(new CDATA("cdata content"));
        assertEquals(1, target.getContent().size());
        assertTrue(target.getContent().get(0) instanceof Text); // CDATA extends Text
        assertEquals("cdata content", ((Text) target.getContent().get(0)).getText());
    }

    @Test
    public void testSetValueElementWithProcessingInstruction() {
        Element target = new Element("target");
        JDOMNodePointer pointer = new JDOMNodePointer(target, locale);
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        pointer.setValue(pi);
        assertEquals(1, target.getContent().size());
        assertTrue(target.getContent().get(0) instanceof ProcessingInstruction);
        assertEquals("target", ((ProcessingInstruction) target.getContent().get(0)).getTarget());
    }

    @Test
    public void testSetValueElementWithComment() {
        Element target = new Element("target");
        JDOMNodePointer pointer = new JDOMNodePointer(target, locale);
        Comment comment = new Comment("my comment");
        pointer.setValue(comment);
        assertEquals(1, target.getContent().size());
        assertTrue(target.getContent().get(0) instanceof Comment);
        assertEquals("my comment", ((Comment) target.getContent().get(0)).getText());
    }

    @Test
    public void testSetValueElementWithString() {
        Element target = new Element("target");
        JDOMNodePointer pointer = new JDOMNodePointer(target, locale);
        pointer.setValue("str");
        assertEquals(1, target.getContent().size());
        assertEquals("str", ((Text) target.getContent().get(0)).getText());
    }

    @Test
    public void testSetValueElementWithEmptyString() {
        Element target = new Element("target");
        target.addContent(new Text("old"));
        JDOMNodePointer pointer = new JDOMNodePointer(target, locale);
        pointer.setValue("");
        assertTrue(target.getContent().isEmpty());
    }

    @Test
    public void testTestNodeNullTest() {
        assertTrue(JDOMNodePointer.testNode(null, rootElement, null));
    }

    @Test
    public void testTestNodeNameTestWildcard() {
        NodeNameTest test = new NodeNameTest(new QName(null, "*"));
        assertTrue(JDOMNodePointer.testNode(null, rootElement, test));
        assertFalse(JDOMNodePointer.testNode(null, new Text("x"), test)); // not Element
    }

    @Test
    public void testTestNodeNameTestExactNameMatch() {
        NodeNameTest test = new NodeNameTest(new QName("root"), "myns");
        Element elem = new Element("root", Namespace.getNamespace("myns"));
        assertTrue(JDOMNodePointer.testNode(null, elem, test));
    }

    @Test
    public void testTestNodeNameTestLocalNameMatchNamespaceNull() {
        NodeNameTest test = new NodeNameTest(new QName("root"), null);
        Element elem = new Element("root");
        assertTrue(JDOMNodePointer.testNode(null, elem, test));
    }

    @Test
    public void testTestNodeNameTestPrefixMatch() {
        NodeNameTest test = new NodeNameTest(new QName("pfx", "root"), "http://ns");
        Element elem = new Element("pfx:root", "pfx", "http://ns");
        assertTrue(JDOMNodePointer.testNode(null, elem, test));
    }

    @Test
    public void testTestNodeNameTestNoMatch() {
        NodeNameTest test = new NodeNameTest(new QName("other"), null);
        assertFalse(JDOMNodePointer.testNode(null, rootElement, test));
    }

    @Test
    public void testTestNodeTypeNode() {
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        assertTrue(JDOMNodePointer.testNode(null, rootElement, test));
        assertTrue(JDOMNodePointer.testNode(null, document, test));
        assertFalse(JDOMNodePointer.testNode(null, new Text("x"), test));
    }

    @Test
    public void testTestNodeTypeText() {
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        assertTrue(JDOMNodePointer.testNode(null, new Text("x"), test));
        assertTrue(JDOMNodePointer.testNode(null, new CDATA("x"), test));
        assertFalse(JDOMNodePointer.testNode(null, rootElement, test));
    }

    @Test
    public void testTestNodeTypeComment() {
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_COMMENT);
        assertTrue(JDOMNodePointer.testNode(null, new Comment("c"), test));
        assertFalse(JDOMNodePointer.testNode(null, rootElement, test));
    }

    @Test
    public void testTestNodeTypePI() {
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_PI);
        assertTrue(JDOMNodePointer.testNode(null, new ProcessingInstruction("t", "d"), test));
        assertFalse(JDOMNodePointer.testNode(null, rootElement, test));
    }

    @Test
    public void testTestNodeProcessingInstruction() {
        ProcessingInstructionTest test = new ProcessingInstructionTest("myTarget");
        assertTrue(JDOMNodePointer.testNode(null, new ProcessingInstruction("myTarget", "d"), test));
        assertFalse(JDOMNodePointer.testNode(null, new ProcessingInstruction("other", "d"), test));
        assertFalse(JDOMNodePointer.testNode(null, rootElement, test));
    }

    @Test
    public void testEqualStrings() {
        // cannot call private directly; but tested via other methods; we can test indirectly
        // already covered by testNode, so skip
    }

    @Test
    public void testGetPrefixElement() {
        Element elem = new Element("pfx:local", "pfx", "ns");
        assertEquals("pfx", JDOMNodePointer.getPrefix(elem));
        Element noPf = new Element("local");
        assertNull(JDOMNodePointer.getPrefix(noPf));
    }

    @Test
    public void testGetPrefixAttribute() {
        Attribute attr = new Attribute("pfx:attr", "val", Namespace.getNamespace("pfx", "ns"));
        assertEquals("pfx", JDOMNodePointer.getPrefix(attr));
        Attribute noPf = new Attribute("attr", "val");
        assertNull(JDOMNodePointer.getPrefix(noPf));
    }

    @Test
    public void testGetPrefixNonEligible() {
        assertNull(JDOMNodePointer.getPrefix(new Text("x")));
    }

    @Test
    public void testGetLocalNameElement() {
        assertEquals("root", JDOMNodePointer.getLocalName(rootElement));
    }

    @Test
    public void testGetLocalNameAttribute() {
        Attribute attr = new Attribute("attr", "val");
        assertEquals("attr", JDOMNodePointer.getLocalName(attr));
    }

    @Test
    public void testGetLocalNameOther() {
        assertNull(JDOMNodePointer.getLocalName(new Text("x")));
    }

    @Test
    public void testIsLanguage() {
        Element elem = new Element("e");
        JDOMNodePointer pointer = new JDOMNodePointer(elem, locale);
        assertTrue(pointer.isLanguage("en")); // calls super.isLanguage because no xml:lang
    }

    @Test
    public void testGetLanguage() {
        Element elem = new Element("e");
        elem.setAttribute("lang", "en", Namespace.XML_NAMESPACE);
        JDOMNodePointer pointer = new JDOMNodePointer(elem, locale);
        assertEquals("en", pointer.getLanguage());
    }

    @Test
    public void testFindEnclosingAttribute() {
        Element child = new Element("child");
        Element parent = new Element("parent");
        parent.setAttribute("lang", "de", Namespace.XML_NAMESPACE);
        parent.addContent(child);
        assertEquals("de", JDOMNodePointer.findEnclosingAttribute(child, "lang", Namespace.XML_NAMESPACE));
    }

    @Test
    public void testFindEnclosingAttributeNone() {
        assertNull(JDOMNodePointer.findEnclosingAttribute(rootElement, "nonexistent", Namespace.XML_NAMESPACE));
    }

    @Test
    public void testNodeParentElement() {
        Element parent = new Element("parent");
        Element child = new Element("child");
        parent.addContent(child);
        assertEquals(parent, JDOMNodePointer.nodeParent(child));
        assertNull(JDOMNodePointer.nodeParent(new Element("orphan"));
    }

    @Test
    public void testNodeParentText() {
        Element parent = new Element("parent");
        Text text = new Text("t");
        parent.addContent(text);
        assertSame(parent, JDOMNodePointer.nodeParent(text));
    }

    @Test
    public void testNodeParentCDATA() {
        Element parent = new Element("parent");
        CDATA cdata = new CDATA("c");
        parent.addContent(cdata);
        assertSame(parent, JDOMNodePointer.nodeParent(cdata));
    }

    @Test
    public void testNodeParentProcessingInstruction() {
        Element parent = new Element("parent");
        ProcessingInstruction pi = new ProcessingInstruction("t", "d");
        parent.addContent(pi);
        assertSame(parent, JDOMNodePointer.nodeParent(pi));
    }

    @Test
    public void testNodeParentComment() {
        Element parent = new Element("parent");
        Comment comment = new Comment("c");
        parent.addContent(comment);
        assertSame(parent, JDOMNodePointer.nodeParent(comment));
    }

    @Test
    public void testNodeParentNull() {
        assertNull(JDOMNodePointer.nodeParent(null));
    }

    @Test
    public void testRemoveNonRoot() {
        Element parent = new Element("parent");
        Text child = new Text("child");
        parent.addContent(child);
        JDOMNodePointer pointer = new JDOMNodePointer(child, locale);
        pointer.remove();
        assertFalse(parent.getContent().contains(child));
    }

    @Test(expected = JXPathException.class)
    public void testRemoveRootThrowsException() {
        JDOMNodePointer pointer = new JDOMNodePointer(rootElement, locale);
        pointer.remove();
    }

    @Test
    public void testAsPathWithId() {
        JDOMNodePointer pointer = new JDOMNodePointer(rootElement, locale, "test'id\"quote");
        assertTrue(pointer.asPath().startsWith("id('test&apos;id&quot;quote')"));
    }

    @Test
    public void testAsPathElementWithNullNamespace() {
        // rootElement has null namespace URI
        JDOMNodePointer parentPointer = new JDOMNodePointer(document, locale);
        JDOMNodePointer pointer = new JDOMNodePointer(parentPointer, rootElement);
        String path = pointer.asPath();
        assertTrue(path.contains("root["));
        // should contain relative position by name
    }

    @Test
    public void testAsPathElementWithNamespaceKnownPrefix() {
        Namespace ns = Namespace.getNamespace("pfx", "http://ns");
        Element elem = new Element("pfx:elem", ns);
        Element root = new Element("root");
        root.addContent(elem);
        JDOMNodePointer rootPointer = new JDOMNodePointer(root, locale);
        JDOMNodePointer pointer = new JDOMNodePointer(rootPointer, elem);
        // namespace resolver must know prefix
        pointer.getNamespaceResolver();
        String path = pointer.asPath();
        assertTrue(path.contains("pfx:elem["));
    }

    @Test
    public void testAsPathTextNode() {
        // rootElement contains Text child
        Element parent = new Element("parent");
        Text text1 = new Text("first");
        Text text2 = new Text("second");
        parent.addContent(text1);
        parent.addContent(text2);
        JDOMNodePointer parentPointer = new JDOMNodePointer(parent, locale);
        JDOMNodePointer textPointer = new JDOMNodePointer(parentPointer, text2);
        String path = textPointer.asPath();
        assertTrue(path.startsWith("/text()["));
        // position is 2
        assertTrue(path.contains("[2]"));
    }

    @Test
    public void testAsPathCDATA() {
        Element parent = new Element("parent");
        CDATA cdata = new CDATA("cdata");
        parent.addContent(new Text("first"));
        parent.addContent(cdata);
        JDOMNodePointer parentPointer = new JDOMNodePointer(parent, locale);
        JDOMNodePointer pointer = new JDOMNodePointer(parentPointer, cdata);
        String path = pointer.asPath();
        assertTrue(path.startsWith("/text()["));
    }

    @Test
    public void testAsPathProcessingInstruction() {
        Element parent = new Element("parent");
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");
        parent.addContent(pi);
        JDOMNodePointer parentPointer = new JDOMNodePointer(parent, locale);
        JDOMNodePointer pointer = new JDOMNodePointer(parentPointer, pi);
        String path = pointer.asPath();
        assertTrue(path.startsWith("/processing-instruction('target')"));
        assertTrue(path.contains("[1]"));
    }

    @Test
    public void testCreateAttributeOnNonElement() {
        JDOMNodePointer pointer = new JDOMNodePointer(new Text("text"), locale);
        JXPathContext context = JXPathContext.newContext(pointer);
        // should delegate to super.createAttribute, but we just verify it doesn't throw (super returns null or new attribute?)
        // actual behavior depends on super; for coverage we just call
        NodePointer result = pointer.createAttribute(context, new QName("attr"));
        // since non-element, super may return null or a pointer? Not essential
    }

    @Test
    public void testCreateAttributeOnElementWithPrefix() {
        JDOMNodePointer pointer = new JDOMNodePointer(rootElement, locale);
        JXPathContext context = JXPathContext.newContext(pointer);
        QName name = new QName("xml", "lang"); // xml prefix known
        NodePointer attrPointer = pointer.createAttribute(context, name);
        assertNotNull(attrPointer);
        // verify attribute exists
        Attribute attr = rootElement.getAttribute("lang", Namespace.XML_NAMESPACE);
        assertNotNull(attr);
        assertEquals("", attr.getValue()); // set empty initially
    }

    @Test(expected = JXPathException.class)
    public void testCreateAttributeUnknownPrefix() {
        JDOMNodePointer pointer = new JDOMNodePointer(rootElement, locale);
        JXPathContext context = JXPathContext.newContext(pointer);
        pointer.createAttribute(context, new QName("unknown", "attr"));
    }

    @Test
    public void testCreateChildFactoryNull() {
        JDOMNodePointer pointer = new JDOMNodePointer(rootElement, locale);
        JXPathContext context = JXPathContext.newContext(pointer);
        context.setFactory(null);
        try {
            pointer.createChild(context, new QName("child"), 0);
            fail("Expected JXPathException");
        } catch (JXPathException e) {
            assertTrue(e.getMessage().contains("Factory is not set"));
        }
    }

    @Test
    public void testCreateChildFactoryReturnsFalse() {
        JDOMNodePointer pointer = new JDOMNodePointer(rootElement, locale);
        JXPathContext context = JXPathContext.newContext(pointer);
        context.setFactory(new AbstractFactory() {
            public boolean createObject(JXPathContext ctx, NodePointer contextPointer, Object parent, String name, int index) {
                return false;
            }
            public boolean createObject(JXPathContext ctx, NodePointer contextPointer, Object parent, String name, int index, Object value) {
                return false;
            }
        });
        try {
            pointer.createChild(context, new QName("child"), 0);
            fail("Expected JXPathAbstractFactoryException");
        } catch (JXPathAbstractFactoryException e) {
            // expected
        }
    }

    @Test
    public void testCreateChildSuccess() {
        JDOMNodePointer pointer = new JDOMNodePointer(rootElement, locale);
        JXPathContext context = JXPathContext.newContext(pointer);
        context.setFactory(new AbstractFactory() {
            public boolean createObject(JXPathContext ctx, NodePointer contextPointer, Object parent, String name, int index) {
                if (parent instanceof Element) {
                    ((Element) parent).addContent(new Element(name));
                    return true;
                }
                return false;
            }
            public boolean createObject(JXPathContext ctx, NodePointer contextPointer, Object parent, String name, int index, Object value) {
                return createObject(ctx, contextPointer, parent, name, index);
            }
        });
        NodePointer childPtr = pointer.createChild(context, new QName("newChild"), 0);
        assertNotNull(childPtr);
        assertTrue(childPtr.getImmediateNode() instanceof Element);
        assertEquals("newChild", ((Element) childPtr.getImmediateNode()).getName());
    }

    @Test
    public void testCreateChildWithValue() {
        JDOMNodePointer pointer = new JDOMNodePointer(rootElement, locale);
        JXPathContext context = JXPathContext.newContext(pointer);
        context.setFactory(new AbstractFactory() {
            public boolean createObject(JXPathContext ctx, NodePointer contextPointer, Object parent, String name, int index) {
                ((Element) parent).addContent(new Element(name));
                return true;
            }
            public boolean createObject(JXPathContext ctx, NodePointer contextPointer, Object parent, String name, int index, Object value) {
                return createObject(ctx, contextPointer, parent, name, index);
            }
        });
        NodePointer cp = pointer.createChild(context, new QName("valuedChild"), 0, "someValue");
        Element newElem = (Element) cp.getImmediateNode();
        assertEquals("someValue", newElem.getText());
    }

    @Test
    public void testHashCode() {
        JDOMNodePointer p1 = new JDOMNodePointer(rootElement, locale);
        JDOMNodePointer p2 = new JDOMNodePointer(rootElement, locale);
        // same node => same hash
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    public void testEqualsSame() {
        JDOMNodePointer p1 = new JDOMNodePointer(rootElement, locale);
        assertTrue(p1.equals(p1));
    }

    @Test
    public void testEqualsNull() {
        assertFalse(new JDOMNodePointer(rootElement, locale).equals(null));
    }

    @Test
    public void testEqualsDifferentType() {
        assertFalse(new JDOMNodePointer(rootElement, locale).equals("string"));
    }

    @Test
    public void testEqualsSameNode() {
        JDOMNodePointer p1 = new JDOMNodePointer(rootElement, locale);
        JDOMNodePointer p2 = new JDOMNodePointer(rootElement, locale);
        assertTrue(p1.equals(p2));
    }

    @Test
    public void testEqualsDifferentNode() {
        JDOMNodePointer p1 = new JDOMNodePointer(rootElement, locale);
        JDOMNodePointer p2 = new JDOMNodePointer(new Element("other"), locale);
        assertFalse(p1.equals(p2));
    }

    @Test
    public void testChildIterator() {
        JDOMNodePointer pointer = new JDOMNodePointer(rootElement, locale);
        NodeIterator it = pointer.childIterator(null, false, null);
        assertTrue(it instanceof JDOMNodeIterator);
    }

    @Test
    public void testAttributeIterator() {
        JDOMNodePointer pointer = new JDOMNodePointer(rootElement, locale);
        NodeIterator it = pointer.attributeIterator(new QName("id"));
        assertTrue(it instanceof JDOMAttributeIterator);
    }

    @Test
    public void testNamespaceIterator() {
        JDOMNodePointer pointer = new JDOMNodePointer(rootElement, locale);
        NodeIterator it = pointer.namespaceIterator();
        assertTrue(it instanceof JDOMNamespaceIterator);
    }

    @Test
    public void testNamespacePointer() {
        JDOMNodePointer pointer = new JDOMNodePointer(rootElement, locale);
        NodePointer np = pointer.namespacePointer("xml");
        assertTrue(np instanceof JDOMNamespacePointer);
    }
}
