//----------------------------------------------------------------------------
/*
  Go Modem Protocol

  Go Modem Protocol Specification
  http://www.britgo.org/tech/gmp.html

  Simple version of the protocol:
  Appendix A in Call For Participation to the FJK Computer Go Tournament 2000
  http://sig-gi.c.u-tokyo.ac.jp/fjk2k-go/cfp-english.txt
*/
//----------------------------------------------------------------------------

package net.sf.gogui.gmp;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import net.sf.gogui.util.StringUtil;

/** Static utility functions. */
class Util
{
    public static String format(int i)
    {
        StringBuffer s = new StringBuffer(8);
        for (int k = 0; k < 8; ++k)
        {
            if (i % 2 == 0)
                s.append('0');
            else
                s.append('1');
            i >>= 1;
        }
        s.reverse();
        return s.toString();
    }

    public static void log(String line, boolean verbose)
    {
        if (! verbose)
            return;
        System.err.println("gmp: " + line);
        System.err.flush();
    }

}

/** GMP command. */
class Cmd
{
    public static final int OK = 0;

    public static final int DENY = 1;

    public static final int NEWGAME = 2;

    public static final int QUERY = 3;

    public static final int ANSWER = 4;

    public static final int MOVE = 5;

    public static final int UNDO = 6;

    public static final int EXTENDED = 7;

    public static final int MASK_MOVE_COLOR = 0x200;

    public static final int MASK_MOVE_POINT = 0x1ff;

    public static final int QUERY_GAME = 0;

    public static final int QUERY_BUFSIZE = 1;

    public static final int QUERY_VERSION = 2;

    public static final int QUERY_NUMSTONES = 3;

    public static final int QUERY_TIMEBLACK = 4;

    public static final int QUERY_TIMEWHITE = 5;

    public static final int QUERY_CHARSET = 6;

    public static final int QUERY_RULES = 7;

    public static final int QUERY_HANDICAP = 8;

    public static final int QUERY_SIZE = 9;

    public static final int QUERY_TIMELIMIT = 10;

    public static final int QUERY_COLOR = 11;

    public static final int QUERY_WHO = 12;

    public int m_cmd;

    public int m_val;

    public Cmd(int cmd, int val)
    {
        m_cmd = cmd;
        m_val = val;
    }

    public static String answerValToString(int val, int query)
    {
        boolean zeroMeansUnknown = true;
        switch (query)
        {
        case QUERY_GAME:
            if (val == 1)
                return "GO";
            if (val == 2)
                return "CHESS";
            if (val == 3)
                return "OTHELLO";
            break;
        case QUERY_BUFSIZE:
            return Integer.toString(4 + val * 16) + " BYTES";
        case QUERY_VERSION:
            zeroMeansUnknown = false;
            break;
        case QUERY_CHARSET:
            if (val == 1)
                return "ASCII";
            if (val == 2)
                return "JAPANESE";
            break;
        case QUERY_RULES:
            if (val == 1)
                return "JAPANESE";
            if (val == 2)
                return "CHINESE (SST)";
            break;
        case QUERY_HANDICAP:
            if (val == 1)
                return "NONE";
            break;
        case QUERY_COLOR:
            if (val == 1)
                return "WHITE";
            if (val == 2)
                return "BLACK";
            break;
        case QUERY_WHO:
            if (val == 1)
                return "NEMESIS";
            if (val == 2)
                return "MANY FACES OF GO";
            if (val == 3)
                return "SMART GO BOARD";
            if (val == 4)
                return "GOLIATH";
            if (val == 5)
                return "GO INTELLECT";
            if (val == 6)
                return "STAR OF POLAND";
            break;
        default:
            break;
        }
        if (val == 0 && zeroMeansUnknown)
            return "UNKNOWN";
        return Integer.toString(val);
    }

    public static String cmdToString(int cmd)
    {
        switch (cmd)
        {
        case OK:
            return "OK";
        case DENY:
            return "DENY";
        case NEWGAME:
            return "NEWGAME";
        case QUERY:
            return "QUERY";
        case ANSWER:
            return "ANSWER";
        case MOVE:
            return "MOVE";
        case UNDO:
            return "UNDO";
        case EXTENDED:
            return "EXTENDED";
        default:
            return Integer.toString(cmd);
        }
    }

