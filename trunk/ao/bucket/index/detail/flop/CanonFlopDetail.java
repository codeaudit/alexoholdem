package ao.bucket.index.detail.flop;

import ao.bucket.index.flop.Flop;
import ao.holdem.model.card.Card;
import ao.holdem.persist.GenericBinding;
import ao.odds.agglom.Odds;
import ao.odds.agglom.impl.PreciseHeadsUpOdds;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

/**
 * Date: Jan 9, 2009
 * Time: 12:33:06 PM
 */
public class CanonFlopDetail
{
    //--------------------------------------------------------------------
    private final Card FLOP_A;
    private final Card FLOP_B;
    private final Card FLOP_C;
    private final byte REPRESENTS;
    private final Odds HEADS_UP_ODDS;
    private final int  FIRST_CANON_TURN;
    private final char CANON_TURN_COUNT;


    //--------------------------------------------------------------------
    public CanonFlopDetail(
            Card flopA,
            Card flopB,
            Card flopC,
            byte represents,
            Odds headsUpOdds,
            int  firstCanonTurn,
            char canonTurnCount)
    {
        FLOP_A           = flopA;
        FLOP_B           = flopB;
        FLOP_C           = flopC;
        REPRESENTS       = represents;
        HEADS_UP_ODDS    = headsUpOdds;
        FIRST_CANON_TURN = firstCanonTurn;
        CANON_TURN_COUNT = canonTurnCount;
    }


    //--------------------------------------------------------------------
    public Card exampleA()
    {
        return FLOP_A;
    }

    public Card exampleB()
    {
        return FLOP_B;
    }

    public Card exampleC()
    {
        return FLOP_C;
    }


    //--------------------------------------------------------------------
    public byte represents()
    {
        return REPRESENTS;
    }


    //--------------------------------------------------------------------
    public Odds headsUpOdds()
    {
        return HEADS_UP_ODDS;
    }


    //--------------------------------------------------------------------
    public int firstCanonTurn()
    {
        return FIRST_CANON_TURN;
    }

    public char canonTurnCount()
    {
        return CANON_TURN_COUNT;
    }


    //--------------------------------------------------------------------
    public static final Binding BINDING = new Binding();

    public static class Binding extends GenericBinding<CanonFlopDetail>
    {
        public CanonFlopDetail read(TupleInput in) {
            return new CanonFlopDetail(
                    Card.BINDING.entryToObject( in ),
                    Card.BINDING.entryToObject( in ),
                    Card.BINDING.entryToObject( in ),
                    in.readByte(),
                    Odds.BINDING.read(in),
                    in.readInt(),
                    in.readChar());
        }

        public void write(CanonFlopDetail obj, TupleOutput out) {
            Card.BINDING.objectToEntry( obj.FLOP_A, out );
            Card.BINDING.objectToEntry( obj.FLOP_B, out );
            Card.BINDING.objectToEntry( obj.FLOP_C, out );
            out.writeByte( obj.REPRESENTS );
            Odds.BINDING.write( obj.HEADS_UP_ODDS, out );
            out.writeInt( obj.FIRST_CANON_TURN);
            out.writeChar( obj.CANON_TURN_COUNT);
        }
    }


    //--------------------------------------------------------------------
    @Override public String toString()
    {
        return "CanonFlopDetail{" +
               "FLOP_A=" + FLOP_A +
               ", FLOP_B=" + FLOP_B +
               ", FLOP_C=" + FLOP_C +
               ", REPRESENTS=" + REPRESENTS +
               ", HEADS_UP_ODDS=" + HEADS_UP_ODDS +
               ", FIRST_CANON_TURN=" + FIRST_CANON_TURN +
               ", CANON_TURN_COUNT=" + CANON_TURN_COUNT +
               '}';
    }


    //--------------------------------------------------------------------
    @Override public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CanonFlopDetail that = (CanonFlopDetail) o;

        return CANON_TURN_COUNT == that.CANON_TURN_COUNT &&
               FIRST_CANON_TURN == that.FIRST_CANON_TURN &&
               REPRESENTS == that.REPRESENTS &&
               FLOP_A == that.FLOP_A &&
               FLOP_B == that.FLOP_B &&
               FLOP_C == that.FLOP_C &&
               HEADS_UP_ODDS.equals(that.HEADS_UP_ODDS);
    }

    @Override public int hashCode()
    {
        int result = FLOP_A.hashCode();
        result = 31 * result + FLOP_B.hashCode();
        result = 31 * result + FLOP_C.hashCode();
        result = 31 * result + (int) REPRESENTS;
        result = 31 * result + HEADS_UP_ODDS.hashCode();
        result = 31 * result + FIRST_CANON_TURN;
        result = 31 * result + (int) CANON_TURN_COUNT;
        return result;
    }


    //--------------------------------------------------------------------
    public static class Buffer
    {
        public Card FLOP_A           = null;
        public Card FLOP_B           = null;
        public Card FLOP_C           = null;
        public byte REPRESENTS       = 0;
        public Odds HEADS_UP_ODDS    = null;
        public int  FIRST_CANON_TURN = -1;
        public char CANON_TURN_COUNT = 0;

        public Buffer(Flop flop)
        {
            FLOP_A = flop.community().flopA();
            FLOP_B = flop.community().flopB();
            FLOP_C = flop.community().flopC();

            HEADS_UP_ODDS =
                new PreciseHeadsUpOdds().compute(
                        flop.hole(), flop.community());
        }

        public CanonFlopDetail toDetail()
        {
            return new CanonFlopDetail(
                    FLOP_A, FLOP_B, FLOP_C,
                    REPRESENTS, HEADS_UP_ODDS,
                    FIRST_CANON_TURN, CANON_TURN_COUNT);
        }
    }
}