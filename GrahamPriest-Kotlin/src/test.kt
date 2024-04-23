@file:Suppress("LocalVariableName")
fun test() : String
{
    return listOf(::exA1, ::exA2, ::exA3, ::exB1, ::exB2, ::exB3, ::exC1, ::exC2, ::exC3, ::exC4)
        .map { function -> function.invoke() }
        .joinToString(separator = "\n\n\n")
}

private fun exA1() : String
{
    // { P → Q, R → Q } ⊢ { (P ∨ R) → Q }
    val formulaFactory = FormulaFactory()
    val P = formulaFactory.newAtom("P")
    val Q = formulaFactory.newAtom("Q")
    val R = formulaFactory.newAtom("R")

    val premise1 = formulaFactory.new(P, Operation.Imply, Q)
    val premise2 = formulaFactory.new(R, Operation.Imply, Q)
    val conclusion = formulaFactory.new(formulaFactory.new(P, Operation.Or, R), Operation.Imply, Q)
    val proof = Theory(listOf(premise1, premise2), conclusion).prove()
    return "PROVE: { $premise1, $premise2 } ⊢ { $conclusion }\n$proof"
}

private fun exA2() : String
{
    // { P → (Q ∨ R), P & ~R } ⊢ { Q }
    val formulaFactory = FormulaFactory()
    val P = formulaFactory.newAtom("P")
    val Q = formulaFactory.newAtom("Q")
    val R = formulaFactory.newAtom("R")

    val premise1 = formulaFactory.new(P, Operation.Imply, formulaFactory.new(Q, Operation.Or, R))
    val premise2 = formulaFactory.new(P, Operation.And, formulaFactory.new(Operation.Non, R))
    val proof = Theory(listOf(premise1, premise2), Q).prove()
    return "PROVE: { $premise1, $premise2 } ⊢ { $Q }\n$proof"
}

private fun exA3() : String
{
    // { P ∨ (Q & R) } ⊢ { (P ∨ Q) & (P ∨ R) }
    val formulaFactory = FormulaFactory()
    val P = formulaFactory.newAtom("P")
    val Q = formulaFactory.newAtom("Q")
    val R = formulaFactory.newAtom("R")

    val premise1 = formulaFactory.new(P, Operation.Or, formulaFactory.new(Q, Operation.And, R))
    val conclusion = formulaFactory.new(formulaFactory.new(P, Operation.Or, Q), Operation.And, formulaFactory.new(P, Operation.Or, R))
    val proof = Theory(listOf(premise1), conclusion).prove()
    return "PROVE: { $premise1 } ⊢ { $conclusion }\n$proof"
}

private fun exB1() : String
{
    // ⊢ { ◇P ↔ ~□~P }
    val formulaFactory = FormulaFactory()
    val P = formulaFactory.newAtom("P")

    val statement = formulaFactory.new(
        formulaFactory.new(Operation.Possible, P), Operation.BiImply,
        formulaFactory.new(Operation.Non, formulaFactory.new(Operation.Necessary, formulaFactory.new(Operation.Non, P))))
    val proof = Theory(listOf(), statement).prove()
    return "PROVE: ⊢ { $statement }\n$proof"
}

private fun exB2() : String
{
    // ⊢ { ◇(A ∨ B) → (◇A ∨ ◇B) }
    val formulaFactory = FormulaFactory()
    val A = formulaFactory.newAtom("A")
    val B = formulaFactory.newAtom("B")

    val statement = formulaFactory.new(
        formulaFactory.new(Operation.Possible, formulaFactory.new(A, Operation.Or, B)), Operation.Imply,
        formulaFactory.new(formulaFactory.new(Operation.Possible, A), Operation.Or, formulaFactory.new(Operation.Possible, B)))
    val proof = Theory(listOf(), statement).prove()
    return "PROVE: ⊢ { $statement }\n$proof"
}

