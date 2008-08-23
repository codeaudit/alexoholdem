package ao.bucket.index.iso_case;

import ao.bucket.index.iso_cards.WildSuit;

/**
 * Date: Aug 14, 2008
 * Time: 3:41:50 AM
 */
public class FlopCase
{
    //--------------------------------------------------------------------
    public static FlopCase CASE_12_113 =
            new FlopCase(WildSuit.ONE, WildSuit.TWO,
                         WildSuit.ONE, WildSuit.ONE, WildSuit.THREE);
    
    public static FlopCase CASE_12_133 =
            new FlopCase(WildSuit.ONE, WildSuit.TWO,
                         WildSuit.ONE, WildSuit.THREE, WildSuit.THREE);

    public static FlopCase CASE_WW_2WW =
            new FlopCase(WildSuit.WILD, WildSuit.WILD,
                         WildSuit.TWO, WildSuit.WILD, WildSuit.WILD);
    
    public static FlopCase CASE_12_123 =
            new FlopCase(WildSuit.ONE, WildSuit.TWO,
                         WildSuit.ONE, WildSuit.TWO, WildSuit.THREE);

    public static FlopCase CASE_12_134 =
            new FlopCase(WildSuit.ONE, WildSuit.TWO,
                         WildSuit.ONE, WildSuit.THREE, WildSuit.FOUR);

    public static FlopCase CASE_12_1WW =
            new FlopCase(WildSuit.ONE, WildSuit.TWO,
                         WildSuit.ONE, WildSuit.WILD, WildSuit.WILD);

    public static FlopCase CASE_WW_223 =
            new FlopCase(WildSuit.WILD, WildSuit.WILD,
                         WildSuit.TWO, WildSuit.TWO, WildSuit.THREE);

    public static FlopCase CASE_WW_222 =
            new FlopCase(WildSuit.WILD, WildSuit.WILD,
                         WildSuit.TWO, WildSuit.TWO, WildSuit.TWO);

    public static FlopCase CASE_12_111 =
            new FlopCase(WildSuit.ONE, WildSuit.TWO,
                         WildSuit.ONE, WildSuit.ONE, WildSuit.ONE);

    public static FlopCase CASE_12_112 =
            new FlopCase(WildSuit.ONE, WildSuit.TWO,
                         WildSuit.ONE, WildSuit.ONE, WildSuit.TWO);

    public static FlopCase CASE_11_122 =
            new FlopCase(WildSuit.ONE, WildSuit.ONE,
                         WildSuit.ONE, WildSuit.TWO, WildSuit.TWO);

    public static FlopCase CASE_11_223 =
            new FlopCase(WildSuit.ONE, WildSuit.ONE,
                         WildSuit.TWO, WildSuit.TWO, WildSuit.THREE);

    public static FlopCase CASE_11_222 =
            new FlopCase(WildSuit.ONE, WildSuit.ONE,
                         WildSuit.TWO, WildSuit.TWO, WildSuit.TWO);

    public static FlopCase CASE_11_112 =
            new FlopCase(WildSuit.ONE, WildSuit.ONE,
                         WildSuit.ONE, WildSuit.ONE, WildSuit.TWO);

    public static FlopCase CASE_11_123 =
            new FlopCase(WildSuit.ONE, WildSuit.ONE,
                         WildSuit.ONE, WildSuit.TWO, WildSuit.THREE);
    
    public static FlopCase CASE_11_1WW =
            new FlopCase(WildSuit.ONE, WildSuit.ONE,
                         WildSuit.ONE, WildSuit.WILD, WildSuit.WILD);

    public static FlopCase CASE_11_3WW =
            new FlopCase(WildSuit.ONE, WildSuit.ONE,
                         WildSuit.THREE, WildSuit.WILD, WildSuit.WILD);

    public static FlopCase CASE_11_234 =
            new FlopCase(WildSuit.ONE, WildSuit.ONE,
                         WildSuit.TWO, WildSuit.THREE, WildSuit.FOUR);

    public static FlopCase CASE_11_WWW =
            new FlopCase(WildSuit.ONE, WildSuit.ONE,
                         WildSuit.WILD, WildSuit.WILD, WildSuit.WILD);

    public static FlopCase CASE_11_111 =
            new FlopCase(WildSuit.ONE, WildSuit.ONE,
                         WildSuit.ONE, WildSuit.ONE, WildSuit.ONE);

    public static FlopCase CASE_12_223 =
            new FlopCase(WildSuit.ONE, WildSuit.TWO,
                         WildSuit.TWO, WildSuit.TWO, WildSuit.THREE);

    public static FlopCase CASE_12_233 =
            new FlopCase(WildSuit.ONE, WildSuit.TWO,
                         WildSuit.TWO, WildSuit.THREE, WildSuit.THREE);

    public static FlopCase CASE_12_234 =
            new FlopCase(WildSuit.ONE, WildSuit.TWO,
                         WildSuit.TWO, WildSuit.THREE, WildSuit.FOUR);

    public static FlopCase CASE_12_2WW =
            new FlopCase(WildSuit.ONE, WildSuit.TWO,
                         WildSuit.TWO, WildSuit.WILD, WildSuit.WILD);

    public static FlopCase CASE_12_334 =
            new FlopCase(WildSuit.ONE, WildSuit.TWO,
                         WildSuit.THREE, WildSuit.THREE, WildSuit.FOUR);

    public static FlopCase CASE_12_333 =
            new FlopCase(WildSuit.ONE, WildSuit.TWO,
                         WildSuit.THREE, WildSuit.THREE, WildSuit.THREE);

    public static FlopCase CASE_12_222 =
            new FlopCase(WildSuit.ONE, WildSuit.TWO,
                         WildSuit.TWO, WildSuit.TWO, WildSuit.TWO);

    public static FlopCase CASE_12_122 =
            new FlopCase(WildSuit.ONE, WildSuit.TWO,
                         WildSuit.ONE, WildSuit.TWO, WildSuit.TWO);


    //--------------------------------------------------------------------
    private final WildSuit HOLE_A, HOLE_B,
                           FLOP_A, FLOP_B, FLOP_C;


    //--------------------------------------------------------------------
    public FlopCase(WildSuit holeA, WildSuit holeB,
                    WildSuit flopA, WildSuit flopB, WildSuit flopC)
    {
        HOLE_A = holeA;
        HOLE_B = holeB;
        FLOP_A = flopA;
        FLOP_B = flopB;
        FLOP_C = flopC;
    }


    //--------------------------------------------------------------------
    public String toString()
    {
        return "[" + HOLE_A + ", " + HOLE_B + "]" +
               "[" + FLOP_A + ", " + FLOP_B + ", " + FLOP_C +"]";
    }


    //--------------------------------------------------------------------
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FlopCase flopCase = (FlopCase) o;
        return HOLE_A == flopCase.HOLE_A &&
               HOLE_B == flopCase.HOLE_B &&
               FLOP_A == flopCase.FLOP_A &&
               FLOP_B == flopCase.FLOP_B &&
               FLOP_C == flopCase.FLOP_C;
    }

    public int hashCode()
    {
        int result = 0;
        result = 31 * result + HOLE_A.hashCode();
        result = 31 * result + HOLE_B.hashCode();
        result = 31 * result + FLOP_A.hashCode();
        result = 31 * result + FLOP_B.hashCode();
        result = 31 * result + FLOP_C.hashCode();
        return result;
    }
}