//-----------------------------------------------------------------------------
// $Id$
// $Source$
//-----------------------------------------------------------------------------

package board;

//-----------------------------------------------------------------------------

public class Score
{
    public int m_areaBlack;

    public int m_areaWhite;

    public int m_capturedBlack;

    public int m_capturedWhite;

    public float m_result;

    public float m_resultChinese;

    public float m_resultJapanese;

    public int m_rules;

    public int m_territoryBlack;

    public int m_territoryWhite;

    public String formatResult()
    {
        if (m_result > 0)
            return "Black wins by " + m_result;
        else if (m_result < 0)
            return "White wins by " + (-m_result);
        else
            return "Even";
    }
}

//-----------------------------------------------------------------------------
