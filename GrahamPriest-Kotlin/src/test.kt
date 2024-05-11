@file:Suppress("LocalVariableName")
fun test(log : (String) -> Unit)
{
    val functions = arrayOf(
        ::exProp1, ::exProp2, ::exProp3,
        ::exModal1, ::exModal2, ::exModal3,
        ::exPred1, ::exPred2, ::exPred3, ::exPred4,
        ::exModal4, ::exModal5, ::exModal6,
        ::exModal7, ::exModal8, ::exModal9
    )

    for (function in functions)
        function(log)
}

private fun exProp1(log : (String) -> Unit)
{
    // { P → Q, R → Q } ⊢ { (P ∨ R) → Q }
    val logic = PropositionalLogic()
    val formulaFactory = FormulaFactory(logic)
    val P = formulaFactory.newAtom("P")
    val Q = formulaFactory.newAtom("Q")
    val R = formulaFactory.newAtom("R")

    val premise1 = formulaFactory.new(P, Operation.Imply, Q)
    val premise2 = formulaFactory.new(R, Operation.Imply, Q)
    val conclusion = formulaFactory.new(formulaFactory.new(P, Operation.Or, R), Operation.Imply, Q)
    val proof = Theory(logic, listOf(premise1, premise2), conclusion).prove()
    log("PROVE: { $premise1, $premise2 } ⊢ { $conclusion }\n$proof")
}

private fun exProp2(log : (String) -> Unit)
{
    // { P → (Q ∨ R), P & ~R } ⊢ { Q }
    val logic = PropositionalLogic()
    val formulaFactory = FormulaFactory(logic)
    val P = formulaFactory.newAtom("P")
    val Q = formulaFactory.newAtom("Q")
    val R = formulaFactory.newAtom("R")

    val premise1 = formulaFactory.new(P, Operation.Imply, formulaFactory.new(Q, Operation.Or, R))
    val premise2 = formulaFactory.new(P, Operation.And, formulaFactory.new(Operation.Non, R))
    val proof = Theory(logic, listOf(premise1, premise2), Q).prove()
    log("PROVE: { $premise1, $premise2 } ⊢ { $Q }\n$proof")
}

private fun exProp3(log : (String) -> Unit)
{
    // { P ∨ (Q & R) } ⊢ { (P ∨ Q) & (P ∨ R) }
    val logic = PropositionalLogic()
    val formulaFactory = FormulaFactory(logic)
    val P = formulaFactory.newAtom("P")
    val Q = formulaFactory.newAtom("Q")
    val R = formulaFactory.newAtom("R")

    val premise1 = formulaFactory.new(P, Operation.Or, formulaFactory.new(Q, Operation.And, R))
    val conclusion = formulaFactory.new(formulaFactory.new(P, Operation.Or, Q), Operation.And, formulaFactory.new(P, Operation.Or, R))
    val proof = Theory(logic, listOf(premise1), conclusion).prove()
    log("PROVE: { $premise1 } ⊢ { $conclusion }\n$proof")
}

private fun exModal1(log : (String) -> Unit)
{
    // ⊢ₖ { ◇P ↔ ~□~P }
    val logic = ModalLogic(ModalLogicType.K)
    val formulaFactory = FormulaFactory(logic)
    val P = formulaFactory.newAtom("P")

    val statement = formulaFactory.new(
        formulaFactory.new(Operation.Possible, P), Operation.BiImply,
        formulaFactory.new(Operation.Non, formulaFactory.new(Operation.Necessary, formulaFactory.new(Operation.Non, P))))
    val proof = Theory(logic, listOf(), statement).prove()
    log("PROVE: ⊢ₖ { $statement }\n$proof")
}

private fun exModal2(log : (String) -> Unit)
{
    // ⊢ₖ { ◇(P ∨ Q) → (◇P ∨ ◇Q) }
    val logic = ModalLogic(ModalLogicType.K)
    val formulaFactory = FormulaFactory(logic)
    val P = formulaFactory.newAtom("P")
    val Q = formulaFactory.newAtom("Q")

    val statement = formulaFactory.new(
        formulaFactory.new(Operation.Possible, formulaFactory.new(P, Operation.Or, Q)), Operation.Imply,
        formulaFactory.new(formulaFactory.new(Operation.Possible, P), Operation.Or, formulaFactory.new(Operation.Possible, Q)))
    val proof = Theory(logic, listOf(), statement).prove()
    log("PROVE: ⊢ₖ { $statement }\n$proof")
}

