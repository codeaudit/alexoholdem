package ao.ai.simple;

import ao.ai.AbstractPlayer;
import ao.holdem.engine.analysis.Analysis;
import ao.holdem.engine.state.State;
import ao.holdem.model.Avatar;
import ao.holdem.model.Chips;
import ao.holdem.model.act.Action;
import ao.holdem.model.act.FallbackAction;
import ao.holdem.model.card.sequence.CardSequence;

import java.util.Map;

/**
 * User: alex
 * Date: 3-Apr-2009
 * Time: 4:57:35 PM
 */
public class AlwaysCallBot extends AbstractPlayer
{
    //--------------------------------------------------------------------
    public void handEnded(Map<Avatar, Chips> deltas) {}


    //--------------------------------------------------------------------
    public Action act(State state,
                      CardSequence cards,
                      Analysis analysis)
    {
        return state.reify(FallbackAction.CHECK_OR_CALL);
    }
}
