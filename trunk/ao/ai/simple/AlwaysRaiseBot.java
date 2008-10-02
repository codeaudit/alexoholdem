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
 *
 */
public class AlwaysRaiseBot extends AbstractPlayer
{
    //--------------------------------------------------------------------
    public void handEnded(Map<Avatar, Chips> deltas) {}

    
    //--------------------------------------------------------------------
    public Action act(State        state,
                      CardSequence cards,
                      Analysis     analysis)
    {
        return state.reify(FallbackAction.RAISE_OR_CALL);
    }
}