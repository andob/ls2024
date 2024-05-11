import kotlinx.browser.document

fun main()
{
    //todo kotlin wasm doesn't work
    val resultDiv = document.getElementById("resultDiv")

    try
    {
        val logBuilder = StringBuilder()
        val logger : (String) -> Unit = { logLine ->
            logBuilder.append('\n').append(logLine)
        }

        test(logger)

        resultDiv?.textContent = logBuilder.toString()
    }
    catch (ex : Throwable)
    {
        resultDiv?.textContent = ex.stackTraceToString()
        println(ex)
    }
}
