package edu.sumdu.tss.elephant.helper.sql;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

class ScriptReaderTest {

    /*
     * The test was taken from the official repository h2 database:
     * https://github.com/h2database/h2database/blob/master/h2/src/test/org/h2/test/unit/TestScriptReader.java
     */
    @Test
    @DisplayName("Test the Script Reader")
    public void testCommon() {
        String s;
        ScriptReader source;

        s = "$$;$$;";
        source = new ScriptReader(new StringReader(s));
        assertEquals("$$;$$", source.readStatement());
        assertEquals(null, source.readStatement());
        source.close();

        s = "a;';';\";\";--;\n;/*;\n*/;//;\na;";
        source = new ScriptReader(new StringReader(s));
        assertEquals("a", source.readStatement());
        assertEquals("';'", source.readStatement());
        assertEquals("\";\"", source.readStatement());
        assertEquals("--;\n", source.readStatement());
        assertEquals("/*;\n*/", source.readStatement());
        assertEquals("//;\na", source.readStatement());
        assertEquals(null, source.readStatement());
        source.close();

        s = "/\n$ \n\n $';$$a$$ $\n;'";
        source = new ScriptReader(new StringReader(s));
        assertEquals("/\n$ \n\n $';$$a$$ $\n;'", source.readStatement());
        assertEquals(null, source.readStatement());
        source.close();

        s = "//";
        source = new ScriptReader(new StringReader(s));
        assertEquals("//", source.readStatement());
        assertTrue(source.isInsideRemark());
        assertFalse(source.isBlockRemark());
        source.close();

        // check handling of unclosed block comments
        s = "/*xxx";
        source = new ScriptReader(new StringReader(s));
        assertEquals("/*xxx", source.readStatement());
        assertTrue(source.isBlockRemark());
        source.close();

        s = "/*xxx*";
        source = new ScriptReader(new StringReader(s));
        assertEquals("/*xxx*", source.readStatement());
        assertTrue(source.isBlockRemark());
        source.close();

        s = "/*xxx* ";
        source = new ScriptReader(new StringReader(s));
        assertEquals("/*xxx* ", source.readStatement());
        assertTrue(source.isBlockRemark());
        source.close();

        s = "/*xxx/";
        source = new ScriptReader(new StringReader(s));
        assertEquals("/*xxx/", source.readStatement());
        assertTrue(source.isBlockRemark());
        source.close();

        // nested comments
        s = "/*/**/SCRIPT;*/";
        source = new ScriptReader(new StringReader(s));
        assertEquals("/*/**/SCRIPT;*/", source.readStatement());
        assertTrue(source.isBlockRemark());
        source.close();

        s = "/* /* */ SCRIPT; */";
        source = new ScriptReader(new StringReader(s));
        assertEquals("/* /* */ SCRIPT; */", source.readStatement());
        assertTrue(source.isBlockRemark());
        source.close();
    }
}