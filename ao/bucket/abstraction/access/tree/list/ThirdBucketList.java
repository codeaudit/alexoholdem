package ao.bucket.abstraction.access.tree.list;

import ao.bucket.abstraction.access.tree.BucketList;
import ao.util.data.LongBitSet;

import java.io.File;

/**
 * Date: Jan 28, 2009
 * Time: 1:55:44 PM
 */
public class ThirdBucketList implements BucketList
{
    //--------------------------------------------------------------------
    private static final String A_FILE = "a";
    private static final String B_FILE = "b";
    private static final String C_FILE = "c";

    private static final int    A_BIT  =          1;
    private static final int    B_BIT  = A_BIT << 1;
    private static final int    C_BIT  = B_BIT << 1;


    //--------------------------------------------------------------------
    private final File       DIR;
    private final LongBitSet A, B, C;


    //--------------------------------------------------------------------
    public ThirdBucketList(File dir, long size)
    {
        DIR = dir;
        A   = retrieveOrCreate(new File(dir, A_FILE), size);
        B   = retrieveOrCreate(new File(dir, B_FILE), size);
        C   = retrieveOrCreate(new File(dir, C_FILE), size);
    }

    private static LongBitSet retrieveOrCreate(File from, long size)
    {
        LongBitSet bits = LongBitSet.retrieve(from);
        return bits == null
               ? new LongBitSet(size)
               : bits;
    }


    //--------------------------------------------------------------------
    public void set(long index, byte bucket)
    {
        assert 0 <= bucket && bucket <= 7;

        boolean a = ((bucket & A_BIT) == 0);
        boolean b = ((bucket & B_BIT) == 0);
        boolean c = ((bucket & C_BIT) == 0);

        A.set(index, a);
        B.set(index, b);
        C.set(index, c);
    }


    //--------------------------------------------------------------------
    public byte get(long index)
    {
        boolean a = A.get(index);
        boolean b = A.get(index);
        boolean c = A.get(index);

        int bucket = 0;
        if (a) bucket |= A_BIT;
        if (b) bucket |= B_BIT;
        if (c) bucket |= C_BIT;

        return (byte) bucket;
    }


    //--------------------------------------------------------------------
    public void flush()
    {
        LongBitSet.persist(A, new File(DIR, A_FILE));
        LongBitSet.persist(B, new File(DIR, B_FILE));
        LongBitSet.persist(C, new File(DIR, C_FILE));
    }
}