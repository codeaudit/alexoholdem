package ao.state;

import ao.holdem.engine.HoldemRuleBreach;
import ao.holdem.model.BettingRound;
import ao.holdem.model.Money;
import ao.holdem.model.act.EasyAction;
import ao.holdem.model.act.RealAction;
import ao.holdem.model.act.SimpleAction;
import ao.persist.PlayerHandle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * To handle the corner case:
 *  unexpected move on burney, PREFLOP
 *   2	0	0	2	2	[7d, 8c, 9d, Jd, 4d]
 *   burney    812871541  2  1 B   -     -     -   1000    5    4 Js 4s
 *   THyde     812871541  2  2 BA  -     -     -      1    1    2 5c 3d
 * Awareness of bet quantities is needed, fortunetally
 *  this corner case is irrelavent because no action is necessary.
 *
 * Also, handling cases such as:
 *  unexpected move on bolt, PREFLOP
 *   5	0	0	0	1	[]
 *   bolt      813890996  5  1 B   -     -     -         2664    5   15
 *   d-l       813890996  5  2 BQ  -     -     -         1560   10    0
 *   MrX       813890996  5  3 fQ  -     -     -         1273    0    0 
 *   Jupiler   813890996  5  4 f   -     -     -         1854    0    0
 *   jester    813890996  5  5 f   -     -     -         1615    0    0
 * Requires asynchronouse quitting actions, they are not currently
 *  supported.  I will need to implement them if they are used by PS.
 *
 * Note, obects of this class are immutable.
 */
public class HandState
{
    //--------------------------------------------------------------------
    private static final    int BETS_PER_ROUND = 4;
    private static volatile int nextHandId     = 0;


    //--------------------------------------------------------------------
    private final BettingRound round;
    private final PlayerState  players[];
    private final int          nextToAct;
    private final int          remainingRoundBets;
    private final int          latestRoundStaker;
    private final Money        stakes;
    private final int          handId;
    private final HandState    startOfRound;


    //--------------------------------------------------------------------
    // expects blind actions
    public HandState(List<PlayerHandle> clockwiseDealerLast)
    {
        players = new PlayerState[ clockwiseDealerLast.size() ];
        for (int i = 0; i < players.length; i++)
        {
            players[i] = addInitPlayerState(clockwiseDealerLast, i);
        }

        round              = BettingRound.PREFLOP;
        nextToAct          = 0;
        remainingRoundBets = BETS_PER_ROUND - 1; // -1 for upcoming BB
        latestRoundStaker  = -1;
        stakes             = Money.ZERO;
        handId             = nextHandId++;
        startOfRound       = this;
    }
    private PlayerState addInitPlayerState(
            List<PlayerHandle> clockwiseDealerLast,
            int                playerIndex)
    {
        PlayerHandle player = clockwiseDealerLast.get( playerIndex );
        return new PlayerState(false, false, true, Money.ZERO, player);
    }

    // automatically posts blinds
    public static HandState autoBlindInstance(
            List<PlayerHandle> clockwiseDealerLast)
    {
        return new HandState(clockwiseDealerLast)
                    .advanceBlind( RealAction.SMALL_BLIND )
                    .advanceBlind( RealAction.BIG_BLIND );
    }

    // copy constructor
    private HandState(BettingRound copyRound,
                        PlayerState  copyPlayers[],
                        int          copyNextToAct,
                        int          copyRemainingRoundBets,
                        int          copyLatestRoundStaker,
                        Money        copyStakes,
                        int          copyHandId,
                        HandState    copyStartOfRound)
    {
        round              = copyRound;
        players            = copyPlayers;
        nextToAct          = copyNextToAct;
        remainingRoundBets = copyRemainingRoundBets;
        latestRoundStaker  = copyLatestRoundStaker;
        stakes             = copyStakes;
        handId             = copyHandId;
        startOfRound       = (copyStartOfRound == null)
                              ? this : copyStartOfRound;
    }


    //--------------------------------------------------------------------
    public HandState advance(PlayerHandle player, RealAction act)
    {
        validateNextAction(player, act);
        return act.isBlind()
                ? advanceBlind(act)
                : advanceVoluntary(act);
    }
    public HandState advance(RealAction act)
    {
        return advance(nextToAct().handle(), act);
    }


