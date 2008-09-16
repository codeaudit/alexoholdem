package ao.bucket.index.incremental;

import ao.bucket.index.Indexer;
import ao.bucket.index.flop.Flop;
import ao.bucket.index.post_flop.river.CanonRiver;
import ao.bucket.index.post_flop.turn.Turn;
import ao.holdem.model.card.Card;
import ao.holdem.model.card.sequence.CardSequence;

/**
 * Date: Aug 16, 2008
 * Time: 2:48:07 PM
 */
public class IndexerImpl implements Indexer
{
    //--------------------------------------------------------------------
    public long indexOf(CardSequence cards)
    {
        if (cards.community().isPreflop())
        {
            return cards.hole().canonIndex();
        }
        else
        {
            Flop isoFlop = cards.hole().addFlop(
                            cards.community().flopA(),
                            cards.community().flopB(),
                            cards.community().flopC());
            int flopIndex = isoFlop.canonIndex();

            if (! cards.community().hasTurn())
            {
                return flopIndex;
            }
            else
            {
                Card turnCard = cards.community().turn();
                Turn turn     = isoFlop.addTurn(turnCard);
                int turnIndex = turn.canonIndex();
                if (! cards.community().hasRiver())
                {
                    return turnIndex;
                }
                else
                {
                    Card       riverCard = cards.community().river();
                    CanonRiver river     = turn.addRiver(riverCard);
                    return river.canonIndex();
                }
            }
        }
    }
}