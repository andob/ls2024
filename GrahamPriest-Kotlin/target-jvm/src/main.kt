fun log(logLine : String) = println(logLine)

fun main(args : Array<String>)
{
    if (args.isEmpty())
    {
        test(::log)
    }
    else
    {
        val problem = Problem.fromConfig(args.joinToString(separator = "\n"))
        val proof = problem.prove()
        log(proof.toString())
    }
}
