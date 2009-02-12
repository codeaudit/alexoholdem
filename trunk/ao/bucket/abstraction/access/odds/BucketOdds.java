package ao.bucket.abstraction.access.odds;

import ao.bucket.abstraction.access.tree.BucketTree;
import ao.bucket.index.enumeration.BitFilter;
import ao.bucket.index.enumeration.HandEnum;
import ao.bucket.index.enumeration.UniqueFilter;
import ao.bucket.index.flop.Flop;
import ao.bucket.index.flop.FlopLookup;
import ao.bucket.index.hole.CanonHole;
import ao.bucket.index.hole.HoleLookup;
import ao.bucket.index.river.River;
import ao.bucket.index.river.RiverLookup;
import ao.bucket.index.turn.Turn;
import ao.bucket.index.turn.TurnLookup;
import ao.util.data.LongBitSet;
import ao.util.misc.Filters;
import ao.util.misc.Traverser;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Arrays;

/**
 * Date: Jan 29, 2009
 * Time: 12:17:17 PM
 */
public class BucketOdds
{
    //--------------------------------------------------------------------
    private static final Logger LOG =
            Logger.getLogger(BucketOdds.class);

    private static final String STR_FILE = "eval";
    private static final int    BUFFER   = 1300;


    //--------------------------------------------------------------------
    public static BucketOdds retrieveOrCompute(
            File dir, BucketTree bucketTree, char[][][][] holes)
    {
        try {
            return doRetrieveOrCompute(dir, bucketTree, holes);
        } catch (IOException e) {
            LOG.error("unable to retrieveOrCompute");
            e.printStackTrace();
            return null;
        }
    }
    private static BucketOdds doRetrieveOrCompute(
            File dir, BucketTree bucketTree, char[][][][] holes)
            throws IOException
    {
        char[][][] flops  =  holes[  holes.length - 1 ];
        char[][]   turns  =  flops[  flops.length - 1 ];
        char[]     rivers =  turns[  turns.length - 1 ];
        int  riverBuckets = rivers[ rivers.length - 1 ] + 1;

        File            file = new File(dir, STR_FILE);
        SlimRiverHist[] hist = retrieveStrengths(file, riverBuckets);
        int             off  = offset(hist);
        if (hist == null || off != hist.length) {
            hist = computeAndPersistStrengths(off,
                    riverBuckets, bucketTree, holes, file);
        }
        return new BucketOdds(hist);
    }


    //--------------------------------------------------------------------
    private static SlimRiverHist[] retrieveStrengths(
            File file, int riverBuckets) throws IOException
    {
        if (! file.canRead()) return null;
        LOG.debug("retrieving strengths");

        SlimRiverHist[] hist = new SlimRiverHist[ riverBuckets ];
        InputStream in = new BufferedInputStream(
                                new FileInputStream(file));
        byte[] binStrengths =
                new byte[ SlimRiverHist.BINDING_MAX_SIZE ];
        //noinspection ResultOfMethodCallIgnored
        in.read(binStrengths, 0, binStrengths.length);

        for (int i = 0, offset; i < hist.length; i++) {
            TupleInput tin = new TupleInput(binStrengths);
            hist[ i ]      = SlimRiverHist.BINDING.read( tin );
            offset         = hist[ i ].bindingSize();

            System.arraycopy(
                    binStrengths,
                    offset,
                    binStrengths,
                    0,
                    binStrengths.length - offset);
            //noinspection ResultOfMethodCallIgnored
            in.read(binStrengths, binStrengths.length - offset, offset);
        }
        in.close();
        return hist;
    }

    private static int offset(SlimRiverHist[] hist)
    {
        if (hist == null) return 0;
        for (int i = 0; i < hist.length; i++){
            if (hist[i] == null) return i;
        }
        return hist.length;
    }