private fun exB3() : String
{
    // { □(P → Q) } ⊢ { ◇P → ◇Q }
    val formulaFactory = FormulaFactory()
    val P = formulaFactory.newAtom("P")
    val Q = formulaFactory.newAtom("Q")

    val premise = formulaFactory.new(Operation.Necessary, formulaFactory.new(P, Operation.Imply, Q))
    val conclusion = formulaFactory.new(formulaFactory.new(Operation.Possible, P), Operation.Imply, formulaFactory.new(Operation.Possible, Q))
    val proof = Theory(listOf(premise), conclusion).prove()
    return "PROVE: { $premise } ⊢ { $conclusion }\n$proof"
}

private fun exC1() : String
{
    // { ∀x(Human(x) -> Mortal(x)), Human(Socrates) } ⊢ { Mortal(Socrates) }
    val formulaFactory = FormulaFactory()
    val x = formulaFactory.newBindingVariable("x")
    val Socrates = formulaFactory.newUnboundedVariable("Socrates")
    val Human = formulaFactory.newAtom("Human")
    val Mortal = formulaFactory.newAtom(("Mortal"))

    val premise1 = formulaFactory.new(Operation.ForAll(x), formulaFactory.new(Human(x), Operation.Imply, Mortal(x)))
    val premise2 = Human(Socrates)
    val conclusion = Mortal(Socrates)
    val proof = Theory(listOf(premise1, premise2), conclusion).prove()
    return "PROVE: { $premise1, $premise2 } ⊢ { $conclusion }\n$proof"
}

private fun exC2() : String
{
    // { ∀x(Px → ∃y.Sxy) } ⊢ { ∀x.∃y(Px → Sxy ) }
    val formulaFactory = FormulaFactory()
    val x = formulaFactory.newBindingVariable("x")
    val y = formulaFactory.newBindingVariable("y")
    val P = formulaFactory.newAtom("P")
    val S = formulaFactory.newAtom("S")

    val premise = formulaFactory.new(Operation.ForAll(x), formulaFactory.new(P(x), Operation.Imply, formulaFactory.new(Operation.Exists(y), S(x, y))))
    val conclusion = formulaFactory.new(Operation.ForAll(x), formulaFactory.new(Operation.Exists(y), formulaFactory.new(P(x), Operation.Imply, S(x, y))))
    val proof = Theory(listOf(premise), conclusion).prove()
    return "PROVE: { $premise } ⊢ { $conclusion }\n$proof"
}

private fun exC3() : String
{
    // { ∃x.~∃y.Sxy } ⊢ { ~∃x.∀y.Sxy }
    val formulaFactory = FormulaFactory()
    val x = formulaFactory.newBindingVariable("x")
    val y = formulaFactory.newBindingVariable("y")
    val S = formulaFactory.newAtom("S")

    val premise = formulaFactory.new(Operation.Exists(x), formulaFactory.new(Operation.Non, formulaFactory.new(Operation.Exists(y), S(x, y))))
    val conclusion = formulaFactory.new(Operation.Non, formulaFactory.new(Operation.Exists(x), formulaFactory.new(Operation.ForAll(y), S(x, y))))
    val proof = Theory(listOf(premise), conclusion).prove()
    return "PROVE: { $premise } ⊢ { $conclusion }\n$proof"
}

private fun exC4() : String
{
    // ⊢ { ∃x.∃y.Pxy ↔ ∃x.∃y.Pyx }
    val formulaFactory = FormulaFactory()
    val x = formulaFactory.newBindingVariable("x")
    val y = formulaFactory.newBindingVariable("y")
    val P = formulaFactory.newAtom("P")

    x.couldBecomeEquivalentTo(y)
    y.couldBecomeEquivalentTo(x)

    val statement = formulaFactory.new(
        formulaFactory.new(Operation.Exists(x), formulaFactory.new(Operation.Exists(y), P(x, y))), Operation.BiImply,
        formulaFactory.new(Operation.Exists(x), formulaFactory.new(Operation.Exists(y), P(y, x))))
    val proof = Theory(listOf(), statement).prove()
    return "PROVE: ⊢ { $statement }\n$proof"
}
