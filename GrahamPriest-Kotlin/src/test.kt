
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
    val p = formulaFactory.newAtom("P")
    val q = formulaFactory.newAtom("Q")
    val r = formulaFactory.newAtom("R")

    val premise1 = formulaFactory.new(p, Operation.Imply, q)
    val premise2 = formulaFactory.new(r, Operation.Imply, q)
    val conclusion = formulaFactory.new(formulaFactory.new(p, Operation.Or, r), Operation.Imply, q)
    val proof = Theory(listOf(premise1, premise2), conclusion).prove()
    return "PROVE: { $premise1, $premise2 } ⊢ { $conclusion }\n$proof"
}

private fun exA2() : String
{
    // { P → (Q ∨ R), P & ~R } ⊢ { Q }
    val formulaFactory = FormulaFactory()
    val p = formulaFactory.newAtom("P")
    val q = formulaFactory.newAtom("Q")
    val r = formulaFactory.newAtom("R")

    val premise1 = formulaFactory.new(p, Operation.Imply, formulaFactory.new(q, Operation.Or, r))
    val premise2 = formulaFactory.new(p, Operation.And, formulaFactory.new(Operation.Non, r))
    val proof = Theory(listOf(premise1, premise2), q).prove()
    return "PROVE: { $premise1, $premise2 } ⊢ { $q }\n$proof"
}

private fun exA3() : String
{
    // { P ∨ (Q & R) } ⊢ { (P ∨ Q) & (P ∨ R) }
    val formulaFactory = FormulaFactory()
    val p = formulaFactory.newAtom("P")
    val q = formulaFactory.newAtom("Q")
    val r = formulaFactory.newAtom("R")

    val premise1 = formulaFactory.new(p, Operation.Or, formulaFactory.new(q, Operation.And, r))
    val conclusion = formulaFactory.new(formulaFactory.new(p, Operation.Or, q), Operation.And, formulaFactory.new(p, Operation.Or, r))
    val proof = Theory(listOf(premise1), conclusion).prove()
    return "PROVE: { $premise1 } ⊢ { $conclusion }\n$proof"
}

private fun exB1() : String
{
    // ⊢ { ◇P ↔ ~□~P }
    val formulaFactory = FormulaFactory()
    val p = formulaFactory.newAtom("P")

    val statement = formulaFactory.new(
        formulaFactory.new(Operation.Possible, p), Operation.BiImply,
        formulaFactory.new(Operation.Non, formulaFactory.new(Operation.Necessary, formulaFactory.new(Operation.Non, p))))
    val proof = Theory(listOf(), statement).prove()
    return "PROVE: ⊢ { $statement }\n$proof"
}

private fun exB2() : String
{
    // ⊢ { ◇(A ∨ B) → (◇A ∨ ◇B) }
    val formulaFactory = FormulaFactory()
    val p = formulaFactory.newAtom("A")
    val q = formulaFactory.newAtom("B")

    val statement = formulaFactory.new(
        formulaFactory.new(Operation.Possible, formulaFactory.new(p, Operation.Or, q)), Operation.Imply,
        formulaFactory.new(formulaFactory.new(Operation.Possible, p), Operation.Or, formulaFactory.new(Operation.Possible, q)))
    val proof = Theory(listOf(), statement).prove()
    return "PROVE: ⊢ { $statement }\n$proof"
}

private fun exB3() : String
{
    // { □(P → Q) } ⊢ { ◇P → ◇Q }
    val formulaFactory = FormulaFactory()
    val p = formulaFactory.newAtom("P")
    val q = formulaFactory.newAtom("Q")

    val premise = formulaFactory.new(Operation.Necessary, formulaFactory.new(p, Operation.Imply, q))
    val conclusion = formulaFactory.new(formulaFactory.new(Operation.Possible, p), Operation.Imply, formulaFactory.new(Operation.Possible, q))
    val proof = Theory(listOf(premise), conclusion).prove()
    return "PROVE: { $premise } ⊢ { $conclusion }\n$proof"
}

private fun exC1() : String
{
    // Hx = x is a human, Mx = x is mortal
    // { ∀x(Hx -> Mx), HSocrates } ⊢ { MSocrates }
    return ""
}

private fun exC2() : String
{
    // { ∀x(Px → ∃y.Sxy) } ⊢ { ∀x.∃y(Px → Sxy ) }
    return ""
}

private fun exC3() : String
{
    // { ∃x.~∃y.Sxy } ⊢ { ~∃x.∀y.Sxy }
    return ""
}

private fun exC4() : String
{
    // ⊢ { ∃x.∃y.Pxy ↔ ∃x.∃y.Pyx }
    return ""
}