    public boolean equals(Object object)
    {
        if (this == object)
            return true;
        if (object instanceof Cmd)
        {
            Cmd cmd = (Cmd)object;
            return (cmd.m_cmd == m_cmd && cmd.m_val == m_val);
        }
        return false;
    }

    public int hashCode()
    {
        return (m_cmd << 16) | m_val;
    }

    public static String moveValToString(int val, int size)
    {
        StringBuffer result = new StringBuffer(16);
        if ((val & MASK_MOVE_COLOR) == 0)
            result.append("B ");
        else
            result.append("W ");
        if (size == 0)
            result.append(val & MASK_MOVE_POINT);
        else
        {
            Gmp.Move move = parseMove(val, size);
            if (move.m_x < 0 || move.m_y < 0)
                result.append("PASS");
            else
            {
                int x = 'A' + move.m_x;
                if (x >= 'I')
                    ++x;
                char xChar = (char)(x);
                result.append(xChar);
                result.append(move.m_y + 1);
            }
        }
        return result.toString();
    }

    public static Gmp.Move parseMove(int val, int size)
    {
        Gmp.Move move = new Gmp.Move();
        move.m_isBlack = ((val & MASK_MOVE_COLOR) == 0);
        val &= MASK_MOVE_POINT;
        if (val == 0)
        {
            move.m_x = -1;
            move.m_y = -1;
        }
        else
        {
            val -= 1;
            move.m_x = val % size;
            move.m_y = val / size;
        }
        return move;
    }

    public static String queryValToString(int val)
    {
        switch (val)
        {
        case QUERY_GAME:
            return "GAME";
        case QUERY_BUFSIZE:
            return "BUFSIZE";
        case QUERY_VERSION:
            return "VERSION";
        case QUERY_NUMSTONES:
            return "NUMSTONES";
        case QUERY_TIMEBLACK:
            return "TIMEBLACK";
        case QUERY_TIMEWHITE:
            return "TIMEWHITE";
        case QUERY_CHARSET:
            return "CHARSET";
        case QUERY_RULES:
            return "RULES";
        case QUERY_HANDICAP:
            return "HANDICAP";
        case QUERY_SIZE:
            return "SIZE";
        case QUERY_TIMELIMIT:
            return "TIMELIMIT";
        case QUERY_COLOR:
            return "COLOR";
        case QUERY_WHO:
            return "WHO";
        default:
            return "? (" + Integer.toString(val) + ")";
        }
    }

    public String toString(int size, int lastQuery)
    {
        StringBuffer buffer = new StringBuffer(32);
        buffer.append(Cmd.cmdToString(m_cmd));
        switch (m_cmd)
        {
        case OK:
        case DENY:
        case NEWGAME:
            break;
        case QUERY:
            buffer.append(' ');
            buffer.append(Cmd.queryValToString(m_val));
            break;
        case ANSWER:
            buffer.append(' ');
            buffer.append(Cmd.answerValToString(m_val, lastQuery));
            break;
        case MOVE:
            buffer.append(' ');
            buffer.append(Cmd.moveValToString(m_val, size));
            break;
        default:
            buffer.append(' ');
            buffer.append(m_val);
            break;
        }
        return buffer.toString();
    }
};

/** Thread handling sending and resending of packets. */
class WriteThread extends Thread
{
    public WriteThread(OutputStream out, boolean verbose)
    {
        m_out = out;
        m_verbose = verbose;
    }

    public void run()
    {
        try
        {
            synchronized (m_mutex)
            {
                Random random = new Random();
                while (true)
                {
                    if (m_sendInProgress)
                    {
                        long timeout =
                            20000 + (long)(random.nextDouble() * 10000);
                        m_mutex.wait(timeout);
                    }
                    else
                        m_mutex.wait();
                    if (m_sendInProgress)
                        writePacket();
                }
            }
        }
        catch (InterruptedException e)
        {
            return;
        }
        catch (Throwable e)
        {
            StringUtil.printException(e);
        }
    }

    public void resend()
    {
        synchronized (m_mutex)
        {
            m_mutex.notifyAll();
        }
    }