private fun exModal3(log : (String) -> Unit)
{
    // { □(P → Q) } ⊢ₖ { ◇P → ◇Q }
    val logic = ModalLogic(ModalLogicType.K)
    val formulaFactory = FormulaFactory(logic)
    val P = formulaFactory.newAtom("P")
    val Q = formulaFactory.newAtom("Q")

    val premise = formulaFactory.new(Operation.Necessary, formulaFactory.new(P, Operation.Imply, Q))
    val conclusion = formulaFactory.new(formulaFactory.new(Operation.Possible, P), Operation.Imply, formulaFactory.new(Operation.Possible, Q))
    val proof = Theory(logic, listOf(premise), conclusion).prove()
    log("PROVE: { $premise } ⊢ₖ { $conclusion }\n$proof")
}

private fun exModal4(log : (String) -> Unit)
{
    // { □(◇P & ◇Q) } ⊢ₖ { □◇Q }
    val logic = ModalLogic(ModalLogicType.K)
    val formulaFactory = FormulaFactory(logic)
    val P = formulaFactory.newAtom("P")
    val Q = formulaFactory.newAtom("Q")

    val premise = formulaFactory.new(Operation.Necessary, formulaFactory.new(
        formulaFactory.new(Operation.Possible, P), Operation.And, formulaFactory.new(Operation.Possible, Q)))
    val conclusion = formulaFactory.new(Operation.Necessary, formulaFactory.new(Operation.Possible, Q))
    val proof = Theory(logic, listOf(premise), conclusion).prove()
    log("PROVE: { $premise } ⊢ₖ { $conclusion }\n$proof")
}

private fun exModal5(log : (String) -> Unit)
{
    // ⊢ᵦ { □P v □Q } ↔ { □(□P v □Q) }
    val logic = ModalLogic(ModalLogicType.B)
    val formulaFactory = FormulaFactory(logic)
    val P = formulaFactory.newAtom("P")
    val Q = formulaFactory.newAtom("Q")

    val statement = formulaFactory.new(
        formulaFactory.new(formulaFactory.new(Operation.Necessary, P), Operation.Or, formulaFactory.new(Operation.Necessary, Q)),
        Operation.BiImply, formulaFactory.new(Operation.Necessary, formulaFactory.new(
            formulaFactory.new(Operation.Necessary, P), Operation.Or, formulaFactory.new(Operation.Necessary, Q))))
    val proof = Theory(logic, listOf(), statement).prove()
    log("PROVE: ⊢ᵦ { $statement }\n$proof")
}

private fun exModal6(log : (String) -> Unit)
{
    // ⊢ₛ₅ ◇P → ◇◇P
    val logic = ModalLogic(ModalLogicType.S5)
    val formulaFactory = FormulaFactory(logic)
    val P = formulaFactory.newAtom("P")

    val statement = formulaFactory.new(formulaFactory.new(Operation.Possible, P), Operation.Imply,
        formulaFactory.new(Operation.Possible, formulaFactory.new(Operation.Possible, P)))
    val proof = Theory(logic, listOf(), statement).prove()
    log("PROVE: ⊢ₛ₅ { $statement }\n$proof")
}

private fun exModal7(log : (String) -> Unit)
{
    // ⊢ₛ₅ ◇P → □◇P
    val logic = ModalLogic(ModalLogicType.S5)
    val formulaFactory = FormulaFactory(logic)
    val P = formulaFactory.newAtom("P")

    val statement = formulaFactory.new(formulaFactory.new(Operation.Possible, P), Operation.Imply,
        formulaFactory.new(Operation.Necessary, formulaFactory.new(Operation.Possible, P)))
    val proof = Theory(logic, listOf(), statement).prove()
    log("PROVE: ⊢ₛ₅ { $statement }\n$proof")
}

private fun exModal8(log : (String) -> Unit)
{
    // ⊢ₛ₅ □(□P → □Q) ∨ □(□Q → □P)
    val logic = ModalLogic(ModalLogicType.S5)
    val formulaFactory = FormulaFactory(logic)
    val P = formulaFactory.newAtom("P")
    val Q = formulaFactory.newAtom("Q")

    val statement = formulaFactory.new(formulaFactory.new(Operation.Necessary,
        formulaFactory.new(formulaFactory.new(Operation.Necessary, P), Operation.Imply, formulaFactory.new(Operation.Necessary, Q))),
        Operation.Or, formulaFactory.new(Operation.Necessary,
        formulaFactory.new(formulaFactory.new(Operation.Necessary, Q), Operation.Imply, formulaFactory.new(Operation.Necessary, P))))
    val proof = Theory(logic, listOf(), statement).prove()
    log("PROVE: ⊢ₛ₅ { $statement }\n$proof")
}

