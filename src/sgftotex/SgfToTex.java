//----------------------------------------------------------------------------
// $Id$
// $Source$
//----------------------------------------------------------------------------

package sgftotex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Vector;
import utils.FileUtils;
import utils.Options;
import utils.StringUtils;
import version.Version;

//----------------------------------------------------------------------------

class SgfToTex
{
    public static void main(String[] args)
    {
        try
        {
            String options[] = {
                "config:",
                "force",
                "help",
                "pass",
                "title:",
                "version"
            };
            Options opt = Options.parse(args, options);
            if (opt.isSet("help"))
            {
                printUsage(System.out);
                System.exit(0);
            }
            if (opt.isSet("version"))
            {
                System.out.println("SgfToTex " + Version.get());
                System.exit(0);
            }
            boolean usePass = opt.isSet("pass");
            boolean force = opt.isSet("force");
            String title = opt.getString("title", "");
            Vector arguments = opt.getArguments();
            InputStream in;
            OutputStream out;
            if (arguments.size() > 2)
            {
                printUsage(System.err);
                System.exit(-1);
            }
            String inFileName = null;
            if (arguments.size() == 0)
            {
                in = System.in;
                out = System.out;
            }
            else
            {
                inFileName = (String)arguments.get(0);
                File inFile = new File(inFileName);
                in = new FileInputStream(inFile);
                String outFileName;
                if (arguments.size() == 1)
                    outFileName =
                        FileUtils.replaceExtension(inFile, "sgf", "tex");
                else
                    outFileName = (String)arguments.get(1);
                File outFile = new File(outFileName);
                if (outFile.exists() && ! force)
                    throw new Exception("File " + outFile
                                        + " already exists");
                out = new FileOutputStream(outFile);
                if (title.equals(""))
                    title =
                        FileUtils.removeExtension(new File(outFile.getName()),
                                                  "tex");
            }
            convert(in, inFileName, out, title, usePass);
        }
        catch (Throwable t)
        {
            StringUtils.printException(t);
            System.exit(-1);
        }
    }

    private static void convert(InputStream in, String name, OutputStream out,
                                String title, boolean usePass)
        throws sgf.Reader.SgfError
    {
        sgf.Reader reader = new sgf.Reader(in, name, null, 0);
        new latex.Writer(title, out, reader.getGameTree(), usePass);
    }

    private static void printUsage(PrintStream out)
    {
        out.print("Usage: java -jar sgftotex.jar [file.sgf [file.tex]]\n" +
                  "\n" +
                  "-config  config file\n" +
                  "-force   overwrite existing files\n" +
                  "-help    display this help and exit\n" +
                  "-pass    use \\pass command\n" +
                  "-title   use title\n" +
                  "-version print version and exit\n");
    }
}
    
//----------------------------------------------------------------------------