    public void sendPacket(byte[] packet, boolean onlyOnce)
    {
        synchronized (m_mutex)
        {
            m_packet = packet;
            if (onlyOnce)
            {
                writePacket();
                return;
            }
            m_sendInProgress = true;
            m_mutex.notifyAll();
        }
    }

    /** Send talk.
        Non-ASCII characters in the range [4..126] are replaced by '?'.
        @param talk Talk text.
    */
    public boolean sendTalk(String talk)
    {
        Util.log("send talk: '" + talk + "'", m_verbose);
        synchronized (m_mutex)
        {
            int size = talk.length();
            byte buffer[] = new byte[size];
            for (int i = 0; i < size; ++i)
            {
                char c = talk.charAt(i);
                buffer[i] = (byte)((c > 3 && c < 127) ? c : '?');
            }
            try
            {
                m_out.write(buffer);
                m_out.flush();
            }
            catch (IOException e)
            {
                return false;
            }
            StringBuffer logText = new StringBuffer(256);
            logText.append("send");
            for (int i = 0; i < buffer.length; ++i)
            {
                logText.append(' ');
                logText.append(Util.format(buffer[i]));
            }
            Util.log(logText.toString(), m_verbose);
            return true;
        }
    }

    public void stopSend()
    {
        synchronized (m_mutex)
        {
            m_sendInProgress = false;
            m_mutex.notifyAll();
        }
    }

    private boolean m_sendInProgress;

    private final boolean m_verbose;

    private byte[] m_packet;

    private final Object m_mutex = new Object();

    private final OutputStream m_out;

    private void writePacket()
    {
        Util.log("send "
                 + Util.format(m_packet[0]) + " "
                 + Util.format(m_packet[1]) + " "
                 + Util.format(m_packet[2]) + " "
                 + Util.format(m_packet[3]), m_verbose);
        try
        {
            m_out.write(m_packet);
            m_out.flush();
        }
        catch (IOException e)
        {
            Util.log("IOException", true);
        }
    }
}

