//=============================================================================
// $Id$
// $Source$
//=============================================================================

package gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.html.*;

//=============================================================================

class Help
    extends JDialog
    implements ActionListener, HyperlinkListener
{
    public Help(Frame owner, URL contents)
    {
        super(owner, "GoGui: Help");
        m_contents = contents;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Container contentPane = getContentPane();
        contentPane.add(createButtons(), BorderLayout.NORTH);
        m_editorPane = new JEditorPane();
        m_editorPane.setEditable(false);
        m_editorPane.addHyperlinkListener(this);
        JScrollPane scrollPane =
            new JScrollPane(m_editorPane,
                            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(600, 480));
        contentPane.add(scrollPane, BorderLayout.CENTER);
        pack();
        setVisible(true);
        loadURL(m_contents);
        appendHistory(m_contents);
    }

    public void actionPerformed(ActionEvent event)
    {
        String command = event.getActionCommand();
        if (command.equals("back"))
            back();
        else if (command.equals("contents"))
        {
            loadURL(m_contents);
            appendHistory(m_contents);
        }
        else if (command.equals("forward"))
            forward();
    }

    public void hyperlinkUpdate(HyperlinkEvent e)
    {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
        {
            if (e instanceof HTMLFrameHyperlinkEvent)
            {
                HTMLFrameHyperlinkEvent  evt = (HTMLFrameHyperlinkEvent)e;
                HTMLDocument doc = (HTMLDocument)m_editorPane.getDocument();
                doc.processHTMLFrameHyperlinkEvent(evt);
            }
            else
            {
                loadURL(e.getURL());
                appendHistory(e.getURL());
            }
        }
    }

    private int m_historyIndex = -1;
    private JButton m_buttonBack;
    private JButton m_buttonForward;
    private JEditorPane m_editorPane;
    private java.util.List m_history = new ArrayList();
    private URL m_contents;

    private void appendHistory(URL url)
    {
        if (m_historyIndex >= 0 && getHistory(m_historyIndex).equals(url))
            return;
        if (m_historyIndex + 1 < m_history.size())
        {
            if (! getHistory(m_historyIndex + 1).equals(url))
            {
                m_history = m_history.subList(0, m_historyIndex + 1);
                m_history.add(url);
            }
        }
        else
            m_history.add(url);
        ++m_historyIndex;
        if (m_historyIndex > 0)
            m_buttonBack.setEnabled(true);
    }

    private void back()
    {
        assert(m_historyIndex > 0);
        assert(m_historyIndex < m_history.size());
        --m_historyIndex;
        loadURL(getHistory(m_historyIndex));
        if (m_historyIndex == 0)
            m_buttonBack.setEnabled(false);
        m_buttonForward.setEnabled(true);
    }

    private JComponent createButtons()
    {
        JToolBar toolBar = new JToolBar();
        m_buttonBack = createToolBarButton("Left.png", "back");
        m_buttonBack.setEnabled(false);
        toolBar.add(m_buttonBack);
        m_buttonForward = createToolBarButton("Right.png", "forward");
        m_buttonForward.setEnabled(false);
        toolBar.add(m_buttonForward);
        JButton contents = createToolBarButton("Home.png", "contents");
        toolBar.add(contents);
        return toolBar;
    }

    private JButton createToolBarButton(String icon, String command)
    {
        final String prefix = "org/javalobby/icons/20x20png/";
        JButton button;
        URL u = getClass().getClassLoader().getResource(prefix + icon);
        if (u == null)
            // Fallback, shouldn't happen if image exists.
            button = new JButton(icon);
        else
            button = new JButton(new ImageIcon(u));
        button.setActionCommand(command);
        button.addActionListener(this);
        return button;
    }

    private void forward()
    {
        assert(m_historyIndex + 1 < m_history.size());
        ++m_historyIndex;
        loadURL(getHistory(m_historyIndex));
        if (m_historyIndex == m_history.size() - 1)
            m_buttonForward.setEnabled(false);
        m_buttonBack.setEnabled(true);
    }

    private URL getHistory(int index)
    {
        return (URL)m_history.get(index);
    }

    private void loadURL(URL url)
    {
        try
        {
            m_editorPane.setPage(url);
        }
        catch (IOException e)
        {
            String message =
                "Could not load page\n" +
                url.toString() + ":\n" +
                e.getMessage();
            JOptionPane.showMessageDialog(this, message, "GoGui: Error",
                                          JOptionPane.ERROR_MESSAGE);
        }
    }
}

//=============================================================================
