package ao.decision.data;

import ao.decision.attr.Attribute;
import ao.decision.attr.AttributeSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

/**
 *
 */
public class Context
{
    //--------------------------------------------------------------------
    private Map<Object, Attribute> ctx = new HashMap<Object, Attribute>();


    //--------------------------------------------------------------------
    public Context() {}
    public Context(Collection<Attribute> attributes)
    {
        for (Attribute<?> attr : attributes) add( attr );
    }


    //--------------------------------------------------------------------
    protected void add(Attribute<?> attr)
    {
        if (ctx.put(attr.set().type(), attr) != null)
        {
            throw new Error("duplicate type " + attr.set().type());
        }
    }


    //--------------------------------------------------------------------
    public <T> Example<T> withTarget(Attribute<T> targetAttribute)
    {
        return new Example<T>(attributes(), targetAttribute);
    }


    //--------------------------------------------------------------------
    public Collection<Attribute> attributes()
    {
        return ctx.values();
    }

    public Collection<AttributeSet<?>> attributeSets()
    {
        Collection<AttributeSet<?>> attributeSets =
                new ArrayList<AttributeSet<?>>();
        for (Attribute<?> attribute : attributes())
        {
            attributeSets.add( attribute.set() );
        }
        return attributeSets;
    }

    public Attribute<?> attribute(Object ofType)
    {
        return ctx.get( ofType );
    }

    @SuppressWarnings("unchecked")
    public <T> Attribute<T> attribute(AttributeSet<T> ofType)
    {
        return (ofType == null)
                ? null
                : (Attribute<T>) attribute( ofType.type() );
    }
}