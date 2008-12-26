package ao.bucket.abstraction.flop;

import ao.bucket.abstraction.set.BucketSet;
import ao.bucket.index.CanonTraverser;
import ao.bucket.index.flop.Flop;
import ao.bucket.index.flop.FlopLookup;
import ao.odds.agglom.impl.GeneralHistFinder;
import ao.odds.eval.eval5.Eval5;
import ao.util.data.IntList;
import ao.util.misc.Traverser;
import ao.util.persist.PersistentShorts;
import org.apache.log4j.Logger;

/**
 * User: AOstrovsky
 * Date: Dec 14, 2008
 * Time: 4:25:59 PM
 */
public class FlopBucketizerImpl
        implements FlopBucketizer
{
    //--------------------------------------------------------------------
    private static final Logger LOG =
            Logger.getLogger(FlopBucketizerImpl.class);

    private static final String  MEAN_FILE =
            "lookup/bucket/flop/means.bin";
    private static final short[] MEANS =
            computeOrRetrieveCanonFlopMeans();


    //--------------------------------------------------------------------
    private static short[] computeOrRetrieveCanonFlopMeans()
    {
        LOG.debug("attempting to retrieve canon flop means");
        short[] means = PersistentShorts.readBinary(MEAN_FILE);
        if (means == null)
        {
            means = computeCanonFlopMeans();
            PersistentShorts.writeBinary(means, MEAN_FILE);
        }
        LOG.debug("done retrieving/computing canon flop means");
        return means;
    }
    private static short[] computeCanonFlopMeans()
    {
        LOG.debug("computing canon flop means");

        final short[] means = new short[ FlopLookup.CANON_FLOP_COUNT ];
        final int[]   count = new int[1];
        new CanonTraverser().traverseFlops(new Traverser<Flop>() {
            public void traverse(Flop flop) {
                means[ flop.canonIndex() ] =
                        (short) Math.round(
                        new GeneralHistFinder().compute(
                                flop.hole(), flop.toCommunity()).mean());
                if (count[0]++ % 10000 == 0) System.out.print(".");
            }
        });
        System.out.println();

        return means;
    }


    //--------------------------------------------------------------------
    public <T extends BucketSet> T
            bucketize(BucketSet            onTopOf,
                      char                 numBuckets,
                      BucketSet.Builder<T> with)
    {
        assert numBuckets >= 1;
        LOG.debug("bucketizing flop into " +
                  (int)(numBuckets) + " buckets");

        T buckets = with.newInstance(
                      FlopLookup.CANON_FLOP_COUNT, numBuckets);

        char   nextBucket = 0, lastBucket = (char)(numBuckets - 1);
        int    cummFill   = 0;
        double perBucket  =
                ((double) FlopLookup.CANON_FLOP_COUNT) / numBuckets;
        for (IntList meanStrata : byMean())
        {
            if (meanStrata == null) continue;

            double delta = cummFill - perBucket * (nextBucket + 1);
            if (nextBucket != lastBucket)
            {
                if (delta < 0)
                {
                    double overflow = delta + meanStrata.size();
                    if (overflow > meanStrata.size() / 2)
                    {
                        nextBucket++;
                    }
                }
                else
                {
                    nextBucket++;
                }
            }

            cummFill += meanStrata.size();
            for (int i = meanStrata.size() - 1; i >= 0; i--)
            {
                buckets.add(meanStrata.get(i), nextBucket);
            }
        }

        return buckets;
    }


    //--------------------------------------------------------------------
    private IntList[] byMean()
    {
        LOG.debug("sorting flops by mean");

        IntList[] byMean = new IntList[ Eval5.VALUE_COUNT ];
        for (int canon = 0; canon < MEANS.length; canon++)
        {
            byMean[ MEANS[canon] ] =
                    IntList.addTo(byMean[ MEANS[canon] ], canon);
        }

        LOG.debug("done sorting by mean");
        return byMean;
    }


    //--------------------------------------------------------------------
    public String id()
    {
        return getClass().getSimpleName();
    }


    //--------------------------------------------------------------------
    @Override public String toString()
    {
        return id();
    }
}
