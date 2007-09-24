//----------------------------------------------------------------------------
// Main.java
//----------------------------------------------------------------------------

package net.sf.gogui.gtpdisplay;

import java.io.PrintStream;
import java.util.ArrayList;
import net.sf.gogui.gui.GuiUtil;
import net.sf.gogui.util.Options;
import net.sf.gogui.util.StringUtil;
import net.sf.gogui.version.Version;

/** GtpDisplay main function. */
public final class Main
{
    public static void main(String[] args)
    {
        try
        {
            String options[] = {
                "config:",
                "help",
                "laf:",
                "verbose",
                "version"
            };
            Options opt = Options.parse(args, options);
            if (opt.contains("help"))
            {
                printUsage(System.out);
                System.exit(0);
            }
            if (opt.contains("version"))
            {
                System.out.println("GtpDisplay " + Version.get());
                System.exit(0);
            }
            boolean verbose = opt.contains("verbose");
            String lookAndFeel = opt.get("laf", null);
            ArrayList arguments = opt.getArguments();
            if (arguments.size() > 1)
            {
                printUsage(System.err);
                System.exit(-1);
            }
            String program = null;
            if (arguments.size() == 1)
                program = (String)arguments.get(0);
            GuiUtil.initLookAndFeel(lookAndFeel);
            GtpDisplay gtpDisplay
                = new GtpDisplay(program, verbose);
            gtpDisplay.mainLoop(System.in, System.out);
            gtpDisplay.close();
        }
        catch (Throwable t)
        {
            StringUtil.printException(t);
            System.exit(-1);
        }
    }

    /** Make constructor unavailable; class is for namespace only. */
    private Main()
    {
    }

    private static void printUsage(PrintStream out)
    {
        String helpText =
            "Usage: java -jar gtpdisplay.jar program\n" +
            "\n" +
            "-config       Config file\n" +
            "-help         Print help and exit\n" +
            "-laf          Set Swing look and feel\n" +
            "-verbose      Log GTP stream to stderr\n" +
            "-version      Print version and exit\n";
        out.print(helpText);
    }
}
