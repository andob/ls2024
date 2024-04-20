
@Suppress("LocalVariableName")
fun main()
{
    val i = PossibleWorld(name = "i")
    val j = PossibleWorld(name = "j")
    val k = PossibleWorld(name = "k")

    val P = AtomicFormula(name = "P", world = i)
    val Q = AtomicFormula(name = "Q")
    val R = AtomicFormula(name = "R")

    //P -> Q, R -> Q ├─ (P v R) -> Q
    val premise1 = ComplexFormula(P, Operation.Imply, Q)
    val premise2 = ComplexFormula(R, Operation.Imply, Q)
    val conclusion = ComplexFormula(ComplexFormula(P, Operation.Or, R), Operation.Imply, Q)

    val theory = Theory(
        premises = listOf(premise1, premise2),
        conclusion = conclusion,
        possibleWorlds = listOf(i, j, k),
        shouldStopOnFirstContradiction = true,
    )

    val proofTree = theory.prove()
    println(proofTree)
}
