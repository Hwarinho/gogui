//-----------------------------------------------------------------------------
// $Id$
// $Source$
//-----------------------------------------------------------------------------

package gui;

import java.io.*;
import java.lang.*;
import java.util.*;

//-----------------------------------------------------------------------------

class Preferences
{
    public static class Error extends Exception
    {
        public Error(String s)
        {
            super(s);
        }
    }    

    public Preferences()
    {
        setDefaults();
        load();
    }

    public String getAnalyzeCommand()
    {
        return getStringProperty("analyze-command");
    }

    public boolean getAnalyzeCommandEnabled()
    {
        return getIntProperty("analyze-command-enabled") != 0;
    }

    public int getBoardSize()
    {
        return getIntProperty("boardsize");
    }

    public int getGtpShellHistoryMax()
    {
        return getIntProperty("gtpshell-history-max");
    }

    public int getGtpShellHistoryMin()
    {
        return getIntProperty("gtpshell-history-min");
    }

    public void load()
    {
        m_changed = true;
        File file = getFilename();
        if (! file.exists())
            return;
        try
        {
            InputStream in = new FileInputStream(file);
            m_properties.load(in);
        }
        catch (FileNotFoundException e)
        {
            return;
        }
        catch (IOException e)
        {
            return;
        }
        m_changed = false;
    }

    public void save() throws Error
    {
        if (! m_changed)
            return;
        File file = getFilename();
        try
        {
            FileOutputStream out = new FileOutputStream(file);
            m_properties.store(out, "GoGui preferences");
        }
        catch (FileNotFoundException e)
        {
            throw new Error(e.getMessage());
        }
        catch (IOException e)
        {
            throw new Error(e.getMessage());
        }
        m_changed = false;
    }

    public void setAnalyzeCommand(String analyzeCommand)
    {
        setStringProperty("analyze-command", analyzeCommand);
    }

    public void setAnalyzeCommandEnabled(boolean enabled)
    {
        setIntProperty("analyze-command-enabled", enabled ? 1 : 0);
    }

    public void setBoardSize(int boardSize)
    {
        setIntProperty("boardsize", boardSize);
    }

    public void setGtpShellHistoryMax(int value)
    {
        setIntProperty("gtpshell-history-max", value);
    }

    public void setGtpShellHistoryMin(int value)
    {
        setIntProperty("gtpshell-history-min", value);
    }

    /** Properties changed since last load? */
    private boolean m_changed;

    private Properties m_properties = new Properties();

    private boolean contains(String key)
    {
        return m_properties.getProperty(key) != null;
    }

    private File getFilename()
    {
        String home = System.getProperty("user.home");
        File dir = new File(home, ".gogui");
        if (! dir.exists())
            dir.mkdir();
        return new File(dir, "config");        
    }

    private int getIntProperty(String key)
    {
        return Integer.parseInt(getStringProperty(key));
    }

    private String getStringProperty(String key)
    {
        return m_properties.getProperty(key);
    }

    private void setDefaults()
    {
        setStringProperty("analyze-command", "");
        setIntProperty("analyze-command-enabled", 0);
        setIntProperty("boardsize", 19);
        setIntProperty("gtpshell-history-max", 3000);
        setIntProperty("gtpshell-history-min", 2000);
    }

    private void setIntProperty(String key, int value)
    {
        setStringProperty(key, Integer.toString(value));
    }

    private void setStringProperty(String key, String value)
    {
        if (contains(key))
            if (getStringProperty(key).equals(value))
                return;
        m_properties.setProperty(key, value);
        m_changed = true;
    }
}

//-----------------------------------------------------------------------------
