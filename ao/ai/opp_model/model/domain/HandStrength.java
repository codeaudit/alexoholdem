package ao.ai.opp_model.model.domain;

/**
 *
 */
public enum HandStrength
{
    //--------------------------------------------------------------------
//    DELTA_m100(-1.00, -0.95),
//    DELTA_m90 (-0.95, -0.85),
//    DELTA_m80 (-0.85, -0.75),
//    DELTA_m70 (-0.75, -0.65),
//    DELTA_m60 (-0.65, -0.55),
//    DELTA_m50 (-0.55, -0.45),
//    DELTA_m40 (-0.45, -0.35),
//    DELTA_m30 (-0.35, -0.25),
    DELTA_m30 (-1.00, -0.25),
    DELTA_m20 (-0.25, -0.15),

    DELTA_m10 (-0.15, -0.05),
//    DELTA_m13 (-0.15, -0.10), //-12.5
//    DELTA_m07 (-0.10, -0.05), //-7.5

    DELTA_nil (-0.05,  0.05),
    DELTA_p10 ( 0.05,  0.15),
    DELTA_p20 ( 0.15,  0.25),

    DELTA_p30 ( 0.25,  0.35),
    DELTA_p40 ( 0.35,  0.45),
//    DELTA_p28 ( 0.25,  0.30), // 27.5
//    DELTA_p33 ( 0.30,  0.35), // 32.5
//    DELTA_p37 ( 0.35,  0.40),
//    DELTA_p43 ( 0.40,  0.45), // 42.5

    DELTA_p50 ( 0.45,  1.00);
//    DELTA_p50 ( 0.45,  0.55),
//    DELTA_p60 ( 0.55,  0.65),
//    DELTA_p70 ( 0.65,  1.00);
//    DELTA_p70 ( 0.65,  0.75),
//    DELTA_p80 ( 0.75,  0.85),
//    DELTA_p90 ( 0.85,  0.95),
//    DELTA_p100( 0.95,  1.00);


    //--------------------------------------------------------------------
    public static HandStrength fromPercent(double plusMinusOne)
    {
        assert -1 <= plusMinusOne && plusMinusOne <= 1;

        for (HandStrength strength : values())
        {
            if (strength.intersects( plusMinusOne ))
            {
                return strength;
            }
        }
        throw new Error("not in [-1 .. 1]: " + plusMinusOne);
    }


    //--------------------------------------------------------------------
    private final double FROM_AND_INCLUDING;
    private final double UP_TO_AND_INCLUDING;


    //--------------------------------------------------------------------
    private HandStrength(
            double fromAndIncluding,
            double upToAndIncluding)
    {
        FROM_AND_INCLUDING  = fromAndIncluding;
        UP_TO_AND_INCLUDING = upToAndIncluding;
    }

    private boolean intersects(double value)
    {
        return FROM_AND_INCLUDING <= value &&
                                     value <= UP_TO_AND_INCLUDING;
    }
}