private fun exModal9(log : (String) -> Unit)
{
    // ⊢ₛ₅ □(◇P → Q) ↔ □(P → □Q)
    val logic = ModalLogic(ModalLogicType.S5)
    val formulaFactory = FormulaFactory(logic)
    val P = formulaFactory.newAtom("P")
    val Q = formulaFactory.newAtom("Q")

    val statement = formulaFactory.new(formulaFactory.new(Operation.Necessary,
        formulaFactory.new(formulaFactory.new(Operation.Possible, P), Operation.Imply, Q)),
        Operation.BiImply, formulaFactory.new(Operation.Necessary,
        formulaFactory.new(P, Operation.Imply, formulaFactory.new(Operation.Necessary, Q))))
    val proof = Theory(logic, listOf(), statement).prove()
    log("PROVE: ⊢ₛ₅ { $statement }\n$proof")
}

private fun exPred1(log : (String) -> Unit)
{
    // { ∀x(Human(x) -> Mortal(x)), Human(Socrates) } ⊢ { Mortal(Socrates) }
    val logic = FirstOrderLogic()
    val formulaFactory = FormulaFactory(logic)
    val x = formulaFactory.newBindingVariable("x")
    val Socrates = formulaFactory.newUnboundedVariable("Socrates")
    val Human = formulaFactory.newAtom("Human")
    val Mortal = formulaFactory.newAtom(("Mortal"))

    val premise1 = formulaFactory.new(Operation.ForAll(x), formulaFactory.new(Human(x), Operation.Imply, Mortal(x)))
    val premise2 = Human(Socrates)
    val conclusion = Mortal(Socrates)
    val proof = Theory(logic, listOf(premise1, premise2), conclusion).prove()
    log("PROVE: { $premise1, $premise2 } ⊢ { $conclusion }\n$proof")
}

private fun exPred2(log : (String) -> Unit)
{
    // { ∀x(Px → ∃y.Sxy) } ⊢ { ∀x.∃y(Px → Sxy ) }
    val logic = FirstOrderLogic()
    val formulaFactory = FormulaFactory(logic)
    val x = formulaFactory.newBindingVariable("x")
    val y = formulaFactory.newBindingVariable("y")
    val P = formulaFactory.newAtom("P")
    val S = formulaFactory.newAtom("S")

    val premise = formulaFactory.new(Operation.ForAll(x), formulaFactory.new(P(x), Operation.Imply, formulaFactory.new(Operation.Exists(y), S(x, y))))
    val conclusion = formulaFactory.new(Operation.ForAll(x), formulaFactory.new(Operation.Exists(y), formulaFactory.new(P(x), Operation.Imply, S(x, y))))
    val proof = Theory(logic, listOf(premise), conclusion).prove()
    log("PROVE: { $premise } ⊢ { $conclusion }\n$proof")
}

private fun exPred3(log : (String) -> Unit)
{
    // { ∃x.~∃y.Sxy } ⊢ { ~∃x.∀y.Sxy }
    val logic = FirstOrderLogic()
    val formulaFactory = FormulaFactory(logic)
    val x = formulaFactory.newBindingVariable("x")
    val y = formulaFactory.newBindingVariable("y")
    val S = formulaFactory.newAtom("S")

    val premise = formulaFactory.new(Operation.Exists(x), formulaFactory.new(Operation.Non, formulaFactory.new(Operation.Exists(y), S(x, y))))
    val conclusion = formulaFactory.new(Operation.Non, formulaFactory.new(Operation.Exists(x), formulaFactory.new(Operation.ForAll(y), S(x, y))))
    val proof = Theory(logic, listOf(premise), conclusion).prove()
    log("PROVE: { $premise } ⊢ { $conclusion }\n$proof")
}

private fun exPred4(log : (String) -> Unit)
{
    // ⊢ { ∃x.∃y.Pxy ↔ ∃x.∃y.Pyx }
    val logic = FirstOrderLogic()
    val formulaFactory = FormulaFactory(logic)
    val x = formulaFactory.newBindingVariable("x")
    val y = formulaFactory.newBindingVariable("y")
    val P = formulaFactory.newAtom("P")

    x.couldBecomeEquivalentTo(y)
    y.couldBecomeEquivalentTo(x)

    val statement = formulaFactory.new(
        formulaFactory.new(Operation.Exists(x), formulaFactory.new(Operation.Exists(y), P(x, y))), Operation.BiImply,
        formulaFactory.new(Operation.Exists(x), formulaFactory.new(Operation.Exists(y), P(y, x))))
    val proof = Theory(logic, listOf(), statement).prove()
    log("PROVE: ⊢ { $statement }\n$proof")
}
