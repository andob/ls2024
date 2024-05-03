interface ILogic
{
    fun getRules() : Array<IRule>
    fun isOperationAvailable(operation : Operation) : Boolean
}

/*todo modal logic types:
* ρ (rho), reflexivity: for all w, wRw.
* σ (sigma), symmetry: for all w1 , w2 , if w1 Rw2 , then w2Rw1.
* τ (tau), transitivity: for all w1 , w2 , w3 , if w1 Rw2 and w2 Rw3, then w1Rw3.
* η (eta), extendability: for all w1 , there is a w2 such that w1 Rw2.
* K = {}
* T = { ρ }
* D = { η }
* B = { ρ, σ }
* S4 = { ρ, τ }
* S5 = { ρ, σ, τ }
*/
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