/** Handles incoming packets and allows to send commands. */
class MainThread
    extends Thread
{
    /** Result returned by MainThread.waitCmd. */
    public static class WaitResult
    {
        public boolean m_success;

        public String m_response;

        public int m_val;
    }

    public MainThread(InputStream in, OutputStream out, int size,
                      int colorIndex, boolean simple, boolean verbose)
    {
        assert size >= 1;
        assert size <= 22;
        assert colorIndex >= 0;
        assert colorIndex <= 2;
        m_verbose = verbose;
        m_in = in;
        m_size = size;
        m_colorIndex = colorIndex;
        m_simple = simple;
        m_writeThread = new WriteThread(out, verbose);
        m_writeThread.start();
    }

    /** Get all talk text received so far and clear talk buffer. */
    public String getTalk()
    {
        synchronized (m_mutex)
        {
            String result = m_talkBuffer.toString();
            m_talkBuffer.setLength(0);
            return result;
        }
    }

    public void interruptCommand()
    {
        synchronized (m_mutex)
        {
            m_state = STATE_INTERRUPTED;
            m_mutex.notifyAll();
        }
    }

    public boolean queue(StringBuffer response)
    {
        synchronized (m_mutex)
        {
            int size = m_cmdQueue.size();
            for (int i = 0; i < size; ++i)
            {
                Cmd cmd = (Cmd)m_cmdQueue.get(i);
                response.append(cmd.toString(m_size, m_lastQuery));
                response.append('\n');
            }
            return true;
        }
    }

    public void run()
    {
        try
        {
            byte buffer[] = new byte[16];
            while (true)
            {
                int n = m_in.read(buffer);
                synchronized (m_mutex)
                {
                    if (n < 0)
                        break;
                    for (int i = 0; i < n; ++i)
                    {
                        int b = buffer[i];
                        if (b < 0)
                            b += 256;
                        Util.log("recv " + Util.format(b), m_verbose);
                        handleByte(b);
                    }
                }
            }
        }
        catch (Throwable e)
        {
            StringUtil.printException(e);
        }
        synchronized (m_mutex)
        {
            m_state = STATE_DISCONNECTED;
        }
        Util.log("input stream was closed", m_verbose);
        m_writeThread.interrupt();
    }

    public boolean send(Cmd cmd, StringBuffer response)
    {
        synchronized (m_mutex)
        {
            while (m_state == STATE_WAIT_ANSWER_OK
                   || m_state == STATE_WAIT_ANSWER)
            {
                try
                {
                    sleep(1000);
                }
                catch (InterruptedException ignored)
                {
                }
            }
            if (m_state == STATE_WAIT_OK)
            {
                response.append("Command in progress");
                return false;
            }
            if (m_state == STATE_DISCONNECTED)
            {
                response.append("GMP connection broken");
                return false;
            }
            if (! m_cmdQueue.isEmpty())
            {
                Cmd stackCmd = (Cmd)m_cmdQueue.get(0);
                if (! stackCmd.equals(cmd))
                {
                    response.append("Received ");
                    response.append(stackCmd.toString(m_size, m_lastQuery));
                    return false;
                }
                m_cmdQueue.remove(0);
                return true;
            }
            sendCmd(cmd.m_cmd, cmd.m_val);
            while (true)
            {
                try
                {
                    m_mutex.wait();
                }
                catch (InterruptedException e)
                {
                    System.err.println("Interrupted");
                }
                switch (m_state)
                {
                case STATE_IDLE:
                    return true;
                case STATE_DENY:
                    response.append("Command denied");
                    m_state = STATE_IDLE;
                    return false;
                case STATE_WAIT_OK:
                    continue;
                case STATE_INTERRUPTED:
                    // GMP connection cannot be used anymore after sending
                    // was interrupted
                    response.append("GMP connection closed");
                    m_state = STATE_DISCONNECTED;
                    return false;
                case STATE_DISCONNECTED:
                    response.append("GMP connection broken");
                    return false;
                default:
                    return false;
                }
            }
        }
    }

    public boolean sendTalk(String text)
    {
        return m_writeThread.sendTalk(text);
    }

    public WaitResult waitCmd(int cmd, int valMask, int valCondition)
    {
        synchronized (m_mutex)
        {
            WaitResult result = new WaitResult();
            result.m_success = false;
            while (true)
            {
                if (! m_cmdQueue.isEmpty())
                {
                    Cmd stackCmd = (Cmd)m_cmdQueue.get(0);
                    if (stackCmd.m_cmd != cmd
                        || ((stackCmd.m_val & valMask) != valCondition))
                    {
                        result.m_response =
                            ("Received " +
                             stackCmd.toString(m_size, m_lastQuery));
                        return result;
                    }
                    result.m_success = true;
                    result.m_val = stackCmd.m_val;
                    m_cmdQueue.remove(0);
                    return result;
                }
                if (m_state == STATE_DISCONNECTED)
                {
                    result.m_response = "GMP connection broken";
                    return result;
                }
                try
                {
                    Util.log("Waiting for " + Cmd.cmdToString(cmd) + "...",
                             m_verbose);
                    m_mutex.wait();
                    if (m_state == STATE_INTERRUPTED)
                    {
                        result.m_response = "Interrupted";
                        return result;
                    }
                }
                catch (InterruptedException e)
                {
                    System.err.println("Interrupted");
                }
            }
        }
    }

    private static final int STATE_IDLE = 0;

    private static final int STATE_DISCONNECTED = 1;

    private static final int STATE_WAIT_OK = 2;

    private static final int STATE_DENY = 3;

    private static final int STATE_INTERRUPTED = 4;

    private static final int STATE_WAIT_ANSWER_OK = 5;

    private static final int STATE_WAIT_ANSWER = 6;

    private final boolean m_verbose;

    private boolean m_hisLastSeq;

    private boolean m_myLastSeq;

    private final boolean m_simple;

    private int m_lastQuery;

    private int m_state = STATE_IDLE;

    private final int m_colorIndex;

    private int m_pending;

    private final int m_size;

    private int m_queryCount;

    private static int s_queries[] =
    {
        Cmd.QUERY_COLOR,
        Cmd.QUERY_HANDICAP
    };

    private int[] m_inBuffer = new int[4];

    private final InputStream m_in;

    private final Object m_mutex = new Object();

    private final StringBuffer m_talkBuffer = new StringBuffer();

    private final StringBuffer m_talkLine = new StringBuffer();

    private final ArrayList m_cmdQueue = new ArrayList(32);

    private final WriteThread m_writeThread;

    private void answerQuery(int val)
    {
        int answer = 0;
        m_lastQuery = val;
        if (val == Cmd.QUERY_COLOR)
            answer = m_colorIndex;
        else if (val == Cmd.QUERY_SIZE)
            answer = m_size;
        else if (val == Cmd.QUERY_HANDICAP)
            answer = 1;
        sendCmd(Cmd.ANSWER, answer);
    }

    private boolean checkChecksum()
    {
        int b0 = m_inBuffer[0];
        int b2 = m_inBuffer[2];
        int b3 = m_inBuffer[3];
        int checksum = getChecksum(b0, b2, b3);
        return (checksum == m_inBuffer[1]);
    }

    private int getChecksum(int b0, int b2, int b3)
    {
        return ((b0 + b2 + b3) | 0x80) & 0xff;
    }

    private boolean getAck()
    {
        int ack = (m_inBuffer[0] & 2);
        return (ack != 0);
    }

    private Cmd getCmd()
    {
        int cmd = (m_inBuffer[2] >> 4) & 0x7;
        int val = ((m_inBuffer[2] & 0x07) << 7) | (m_inBuffer[3] & 0x7f);
        return new Cmd(cmd, val);
    }

    private boolean getSeq()
    {
        int seq = (m_inBuffer[0] & 1);
        return (seq != 0);
    }

    private void handleByte(int b)
    {
        // Talk character
        if (b > 3 && b < 128)
        {
            char c = (char)b;
            if (c == '\r')
            {
                Util.log("talk char '\\r'", m_verbose);
                return;
            }
            if (c == '\n')
            {
                Util.log("talk char '\\n'", m_verbose);
                Util.log("talk: " + m_talkLine, m_verbose);
                m_talkLine.setLength(0);
                return;
            }
            Util.log("talk '" + c + "'", m_verbose);
            m_talkLine.append(c);
            m_talkBuffer.append(c);
            return;
        }
        // Start byte
        if (b < 4)
        {
            if (m_pending > 0)
                Util.log("new start byte. discarding old bytes", m_verbose);
            m_inBuffer[0] = b;
            m_pending = 3;
            return;
        }
        // Other command byte
        if (m_pending > 0)
        {
            int index = 4 - m_pending;
            assert index > 0;
            assert index < 4;
            m_inBuffer[index] = b;
            --m_pending;
            if (m_pending == 0)
            {
                if (! checkChecksum())
                {
                    Util.log("bad checksum", m_verbose);
                    return;
                }
                handlePacket();
            }
            return;
        }
        Util.log("discarding command byte", m_verbose);
    }

    private void handleCmd(Cmd cmd)
    {
        Util.log("received " + cmd.toString(m_size, m_lastQuery), m_verbose);
        if (cmd.m_cmd == Cmd.QUERY)
            answerQuery(cmd.m_val);
        else if (cmd.m_cmd == Cmd.ANSWER)
        {
            if (m_queryCount < s_queries.length - 1)
            {
                ++m_queryCount;
                int val = s_queries[m_queryCount];
                m_lastQuery = val;
                sendCmd(Cmd.QUERY, val);
            }
            else
            {
                sendOk();
                m_state = STATE_IDLE;
                m_mutex.notifyAll();
            }
        }
        else if (cmd.m_cmd == Cmd.NEWGAME && m_simple)
        {
            m_cmdQueue.add(cmd);
            m_queryCount = 0;
            int val = s_queries[m_queryCount];
            m_lastQuery = val;
            sendCmd(Cmd.QUERY, val);
            m_mutex.notifyAll();
        }
        else
        {
            sendOk();
            if (cmd.m_cmd == Cmd.DENY)
            {
                if (m_state == STATE_WAIT_ANSWER_OK)
                    m_state = STATE_IDLE;
                else
                {
                    m_state = STATE_DENY;
                    m_mutex.notifyAll();
                }
            }
            else
            {
                m_cmdQueue.add(cmd);
                m_state = STATE_IDLE;
                m_mutex.notifyAll();
            }
        }
    }

    private void handlePacket()
    {
        Cmd cmd = getCmd();
        boolean seq = getSeq();
        boolean ack = getAck();
        if (m_state == STATE_WAIT_OK
            || m_state == STATE_WAIT_ANSWER_OK
            || m_state == STATE_WAIT_ANSWER)
        {
            if (cmd.m_cmd == Cmd.OK)
            {
                if (ack != m_myLastSeq)
                {
                    Util.log("sequence error", m_verbose);
                    return;
                }
                Util.log("received OK", m_verbose);
                m_state = STATE_IDLE;
                m_writeThread.stopSend();
                m_mutex.notifyAll();
                return;
            }
            if (seq == m_hisLastSeq)
            {
                Util.log("old cmd. resending OK", m_verbose);
                sendOk();
                return;
            }
            if (ack == m_myLastSeq)
            {
                m_state = STATE_IDLE;
                m_writeThread.stopSend();
                m_hisLastSeq = seq;
                handleCmd(cmd);
                return;
            }
            /* Actually GMP requires to abandon command on conflict,
               but since we might have sent it out repeatedly already,
               the opponent could have detected the conflict and accepted
               the resent command, so it's easier to ignore the conflict.
            */
            Util.log("ignore conflict", m_verbose);
            m_writeThread.resend();
        }
        else
        {
            assert m_state == STATE_IDLE;
            if (cmd.m_cmd == Cmd.OK)
            {
                Util.log("ignoring unexpected OK", m_verbose);
                return;
            }
            if (ack != m_myLastSeq)
            {
                Util.log("ignoring old cmd", m_verbose);
                return;
            }
            if (seq == m_hisLastSeq)
            {
                Util.log("old cmd. resending OK", m_verbose);
                sendOk();
                return;
            }
            m_hisLastSeq = seq;
            handleCmd(cmd);
        }
    }

    private byte makeCmdByte1(int cmd, int val)
    {
        val = val & 0x000003FF;
        return (byte)(0x0080 | (cmd << 4) | (val >> 7));
    }

    private byte makeCmdByte2(int val)
    {
        return (byte)(0x0080 | (val & 0x0000007F));
    }

    private boolean sendCmd(int cmd, int val)
    {
        Util.log("send " + (new Cmd(cmd, val)).toString(m_size, m_lastQuery),
                 m_verbose);
        boolean isOkCmd = (cmd == Cmd.OK);
        if (! isOkCmd)
            m_myLastSeq = ! m_myLastSeq;
        byte packet[] = new byte[4];
        packet[0] = (byte)(m_myLastSeq ? 1 : 0);
        packet[0] |= (byte)(m_hisLastSeq ? 2 : 0);
        packet[2] = makeCmdByte1(cmd, val);
        packet[3] = makeCmdByte2(val);
        setChecksum(packet);
        m_writeThread.sendPacket(packet, isOkCmd);
        if (! isOkCmd)
        {
            if (cmd == Cmd.ANSWER)
                m_state = STATE_WAIT_ANSWER_OK;
            else if (cmd == Cmd.QUERY)
                m_state = STATE_WAIT_ANSWER;
            else
                m_state = STATE_WAIT_OK;
        }
        return true;
    }

    private boolean sendOk()
    {
        return sendCmd(Cmd.OK, 0);
    }

    private void setChecksum(byte[] packet)
    {
        int b0 = packet[0];
        int b2 = packet[2];
        int b3 = packet[3];
        int checksum = getChecksum(b0, b2, b3);
        packet[1] = (byte)(0x0080 | checksum);
    }
}

