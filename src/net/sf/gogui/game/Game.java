//----------------------------------------------------------------------------
// Game.java
//----------------------------------------------------------------------------

package net.sf.gogui.game;

import net.sf.gogui.go.ConstBoard;
import net.sf.gogui.go.ConstPointList;
import net.sf.gogui.go.Board;
import net.sf.gogui.go.GoColor;
import net.sf.gogui.go.GoPoint;
import net.sf.gogui.go.Komi;
import net.sf.gogui.go.Move;
import net.sf.gogui.util.ObjectUtil;

/** Manages a tree, board, current node and clock. */
public class Game
    implements ConstGame
{
    public Game(int boardSize)
    {
        m_board = new Board(boardSize);
        m_clock = new Clock();
        init(boardSize, null, null, "", null);
    }

    public Game(int boardSize, Komi komi, ConstPointList handicap,
                String rules, TimeSettings timeSettings)
    {
        m_board = new Board(boardSize);
        m_clock = new Clock();
        init(boardSize, komi, handicap, rules, timeSettings);
    }

    public Game(GameTree tree)
    {
        int boardSize = tree.getBoardSize();
        m_board = new Board(boardSize);
        m_clock = new Clock();
        init(tree);
    }

    /** Add a mark property to current node. */
    public void addMarked(GoPoint point, MarkType type)
    {
        m_current.addMarked(point, type);
        setModified();
    }

    /** Clear modified flag.
        Can be used for instance after game was saved.
        @see #isModified()
    */
    public void clearModified()
    {
        m_modified = false;
    }

    /** Append new empty node and make it current node.
        Can be use for instance to prepare for setup stones if current node
        contains a move.
    */
    public void createNewChild()
    {
        Node node = new Node();
        m_current.append(node);
        m_current = node;
        setModified();
    }

    public ConstBoard getBoard()
    {
        return m_board;
    }

    public ConstClock getClock()
    {
        return m_clock;
    }

    public ConstNode getCurrentNode()
    {
        return m_current;
    }

    public ConstGameInformation getGameInformation(ConstNode node)
    {
        return m_tree.getGameInformationConst(node);
    }

    public ConstNode getGameInformationNode()
    {
        return m_tree.getGameInformationNode(m_current);
    }

    public int getMoveNumber()
    {
        return NodeUtil.getMoveNumber(getCurrentNode());
    }

    public ConstNode getRoot()
    {
        return m_tree.getRoot();
    }

    public int getSize()
    {
        return m_board.getSize();
    }

    public GoColor getToMove()
    {
        return m_board.getToMove();
    }

    public ConstGameTree getTree()
    {
        return m_tree;
    }

    public void gotoNode(ConstNode node)
    {
        assert node != null;
        assert NodeUtil.getRoot(node) == getRoot();
        m_current = (Node)node;
        updateBoard();
    }

    public void haltClock()
    {
        m_clock.halt();
    }

    public final void init(int boardSize, Komi komi, ConstPointList handicap,
                           String rules, TimeSettings timeSettings)
    {
        m_tree = new GameTree(boardSize, komi, handicap, rules, timeSettings);
        m_current = m_tree.getRoot();
        updateBoard();
        updateClock();
        m_clock.reset();
        m_clock.halt();
        m_modified = false;
    }

    public final void init(GameTree tree)
    {
        m_tree = tree;
        m_current = m_tree.getRoot();
        updateBoard();
        updateClock();
        m_clock.reset();
        m_clock.halt();
        m_modified = false;
    }

    /** Check if game was modified.
        @return true, if game was mofified since constructor or last call to
        one of the init() functions or to clearModified().
    */
    public boolean isModified()
    {
        return m_modified;
    }

    public void keepOnlyMainVariation()
    {
        m_tree.keepOnlyMainVariation();
        setModified();
    }

    public void keepOnlyPosition()
    {
        ConstGameInformation info = getGameInformation(m_current);
        m_tree = NodeUtil.makeTreeFromPosition(info, m_board);
        m_board.init(m_board.getSize());
        m_current = m_tree.getRoot();
        updateBoard();
        setModified();
    }

    /** Make current node the main variation. */
    public void makeMainVariation()
    {
        NodeUtil.makeMainVariation(m_current);
        setModified();
    }

    public void play(Move move)
    {
        m_clock.stopMove();
        Node node = new Node(move);
        if (m_clock.isInitialized())
        {
            assert ! m_clock.isRunning();
            GoColor color = move.getColor();
            // Round time to seconds
            long timeLeft = m_clock.getTimeLeft(color) / 1000L;
            node.setTimeLeft(color, (double)timeLeft);
            if (m_clock.isInByoyomi(color))
                node.setMovesLeft(color, m_clock.getMovesLeft(color));
        }
        m_current.append(node);
        m_current = node;
        updateBoard();
        setModified();
        m_clock.startMove(getToMove());
    }

    /** Remove a mark property from current node. */
    public void removeMarked(GoPoint point, MarkType type)
    {
        m_current.removeMarked(point, type);
        setModified();
    }

    public void resetClock()
    {
        m_clock.reset();
    }

    public void restoreClock()
    {
        if (! getClock().isInitialized())
            return;
        NodeUtil.restoreClock(getCurrentNode(), m_clock);
    }

    public void resumeClock()
    {
        m_clock.resume();
    }

    /** Set clock listener.
        If the clock has a listener, the clock should be stopped with
        haltClock() if it is no longer used, otherwise the timer thread can
        keep an application from terminating.
    */
    public void setClockListener(Clock.Listener listener)
    {
        m_clock.setListener(listener);
    }

    /** Set comment in current node. */
    public void setComment(String comment)
    {
        setComment(comment, m_current);
    }

    public void setComment(String comment, ConstNode node)
    {
        assert NodeUtil.getRoot(node) == getRoot();
        if (! ObjectUtil.equals(comment, node.getComment()))
            setModified();
        ((Node)node).setComment(comment);
    }

    public void setGameInformation(ConstGameInformation info, ConstNode node)
    {
        assert NodeUtil.getRoot(node) == getRoot();
        ((Node)node).createGameInformation();
        if (! ((Node)node).getGameInformation().equals(info))
        {
            ((Node)node).getGameInformation().copyFrom(info);
            updateClock();
            setModified();
        }
    }

    public void setKomi(Komi komi)
    {
        Node node = m_tree.getGameInformationNode(m_current);
        GameInformation info = node.getGameInformation();
        info.setKomi(komi);
        setGameInformation(info, node); // updates m_modified
    }

    /** Set label in current node. */
    public void setLabel(GoPoint point, String value)
    {
        if (! ObjectUtil.equals(value, m_current.getLabel(point)))
            setModified();
        m_current.setLabel(point, value);
    }

    public void setPlayer(GoColor c, String name)
    {
        Node node = m_tree.getGameInformationNode(m_current);
        GameInformation info = node.getGameInformation();
        info.setPlayer(c, name);
        setGameInformation(info, node); // updates m_modified
    }

    public void setResult(String result)
    {
        Node node = m_tree.getGameInformationNode(m_current);
        GameInformation info = node.getGameInformation();
        info.setResult(result);
        setGameInformation(info, node); // updates m_modified
    }

    public void setToMove(GoColor color)
    {
        assert color != null;
        assert ! color.equals(GoColor.EMPTY);
        if (! ObjectUtil.equals(color, m_current.getPlayer())
            || color.equals(m_board.getToMove()))
            setModified();
        m_current.setPlayer(color);
        updateBoard();
    }

    public void setup(GoPoint p, GoColor c)
    {
        assert p != null;
        m_current.removeSetup(p);
        Node father = m_current.getFather();
        if (father != null)
        {
            m_boardUpdater.update(getTree(), father, m_board);
            GoColor oldColor = m_board.getColor(p);
            if (oldColor == c)
            {
                updateBoard();
                return;
            }
        }
        if (c != GoColor.EMPTY || father != null)
            m_current.addStone(c, p);
        setModified();
        updateBoard();
    }

    public void startClock()
    {
        m_clock.startMove(getToMove());
    }

    /** Truncate current node and subtree.
        New current node is the father of the old current node.
    */
    public void truncate()
    {
        Node father = m_current.getFather();
        assert father != null;
        Node oldCurrentNode = m_current;
        m_current = father;
        m_current.removeChild(oldCurrentNode);
        setModified();
    }

    /** Remove children of currentNode. */
    public void truncateChildren()
    {
        NodeUtil.truncateChildren(m_current);
        setModified();
    }

    /** See #isModified() */
    private boolean m_modified;

    private final Board m_board;

    private final BoardUpdater m_boardUpdater = new BoardUpdater();

    private GameTree m_tree;

    private Node m_current;

    private final Clock m_clock;

    private void setModified()
    {
        m_modified = true;
    }

    private void updateBoard()
    {
        m_boardUpdater.update(m_tree, m_current, m_board);
    }

    private void updateClock()
    {
        ConstNode node = getGameInformationNode();
        ConstGameInformation info = node.getGameInformationConst();
        if (info != null)
            m_clock.setTimeSettings(info.getTimeSettings());
    }
}
