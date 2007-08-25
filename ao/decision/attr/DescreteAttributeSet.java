package ao.decision.attr;

import java.util.*;

/**
 *
 */
public class DescreteAttributeSet<T>
        implements AttributeSet<T>
{
    //--------------------------------------------------------------------
    private final Object               type;
    private final Map<T, Attribute<T>> attributes;
    private       int                  nextId = 0;


    //--------------------------------------------------------------------
    public DescreteAttributeSet(Object type)
    {
        this.type       = type;
        this.attributes = new LinkedHashMap<T, Attribute<T>>();
    }


    //--------------------------------------------------------------------
    public void add(T val)
    {
        if (! attributes.containsKey(val))
        {
            attributes.put( val,
                            new Attribute<T>(this, val, nextId++));
        }
    }


    //--------------------------------------------------------------------
    public boolean isDescrete() {  return true;  }

    public boolean typeEquals(AttributeSet<?> attributeSet)
    {
        return type().equals( attributeSet.type() );
    }

    public Object type()
    {
        return type;
    }


    //--------------------------------------------------------------------
    public Attribute<T> instanceOf(T value)
    {
        add(value);
        return attributes.get( value ); 
    }


    //--------------------------------------------------------------------
    public Collection<Attribute<T>> values()
    {
        return attributes.values();
    }
}