/** GMP connection.
    This class is final because it starts a thread in its constructor which
    might conflict with subclassing because the subclass constructor will
    be called after the thread is started.
*/
public final class Gmp
{
    /** Result returned by Gmp.waitMove. */
    public static class Move
    {
        public boolean m_isBlack;

        public int m_x;

        public int m_y;
    }

    /** Create a GMP connection.
        @param input Input stream
        @param output Output stream
        @param size Board size 1-22
        Gmp supports only sizes up to 22 (number of bits in MOVE cmd)
        @param simple Use simple version of GMP
        @param colorIndex Color computer color on your side
        0=unknown, 1=white, 2=black
        @param verbose Log everything sent and received to stderr
    */
    public Gmp(InputStream input, OutputStream output, int size,
               int colorIndex, boolean simple, boolean verbose)
    {
        m_size = size;
        m_mainThread =
            new MainThread(input, output, size, colorIndex, simple, verbose);
        m_mainThread.start();
    }

    /** Get all talk text received so far and clear talk buffer. */
    public String getTalk()
    {
        return m_mainThread.getTalk();
    }

    /** Interrupt waiting for a command or response.
        This function can be called from other threads to interrupt the
        blocking functions that wait for a command or response.
        After calling this function the GMP connection cannot be used anymore
        and all functions will return a broken connection error.
    */
    public void interruptCommand()
    {
        m_mainThread.interruptCommand();
    }

