import kotlinx.browser.document

fun main()
{
    //todo write a hackish parser by using JS eval()
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
        console.log(ex)
    }
}
