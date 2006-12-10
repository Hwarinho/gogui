//----------------------------------------------------------------------------
// $Id$
//----------------------------------------------------------------------------

package net.sf.gogui.game;

import java.util.ArrayList;
import java.util.Map;
import net.sf.gogui.go.GoColor;
import net.sf.gogui.go.GoPoint;
import net.sf.gogui.go.Move;

/** Const functions of go.Node.
    @see Node
*/
public interface ConstNode
{
    GoPoint getAddBlack(int i);

    GoPoint getAddEmpty(int i);

    GoPoint getAddWhite(int i);

    String getComment();

    ConstNode getFatherConst();

    ConstNode getChildConst();

    ConstNode getChildConst(int i);

    int getChildIndex(ConstNode child);

    Map getLabelsConst();

    ArrayList getMarkedConst(MarkType type);

    Move getMove();

    int getMovesLeft(GoColor color);

    int getNumberAddBlack();

    int getNumberAddEmpty();

    int getNumberAddWhite();

    int getNumberChildren();

    GoColor getPlayer();

    Map getSgfPropertiesConst();

    double getTimeLeft(GoColor color);

    GoColor getToMove();

    float getValue();

    boolean hasSetup();

    ConstNode variationAfter(ConstNode child);

    ConstNode variationBefore(ConstNode child);
}