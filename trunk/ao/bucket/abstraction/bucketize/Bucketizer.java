package ao.bucket.abstraction.bucketize;

import ao.bucket.abstraction.access.tree.BucketTree.Branch;

/**
 * Date: Jan 9, 2009
 * Time: 10:01:28 AM
 */
public interface Bucketizer
{
    public boolean bucketize(Branch branch,
                             byte   numBuckets);

    public String id();
}