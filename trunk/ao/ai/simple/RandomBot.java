package ao.ai.simple;

import ao.ai.AbstractPlayer;
import ao.state.HandState;
import ao.holdem.model.card.Hole;
import ao.holdem.model.act.EasyAction;
import ao.holdem.model.Money;
import ao.state.StateManager;
import ao.util.rand.Rand;
import ao.persist.PlayerHandle;

import java.util.Map;


/**
 *
 */
public class RandomBot extends AbstractPlayer
{
    //--------------------------------------------------------------------
    public void handEnded(Map<PlayerHandle, Money> deltas) {}


    //--------------------------------------------------------------------
    protected EasyAction act(StateManager env, HandState state, Hole hole)
    {
        return Rand.fromArray( EasyAction.values() );
    }
}