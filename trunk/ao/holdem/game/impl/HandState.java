package ao.holdem.game.impl;

import ao.holdem.def.model.card.Card;
import ao.holdem.def.model.cards.Hole;
import ao.holdem.def.model.cards.community.Community;
import ao.holdem.def.model.cards.community.Flop;
import ao.holdem.def.model.cards.community.River;
import ao.holdem.def.model.cards.community.Turn;
import ao.holdem.def.state.domain.BettingRound;
import ao.holdem.def.state.domain.Decider;
import ao.holdem.def.state.domain.Opposition;
import ao.holdem.def.state.env.GodEnvironment;
import ao.holdem.def.state.env.Player;
import ao.holdem.def.state.env.Position;
import ao.holdem.def.state.env.TakenAction;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Poker hand (ie. from pre-flop till showdown) history.
 */
public class HandState
{
    //--------------------------------------------------------------------
    private final static Logger log =
            Logger.getLogger(HandState.class.getName());

    private static final int DEALER = 0;


    //--------------------------------------------------------------------
    private Hole[]      holes;
    private int[]       commitment;
    private TakenAction actions[];
    private Community   community;
    private boolean[]   folded;
    private int         toMatch;
    private int         remainingBets;
    private int         smallBlind;
    private int         bigBlind;


    //--------------------------------------------------------------------
    public HandState(int numPlayers)
    {
        log.debug("initiating poker hand state.");

        holes      = new Hole[ numPlayers ];
        community  = new Community();
        folded     = new boolean[ numPlayers ];
        commitment = new int[ numPlayers ];
        actions    = new TakenAction[ numPlayers ];

//        active = new ArrayList<Integer>( numPlayers );
    }


    //--------------------------------------------------------------------
    public GodEnvironment envFor(int awayFromDealer)
    {
        log.debug("producing environment for: " +
                  awayFromDealer + " clockwise from dealer.");

        int    yourPosition     = -1;
        Player fromFirstToAct[] = new Player[ players() ];
        int    byPosition[]     = position2dealerDistance();
        for (int i = 0; i < players(); i++)
        {
            int dealerDistance = byPosition[i];

            Position pos      = new Position(i, players(), preFlop());
            fromFirstToAct[i] =
                    new Player(pos,
                               commitment[  dealerDistance ],
                               isActive(    dealerDistance ),
                               lastActionOf( dealerDistance ));

            if (awayFromDealer == dealerDistance)
            {
                yourPosition = i;
            }
        }

        return new GodEnvironment(
                    community,
                    fromFirstToAct,
                    yourPosition,
                    toMatch - commitment[awayFromDealer],
                    remainingBets,
                    domainBettingRound(),
                    holes, position2dealerDistance());
    }

    public Decider domainDecider(int awayFromDealer)
    {
        return canCheck(awayFromDealer)
                ? Decider.CHECK_RAISE
                : canRaise()
                   ? Decider.FOLD_CALL_RAISE
                   : Decider.FOLD_CALL;
    }
    public Opposition domainOpposition()
    {
        return Opposition.fromPlayers( players() );
    }
    public BettingRound domainBettingRound()
    {
        return community.flop() == null
                ? BettingRound.PREFLOP
                : community.turn() == null
                   ? BettingRound.FLOP
                   : community.river() == null
                      ? BettingRound.TURN
                      : BettingRound.RIVER;
    }

    public boolean canCheck(int awayFromDealer)
    {
        return toMatch <= commitment[ awayFromDealer ];
    }
    public boolean canRaise()
    {
        return remainingBets > 0;
    }


    //--------------------------------------------------------------------
    public void toMatch(int smallBlinds)
    {
        if (toMatch < smallBlinds)
        {
            log.debug("raise from " + toMatch +
                             " to " + smallBlinds + ".");
            toMatch = smallBlinds;
        }
    }

    public void remainingBets(int remainingRaises)
    {
        log.debug("bets remaining in hand: " + remainingRaises + ".");
        this.remainingBets = remainingRaises;
    }


    //--------------------------------------------------------------------
    public void checked(int awayFromDealer)
    {
        log.debug(awayFromDealer + " clockwise from dealer checks.");

        actions[ awayFromDealer ] = TakenAction.CHECK;
    }

    public void called(int awayFromDealer)
    {
        log.debug(awayFromDealer + " clockwise from dealer calls.");

        commit(awayFromDealer, toMatch);
        actions[ awayFromDealer ] = TakenAction.CALL;
    }

