fun hash(vararg items : Any?) : Int
{
    var hashCode = 0

    if (items.isNotEmpty())
    {
        hashCode = items[0]?.hashCode()?:0
        for (i in 1 until items.size)
        {
            hashCode = hashCode * 31 + (items[i]?.hashCode()?:0)
        }
    }

    return hashCode
}

class Lazy<T>(private val factory : () -> T)
{
    private var value : T? = null

    fun get() : T
    {
        if (value == null)
            value = factory()
        return value!!
    }
}
