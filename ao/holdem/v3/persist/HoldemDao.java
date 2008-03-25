package ao.holdem.v3.persist;

import ao.holdem.v3.model.Avatar;
import ao.holdem.v3.model.hand.Replay;
import ao.util.rand.Rand;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.collections.StoredMap;
import com.sleepycat.collections.TransactionWorker;
import com.sleepycat.je.*;

import java.util.*;

/**
 *
 */
public class HoldemDao
{
    //--------------------------------------------------------------------
    private HoldemDb       db;
    private StoredMap      hands;
    private StoredMap      avatars;


    //--------------------------------------------------------------------
    public HoldemDao(HoldemDb database, HoldemViews views)
    {
        db      = database;
        hands   = views.hands();
        avatars = views.avatars();
    }


    //--------------------------------------------------------------------
    public void presist(final Replay hand)
    {
        db.atomic(new TransactionWorker() {
            public void doWork() throws Exception {
                hands.put(hand.id(), hand);

                for (Avatar avatar : hand.players())
                {
                    avatars.put(avatar, hand.id());
                }
            }
        });
    }


    //--------------------------------------------------------------------
    public List<Replay> retrieve(Avatar withPlayer)
    {
        return retrieve(withPlayer, Integer.MAX_VALUE);
    }

    @SuppressWarnings("unchecked")
    public List<Replay> retrieve(
            Avatar withPlayer, int numLatest)
    {
        List<Replay> playerHands = new ArrayList<Replay>();

        int count = 0;
        for (Object handId : avatars.duplicates(withPlayer))
        {
            if (count++ >= numLatest) break;

            playerHands.add(
                    (Replay) hands.get( handId ));
        }

        return playerHands;
    }


    //--------------------------------------------------------------------
    public void printByPrevalence(int howMany)
    {
        for (Map.Entry<Avatar, Integer> e :
                mostPrevalent(howMany).entrySet())
        {
            System.out.println(e.getKey() + ": " + e.getValue());
        }
    }

    public Map<Avatar, Integer> mostPrevalent(int howMany)
    {
        Map<Avatar, Integer> byAvatar =
                new LinkedHashMap<Avatar, Integer>();
        for (Map.Entry<Double, Avatar> e :
                safeByHands(howMany).entrySet())
        {
            byAvatar.put(e.getValue(),
                         (int) Math.round(e.getKey()));
        }
        return byAvatar;
    }

    private SortedMap<Double, Avatar> safeByHands(int howMany)
    {
        try
        {
            return byHands(howMany);
        }
        catch (DatabaseException e)
        {
            throw new Error( e );
        }
    }
    private SortedMap<Double, Avatar> byHands(int howMany)
            throws DatabaseException
    {
        SortedMap<Double, Avatar> byHands =
                new TreeMap<Double, Avatar>();

        double min    = Long.MIN_VALUE;
        Cursor cursor = db.openAvatarCursor();

        DatabaseEntry foundKey  = new DatabaseEntry();
        DatabaseEntry foundData = new DatabaseEntry();

        while (cursor.getNextNoDup(
                    foundKey,
                    foundData,
                    LockMode.DEFAULT) ==
                      OperationStatus.SUCCESS)
        {
            Avatar avatar =
                    Avatar.BINDING.entryToObject(
                            new TupleInput(foundKey.getData()));

            int    count      = cursor.count();
            double fuzzyCount = count + Rand.nextDouble();
            if (min < fuzzyCount ||
                byHands.size() < howMany)
            {
                byHands.put(fuzzyCount, avatar);
                if (byHands.size() > howMany)
                {
                    byHands.remove( byHands.firstKey() );
                }
                min = byHands.firstKey();
            }
        }
        cursor.close();

        return byHands;
    }
}
