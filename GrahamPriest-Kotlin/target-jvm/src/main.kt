import java.io.File

fun log(logLine : String) = println(logLine)

fun main(args : Array<String>)
{
    if (args.isEmpty())
    {
        testExPred1(::log)
        testExPred2(::log)
        testExPred3(::log)
        testExPred4(::log)

        testAllProblemsFromFile(File("./backend/demo.ini"))
    }
    else
    {
        val problem = Problem.fromConfig(args.joinToString(separator = "\n"))
        val proof = problem.prove()
        log(proof.toString())
    }
}

private fun testAllProblemsFromFile(iniFile : File)
{
    var currentProblemName = ""
    var currentProblemContents = StringBuilder()

    val problems : MutableList<Pair<String, String>> = mutableListOf()

    for (iniFileLine in iniFile.readText().lines())
    {
        if (iniFileLine.startsWith("["))
        {
            if (currentProblemContents.isNotEmpty())
            {
                problems.add(Pair(currentProblemName, currentProblemContents.toString()))
                currentProblemContents = StringBuilder()
            }

            currentProblemName = iniFileLine.trimStart('[').trim().trimEnd(']')
        }
        else
        {
            currentProblemContents.append(iniFileLine).append('\n')
        }
    }

    problems.add(Pair(currentProblemName, currentProblemContents.toString()))

    for ((problemName, problemContents) in problems)
    {
        val problem = Problem.fromConfig(problemContents)
        val proof = problem.prove()
        log("${proof}\n\n")

        val expectedNotProved = problemContents.contains("NOT PROVED! [EXPECTED]")
        val expectedProved = !expectedNotProved && problemContents.contains("PROVED! [EXPECTED]")

        if (expectedNotProved && proof.isProofCorrect)
        {
            throw RuntimeException("Problem $problemName: expected NOT PROVED but got PROVED!")
        }

        if (expectedProved && !proof.isProofCorrect)
        {
            throw RuntimeException("Problem $problemName: expected PROVED but got NOT PROVED!")
        }
    }

    log("ALL TESTS PASSED!\n")
}

private fun testExPred1(log : (String) -> Unit)
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
    val proof = Problem(logic, listOf(premise1, premise2), conclusion).prove()
    log("PROVE: { $premise1, $premise2 } ⊢ { $conclusion }\n$proof\n\n")
    if (!proof.isProofCorrect)
        throw RuntimeException("EX pred 1 not proved!")
}

private fun testExPred2(log : (String) -> Unit)
{
    // { ∀x(Px ⊃ ∃y.Sxy) } ⊢ { ∀x.∃y(Px ⊃ Sxy ) }
    val logic = FirstOrderLogic()
    val formulaFactory = FormulaFactory(logic)
    val x = formulaFactory.newBindingVariable("x")
    val y = formulaFactory.newBindingVariable("y")
    val P = formulaFactory.newAtom("P")
    val S = formulaFactory.newAtom("S")

    val premise = formulaFactory.new(Operation.ForAll(x), formulaFactory.new(P(x), Operation.Imply, formulaFactory.new(Operation.Exists(y), S(x, y))))
    val conclusion = formulaFactory.new(Operation.ForAll(x), formulaFactory.new(Operation.Exists(y), formulaFactory.new(P(x), Operation.Imply, S(x, y))))
    val proof = Problem(logic, listOf(premise), conclusion).prove()
    log("PROVE: { $premise } ⊢ { $conclusion }\n$proof\n\n")
    if (!proof.isProofCorrect)
        throw RuntimeException("EX pred 2 not proved!")
}

private fun testExPred3(log : (String) -> Unit)
{
    // { ∃x.¬∃y.Sxy } ⊢ { ¬∃x.∀y.Sxy }
    val logic = FirstOrderLogic()
    val formulaFactory = FormulaFactory(logic)
    val x = formulaFactory.newBindingVariable("x")
    val y = formulaFactory.newBindingVariable("y")
    val S = formulaFactory.newAtom("S")

    val premise = formulaFactory.new(Operation.Exists(x), formulaFactory.new(Operation.Non, formulaFactory.new(Operation.Exists(y), S(x, y))))
    val conclusion = formulaFactory.new(Operation.Non, formulaFactory.new(Operation.Exists(x), formulaFactory.new(Operation.ForAll(y), S(x, y))))
    val proof = Problem(logic, listOf(premise), conclusion).prove()
    log("PROVE: { $premise } ⊢ { $conclusion }\n$proof\n\n")
    if (!proof.isProofCorrect)
        throw RuntimeException("EX pred 3 not proved!")
}

private fun testExPred4(log : (String) -> Unit)
{
    // ⊢ { ∃x.∃y.Pxy ≡ ∃x.∃y.Pyx }
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
    val proof = Problem(logic, listOf(), statement).prove()
    log("PROVE: ⊢ { $statement }\n$proof\n\n")
    if (!proof.isProofCorrect)
        throw RuntimeException("EX pred 4 not proved!")
}
