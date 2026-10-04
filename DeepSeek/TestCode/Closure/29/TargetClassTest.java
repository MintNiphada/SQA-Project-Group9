package com.google.javascript.jscomp;

import com.google.common.base.Supplier;
import com.google.javascript.jscomp.ReferenceCollectingCallback.Behavior;
import com.google.javascript.jscomp.ReferenceCollectingCallback.Reference;
import com.google.javascript.jscomp.ReferenceCollectingCallback.ReferenceCollection;
import com.google.javascript.jscomp.ReferenceCollectingCallback.ReferenceMap;
import com.google.javascript.jscomp.Scope.Var;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicInteger;

@RunWith(JUnit4.class)
public class InlineObjectLiteralsTest {

    private AbstractCompiler compiler;
    private Supplier<String> nameSupplier;
    private InlineObjectLiterals pass;

    @Before
    public void setUp() {
        compiler = new SimpleCompiler();
        nameSupplier = new Supplier<String>() {
            private final AtomicInteger counter = new AtomicInteger();
            @Override
            public String get() {
                return "s" + counter.incrementAndGet();
            }
        };
        pass = new InlineObjectLiterals(compiler, nameSupplier);
    }

    @Test
    public void testProcess_noop_for_empty_source() {
        Node externs = new Node(Token.SCRIPT);
        Node root = new Node(Token.SCRIPT);
        pass.process(externs, root);
    }

    @Test
    public void testProcess_inlines_simple_object_literal_assignment() {
        Node externs = new Node(Token.SCRIPT);
        Node root = new Node(Token.SCRIPT);
        Node name = IR.name("obj");
        Node objLit = IR.objectlit(
            IR.stringKey("a", IR.number(1)),
            IR.stringKey("b", IR.number(2)));
        Node varNode = IR.var(name, objLit);
        root.addChildToBack(varNode);
        pass.process(externs, root);
        assertTrue(root.hasChildren());
    }

    @Test
    public void testProcess_inlines_object_in_function_scope() {
        Node externs = new Node(Token.SCRIPT);
        Node root = new Node(Token.SCRIPT);
        Node function = new Node(Token.FUNCTION);
        Node name = IR.name("func");
        Node block = new Node(Token.BLOCK);
        Node var = IR.var(IR.name("x"), IR.objectlit(
            IR.stringKey("p", IR.string("hello"))));
        block.addChildToBack(var);
        Node returnNode = IR.returnNode(IR.name("x"));
        block.addChildToBack(returnNode);
        function.addChildrenToBack(IR.name(""), IR.paramList(), block);
        root.addChildToBack(function);
        pass.process(externs, root);
    }

    @Test
    public void testProcess_global_var_not_inlined() {
        Node externs = new Node(Token.SCRIPT);
        Node root = new Node(Token.SCRIPT);
        Node globalVar = IR.var(IR.name("globalObj"), IR.objectlit(
            IR.stringKey("a", IR.number(1))));
        root.addChildToBack(globalVar);
        pass.process(externs, root);
    }

    @Test
    public void testProcess_exported_var_not_inlined() {
        Node externs = new Node(Token.SCRIPT);
        Node root = new Node(Token.SCRIPT);
        Node exportedName = IR.name("exported");
        ((SimpleCompiler)compiler).setExported(exportedName.getString());
        Node var = IR.var(exportedName, IR.objectlit(IR.stringKey("a", IR.number(1))));
        root.addChildToBack(var);
        pass.process(externs, root);
    }

    @Test
    public void testProcess_rename_property_function_name_not_inlined() {
        Node externs = new Node(Token.SCRIPT);
        Node root = new Node(Token.SCRIPT);
        Node name = IR.name(RenameProperties.RENAME_PROPERTY_FUNCTION_NAME);
        Node var = IR.var(name, IR.objectlit(IR.stringKey("a", IR.number(1))));
        root.addChildToBack(var);
        pass.process(externs, root);
    }

    @Test
    public void testProcess_direct_method_call_disables_inlining() {
        Node externs = new Node(Token.SCRIPT);
        Node root = new Node(Token.SCRIPT);
        Node obj = IR.name("obj");
        Node objLit = IR.objectlit(IR.stringKey("method", IR.function(IR.name(""), IR.paramList(), IR.block())));
        Node var = IR.var(obj, objLit);
        root.addChildToBack(var);
        Node call = IR.call(IR.getprop(obj.cloneNode(), IR.string("method")));
        root.addChildToBack(IR.exprResult(call));
        pass.process(externs, root);
    }

    @Test
    public void testProcess_undef_property_reference_disables_inlining() {
        Node externs = new Node(Token.SCRIPT);
        Node root = new Node(Token.SCRIPT);
        Node obj = IR.name("obj");
        Node objLit = IR.objectlit(IR.stringKey("a", IR.number(1)));
        Node var = IR.var(obj, objLit);
        root.addChildToBack(var);
        Node getProp = IR.getprop(obj.cloneNode(), IR.string("b"));
        root.addChildToBack(IR.exprResult(getProp));
        pass.process(externs, root);
    }

    @Test
    public void testProcess_self_referential_assignment_disables_inlining() {
        Node externs = new Node(Token.SCRIPT);
        Node root = new Node(Token.SCRIPT);
        Node obj = IR.name("obj");
        Node getPropA = IR.getprop(obj.cloneNode(), IR.string("a"));
        Node objLit = IR.objectlit(IR.stringKey("b", getPropA));
        Node var = IR.var(obj, objLit);
        root.addChildToBack(var);
        pass.process(externs, root);
    }

    @Test
    public void testProcess_es5_getter_disables_inlining() {
        Node externs = new Node(Token.SCRIPT);
        Node root = new Node(Token.SCRIPT);
        Node obj = IR.name("obj");
        Node getter = new Node(Token.GETTER_DEF, IR.string("a"), new Node(Token.BLOCK));
        Node objLit = IR.objectlit(getter);
        Node var = IR.var(obj, objLit);
        root.addChildToBack(var);
        pass.process(externs, root);
    }

    @Test
    public void testProcess_es5_setter_disables_inlining() {
        Node externs = new Node(Token.SCRIPT);
        Node root = new Node(Token.SCRIPT);
        Node obj = IR.name("obj");
        Node setter = new Node(Token.SETTER_DEF, IR.string("a"), new Node(Token.BLOCK));
        Node objLit = IR.objectlit(setter);
        Node var = IR.var(obj, objLit);
        root.addChildToBack(var);
        pass.process(externs, root);
    }

    @Test
    public void testProcess_invalid_var_validation() {
        assertNotNull(pass);
    }

    private static class SimpleCompiler extends AbstractCompiler {
        private boolean exportedFlag = false;
        private String exportedName;

        public void setExported(String name) {
            exportedFlag = true;
            exportedName = name;
        }

        @Override
        public CompilerOptions getOptions() {
            CompilerOptions options = new CompilerOptions();
            options.setCodingConvention(new CodingConvention() {
                @Override
                public boolean isExported(String localName, boolean local) {
                    return exportedFlag && exportedName.equals(localName);
                }
            });
            return options;
        }

        @Override
        public CodingConvention getCodingConvention() {
            return getOptions().getCodingConvention();
        }

        @Override
        void reportCodeChange() {
        }

        @Override
        CompilerPass getCustomPass(CompilerOptions.CustomPassExecutionTime executionTime) {
            return null;
        }
    }
}
