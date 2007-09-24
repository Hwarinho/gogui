//----------------------------------------------------------------------------
// OptionsTest.java
//----------------------------------------------------------------------------

package net.sf.gogui.util;

import java.util.ArrayList;

public final class OptionsTest
    extends junit.framework.TestCase
{
    public static void main(String args[])
    {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite()
    {
        return new junit.framework.TestSuite(OptionsTest.class);
    }

    public void testBasic() throws ErrorMessage
    {
        String specs[] = {
            "flag1",
            "value1:",
            "value2:",
            "flag2",
            "value3:",
            "value4:"
        };
        String args[] = {
            "arg1",
            "-value1", "42",
            "-flag2",
            "-value3", "-9223372036854775807",
            "-value4", "-1",
            "arg2"
        };
        Options opt = new Options(args, specs);
        assertFalse(opt.contains("flag1"));
        assertTrue(opt.contains("flag2"));
        assertTrue(opt.contains("value1"));
        assertFalse(opt.contains("value2"));
        assertEquals(opt.get("value1"), "42");
        assertEquals(opt.getInteger("value1"), 42);
        assertEquals(opt.getInteger("value2", -98), -98);
        assertEquals(opt.getLong("value3"), -9223372036854775807L);
        ArrayList arguments = opt.getArguments();
        assertEquals(arguments.size(), 2);
        assertEquals(arguments.get(0), "arg1");
        assertEquals(arguments.get(1), "arg2");
    }

    public void testStopParsing() throws ErrorMessage
    {
        String specs[] = { "flag1", "value1:", "value2:", "flag2" };
        String args[] = { "-value1", "foo", "--", "-arg1" };
        Options opt = new Options(args, specs);
        ArrayList arguments = opt.getArguments();
        assertEquals(arguments.size(), 1);
        assertEquals(arguments.get(0), "-arg1");
    }
}
