package ao.bucket.abstraction.hole;

import ao.bucket.abstraction.set.BucketSetImpl;
import ao.bucket.index.CanonTraverser;
import ao.bucket.index.CanonTraverser.Traverser;
import ao.holdem.model.card.Community;
import ao.holdem.model.card.Hole;
import ao.holdem.model.card.sequence.CardSequence;
import ao.odds.agglom.impl.PreciseHeadsUpOdds;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.Comparator;


/**
 *
 */
public class SimpleHoleBucketizer implements HoleBucketizer
{
    //--------------------------------------------------------------------
    private static final Logger LOG =
            Logger.getLogger(SimpleHoleBucketizer.class);


    //--------------------------------------------------------------------
//    private static final Hole   revIndex[]  =
//            new Hole  [ Hole.CANONICAL_COUNT ];
    private static final Double strengths[] =
            new Double[ Hole.CANONICAL_COUNT ];
    private static final Short  inOrder[]   =
            new Short [ Hole.CANONICAL_COUNT ];

    private static void initCanonHoles()
    {
        LOG.info("computing hole strengths");

        new CanonTraverser().traverse(new Traverser() {
            public void traverse(CardSequence cards) {
                Hole hole = cards.hole();

                strengths[ hole.canonIndex() ] =
                        new PreciseHeadsUpOdds().compute(
                                hole, Community.PREFLOP
                        ).strengthVsRandom();
//                         new GeneralHistFinder().compute(
//                                hole, Community.PREFLOP).mean();
//                revIndex  [ hole.canonIndex() ] = hole;
            }
        });

        for (short i = 0; i < inOrder.length; i++) inOrder[ i ] = i;
        Arrays.sort(inOrder, new Comparator<Short>() {
            public int compare(Short a, Short b) {
                return strengths[ a ].compareTo(
                            strengths[ b ]);
            }
        });
    }



    //--------------------------------------------------------------------
    public SimpleHoleBucketizer() {}


    //--------------------------------------------------------------------
    public BucketSetImpl bucketize(char nBuckets)
    {
        if (strengths[0] == null) initCanonHoles();

        BucketSetImpl buckets = new BucketSetImpl(
                                    Hole.CANONICAL_COUNT, nBuckets);

        int index = 0;
        int chunk = (int) Math.ceil(
                      ((double) strengths.length) / nBuckets);
        for (char bucket = 0; bucket < nBuckets; bucket++)
        {
            for (int j = 0;
                     j < chunk && index < strengths.length;
                     j++)
            {
                buckets.add(inOrder[ index++ ], bucket);
            }
        }

        return buckets;
    }


    //--------------------------------------------------------------------
    public String id()
    {
        return "simple_odds";
    }

    @Override
    public String toString()
    {
        return id();
    }
}
