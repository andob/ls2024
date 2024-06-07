import java.io.File

fun log(logLine : String) = println(logLine)

fun main(args : Array<String>)
{
    if (args.isEmpty())
    {
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
            currentProblemName = iniFileLine.trimStart('[').trim().trimEnd(']')

            if (currentProblemContents.isNotEmpty())
            {
                problems.add(Pair(currentProblemName, currentProblemContents.toString()))
                currentProblemContents = StringBuilder()
            }
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

        if (problemContents.contains("NOT PROVED! [EXPECTED]") && proof.isProofCorrect)
        {
            throw RuntimeException("Problem $problemName: expected NOT PROVED but got PROVED!")
        }
        else if (problemContents.contains("PROVED! [EXPECTED]") && !proof.isProofCorrect)
        {
            throw RuntimeException("Problem $problemName: expected PROVED but got NOT PROVED!")
        }
    }
}
