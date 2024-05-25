interface ILogic
{
    fun getRules() : Array<IRule>
    fun isOperationAvailable(operation : Operation) : Boolean
    fun clearCache()
}

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

    override fun clearCache() {}
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

    override fun clearCache() {}
}

class FirstOrderModalLogic(val type : ModalLogicType) : ILogic
{
    val previousResultsOfNecessaryRule = NecessaryRule.PreviousResults()

    override fun getRules() : Array<IRule>
    {
        return arrayOf(*BASIC_RULES,
            NotNecessaryRule(), NotPossibleRule(), NecessaryRule(), PossibleRule(),
            NotExistsRule(), NotForAllRule(), ExistsRule(), ForAllRule(),
            StrictImplicationRule(), NotStrictImplicationRule())
    }

    override fun isOperationAvailable(operation : Operation) : Boolean
    {
        return operation in BASIC_OPERATIONS
                || operation is Operation.Possible || operation is Operation.Necessary
                || operation is Operation.ForAll || operation is Operation.Exists
                || operation == Operation.StrictImply
    }

    override fun clearCache()
    {
        previousResultsOfNecessaryRule.clear()
    }
}