    //--------------------------------------------------------------------
    // TODO: allow for asynchronouse folds
    private HandState advanceVoluntary(RealAction act)
    {
        BettingRound nextRound  = nextBettingRound(act);
        boolean      roundEnder = (round != nextRound);
        boolean      betRaise   =
                        (act.toSimpleAction() == SimpleAction.RAISE);

        PlayerState
              nextPlayers[]     = nextPlayers(act);
        int   nextNextToAct     = nextNextToAct(nextPlayers, roundEnder);
        int   nextRemainingBets = nextRemainingBets(roundEnder, betRaise);
        Money nextStakes        = nextStakes(nextPlayers, betRaise);
        int   nextRoundStaker   =
                nextLatestRoundStaker(act, roundEnder, betRaise);

        return new HandState(nextRound,
                             nextPlayers,
                             nextNextToAct,
                             nextRemainingBets,
                             nextRoundStaker,
                             nextStakes,
                             nextHandId,
                             roundEnder ? null : startOfRound);
    }

    public PlayerState[] nextPlayers(RealAction act)
    {
        PlayerState nextPlayers[] = players.clone();
        nextPlayers[nextToAct] = nextPlayers[nextToAct]
                                    .advance(act, stakes, betSize());
        return nextPlayers;
    }

    private Money nextStakes(PlayerState[] nextPlayers, boolean betRaise)
    {
        return (betRaise)
               ? nextPlayers[nextToAct].commitment()
               : stakes;
    }

    private int nextNextToAct(
            PlayerState[] nextPlayers, boolean roundEnder)
    {
        return roundEnder
                ? nextActiveAfter(nextPlayers, players.length - 1)
                : nextActiveAfter(nextPlayers, nextToAct);
    }

    private BettingRound nextBettingRound(RealAction act)
    {
        return nextActionCritical()
                ? nextToActCanRaise()
                    ? (act.toSimpleAction() == SimpleAction.RAISE)
                        ? round
                        : round.next()
                    : round.next()
                : round;
    }
    // it determines weather or not the next action can end the round.
    private boolean nextActionCritical()
    {
        return latestRoundStaker == nextStakingUnfoldedAfter( nextToAct );// ||
                //nextToAct == nextActiveAfter( nextToAct );
    }

    private int nextLatestRoundStaker(
            RealAction act, boolean roundEnder, boolean betRaise)
    {
        return roundEnder
                ? -1
                : (betRaise
                   ? nextToAct
                   : (latestRoundStaker == -1 && // when all players check
                        act.toSimpleAction() != SimpleAction.FOLD)
                      ? nextToAct
                      : latestRoundStaker);
    }

    private int nextRemainingBets(boolean roundEnder, boolean betRaise)
    {
        return roundEnder
               ? BETS_PER_ROUND
               : (betRaise
                   ? remainingRoundBets - 1
                   : remainingRoundBets);
    }


    //--------------------------------------------------------------------
    private HandState advanceBlind(RealAction act)
    {
        boolean isSmall = act.asSmallBlind().equals( act );

        if ( isSmall && !stakes.equals( Money.ZERO ))
            throw new HoldemRuleBreach("Small Blind already in.");
        if (!isSmall && stakes.compareTo( Money.BIG_BLIND ) >= 0)
            throw new HoldemRuleBreach("Big Blind already in.");

        Money       betSize       = Money.blind(isSmall);
        PlayerState nextPlayers[] = players.clone();
        nextPlayers[nextToAct]    =
                nextPlayers[nextToAct].advanceBlind(act, betSize);

        int nextNextToAct   = nextActiveAfter(nextPlayers, nextToAct);
        int nextRoundStaker =
                !isSmall && act.isAllIn()
                 ? nextToAct : latestRoundStaker;
        
        return new HandState(round,
                             nextPlayers,
                             nextNextToAct,
                             remainingRoundBets,
                             nextRoundStaker,
                             betSize,
                             nextHandId,
                             startOfRound);
    }