    /** Send a new game command and wait for acknowledge.
        @param size Board size, must be the same as the one this Gmp was
        constructed with.
        @param response Will contain error message, if function fails.
        @return true, if command was acknowledged.
    */
    public boolean newGame(int size, StringBuffer response)
    {
        if (size != m_size)
        {
            response.append("Board size must be ");
            response.append(m_size);
            return false;
        }
        return m_mainThread.send(new Cmd(Cmd.NEWGAME, 0), response);
    }

    /** Send a move command and wait for acknowledge.
        @param isBlack true, if color is black.
        @param x x-coordinate, starting with 0, -1 for pass move.
        @param y y-coordinate, starting with 0, -1 for pass move.
        @param response Will contain error message, if function fails.
        @return true, if command was acknowledged.
    */
    public boolean play(boolean isBlack, int x, int y, StringBuffer response)
    {
        if (x >= m_size || y >= m_size || x < -1 || y < -1)
        {
            response.append("Invalid coordinates");
            return false;
        }
        int val = (isBlack ? 0 : Cmd.MASK_MOVE_COLOR);
        if (x >= 0 && y >= 0)
            val |= (1 + x + y * m_size);
        return m_mainThread.send(new Cmd(Cmd.MOVE, val), response);
    }

    /** Get a string showing queued commands.
        The string contains one queued (and already acknowledged) command per
        line.
    */
    public boolean queue(StringBuffer response)
    {
        return m_mainThread.queue(response);
    }

