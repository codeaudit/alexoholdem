package ao.regret.alexo.node;

import ao.regret.InfoNode;
import ao.regret.alexo.AlexoBucket;
import ao.simple.alexo.AlexoAction;
import ao.simple.alexo.state.AlexoState;
import ao.util.text.Txt;

import java.util.Map;

/**
 * 
 */
public class OpponentNode implements PlayerNode
{
    //--------------------------------------------------------------------
    private PlayerKids kids;


    //--------------------------------------------------------------------
    public OpponentNode(AlexoState  state,
                        AlexoBucket bucket,
                        boolean     forFirstToAct)
    {
        kids = new PlayerKids(state, bucket, forFirstToAct, false);
    }


    //--------------------------------------------------------------------
    public InfoNode child(AlexoAction forAction)
    {
        return kids.child( forAction );
    }


    //--------------------------------------------------------------------
    public String toString()
    {
        return toString(0);
    }

    public String toString(int depth)
    {
        StringBuilder str = new StringBuilder();
        for (Map.Entry<AlexoAction, InfoNode> kid : kids.entrySet())
        {
            str.append( Txt.nTimes("\t", depth) )
               .append( kid.getKey() )
               .append( "\n" )
               .append( kid.getValue().toString(depth + 1) )
               .append( "\n" );
        }
        return str.substring(0, str.length()-1);
    }
}