    //--------------------------------------------------------------------
    public boolean atEndOfHand()
    {
        return atShowdown()    ||
               nextToAct == -1 ||
               nextToActIsLastActivePlayer() && nextToActCanCheck();
    }
    private boolean nextToActIsLastActivePlayer()
    {
        return nextToAct == nextActiveAfter(nextToAct);
    }
    private boolean atShowdown()
    {
        return round == null;
    }

    public Money betSize()
    {
        return isSmallBet() ? Money.SMALL_BET : Money.BIG_BET;
    }
    private boolean isSmallBet()
    {
        return round == BettingRound.PREFLOP ||
               round == BettingRound.FLOP;
    }

    public RealAction toRealAction(EasyAction easyAction)
    {
        return easyAction.toRealAction(nextToActCanCheck(),
                                       nextToActCanRaise());
    }
    private boolean nextToActCanCheck()
    {
        return stakes.equals( nextToAct().commitment() );
    }
    private boolean nextToActCanRaise()
    {
        return remainingRoundBets > 0;
    }


    //--------------------------------------------------------------------
    // these functions are provided as convenience,
    //  they are not actually used by state tracking.

    public BettingRound round()
    {
        return round;
    }

    public PlayerState[] players()
    {
        return players;
    }

    public int numActivePlayers()
    {
        int count = 0;
        for (PlayerState state : players)
            if (state.isActive()) count++;
        return count;
    }

    public Collection<PlayerState> unfolded()
    {
        Collection<PlayerState> condenters = new ArrayList<PlayerState>();

        int firstUnfolded = nextUnfoldedAfter( nextToAct - 1 );
        int cursor        = firstUnfolded;
        do
        {
            condenters.add( players[cursor] );
            cursor = nextUnfoldedAfter(cursor);
        }
        while (cursor != firstUnfolded);

        return condenters;
    }

    public Money pot()
    {
        Money pot = Money.ZERO;
        for (PlayerState player : players)
            pot = pot.plus( player.commitment() );
        return pot;
    }

    public Money stakes()
    {
        return stakes;
    }

    public int remainingBetsInRound()
    {
        return remainingRoundBets;
    }

    public Money toCall()
    {
        return stakes.minus( nextToAct().commitment() );
    }
    public int betsToCall()
    {
        return toCall().bets( isSmallBet() );
    }


    //--------------------------------------------------------------------
    private int index(int fromIndex)
    {
        return fromIndex < 0
                ? fromIndex + players.length
                : fromIndex % players.length;
    }

    private int nextActiveAfter(int playerIndex)
    {
        return nextActiveAfter( players, playerIndex );
    }
    private int nextActiveAfter(PlayerState[] states, int playerIndex)
    {
        for (int i = 1; i <= states.length; i++)
        {
            int index = index(playerIndex + i);
            if (states[ index ].isActive())
            {
                return index;
            }
        }
        return -1;
    }

    private int nextStakingUnfoldedAfter(int playerIndex)
    {
        int index = nextUnfoldedAfter(playerIndex);
        while (startOfRound.players[ index ].isAllIn() ||
                players[ index ].isAllIn() &&
               !players[ index ].commitment().equals( stakes ))
        {
            index = nextUnfoldedAfter(index);
        }
        return index;
    }
    private int nextUnfoldedAfter(int playerIndex)
    {
        for (int i = 1; i <= players.length; i++)
        {
            int index = index(playerIndex + i);
            if (! players[ index ].isFolded())
            {
                return index;
            }
        }
        return -1;
    }

    public PlayerState nextToAct()
    {
        return players[ nextToAct ];
    }


    //--------------------------------------------------------------------
    private void validateNextAction(
            PlayerHandle player, RealAction act)
    {
        if (atEndOfHand())
            throw new HoldemRuleBreach("the hand is already done");

        if (! nextToAct().handle().equals( player ))
            throw new HoldemRuleBreach(
                        "expected " + nextToAct().handle() + " not " +
                                      player);

        if (remainingRoundBets == 0 &&
                act.toSimpleAction() == SimpleAction.RAISE)
            throw new HoldemRuleBreach("round betting cap exceeded");
    }


    //--------------------------------------------------------------------
    public String toString()
    {
        return nextToAct() + ", " + round;
    }

    public boolean handsEqual(HandState with)
    {
        return with != null &&
               handId == with.handId;
    }
}
