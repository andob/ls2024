import kotlinx.browser.document

fun main()
{
    val resultDiv = document.getElementById("resultDiv")

    val logBuilder = StringBuilder()
    val log : (String) -> (Unit) = { logLine : String ->
        logBuilder.append('\n').append(logLine)
    }

    try
    {
        test(log)

        resultDiv?.textContent = logBuilder.toString()
    }
    catch (ex : Throwable)
    {
        resultDiv?.textContent = ex.stackTraceToString()
    }
}