    public void raised(int awayFromDealer)
    {
        raised(awayFromDealer, true);
    }
    private void raised(int awayFromDealer, boolean asAction)
    {
        remainingBets--;
        log.debug(awayFromDealer + " clockwise from dealer raises. " +
                  remainingBets + " bets remaining.");

        commit(awayFromDealer, toMatch + betSize());
        if (asAction)
        {
            actions[ awayFromDealer ] = TakenAction.RAISE;
        }
    }

    public void folded(int awayFromDealer)
    {
        log.debug(awayFromDealer + " clockwise from dealer folds.");

        folded[ awayFromDealer ] = true;
        actions[ awayFromDealer ] = TakenAction.FOLD;
    }


    //--------------------------------------------------------------------
    public void dealHoleCards(int awayFromDealer, Hole holeCards)
    {
        log.debug(awayFromDealer + " clockwise from dealer " +
                    "gets " + holeCards + " hole cards.");
        holes[ awayFromDealer ] = holeCards;
    }


    //--------------------------------------------------------------------
    public void designateSmallBlind(int awayFromDealer)
    {
        log.debug("small blind is " + awayFromDealer +
                  " clockwise from dealer.");
        smallBlind = awayFromDealer;
        commit(smallBlind, 1);
    }
    public void designateBigBlind(int awayFromDealer)
    {
        log.debug("big blind is " + awayFromDealer +
                  " clockwise from dealer.");
        bigBlind = awayFromDealer;
        raised(awayFromDealer, false);
//        commit(bigBlind, 2);
    }

    public void designateBlinds()
    {
        designateSmallBlind(
                headsUp() ? DEALER : clockwise(DEALER));
        designateBigBlind(
                clockwise(smallBlind));
    }


    //--------------------------------------------------------------------
    public void commit(int awayFromDealer, int bet)
    {
        assert commitment[ awayFromDealer ] <= bet;

        int delta = bet - commitment[ awayFromDealer ];
        if (delta != 0)
        {
            log.debug(awayFromDealer + " clockwise from dealer " +
                        "raises commitment by " + delta +
                                          " to " + bet   + ".");
            commitment[ awayFromDealer ] = bet;
            toMatch(bet);
        }
    }


    //--------------------------------------------------------------------
    public void dealFlop(Flop flop)
    {
        log.debug("updating community with flop.");
        community = new Community( flop );
    }

    public void dealTurn(Card turn)
    {
        log.debug("updating community with turn.");
        community = new Community(
                        new Turn(community.flop(), turn));
    }
    public void dealTurn(Turn turn)
    {
        log.debug("updating community with turn.");
        community = new Community(turn);
    }

    public void dealRiver(Card river)
    {
        log.debug("updating community with river.");
        community = new Community(
                        new River(community.turn(), river));
    }
    public void dealRiver(River river)
    {
        log.debug("updating community with river.");
        community = new Community(river);
    }


    //--------------------------------------------------------------------
    public boolean isActive(int awayFromDealer)
    {
        return !folded[ awayFromDealer ];
    }

    public TakenAction lastActionOf(int awayFromDealer)
    {
        TakenAction action = actions[ awayFromDealer ];
        return (action == null) ? TakenAction.YET_TO_ACT
                                : action;
    }


    //--------------------------------------------------------------------
    public boolean preFlop()
    {
        return community.flop() == null;
    }

    public boolean preTurn()
    {
        return community.turn() == null;
    }

    public boolean headsUp()
    {
        return players() == 2;
    }


    //--------------------------------------------------------------------
    public int betSize()
    {
        return preTurn() ? 2 : 4;
    }


    //--------------------------------------------------------------------
    public List<Integer> active()
    {
        List<Integer> active = new ArrayList<Integer>();

        for (int awayFromDealerInActionOrder : position2dealerDistance())
        {
            if (isActive(awayFromDealerInActionOrder))
            {
                active.add( awayFromDealerInActionOrder );
            }
        }

        return active;
    }

    // byPosition()[0] = how far away from dealer is first to act
    private int[] position2dealerDistance()
    {
        int[] byPosition = new int[ players() ];

        for (int awayFromFirstToAct = 0,
                 awayFromDealer     = firstToAct();

                 awayFromFirstToAct < byPosition.length;

                 awayFromFirstToAct++,
                 awayFromDealer = clockwise(awayFromDealer))
        {
            byPosition[ awayFromFirstToAct ] = awayFromDealer;
        }

        return byPosition;
    }


    //--------------------------------------------------------------------
    public int players()
    {
        return holes.length;
    }

    private int clockwise(int from)
    {
        return (from + 1) % players();
    }

    private int firstToAct()
    {
        return clockwise(
                preFlop() ? bigBlind : DEALER);
    }
}