    /** Send talk text. */
    public boolean sendTalk(String text)
    {
        return m_mainThread.sendTalk(text);
    }

    /** Send an undo command (one move) and wait for acknowledge.
        @param response Will contain error message, if function fails.
        @return true, if command was acknowledged.
    */
    public boolean undo(StringBuffer response)
    {
        return m_mainThread.send(new Cmd(Cmd.UNDO, 1), response);
    }

    /** Wait for a move command and acknowledge it.
        Returns immediately, if a move was already received and
        queued, or if a conflicting command is/was received.
        @param isBlack true, if waiting for a black move; only move commands
        with the correct color will be acknowledged.
        @param response Will contain error message, if function fails.
        @return true, if command was acknowledged.
    */
    public Move waitMove(boolean isBlack, StringBuffer response)
    {
        MainThread.WaitResult result;
        int valCondition = (isBlack ? 0 : Cmd.MASK_MOVE_COLOR);
        result = m_mainThread.waitCmd(Cmd.MOVE, Cmd.MASK_MOVE_COLOR,
                                      valCondition);
        if (result.m_response != null)
            response.append(result.m_response);
        if (! result.m_success)
            return null;
        return Cmd.parseMove(result.m_val, m_size);
    }

    /** Wait for a new game command and acknowledge it.
        Returns immediately, if a new game was already received and
        queued, or if a conflicting command is/was received.
        @param size The expected board size; only new game commands with
        this size will be acknowledged.
        @param response Will contain error message, if function fails.
        @return true, if command was acknowledged.
    */
    public boolean waitNewGame(int size, StringBuffer response)
    {
        if (size != m_size)
        {
            response.append("Board size must be ");
            response.append(m_size);
            return false;
        }
        MainThread.WaitResult result;
        result = m_mainThread.waitCmd(Cmd.NEWGAME, 0, 0);
        if (result.m_response != null)
            response.append(result.m_response);
        return result.m_success;
    }

    private final int m_size;

    private final MainThread m_mainThread;
}
