interface ILogic
{
    fun getRules() : Array<IRule>
    fun isOperationAvailable(operation : Operation) : Boolean
}

val DEFAULT_LOGIC = FirstOrderModalLogic(ModalLogicType.S5)

private val BASIC_RULES = arrayOf(
    DoubleNegationRule(),
    OrRule(),
    NotOrRule(),
    AndRule(),
    NotAndRule(),
    ImplyRule(),
    NotImplyRule(),
    BiImplyRule(),
    NotBiImplyRule(),
)

private val BASIC_OPERATIONS = arrayOf(
    Operation.Non,
    Operation.Or,
    Operation.And,
    Operation.Imply,
    Operation.BiImply
)

class PropositionalLogic : ILogic
{
    override fun getRules() : Array<IRule> = BASIC_RULES

    override fun isOperationAvailable(operation : Operation) : Boolean
    {
        return operation in BASIC_OPERATIONS
    }
}

class FirstOrderLogic : ILogic
{
    override fun getRules() : Array<IRule>
    {
        return arrayOf(*BASIC_RULES,
            NotExistsRule(), NotForAllRule(), ExistsRule(), ForAllRule())
    }

    override fun isOperationAvailable(operation : Operation) : Boolean
    {
        return operation in BASIC_OPERATIONS || operation is Operation.ForAll || operation is Operation.Exists
    }
}

enum class ModalLogicType { S5 }

class ModalLogic(val type : ModalLogicType) : ILogic
{
    override fun getRules() : Array<IRule>
    {
        return arrayOf(*BASIC_RULES,
            NotNecessaryRule(), NotPossibleRule(), NecessaryRule(), PossibleRule())
    }

    override fun isOperationAvailable(operation : Operation) : Boolean
    {
        return operation in BASIC_OPERATIONS || operation == Operation.Possible || operation == Operation.Necessary
    }
}

class FirstOrderModalLogic(val type : ModalLogicType) : ILogic
{
    override fun getRules() : Array<IRule>
    {
        return arrayOf(*BASIC_RULES,
            NotNecessaryRule(), NotPossibleRule(), NecessaryRule(), PossibleRule(),
            NotExistsRule(), NotForAllRule(), ExistsRule(), ForAllRule())
    }

    override fun isOperationAvailable(operation : Operation) : Boolean
    {
        return operation in BASIC_OPERATIONS || operation == Operation.Possible || operation == Operation.Necessary
                || operation is Operation.ForAll || operation is Operation.Exists
    }
}
