import com.cygns.logik.*

object ConfigParser
{
    fun parse(config : String) : Problem
    {
        val data = config.lines().map { line -> line.trim() }
            .filter { line -> line.isNotEmpty() }
            .map { line -> line.split("( )*=( )*".toRegex()) }
            .associate { tokens -> Pair(tokens[0], tokens[1].trim('\'')) }

        fun missing(what : String) = RuntimeException("Cannot parse input! Missing $what!")
        val description = data["description"] ?: ""
        val logicName = data["logic"] ?: throw missing("logic")
        val variableNames = data["vars"]?.split(",")?.map { it.trim() } ?: throw missing("vars")
        val premisesAsStrings = data.keys.filter { it.startsWith("premise") }.sorted().map { data[it]!!.trim() }
        val conclusionAsString = data["conclusion"]?.trim()?.ifEmpty { null } ?: throw missing("conclusion")

        val logic = when(logicName)
        {
            "PropositionalLogic" -> PropositionalLogic()
            "FirstOrderLogic" -> FirstOrderLogic()
            "KModalLogic" -> FirstOrderModalLogic(ModalLogicType.K)
            "TModalLogic" -> FirstOrderModalLogic(ModalLogicType.T)
            "DModalLogic" -> FirstOrderModalLogic(ModalLogicType.D)
            "BModalLogic" -> FirstOrderModalLogic(ModalLogicType.B)
            "NModalLogic" -> FirstOrderModalLogic(ModalLogicType.N)
            "S2ModalLogic" -> FirstOrderModalLogic(ModalLogicType.S2)
            "S3ModalLogic" -> FirstOrderModalLogic(ModalLogicType.S3)
            "S3.5ModalLogic" -> FirstOrderModalLogic(ModalLogicType.S35)
            "S4ModalLogic" -> FirstOrderModalLogic(ModalLogicType.S4)
            "S5ModalLogic" -> FirstOrderModalLogic(ModalLogicType.S5)
            "KTemporalModalLogic" -> FirstOrderModalLogic(ModalLogicType.Kᵗ)
            else -> throw RuntimeException("Invalid logic $logicName")
        }

        val formulaFactory = FormulaFactory(logic)
        val variables = variableNames.map { name -> formulaFactory.newAtom(name) }

        val converter = StatementToFormulaConverter(formulaFactory, variables)
        val premises = premisesAsStrings.map { premise -> converter.convert(premise) }
        val conclusion = converter.convert(conclusionAsString)

        return Problem(logic, premises, conclusion, description)
    }

    private class StatementToFormulaConverter
    (
        val formulaFactory : FormulaFactory,
        val variables : List<AtomicFormula>,
    )
    {
        fun convert(text : String) : IFormula
        {
            val statement = Logik.parse(text)
            val formula = convertNode(statement.baseNode)
            return formula
        }

        fun convertNode(node : Node) : IFormula
        {
            return when(node)
            {
                is Variable -> findAtomicFormula(node)
                is Not -> formulaFactory.new(Operation.Non, convertNode(node.arg))
                is And -> formulaFactory.new(convertNode(node.left), Operation.And, convertNode(node.right))
                is Or -> formulaFactory.new(convertNode(node.left), Operation.Or, convertNode(node.right))
                is Implies -> formulaFactory.new(convertNode(node.left), Operation.Imply, convertNode(node.right))
                is IfAndOnlyIf -> formulaFactory.new(convertNode(node.left), Operation.BiImply, convertNode(node.right))
                is StrictlyImplies -> formulaFactory.new(convertNode(node.left), Operation.StrictImply, convertNode(node.right))

                is NecessaryInFuture -> formulaFactory.new(Operation.Necessary.InFuture(), convertNode(node.arg))
                is NecessaryInPast -> formulaFactory.new(Operation.Necessary.InPast(), convertNode(node.arg))
                is Necessary -> formulaFactory.new(Operation.Necessary(), convertNode(node.arg))

                is PossibleInFuture -> formulaFactory.new(Operation.Possible.InFuture(), convertNode(node.arg))
                is PossibleInPast -> formulaFactory.new(Operation.Possible.InPast(), convertNode(node.arg))
                is Possible -> formulaFactory.new(Operation.Possible(), convertNode(node.arg))

                else -> throw RuntimeException("Invalid node $node")
            }
        }

        private fun findAtomicFormula(node : Variable) : AtomicFormula
        {
            val variableName = node.toString()
            val formula = variables.find { variable -> variable.name==variableName }
            if (formula != null)
                return formula

            throw RuntimeException("Atomic formula $node was not declared!")
        }
    }
}
