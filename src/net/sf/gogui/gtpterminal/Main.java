//----------------------------------------------------------------------------
// Main.java
//----------------------------------------------------------------------------

package net.sf.gogui.gtpterminal;

import java.io.PrintStream;
import java.util.ArrayList;
import net.sf.gogui.go.GoPoint;
import net.sf.gogui.util.Options;
import net.sf.gogui.util.StringUtil;
import net.sf.gogui.version.Version;

/** GtpTerminal main function. */
public final class Main
{
    /** GtpTerminal main function. */
    public static void main(String[] args)
    {
        try
        {
            String options[] = {
                "config:",
                "help",
                "size:",
                "verbose",
                "version"
            };
            Options opt = Options.parse(args, options);
            if (opt.contains("help"))
            {
                printUsage(System.out);
                return;
            }
            if (opt.contains("version"))
            {
                System.out.println("GtpTerminal " + Version.get());
                return;
            }
            int size = opt.getInteger("size", GoPoint.DEFAULT_SIZE, 1,
                                      GoPoint.MAX_SIZE);
            boolean verbose = opt.contains("verbose");
            ArrayList arguments = opt.getArguments();
            if (arguments.size() != 1)
            {
                printUsage(System.err);
                System.exit(-1);
            }
            String program = (String)arguments.get(0);
            GtpTerminal gtpTerminal = new GtpTerminal(program, size, verbose);
            gtpTerminal.mainLoop();
            gtpTerminal.close();
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
            "Usage: java -jar gtpterminal.jar program\n" +
            "\n" +
            "-config       config file\n" +
            "-help         print help and exit\n" +
            "-size n       board size (default 19)\n" +
            "-verbose      print debug information\n" +
            "-version      print version and exit\n";
        out.print(helpText);
    }
}
