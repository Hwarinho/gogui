//----------------------------------------------------------------------------
// $Id$
//----------------------------------------------------------------------------

package net.sf.gogui.gtp;

/** In-process GTP client connection to a GtpEngine.
    Better performance than GtpEngineConnection, because the connection
    is not made on the streams level, but the command handler is
    called directly at the GtpEngine.
*/
public class GtpEngineClient
    extends GtpClientBase
{
    public GtpEngineClient(GtpEngine engine)
    {
        m_engine = engine;
    }
    
    public void close()
    {
    }

    public boolean isInterruptSupported()
    {
        // TODO: should forward to a GtpEngine function
        return true;
    }

    public String send(String command) throws GtpError
    {
        GtpCommand cmd = new GtpCommand(command);
        m_engine.handleCommand(cmd);
        return cmd.getResponse().toString();
    }

    public void sendInterrupt() throws GtpError
    {
        m_engine.interruptCommand();
    }

    public void waitForExit()
    {
    }

    private GtpEngine m_engine;
}