    //--------------------------------------------------------------------
    private static void persistStrengths(
            SlimRiverHist[] bucketHist, File file)
    {
        try
        {
            doPersistStrengths(bucketHist, file);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    private static void doPersistStrengths(
            SlimRiverHist[] bucketHist, File file) throws IOException
    {
        LOG.debug("persisting strengths");

        OutputStream outFile = new BufferedOutputStream(
                                 new FileOutputStream(file));

        TupleOutput out = new TupleOutput();
        for (SlimRiverHist hist : bucketHist)
        {
            if (hist == null) break;
            SlimRiverHist.BINDING.write(hist, out);

            byte asBinary[] = out.getBufferBytes();
            outFile.write(asBinary, 0, out.getBufferLength());
            out = new TupleOutput(asBinary);
        }
        
        outFile.close();
    }


    //--------------------------------------------------------------------
    private static SlimRiverHist[] computeAndPersistStrengths(
            int          initialOffset,
            int          riverBuckets,
            BucketTree   tree,
            char[][][][] holes,
            File         outFile)
    {
        LOG.debug("computing strengths");
        final SlimRiverHist[] hist = new SlimRiverHist[ riverBuckets ];

        for (int offset = initialOffset;
                 offset < riverBuckets;
                 offset += BUFFER)
        {
            computeStrengths(hist,
                             offset,
                             Math.min(BUFFER,
                                      riverBuckets - offset),
                             tree, holes);
            persistStrengths(hist, outFile);
        }

        System.out.println(" DONE!");
        return hist;
    }
    private static void computeStrengths(
            final SlimRiverHist[] hist,
            final int             offset,
            final int             length,
            final BucketTree      tree,
            final char[][][][]    holes)
    {
        final RiverHist[]  histBuff = new RiverHist[ length ];
        for (int i = 0; i < histBuff.length; i++) {
            histBuff[ i ] = new RiverHist();
        }

        LongBitSet allowHoles  = new LongBitSet(HoleLookup.CANONS);
        LongBitSet allowFlops  = new LongBitSet(FlopLookup.CANONS);
        LongBitSet allowTurns  = new LongBitSet(TurnLookup.CANONS);
        LongBitSet allowRivers = new LongBitSet(RiverLookup.CANONS);
        computeAllowedStrengths(
                offset, length, tree, holes,
                allowHoles, allowFlops, allowTurns, allowRivers);

        LOG.debug("computing strengths for allowed");
        final long[] count = {0};
        HandEnum.rivers(
                new BitFilter<CanonHole>(allowHoles),
                new BitFilter<Flop>     (allowFlops),
                new BitFilter<Turn>     (allowTurns),
                new BitFilter<River>    (allowRivers),
                new Traverser<River>() {
            public void traverse(River river) {
                char absoluteRiverBucket = bucketOf(tree, holes, river);
                histBuff[absoluteRiverBucket - offset].count( river.eval() );
                checkpoint(count[0]++);
            }});

        for (int i = 0; i < length; i++) {
            hist[offset + i] = histBuff[i].slim();
        }
    }

    private static void computeAllowedStrengths(
            final int          offset,
            final int          length,
            final BucketTree   tree,
            final char[][][][] holes,
            final LongBitSet   allowHoles,
            final LongBitSet   allowFlops,
            final LongBitSet   allowTurns,
            final LongBitSet   allowRivers)
    {
        final long[] count = {0};
        LOG.debug("computing allowed");
        HandEnum.rivers(
                new UniqueFilter<CanonHole>(),
                new UniqueFilter<Flop>(),
                new UniqueFilter<Turn>(),
                Filters.not(new BitFilter<River>(allowRivers)),
                new Traverser<River>() {
            public void traverse(River river) {
                Turn      turn = river.turn();
                Flop      flop = turn.flop();
                CanonHole hole = flop.hole();

                char absoluteRiverBucket = bucketOf(tree, holes, river);
                if (offset <= absoluteRiverBucket &&
                              absoluteRiverBucket < (offset + length)) {

                    allowHoles .set(hole.canonIndex());
                    allowFlops .set(flop.canonIndex());
                    allowTurns .set(turn.canonIndex());
                    allowRivers.set(river.canonIndex());

                    checkpoint(count[0]++);
                }
            }});
    }

    private static char bucketOf(
            BucketTree tree, char[][][][] holes, River river)
    {
        Turn      turn = river.turn();
        Flop      flop = turn.flop();
        CanonHole hole = flop.hole();

        try
        {
            return holes[ tree.getHole (  hole.canonIndex() ) ]
                        [ tree.getFlop (  flop.canonIndex() ) ]
                        [ tree.getTurn (  turn.canonIndex() ) ]
                        [ tree.getRiver( river.canonIndex() ) ];
        }
        catch (Throwable t)
        {
            LOG.error("broken bucket structure: " + river);
            LOG.error(Arrays.asList(
                     (int) hole.canonIndex(),
                           flop.canonIndex(),
                           turn.canonIndex(),
                          river.canonIndex()));
            LOG.error(Arrays.asList(
                     tree.getHole (  hole.canonIndex() ),
                     tree.getFlop (  flop.canonIndex() ),
                     tree.getTurn (  turn.canonIndex() ),
                     tree.getRiver( river.canonIndex() )));
            LOG.error(Arrays.deepToString(holes));

            throw new Error(t);
        }
    }


    private static void checkpoint(long count)
    {
        if (count == 0) {
            LOG.debug("starting calculation cycle");
            return;
        }

        if ((count       % (     1000 * 1000)) == 0)
            System.out.print(".");

        if (((count + 1) % (     1000 * 1000 * 50)) == 0)
            System.out.println();

//        if (((count + 1) % (10 * 1000 * 1000 * 50)) == 0)
//            persistStrengths(hist, outFile);
    }
    

    //--------------------------------------------------------------------
    private final SlimRiverHist[] HIST;

//    private BucketOdds(SlimRiverHist[] hist)
//    {
//        HIST = hist;
//    }

    private BucketOdds(SlimRiverHist[] hist)
    {
        HIST = hist;
    }


    //--------------------------------------------------------------------
    public double nonLossProb(char index, char vsIndex)
    {
        return HIST[index].nonLossProb( HIST[vsIndex] );
    }

    public String status(char index)
    {
        SlimRiverHist h = HIST[index];
        return h.mean() + " with " + h.totalCount();
    }

    public SlimRiverHist strength(char index)
    {
        return HIST[ index ];
    }
}